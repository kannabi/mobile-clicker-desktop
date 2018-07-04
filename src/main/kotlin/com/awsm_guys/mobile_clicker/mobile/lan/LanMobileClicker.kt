package com.awsm_guys.mobile_clicker.mobile.lan

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import com.awsm_guys.mobile_clicker.mobile.lan.poko.ClickerMessage
import com.awsm_guys.mobile_clicker.mobile.lan.poko.Header
import com.awsm_guys.mobileclicker.clicker.model.events.*
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class LanMobileClicker(
        var inetAddress: String,
        var port: Int,
        var clickerName: String
): MobileClicker {

    private var clickerPort = 17710
    private val objectMapper by lazy { ObjectMapper() }
    private lateinit var rxSocketWrapper: RxSocketWrapper

    private val eventsSubject = BehaviorSubject.create<ClickerEvent>()

    private val compositeDisposable = CompositeDisposable()


    //TODO: should it be like that?
    override fun init(): Observable<ClickerEvent> =
            Observable.create(
                    OnSubscribeTcpConnection(
                            localPort = clickerPort,
                            targetPort = port,
                            targetAddress = inetAddress,
                            message = objectMapper.writeValueAsString(
                                                ClickerMessage(
                                                    Header.OK, "$clickerPort",
                                                        mutableMapOf( "sessionId" to "321rqfsirgoh")
                                                )
                                        ).toByteArray()
                    )
            )
            .map(::RxSocketWrapper)
            .doOnNext {
                rxSocketWrapper = it
                subscribeToSocketData(rxSocketWrapper.inputObservable)
                eventsSubject.onNext(ConnectionOpen())
            }
            .flatMap { eventsSubject.hide() }

    override fun getName() = clickerName

    private fun subscribeToSocketData(inputObservable: Observable<String>) {
        inputObservable
                .map { objectMapper.readValue(it, ClickerMessage::class.java) }
                .retry()
                .subscribe(::processClickerMessage, {
                    eventsSubject.onNext(ClickerBroken())
                }, {
                    eventsSubject.onNext(ConnectionClose())
                })
    }

    private fun processClickerMessage(message: ClickerMessage) {
        when(message.header) {
            Header.SWITCH_PAGE -> eventsSubject.onNext(PageSwitch(message.body.toInt()))
            else -> Unit
        }
    }

    override fun switchToPage(pageNumber: Int) {
        rxSocketWrapper.sendData(
               getMessage(Header.SWITCH_PAGE, pageNumber.toString(), mutableMapOf())
        )
    }

    private fun getMessage(header: Header, body: String, features: MutableMap<String, String>) =
            objectMapper.writeValueAsString(ClickerMessage(header, body, features))

    override fun disconnect() {
        rxSocketWrapper.close()
        compositeDisposable.clear()
    }
}