package com.awsm_guys.mobile_clicker.mobile.localnetwork

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import com.awsm_guys.mobile_clicker.mobile.MobileConnectionListener
import com.awsm_guys.mobile_clicker.mobile.localnetwork.poko.ClickerMessage
import com.awsm_guys.mobile_clicker.mobile.localnetwork.poko.Header
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable

class UdpMobileConnectionListener: MobileConnectionListener, LoggingMixin {

    private var broadcastPort = 8841
    private val onSubscribeUdpBroadcast by lazy { OnSubscribeUdpListener(broadcastPort) }
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    override fun stopListening() {
        onSubscribeUdpBroadcast.stop()
    }

    override fun startListening(): Observable<MobileClicker> =
        Flowable.create(onSubscribeUdpBroadcast, BackpressureStrategy.LATEST)
                .doOnNext { log("receive ${String(it.data)}") }
                .map {
                    objectMapper.readValue(String(it.data), ClickerMessage::class.java)
                            .apply {
                                features["address"] = it.address.hostAddress
                            }
                }
                .retry()
                .distinctUntilChanged()
                .filter{ it.header == Header.CONNECT }
                .map {
                    LanMobileClicker(
                            it.features["address"]!!, it.features["port"]?.toInt()!!, it.body
                    ) as MobileClicker
                }
                .retry()
                .toObservable()
}