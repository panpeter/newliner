package com.piotrwilczek.newliner

import com.piotrwilczek.newliner.github.ApiClient
import org.apache.tika.Tika
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.LoggerFactory
import java.io.File
import java.io.RandomAccessFile

class NewLiner(val apiClient: ApiClient) {
    private val tika = Tika()
    private val logger = LoggerFactory.getLogger("NewLiner")

    suspend fun appendNewLines(installationId: Long) {
        apiClient.authenticate(installationId)
        val repositories = apiClient.getRepositories()
        repositories.forEach { appendNewLines(installationId, it.cloneUrl, it.defaultBranch) }
    }

    suspend fun appendNewLines(installationId: Long, cloneUrl: String, branch: String) {
        // Get credentials.
        apiClient.authenticate(installationId)
        val token = apiClient.getAuthToken()
        val credentialsProvider = UsernamePasswordCredentialsProvider("x-access-token", token)

        // Prepare dir.
        val repoDir = createTempFile("repo", "").apply { delete() }

        // Clone repo.
        val git = Git.cloneRepository()
            .setCredentialsProvider(credentialsProvider)
            .setURI(cloneUrl)
            .setBranch(branch)
            .setDirectory(repoDir)
            .call()

        // Append new lines.
        appendNewLines(repoDir)

        // Commit and push.
        val modified = git.status().call().modified
        if (modified.isNotEmpty()) {
            git.add().addFilepattern(".").call()
            git.commit()
                .setCommitter(Committer.NAME, Committer.EMAIL)
                .setAuthor(Committer.NAME, Committer.EMAIL)
                .setMessage(Committer.MESSAGE)
                .call()
            git.push().setCredentialsProvider(credentialsProvider).setRemote(cloneUrl).call()
        }

        // Clean up.
        repoDir.deleteRecursively()
        git.close()

        // Log.
        logger.info("Checked $cloneUrl branch $branch. Updated ${modified.size} files: $modified")
    }

    private fun appendNewLines(dir: File) {
        dir.walkTopDown()
            .filterNot { isDirectoryOrEmptyOrGit(it) }
            .filter { isTextFile(it) }
            .filterNot { hasNewlineAtEof(it) }
            .forEach { it.appendText("\n") }
    }

    private fun isDirectoryOrEmptyOrGit(file: File): Boolean {
        return file.isDirectory || file.length() == 0L || file.absolutePath.contains("/.git/")
    }

    private fun isTextFile(file: File) = tika.detect(file)?.startsWith("text/") ?: false

    private fun hasNewlineAtEof(file: File) = getLastChar(file) == '\n'

    private fun getLastChar(file: File) = RandomAccessFile(file, "r").use {
        val lastCharPosition = it.length() - 1
        return@use if (lastCharPosition >= 0) {
            it.seek(lastCharPosition)
            it.readByte().toChar()
        } else {
            0.toChar()
        }
    }
}
