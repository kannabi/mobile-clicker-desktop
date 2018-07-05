package com.awsm_guys.mobile_clicker.mobile.lan

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import com.awsm_guys.mobile_clicker.mobile.lan.poko.ClickerMessage
import com.awsm_guys.mobile_clicker.mobile.lan.poko.Header
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import com.awsm_guys.mobileclicker.clicker.model.events.ClickerEvent
import com.awsm_guys.mobileclicker.clicker.model.events.ConnectionClose
import com.awsm_guys.mobileclicker.clicker.model.events.PageSwitch
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException

class LanMobileClicker(
        var inetAddress: String,
        var port: Int,
        var clickerName: String
): MobileClicker, LoggingMixin {

    private var clickerPort = 17710
    private val objectMapper by lazy { jacksonObjectMapper() }
    private lateinit var rxSocketWrapper: RxSocketWrapper

    private val eventsSubject = BehaviorSubject.create<ClickerEvent>()

    private val compositeDisposable = CompositeDisposable()

    override fun init(): Observable<ClickerEvent> {
        val udpPoller = UdpPoller()
        val id = udpPoller.poll(
                getMessage(Header.OK, "$clickerPort", mutableMapOf( "sessionId" to "321rqfsirgoh")).toByteArray(),
                port, InetAddress.getByName(inetAddress)
        )
        log("start poll")

        ServerSocket().use {
            it.reuseAddress = true
            it.bind(InetSocketAddress(clickerPort))
            rxSocketWrapper = RxSocketWrapper(it.accept())
        }

        log("accept tcp")
        subscribeToSocketData(rxSocketWrapper.inputObservable)
        udpPoller.remove(id)
        udpPoller.clear()

        return eventsSubject.hide()
    }

    override fun getName() = clickerName

    private fun subscribeToSocketData(inputObservable: Observable<String>) {
        compositeDisposable.add(
            inputObservable
                    .doOnNext { println(it) }
                    .map { objectMapper.readValue(it, ClickerMessage::class.java) }
//                    .retry()
                    .subscribeOn(Schedulers.io())
                    .subscribe(::processClickerMessage, {
                        if (it is CloseWithoutMessageException) {
                            waitForReconnect()
                        } else {
                            disconnect()
                        }
                    }, {
                        eventsSubject.onNext(ConnectionClose())
                    })
        )
    }

    private fun processClickerMessage(message: ClickerMessage) {
        when(message.header) {
            Header.SWITCH_PAGE -> eventsSubject.onNext(PageSwitch(message.body.toInt()))
            Header.DISCONNECT -> disconnect()
            else -> Unit
        }
    }

    private fun waitForReconnect() {
        rxSocketWrapper.close()
        log("wait for reconnect")
        try {
            ServerSocket().use {
                it.reuseAddress = true
                it.bind(InetSocketAddress(clickerPort))
                it.soTimeout = 15000
                rxSocketWrapper = RxSocketWrapper(it.accept())
                subscribeToSocketData(rxSocketWrapper.inputObservable)
            }
        } catch (timeoutException: SocketTimeoutException) {
            trace(timeoutException)
            disconnect()
        }
        log("accept tcp")
    }

    override fun switchToPage(pageNumber: Int) {
        rxSocketWrapper.sendData(
               getMessage(Header.SWITCH_PAGE, pageNumber.toString(), mutableMapOf())
        )
    }

    private fun getMessage(header: Header, body: String, features: MutableMap<String, String>) =
            objectMapper.writeValueAsString(ClickerMessage(header, body, features))

    override fun disconnect() {
        log("LanMobileClicker disconnecting")
        rxSocketWrapper.close()
        compositeDisposable.clear()
        eventsSubject.onNext(ConnectionClose())
    }
}