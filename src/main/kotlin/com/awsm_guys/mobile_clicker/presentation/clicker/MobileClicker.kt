package com.awsm_guys.mobile_clicker.presentation.clicker

import com.awsm_guys.mobile_clicker.presentation.clicker.poko.ClickerEvent
import io.reactivex.Observable

interface MobileClicker {
    fun getName(): String

    fun init(maxPage: Int, sessionId: String): Observable<ClickerEvent>

    fun switchToPage(pageNumber: Int)

    fun disconnect()
}