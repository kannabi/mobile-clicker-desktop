package com.awsm_guys.mobile_clicker.mobile

import com.awsm_guys.mobile_clicker.mobile.localnetwork.UdpMobileConnectionListener
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
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
        value?.switchToPage(1)
    }

    @PostConstruct
    fun init() {
        startListeningClickerConnection()
    }

    private fun startListeningClickerConnection() {
        broadcastDisposable =
                mobileConnectionListener?.startListening()
                ?.subscribeOn(Schedulers.io())
                ?.filter(::verifyMobileClicker)
                ?.doOnNext { dropConnectionListening() }
                ?.flatMap(MobileClicker::init)
                ?.subscribe(::mobileClicker::set, Throwable::printStackTrace)
    }


    private fun verifyMobileClicker(mobileClicker: MobileClicker): Boolean {
        //TODO: there will be sending confirmation dialog to view and getting answer
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