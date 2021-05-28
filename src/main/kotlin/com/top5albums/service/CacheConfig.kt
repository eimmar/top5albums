package com.top5albums.service

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {
    companion object {
        const val itunesAlbumCache = "itunes.album"
        const val itunesArtistCache = "itunes.artist"
    }

    @Bean
    fun caffeineConfig() = Caffeine
        .newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(1, TimeUnit.DAYS)

    @Bean
    fun cacheManager(caffeine: Caffeine<Any, Any>) = CaffeineCacheManager(itunesAlbumCache, itunesArtistCache).apply {
        setCaffeine(caffeine)
    }
}
