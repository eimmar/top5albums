package com.top5albums

import com.top5albums.repository.UserFavoriteArtist
import com.top5albums.repository.UserFavoriteArtistRepository
import com.top5albums.service.Album
import com.top5albums.service.ITunesResponse
import com.top5albums.service.ItunesApi
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

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
    @PostMapping("/search")
    fun search(@RequestParam(required = false) term: String?) = itunesApi.searchArtists(term)

    @PostMapping("/save")
    fun save(@RequestParam artistId: Int, @RequestParam userId: Int) {
        userFavoriteArtistRepository.save(UserFavoriteArtist(userId, artistId))
    }

    @PostMapping("/top-5-albums/{userId}")
    fun top5FavoriteAlbums(@PathVariable userId: Int): Flux<ITunesResponse<Any>> {
        val userFavoriteArtist = userFavoriteArtistRepository.findById(userId).get()

        return itunesApi.lookUpAlbums(userFavoriteArtist.artistId.toString(), 5)
    }
}