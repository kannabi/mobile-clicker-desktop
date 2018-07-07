package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web

import com.awsm_guys.mobile_clicker.presentation.PresentationService
import com.awsm_guys.mobile_clicker.presentation.clicker.MobileClicker
import com.awsm_guys.mobile_clicker.presentation.poko.Page
import com.awsm_guys.mobile_clicker.presentation.poko.PresentationInfo
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.ViewEvent
import com.awsm_guys.mobile_clicker.presentation.viewinteractor.ViewInteractor
import com.awsm_guys.mobile_clicker.utils.LoggingMixin
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class WebViewInteractor: ViewInteractor, ControllerListener, LoggingMixin {

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

    override fun startPresentation(filePath: String): PresentationInfo =
            presentationService.startPresentation(filePath)

    override fun verifyMobileClicker(clicker: MobileClicker): Observable<Boolean> = Observable.just(true)

    override fun getEventsObservable(): Observable<ViewEvent> = eventsSubject.hide()

    override fun notifyClickerDisconnected() {
        log("notifyClickerDisconnected")
    }
}