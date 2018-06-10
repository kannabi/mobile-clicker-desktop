package com.awsm_guys.mobile_clicker.mobile

import com.awsm_guys.mobile_clicker.mobile.udp.UdpMobileConnectionListener
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class MobileCommunicationService {

    private val compositeDisposable by lazy { CompositeDisposable() }
    private var broadcastDisposable: Disposable? = null
    private var mobileConnectionListener: MobileConnectionListener? = UdpMobileConnectionListener()
    private var mobileClicker: MobileClicker? = null
    set(value) {
        broadcastDisposable?.dispose()
        broadcastDisposable = null
        field = value
        println(field?.getName())
        field?.init()
    }

    @PostConstruct
    fun init() {
        startListeningClickerConnection()
    }

    private fun startListeningClickerConnection() {
        broadcastDisposable =
                mobileConnectionListener?.startListening()
                ?.filter(this::verifyMobileClicker)
                ?.doOnNext{ dropConnectionListening() }
                ?.subscribe(this::mobileClicker::set, Throwable::printStackTrace)
    }

    private fun verifyMobileClicker(mobileClicker: MobileClicker): Boolean {
        //TODO: there will be sending confirmation dialog to view and getting
        return true
    }

    private fun dropConnectionListening() {
        mobileConnectionListener?.stopListening()
        mobileConnectionListener = null
    }

    @PreDestroy
    fun onDestroy() {
        compositeDisposable.clear()
    }
}