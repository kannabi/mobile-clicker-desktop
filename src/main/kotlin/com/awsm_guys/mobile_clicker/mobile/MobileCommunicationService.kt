package com.awsm_guys.mobile_clicker.mobile

import com.awsm_guys.mobile_clicker.mobile.poko.MobileClicker
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class MobileCommunicationService {

    @Value("\${mobile.broadcastport}")
    private var broadcastPort = -1

    private val compositeDisposable by lazy { CompositeDisposable() }
    private var broadcastDisposable: Disposable? = null
    private var mobileClicker: MobileClicker? = null
    set(value) {
        broadcastDisposable?.dispose()
        broadcastDisposable = null
        println(value)
        field = value
    }

    @PostConstruct
    fun init() {
        startListeningBroadcast()
    }

    private fun startListeningBroadcast() {
        broadcastDisposable = Flowable.create(OnSubscribeUdpBroadcast(broadcastPort), BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .map { MobileClicker(it.address, it.port, it.data.toString()) }
                .distinctUntilChanged()
                .filter(this::verifyMobileClicker)
                .subscribe(this::mobileClicker::set, Throwable::printStackTrace)
    }

    private fun verifyMobileClicker(mobileClicker: MobileClicker): Boolean {
        //TODO: there will be sending confirmation dialog to view and getting
        return true
    }

    @PreDestroy
    fun onDestroy() {
        compositeDisposable.clear()
    }
}