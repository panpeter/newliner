package com.piotrwilczek.newliner.github

import com.google.gson.annotations.SerializedName

data class InstallationEvent(
    val action: String,
    val installation: Installation
)

data class PushEvent(
    val ref: String,
    val repository: Repository,
    val installation: Installation,
    @SerializedName("head_commit") val headCommit: Commit
)

data class Repositories(
    val repositories: List<Repository>
)

data class Repository(
    val url: String,
    @SerializedName("clone_url") val cloneUrl: String,
    @SerializedName("default_branch") val defaultBranch: String
)

data class TokenResponse(
    val token: String
)

data class Installation(
    val id: Long
)

data class Commit(
    val author: Author,
    val message: String
)

data class Author(
    val name: String?,
    val email: String?
)
