package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web

import com.awsm_guys.mobile_clicker.presentation.PresentationService
import com.awsm_guys.mobile_clicker.presentation.clicker.MobileClicker
import com.awsm_guys.mobile_clicker.presentation.poko.Page
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.Close
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.ViewEvent
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.ViewInteractor
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import javax.annotation.PostConstruct

@Component
class WebViewInteractor: ViewInteractor, ControllerListener, StompConnectionListener, LoggingMixin {

    @Autowired
    private lateinit var presentationService: PresentationService

    private val eventsSubject = PublishSubject.create<ViewEvent>()

    @PostConstruct
    fun init() {
        presentationService.attachViewInteractor(this)
    }

    override fun switchPage(page: Page) {
        log("view switching page to ${page.number}")
    }

    override fun startPresentation(filePath: String) = presentationService.startPresentation(filePath)

    override fun verifyMobileClicker(clicker: MobileClicker): Observable<Boolean> = Observable.just(true)

    override fun getEventsObservable(): Observable<ViewEvent> = eventsSubject.hide()

    override fun notifyClickerDisconnected() {
        log("notifyClickerDisconnected")
    }

    override fun onDisconnect(event: SessionDisconnectEvent) {
        eventsSubject.onNext(Close)
    }
}