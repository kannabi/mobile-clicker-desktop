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
import org.springframework.web.socket.WebSocketSession
import javax.annotation.PostConstruct

@Component
class WebViewInteractor @Autowired constructor(
        private var presentationService: PresentationService
) : ViewInteractor, ControllerListener, WebSocketListener, LoggingMixin {

    private val eventsSubject = PublishSubject.create<ViewEvent>()
    private var socketSession: WebSocketSession? = null

    @PostConstruct
    fun init() {
        presentationService.attachViewInteractor(this)
    }

    override fun switchPage(page: Page) {
        log("view switching page to ${page.number}")
    }

    override fun verifyMobileClicker(clicker: MobileClicker): Observable<Boolean> = Observable.just(true)

    override fun getEventsObservable(): Observable<ViewEvent> = eventsSubject.hide()

    override fun notifyClickerDisconnected() {
        log("notifyClickerDisconnected")
    }

    //web socket listeners

    override fun onConnected(session: WebSocketSession) {
        socketSession = session
    }

    override fun onDisconnected(session: WebSocketSession) {
        socketSession = null
        eventsSubject.onNext(Close)
    }

    override fun onMessageReceived(message: String, session: WebSocketSession) {
    }

    //controller listener

    override fun startPresentation(filePath: String) = presentationService.startPresentation(filePath)
}