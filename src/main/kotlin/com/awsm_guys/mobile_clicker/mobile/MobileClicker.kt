package com.awsm_guys.mobile_clicker.mobile

import io.reactivex.Observable

interface MobileClicker {
    fun getName(): String

    fun init()

    fun switchPageObservable(): Observable<Int>

    fun switchToPage(pageNumber: Int)

    fun disconnect()
}