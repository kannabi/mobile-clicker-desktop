package com.awsm_guys.mobile_clicker.utils

interface LoggingMixin {
    fun trace(error: Throwable){
        error.printStackTrace()
    }

    fun log(message: String) {
        println(message)
    }
}