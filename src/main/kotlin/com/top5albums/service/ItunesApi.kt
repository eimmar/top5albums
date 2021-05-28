package com.top5albums.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class ItunesApi {
    private val iTunesClient = WebClient.create("https://itunes.apple.com")

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Cacheable(CacheConfig.itunesArtistCache)
    fun searchArtists(term: String? = null): Mono<ITunesResponse<Artist>> {
        return iTunesClient.get()
            .uri {
                it.path("/search")
                    .queryParam("entity", "allArtist")
                    .queryParam("term", term)
                    .build()
            }
            .retrieve()
            .bodyToMono<ByteArray>()
            .map { objectMapper.readValue(String(it).trim(), object : TypeReference<ITunesResponse<Artist>>() {}) }
    }

    @Cacheable(CacheConfig.itunesAlbumCache)
    fun lookUpAlbums(artistId: String, limit: Int): Mono<ITunesResponse<Album>> {
        return iTunesClient.get()
            .uri {
                it.path("/lookup")
                    .queryParam("entity", "album")
                    .queryParam("id", artistId)
                    .queryParam("limit", limit)
                    .build()
            }
            .retrieve()
            .bodyToMono<ByteArray>()
            .map { objectMapper.readValue(String(it).trim(), object : TypeReference<ITunesResponse<ITunesEntity>>() {}) }
            .map {
                val albums = it.results.filterIsInstance<Album>()

                ITunesResponse(albums.size, albums)
            }
    }
}

data class ITunesResponse<T>(
    val resultCount: Int,
    val results: List<T>
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "wrapperType"
)
@JsonSubTypes(
    Type(value = Artist::class, name = "artist"),
    Type(value = Album::class, name = "collection")
)
interface ITunesEntity

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Artist(
    val artistName: String,
    val artistId: Int,
    val artistLinkUrl: String?,
    val primaryGenreName: String?
): ITunesEntity

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Album(
    val artistName: String,
    val collectionName: String,
    val collectionViewUrl: String,
    val artworkUrl60: String,
    val artworkUrl100: String,
    val collectionPrice: Float,
    val trackCount: Int,
): ITunesEntity
