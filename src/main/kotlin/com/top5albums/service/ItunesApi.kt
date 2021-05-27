package com.top5albums.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux

@Component
class ItunesApi {
    private val iTunesClient = WebClient.create("https://itunes.apple.com")

    private val objectMapper = ObjectMapper().registerKotlinModule()

    fun searchArtists(term: String? = null): Flux<ITunesResponse<Artist>> {

        //TODO: Fix truncated flux results
        return iTunesClient.get()
            .uri {
                it.path("/search")
                    .queryParam("entity", "allArtist")
                    .queryParam("term", term)
                    .build()
            }
            .accept(MediaType.ALL)
            .retrieve()
            .bodyToFlux<ByteArray>()
            .map {
                println(String(it).trim())
                objectMapper.readValue(String(it).trim(), object : TypeReference<ITunesResponse<Artist>>() {})
            }
    }

    fun lookUpAlbums(artistId: String, limit: Int): Flux<ITunesResponse<Any>> {
        return iTunesClient.get()
            .uri {
                it.path("/lookup")
                    .queryParam("entity", "album")
                    .queryParam("id", artistId)
                    .queryParam("limit", limit)
                    .build()
            }
            .accept(MediaType.ALL)
            .retrieve()
            .bodyToFlux<ByteArray>()
            .map {
                objectMapper.readValue(String(it).trim(), object : TypeReference<ITunesResponse<Any>>() {})
            }
    }
}

data class ITunesResponse<T>(
    val resultCount: String,
    val results: List<T>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Artist(
    val artistName: String,
    val artistLinkUrl: String,
    val artistId: Int,
    val primaryGenreName: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Album(
    val albumName: String
)
