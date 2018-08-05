package com.awsm_guys.mobile_clicker.presentation

import com.awsm_guys.mobile_clicker.presentation.clicker.MobileClicker
import com.awsm_guys.mobile_clicker.presentation.clicker.MobileConnectionListener
import com.awsm_guys.mobile_clicker.presentation.clicker.lan.UdpMobileConnectionListener
import com.awsm_guys.mobile_clicker.presentation.clicker.poko.*
import com.awsm_guys.mobile_clicker.presentation.poko.Presentation
import com.awsm_guys.mobile_clicker.presentation.poko.PresentationInfo
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.*
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import com.awsm_guys.mobile_clicker.utils.convertPdfToImages
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
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

    private var currentPage = 1
    private lateinit var sessionId: String

    private lateinit var viewInteractor: ViewInteractor

    private var presentation: Presentation? = null
    private var presentationSize = 0

    private fun startListeningClickerConnection() {
        broadcastDisposable =
                mobileConnectionListener.startListening()
                    .subscribeOn(Schedulers.io())
                    .switchMap (::verifyMobileClicker)
                    .doOnNext {
                        mobileClicker = it
                        dropClickerConnectionListening()
                    }
                    .flatMap { mobileClicker!!.init(presentation!!.pages.size, sessionId) }
                    .subscribe(::processClickerEvents, Throwable::printStackTrace)
    }

    private fun processClickerEvents(event: ClickerEvent) {
        when(event) {
            is ConnectionClose -> {
                log("clicker disconnected")
                mobileClicker = null
                startListeningClickerConnection()
            }
            is ConnectionOpen -> {
                log("connection open")
                mobileClicker?.apply{
                    switchToPage(currentPage)
                    updateMeta(
                        presentationSize,
                        presentation!!.pages.map { (it.number + 1).toString() to it.tinyImageBase64String }.toMap()
                    )
                }

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
        val file = File(filePath)
        sessionId = UUID.randomUUID().toString()
        presentation = Presentation(
                sessionId, file.name, convertPdfToImages(file)
        )
        currentPage = 0
        presentationSize = presentation!!.pages.size
        return PresentationInfo(
                sessionId,
                presentationSize,
                presentation!!.title,
                presentation!!.pages[0]
        )
    }

    private fun processViewEvent(event: ViewEvent) {
        when (event) {
            is SwitchPage -> switchPage(event.page)
            is AskConnectClicker -> startListeningClickerConnection()
            is Close -> presentation?.let { exit() }
            is EndPresentation -> endPresentation()
        }
    }

    private fun switchPage(page: Int) {
        if (page in 0 until presentationSize){
            currentPage = page
            mobileClicker?.switchToPage(currentPage)
            viewInteractor.switchPage(presentation!!.pages[page])
        }
    }

    private fun endPresentation() {
        presentation = null
        dropClickerConnectionListening()
        mobileClicker?.disconnect()
        mobileClicker = null
        broadcastDisposable?.dispose()
        broadcastDisposable = null
        currentPage = 0
    }

    private fun dropClickerConnectionListening() {
        mobileConnectionListener.stopListening()
        //NO U DON'T WANT SET DISPOSING HERE!!!1
    }

    private fun exit() {
        System.exit(0)
    }

    @PreDestroy
    fun onDestroy() {
        compositeDisposable.clear()
        broadcastDisposable?.dispose()
        broadcastDisposable = null
    }
}