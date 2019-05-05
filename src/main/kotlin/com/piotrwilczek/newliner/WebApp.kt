package com.piotrwilczek.newliner

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.slf4j.event.Level

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging) {
        level = Level.INFO
    }
    install(ContentNegotiation) {
        gson {}
    }
    install(Routing) {
        get("/") { call.respond("Works!") }
        get("/error") {
            call.application.log.error("Logging works!")
            call.respond("Works!")
        }

        newliner()
    }
}
