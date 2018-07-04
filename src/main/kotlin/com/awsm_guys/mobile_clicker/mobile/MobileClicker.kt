package com.awsm_guys.mobile_clicker.mobile

import com.awsm_guys.mobileclicker.clicker.model.events.ClickerEvent
import io.reactivex.Observable

interface MobileClicker {
    fun getName(): String

    fun init(): Observable<ClickerEvent>

    fun switchToPage(pageNumber: Int)

    fun disconnect()
}