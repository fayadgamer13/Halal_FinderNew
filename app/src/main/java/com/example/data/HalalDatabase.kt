package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfile::class, FavoriteRestaurant::class, ProductReview::class],
    version = 1,
    exportSchema = false
)
abstract class HalalDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun favoriteRestaurantDao(): FavoriteRestaurantDao
    abstract fun productReviewDao(): ProductReviewDao

    companion object {
        @Volatile
        private var INSTANCE: HalalDatabase? = null

        fun getDatabase(context: Context): HalalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HalalDatabase::class.java,
                    "halal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
