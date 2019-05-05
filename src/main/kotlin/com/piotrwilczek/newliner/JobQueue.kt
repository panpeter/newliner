package com.piotrwilczek.newliner

import com.piotrwilczek.newliner.github.ApiClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class JobQueue {
    private val logger = LoggerFactory.getLogger("NewLinerJobQueue")
    private val githubApiClient = ApiClient(
        NEWLINER_GITHUB_APP_ID,
        NEWLINER_GITHUB_API_KEY
    )

    private val jobs = Channel<Job>(100)

    init {
        GlobalScope.launch {
            val newLiner = NewLiner(githubApiClient)
            for (job in jobs) {
                logger.debug("Got new job $job")
                try {
                    when (job) {
                        is DefaultBranchJob -> newLiner.appendNewLines(job.installationId)
                        is BranchJob -> newLiner.appendNewLines(job.installationId, job.cloneUrl, job.ref)
                    }
                    logger.debug("Success $job")
                } catch (e: Exception) {
                    logger.error("Failed $job", e)
                }
            }
        }
    }

    fun addJob(job: Job) {
        if (!jobs.offer(job)) {
            logger.error("Jobs queue is full.")
        }
    }
}
