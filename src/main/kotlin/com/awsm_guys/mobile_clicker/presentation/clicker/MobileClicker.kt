package com.awsm_guys.mobile_clicker.presentation.clicker

import com.awsm_guys.mobile_clicker.presentation.clicker.poko.ClickerEvent
import io.reactivex.Observable

interface MobileClicker {
    fun getName(): String

    fun init(maxPage: Int, sessionId: String): Observable<ClickerEvent>

    fun switchToPage(pageNumber: Int)

    //This data should be packed in to a data class in future
    fun updateMeta(maxPage: Int, tinySlides: Map<String, String>)

    fun disconnect()
}