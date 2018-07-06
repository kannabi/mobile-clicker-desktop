package com.awsm_guys.mobile_clicker.mobile

import com.awsm_guys.mobile_clicker.mobile.lan.UdpMobileConnectionListener
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import com.awsm_guys.mobileclicker.clicker.model.events.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class MobileCommunicationService: LoggingMixin {

    private val compositeDisposable by lazy { CompositeDisposable() }
    private var broadcastDisposable: Disposable? = null
    private var mobileConnectionListener: MobileConnectionListener? = UdpMobileConnectionListener()
    private var mobileClicker: MobileClicker? = null
        set(value) {
            field = value
            log("${field?.getName()} set")
        }

    private val maxPage = 10
    private var currentPage = 1
    private var sessionId = "321rqfsirgoh"


    @PostConstruct
    fun init() {
        startListeningClickerConnection()
    }

    private fun startListeningClickerConnection() {
        broadcastDisposable =
                mobileConnectionListener?.startListening()
                ?.subscribeOn(Schedulers.io())
                ?.filter(::verifyMobileClicker)
                ?.doOnNext {
                    mobileClicker = it
                    dropConnectionListening()
                }
                ?.flatMap { mobileClicker!!.init(maxPage, sessionId) }
                ?.subscribe(::processClickerEvents, Throwable::printStackTrace)
    }

    private fun processClickerEvents(event: ClickerEvent) {
        when(event){
            is ConnectionClose -> log("connection close")
            is ConnectionOpen -> {
                log("connection open")
                mobileClicker?.switchToPage(currentPage)
            }
            is PageSwitch -> {
                log("page switch ${event.page}")
                currentPage = event.page
                mobileClicker?.switchToPage(currentPage)
            }
            is ClickerBroken -> log("clicker broken")
        }
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
        broadcastDisposable?.dispose()
        broadcastDisposable = null
    }
}