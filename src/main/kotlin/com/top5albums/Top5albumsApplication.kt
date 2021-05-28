package com.top5albums

import com.top5albums.repository.UserFavoriteArtist
import com.top5albums.repository.UserFavoriteArtistRepository
import com.top5albums.service.Album
import com.top5albums.service.ITunesResponse
import com.top5albums.service.ItunesApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@SpringBootApplication
class Top5albumsApplication

fun main(args: Array<String>) {
    runApplication<Top5albumsApplication>(*args)
}

@RestController
@RequestMapping("/favorite-artist")
class FavoriteArtistController(
    private val itunesApi: ItunesApi,
    private val userFavoriteArtistRepository: UserFavoriteArtistRepository
) {
    data class SearchRequest(val term: String?)
    data class SaveRequest(val artistId: Int, val userId: Int)

    @PostMapping("/search")
    fun search(@RequestBody request: SearchRequest) = itunesApi.searchArtists(request.term)

    @PostMapping("/save")
    fun save(@RequestBody request: SaveRequest) {
        userFavoriteArtistRepository.save(UserFavoriteArtist(request.userId, request.artistId))
    }

    @PostMapping("/top-5-albums/{userId}")
    fun top5FavoriteAlbums(@PathVariable userId: Int): Mono<ITunesResponse<Album>> {
        val userFavoriteArtist = userFavoriteArtistRepository.findById(userId)

        if (userFavoriteArtist.isEmpty)
            return Mono.just(ITunesResponse(resultCount = 0, results = emptyList()))

        return itunesApi.lookUpAlbums(userFavoriteArtist.get().artistId.toString(), 5)
    }
}