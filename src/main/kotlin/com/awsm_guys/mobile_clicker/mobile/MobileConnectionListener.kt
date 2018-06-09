package com.awsm_guys.mobile_clicker.mobile

import io.reactivex.Observable

interface MobileConnectionListener {
    fun startListening(): Observable<MobileClicker>

    fun stopListening()
}