package com.awsm_guys.mobile_clicker.mobile.udp

import com.awsm_guys.mobile_clicker.mobile.MobileClicker
import com.awsm_guys.mobile_clicker.mobile.MobileConnectionListener
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class UdpMobileConnectionListener: MobileConnectionListener {

    private var broadcastPort = 8841
    private val onSubscribeUdpBroadcast by lazy { OnSubscribeUdpBroadcast(broadcastPort) }

    override fun stopListening() {
        onSubscribeUdpBroadcast.stop()
    }

    override fun startListening(): Observable<MobileClicker> =
        Flowable.create(onSubscribeUdpBroadcast, BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .map { UdpMobileClicker(it.address, it.port, it.data.toString()) as MobileClicker }
                .distinctUntilChanged()
                .toObservable()
}