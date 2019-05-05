package com.piotrwilczek.newliner

sealed class Job()
data class DefaultBranchJob(val installationId: Long): Job()
data class BranchJob(val installationId: Long, val cloneUrl: String, val ref: String) : Job()
