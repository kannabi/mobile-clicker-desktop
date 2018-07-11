package com.awsm_guys.mobile_clicker.presentation.viewinteractor

sealed class ViewEvent

data class SwitchPage(
        var page: Int
): ViewEvent()

object AskConnectClicker : ViewEvent()

object Close : ViewEvent()