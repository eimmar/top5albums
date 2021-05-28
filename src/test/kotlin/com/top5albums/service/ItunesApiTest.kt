package com.top5albums.service

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import okio.Okio
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class ItunesApiTest {
    companion object {
        private val mockServer = MockWebServer()

        @BeforeAll
        fun setUp() = mockServer.start()

        @AfterAll
        fun tearDown() = mockServer.shutdown()
    }

    private fun createMockFileResponse(testDataFileName: String): MockResponse {
        val fileBuffer = Buffer()

        fileBuffer.writeAll(
            Okio.source(
                File("${System.getProperty("user.dir")}/src/test/kotlin/com/top5albums/service/testData/$testDataFileName")
            )
        )

        return MockResponse().apply {
            addHeader("Content-Type", "text/javascript; charset=utf-8")
            addHeader("content-disposition", "attachment; filename=1.txt")
            body = fileBuffer
        }
    }

    @Test
    fun `It must return only albums`() {
        val itunesApi = ItunesApi("http://localhost:${mockServer.port}")

        // 1st line contains artist
        mockServer.enqueue(createMockFileResponse("album-response.txt"))

        assertEquals(
            expected = ITunesResponse(
                resultCount = 1,
                results = listOf(
                    Album(
                        artistName = "Benny Andersson, Björn Ulvaeus, Meryl Streep & Amanda Seyfried",
                        collectionName = "Mamma Mia! (The Movie Soundtrack feat. the Songs of ABBA) [Bonus Track Version]",
                        collectionViewUrl = "https://music.apple.com/us/album/mamma-mia-movie-soundtrack-feat-songs-abba-bonus-track/1440767912?uo=4",
                        artworkUrl60 = "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/c5/a0/61/c5a061a8-0df2-490d-e6f3-90658203ffdc/source/60x60bb.jpg",
                        artworkUrl100 = "https://is1-ssl.mzstatic.com/image/thumb/Music114/v4/c5/a0/61/c5a061a8-0df2-490d-e6f3-90658203ffdc/source/100x100bb.jpg",
                        collectionPrice = 5.99f,
                        trackCount = 18,
                    ),
                )
            ),
            actual = itunesApi.lookUpAlbums("1", 5).block()
        )
    }

    @Test
    fun `It must return artists`() {
        val itunesApi = ItunesApi("http://localhost:${mockServer.port}")

        mockServer.enqueue(createMockFileResponse("artist-response.txt"))

        assertEquals(
            expected = ITunesResponse(
                resultCount = 2,
                results = listOf(
                    Artist(
                        artistName = "ABBA",
                        artistId = 372976,
                        artistLinkUrl = null,
                        primaryGenreName = "Pop"
                    ),
                    Artist(
                        artistName = "Abba Yahudah",
                        artistId = 203091003,
                        artistLinkUrl = null,
                        primaryGenreName = "Reggae"
                    ),
                )
            ),
            actual = itunesApi.searchArtists().block()
        )
    }
}
