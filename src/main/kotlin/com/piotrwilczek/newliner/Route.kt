package com.piotrwilczek.newliner

import com.piotrwilczek.newliner.github.InstallationEvent
import com.piotrwilczek.newliner.github.PushEvent
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.newliner(pathPrefix: String = "") {
    val jobQueue = JobQueue()

    fun handleInstallationEvent(event: InstallationEvent) {
        if (event.action == "created" || event.action == "added") {
            jobQueue.addJob(DefaultBranchJob(event.installation.id))
        }
    }

    fun handlePushEvent(event: PushEvent) {
        if (event.headCommit.author.email != Committer.EMAIL) {
            jobQueue.addJob(
                BranchJob(
                    event.installation.id,
                    event.repository.cloneUrl,
                    event.ref
                )
            )
        }
    }

    post("$pathPrefix/webhook") {
        if (!call.request.headers.contains("X-Hub-Signature")) {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }
        val event = call.request.header("X-GitHub-Event")
        when (event) {
            "installation",
            "installation_repositories" -> {
                handleInstallationEvent(call.receive())
            }
            "integration_installation_repositories" -> { } // ignore
            "push" -> handlePushEvent(call.receive())
        }

        call.respond("")
    }
}
