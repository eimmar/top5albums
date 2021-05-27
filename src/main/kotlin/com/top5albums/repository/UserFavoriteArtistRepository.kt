package com.top5albums.repository

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.Id

interface UserFavoriteArtistRepository: JpaRepository<UserFavoriteArtist, Int>

@Entity
data class UserFavoriteArtist(
    @Id val userId: Int,
    val artistId: Int
)
