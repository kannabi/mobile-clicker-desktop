package com.awsm_guys.mobile_clicker.presentation.viewinteractor

import com.awsm_guys.mobile_clicker.presentation.clicker.MobileClicker
import com.awsm_guys.mobile_clicker.presentation.poko.Page
import io.reactivex.Observable

interface ViewInteractor {

    fun getEventsObservable(): Observable<ViewEvent>

    fun switchPage(page: Page)

    fun verifyMobileClicker(clicker: MobileClicker): Observable<Boolean>

    fun notifyClickerDisconnected()
}