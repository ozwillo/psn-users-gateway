package com.ozwillo.usersgw.controller

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/status")
class StatusController {

    @GetMapping
    fun status(): String {
        return "OK"
    }
}
