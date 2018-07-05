package com.awsm_guys.mobileclicker.clicker.model.events

sealed class ClickerEvent

class ConnectionOpen: ClickerEvent()

class ConnectionClose: ClickerEvent()

class ClickerBroken: ClickerEvent()

data class PageSwitch(val page: Int): ClickerEvent()