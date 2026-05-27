package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Halal Explorer",
    val avatarName: String = "Default",
    val accentColorIndex: Int = 0, // 0: Emerald Green, 1: Golden Amber, 2: Mystic Teal, 3: Ocean Blue, 4: Coral Red
    val prayerNotificationsEnabled: Boolean = true,
    val generalNotificationsEnabled: Boolean = true
)

@Entity(tableName = "favorite_restaurants")
data class FavoriteRestaurant(
    @PrimaryKey val id: String,
    val name: String,
    val cuisine: String,
    val address: String,
    val certification: String, // e.g. "100% Certified", "Halal Friendly", "Seafood/Veggie Only"
    val rating: Double,
    val distance: String
)

@Entity(tableName = "product_reviews")
data class ProductReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val productName: String,
    val userName: String,
    val userAvatar: String,
    val rating: Int,
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis()
)
