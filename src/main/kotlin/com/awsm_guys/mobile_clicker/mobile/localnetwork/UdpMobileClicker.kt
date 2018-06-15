package com.awsm_guys.mobile_clicker.mobile.localnetwork

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import com.awsm_guys.mobile_clicker.mobile.localnetwork.poko.ClickerMessage
import com.awsm_guys.mobile_clicker.mobile.localnetwork.poko.Header
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Observable

class UdpMobileClicker(
        var inetAddress: String,
        var port: Int,
        var clickerName: String
): MobileClicker {

    private var clickerPort = 17710
    private val objectMapper by lazy { ObjectMapper() }
    private lateinit var rxSocketWrapper: RxSocketWrapper


    //TODO: should it be like that?
    override fun init(): Observable<MobileClicker> =
            Observable.create(
                    OnSubscribeTcpConnection(
                            localPort = clickerPort,
                            targetPort = port,
                            targetAddress = inetAddress,
                            message = objectMapper.writeValueAsString(
                                                ClickerMessage(
                                                    Header.OK, "$clickerPort", mutableMapOf()
                                                )
                                        ).toByteArray()
                    )
            )
            .map(::RxSocketWrapper)
            .doOnNext{
                rxSocketWrapper = it
            }
            .map { this }

    override fun getName() = clickerName

    override fun switchPageObservable(): Observable<Int> =
            rxSocketWrapper.inputObservable
                    .map { objectMapper.readValue(it, ClickerMessage::class.java) }
                    .retry()
                    .filter{ it.header == Header.SWITCH_PAGE }
                    .map { it.body.toInt() }
                    .retry()

    override fun switchToPage(pageNumber: Int) {
        rxSocketWrapper.sendData(
               getMessage(Header.SWITCH_PAGE, "1", mutableMapOf())
        )
    }

    private fun getMessage(header: Header, body: String, features: MutableMap<String, String>) =
            objectMapper.writeValueAsString(ClickerMessage(header, body, features))

    override fun disconnect() =
        rxSocketWrapper.close()
}