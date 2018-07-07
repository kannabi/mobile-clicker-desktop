package com.awsm_guys.mobile_clicker.presentation

import com.awsm_guys.mobile_clicker.presentation.clicker.MobileClicker
import com.awsm_guys.mobile_clicker.presentation.clicker.MobileConnectionListener
import com.awsm_guys.mobile_clicker.presentation.clicker.lan.UdpMobileConnectionListener
import com.awsm_guys.mobile_clicker.presentation.clicker.poko.*
import com.awsm_guys.mobile_clicker.presentation.poko.Presentation
import com.awsm_guys.mobile_clicker.presentation.poko.PresentationInfo
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.*
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
class PresentationService: LoggingMixin {

    private val compositeDisposable by lazy { CompositeDisposable() }
    private var broadcastDisposable: Disposable? = null
    private var mobileConnectionListener: MobileConnectionListener = UdpMobileConnectionListener()
    private var mobileClicker: MobileClicker? = null
        set(value) {
            field = value
            log("${field?.getName()} set")
        }

    private val maxPage = 10
    private var currentPage = 1
    private var sessionId = "321rqfsirgoh"

    private lateinit var viewInteractor: ViewInteractor

    private lateinit var presentation: Presentation

    private fun startListeningClickerConnection() {
        broadcastDisposable =
                mobileConnectionListener.startListening()
                    .subscribeOn(Schedulers.io())
                    .switchMap (::verifyMobileClicker)
                    .doOnNext {
                        mobileClicker = it
                        dropClickerConnectionListening()
                    }
                    .flatMap { mobileClicker!!.init(maxPage, sessionId) }
                    .subscribe(::processClickerEvents, Throwable::printStackTrace)
    }

    private fun processClickerEvents(event: ClickerEvent) {
        when(event){
            is ConnectionClose -> log("connection close")
            is ConnectionOpen -> {
                log("connection open")
                mobileClicker?.switchToPage(currentPage)
            }
            is PageSwitch -> {
                log("clicker switch to a page ${event.page}")
                switchPage(event.page)
            }
            is ClickerBroken -> log("clicker broken")
        }
    }

    private fun verifyMobileClicker(mobileClicker: MobileClicker): Observable<MobileClicker> =
            viewInteractor.verifyMobileClicker(mobileClicker)
                    .flatMap {
                        if (it) Observable.just(mobileClicker) else Observable.empty()
                    }

    fun attachViewInteractor(viewInteractor: ViewInteractor) {
        this.viewInteractor = viewInteractor
        compositeDisposable.add(
                viewInteractor.getEventsObservable()
                        .subscribeOn(Schedulers.io())
                        .subscribe(::processViewEvent, ::trace)
        )
    }

    fun startPresentation(filePath: String): PresentationInfo {
        TODO()
    }

    private fun processViewEvent(event: ViewEvent) {
        when (event) {
            is SwitchPage -> switchPage(event.page)
            is AskConnectClicker -> startListeningClickerConnection()
            is Close -> exit()
        }
    }

    private fun switchPage(page: Int) {
        currentPage = page
        mobileClicker?.switchToPage(currentPage)
        viewInteractor.switchPage(presentation.pages[page])
    }

    private fun dropClickerConnectionListening() {
        mobileConnectionListener.stopListening()
    }

    private fun exit() {

    }

    @PreDestroy
    fun onDestroy() {
        compositeDisposable.clear()
        broadcastDisposable?.dispose()
        broadcastDisposable = null
    }
}