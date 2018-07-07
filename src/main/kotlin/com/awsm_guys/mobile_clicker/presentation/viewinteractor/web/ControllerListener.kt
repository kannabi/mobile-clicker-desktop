package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web

import com.awsm_guys.mobile_clicker.presentation.poko.PresentationInfo

interface ControllerListener {
    fun startPresentation(filePath: String): PresentationInfo?
}