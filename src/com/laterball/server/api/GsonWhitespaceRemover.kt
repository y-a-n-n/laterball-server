package com.laterball.server.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.call.TypeInfo
import io.ktor.client.features.json.JsonSerializer
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.readText

/**
 * [JsonSerializer] using [Gson] as backend.
 */
public class GsonWhitespaceRemover(block: GsonBuilder.() -> Unit = {}) : JsonSerializer {
    private val backend: Gson = GsonBuilder().apply(block).create()

    override fun write(data: Any, contentType: ContentType): OutgoingContent =
        TextContent(backend.toJson(data), contentType)

    override fun read(type: TypeInfo, body: Input): Any {
        val text = body.readText()
        return backend.fromJson(text, type.reifiedType)
    }
}