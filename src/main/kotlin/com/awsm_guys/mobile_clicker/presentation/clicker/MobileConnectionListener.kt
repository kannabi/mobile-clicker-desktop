package com.awsm_guys.mobile_clicker.presentation.clicker

import io.reactivex.Observable

interface MobileConnectionListener {
    fun startListening(): Observable<MobileClicker>

    fun stopListening()
}