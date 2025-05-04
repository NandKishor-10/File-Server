package com.nandkishor.fileserver.server

import fi.iki.elonen.NanoHTTPD
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URLDecoder
import java.net.URLEncoder

class FileServer(
    private val rootDir: File,
    port: Int
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val rawUri = session.uri.removePrefix("/")
        val decodedUri = URLDecoder.decode(rawUri, "UTF-8")
        val requestedFile = File(rootDir, decodedUri)

        return when {
            requestedFile.isDirectory -> serveDirectory(decodedUri, requestedFile)
            requestedFile.isFile -> serveFile(requestedFile)
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found")
        }
    }

    private fun serveDirectory(decodedUri: String, dir: File): Response {
        val files = dir.listFiles()?.sortedBy { it.name.lowercase() } ?: emptyList()
        val html = buildString {
            append("<html><body>")
            append("<h2>📁 Available Files and Folder in Root:$decodedUri</h2>")
            if (decodedUri.isNotEmpty()) {
                val parent = File(decodedUri).parent ?: ""
                append("<p><a href=\"/${URLEncoder.encode(parent, "UTF-8")}\">⬅️ Go up</a></p>")
            }
            append("<ul>")
            for (file in files) {
                val path = "$decodedUri/${file.name}".trim(' ', '/')
                val encodedPath = path.split("/").joinToString("/") {
                    URLEncoder.encode(it, "UTF-8").replace("+", "%20")
                }
                val displayName = file.name + if (file.isDirectory) "/" else ""
                append("<li><a href=\"/$encodedPath\">$displayName</a></li>")
            }
            append("</ul></body></html>")
        }
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    private fun serveFile(file: File): Response {
        val stream = BufferedInputStream(FileInputStream(file))
        return newChunkedResponse(Response.Status.OK, "application/octet-stream", stream)
    }
}
