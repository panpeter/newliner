package com.piotrwilczek.newliner

import io.github.cdimascio.dotenv.dotenv

val SystemEnv = dotenv { ignoreIfMissing = true }

val NEWLINER_GITHUB_APP_ID = requireNotNull(SystemEnv["NEWLINER_GITHUB_APP_ID"])
val NEWLINER_GITHUB_API_KEY = requireNotNull(SystemEnv["NEWLINER_GITHUB_PRIVATE_KEY"])

object Committer {
    val NAME = SystemEnv["NEWLINER_COMMITTER_NAME"] ?: "Newliner App"
    val EMAIL = SystemEnv["NEWLINER_COMMITTER_EMAIL"] ?: "piotrwilczek+newliner@gmail.com"
    val MESSAGE = SystemEnv["NEWLINER_COMMIT_MESSAGE"] ?: "Add newline at end of file\n\n" +
    "This commit was pushed by Newliner App.\n" +
    "For more information check https://newliner.github.io/\n"
}
