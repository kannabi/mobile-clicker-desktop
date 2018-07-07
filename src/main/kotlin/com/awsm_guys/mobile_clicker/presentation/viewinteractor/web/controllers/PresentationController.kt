package com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.controllers

import com.awsm_guys.mobile_clicker.presentation.viewinteractor.web.ControllerListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/")
class PresentationController {
    @Autowired
    private lateinit var controllerListener: ControllerListener


    @RequestMapping(value = ["/subscribe"], method = [(RequestMethod.POST)])
    @ResponseBody
    fun startPresentation(filePath: String) = controllerListener.startPresentation(filePath)
}