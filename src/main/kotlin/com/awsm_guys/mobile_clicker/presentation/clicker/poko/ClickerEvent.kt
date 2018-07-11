package com.awsm_guys.mobile_clicker.presentation.clicker.poko

sealed class ClickerEvent

class ConnectionOpen: ClickerEvent()

class ConnectionClose: ClickerEvent()

class ClickerBroken: ClickerEvent()

data class PageSwitch(val page: Int): ClickerEvent()