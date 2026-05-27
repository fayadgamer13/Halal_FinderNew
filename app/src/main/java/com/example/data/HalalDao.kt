package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
}

@Dao
interface FavoriteRestaurantDao {
    @Query("SELECT * FROM favorite_restaurants")
    fun getAllFavorites(): Flow<List<FavoriteRestaurant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(restaurant: FavoriteRestaurant)

    @Query("DELETE FROM favorite_restaurants WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_restaurants WHERE id = :id)")
    fun isFavoriteExists(id: String): Flow<Boolean>
}

@Dao
interface ProductReviewDao {
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY timestamp DESC")
    fun getReviewsForProduct(productId: String): Flow<List<ProductReview>>

    @Query("SELECT * FROM product_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ProductReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ProductReview)
}
