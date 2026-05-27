package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProductReview
import com.example.data.UserProfile
import com.example.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Loading : ScanUiState
    data class Success(val result: ScanResult) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

data class ThemeColorPalette(
    val name: String,
    val primary: Long,
    val container: Long,
    val darkPrimary: Long
)

class HalalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HalalRepository(application)

    // Palettes array corresponding to accentColorIndex in UserProfile
    val accentPalettes = listOf(
        ThemeColorPalette("Emerald Green", 0xFF386B40, 0xFFD1E8D1, 0xFF8DC095),
        ThemeColorPalette("Golden Amber",  0xFFD84315, 0xFFFBE9E7, 0xFFFFAB91),
        ThemeColorPalette("Mystic Teal",   0xFF006064, 0xFFE0F7FA, 0xFF80DEEA),
        ThemeColorPalette("Ocean Blue",    0xFF1565C0, 0xFFE3F2FD, 0xFF90CAF9),
        ThemeColorPalette("Coral Pink",    0xFFC2185B, 0xFFFCE4EC, 0xFFF48FB1)
    )

    // Profile State
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    // Favorites State
    val favoritesList = repository.favoritesList.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Restaurants lists (filtered)
    private val _restaurantSearchQuery = MutableStateFlow("")
    val restaurantSearchQuery = _restaurantSearchQuery.asStateFlow()

    private val _cuisineFilter = MutableStateFlow("All")
    val cuisineFilter = _cuisineFilter.asStateFlow()

    private val _certFilter = MutableStateFlow("All")
    val certFilter = _certFilter.asStateFlow()

    val restaurantsList: StateFlow<List<Restaurant>> = combine(
        favoritesList,
        _restaurantSearchQuery,
        _cuisineFilter,
        _certFilter
    ) { favs, query, cuisine, cert ->
        repository.getRestaurants(favs).filter { rest ->
            val matchesQuery = rest.name.contains(query, ignoreCase = true) ||
                    rest.cuisine.contains(query, ignoreCase = true) ||
                    rest.address.contains(query, ignoreCase = true)
            val matchesCuisine = cuisine == "All" || rest.cuisine.contains(cuisine, ignoreCase = true)
            val matchesCert = cert == "All" || when (cert) {
                "100% Certified" -> rest.certification.contains("100% Certified", ignoreCase = true) || rest.certification.contains("HMC", ignoreCase = true)
                "Halal Friendly" -> rest.certification.contains("Friendly", ignoreCase = true)
                "Vegan/Seafood" -> rest.certification.contains("Vegan", ignoreCase = true) || rest.certification.contains("Seafood", ignoreCase = true)
                else -> true
            }
            matchesQuery && matchesCuisine && matchesCert
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Product search Catalog and details
    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery = _productSearchQuery.asStateFlow()

    val productsList: StateFlow<List<Product>> = _productSearchQuery.map { query ->
        if (query.isBlank()) {
            repository.presetProducts
        } else {
            repository.presetProducts.filter { prod ->
                prod.name.contains(query, ignoreCase = true) ||
                        prod.barcode.contains(query) ||
                        prod.ingredients.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.presetProducts
    )

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct = _selectedProduct.asStateFlow()

    val selectedProductReviews: StateFlow<List<ProductReview>> = _selectedProduct.flatMapLatest { prod ->
        if (prod == null) {
            flowOf(emptyList())
        } else {
            repository.getProductReviews(prod.id)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Scan State
    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState = _scanUiState.asStateFlow()

    // Prayer times ticking calculations
    data class PrayerTime(val name: String, val timeString: String, val hour24: Int, val minute: Int)
    
    val prayerTimes = listOf(
        PrayerTime("Fajr", "04:32 AM", 4, 32),
        PrayerTime("Sunrise", "05:54 AM", 5, 54),
        PrayerTime("Dhuhr", "12:18 PM", 12, 18),
        PrayerTime("Asr", "03:42 PM", 15, 42),
        PrayerTime("Maghrib", "06:22 PM", 18, 22),
        PrayerTime("Isha", "07:44 PM", 19, 44)
    )

    val nextPrayerState = flow {
        while (true) {
            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMin = now.get(Calendar.MINUTE)
            val currentSec = now.get(Calendar.SECOND)
            val currentTotalSec = (currentHour * 3600) + (currentMin * 60) + currentSec

            var targetSecs = 0
            var name = ""
            var found = false

            for (prayer in prayerTimes) {
                val pTotalSec = (prayer.hour24 * 3600) + (prayer.minute * 60)
                if (pTotalSec > currentTotalSec) {
                    targetSecs = pTotalSec
                    name = prayer.name
                    found = true
                    break
                }
            }

            if (!found) {
                // Next prayer is tomorrow's Fajr
                val fajr = prayerTimes.first()
                targetSecs = ((fajr.hour24 + 24) * 3600) + (fajr.minute * 60)
                name = fajr.name
            }

            val diffTotalSecs = targetSecs - currentTotalSec
            val hours = diffTotalSecs / 3600
            val minutes = (diffTotalSecs % 3600) / 60
            val seconds = diffTotalSecs % 60

            val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            val progress = 1f - (diffTotalSecs.toFloat() / (24f * 3600f)).coerceIn(0f, 1f)

            emit(Triple(name, countdownStr, progress))
            delay(1000)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Triple("Fajr", "00:00:00", 0f)
    )

    // Profile Settings Action handlers
    fun updateProfileName(name: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.saveUserProfile(current.copy(name = name))
        }
    }

    fun updateProfileAvatar(avatarName: String) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.saveUserProfile(current.copy(avatarName = avatarName))
        }
    }

    fun updateThemeIndex(index: Int) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.saveUserProfile(current.copy(accentColorIndex = index))
        }
    }

    fun togglePrayerNotification(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.saveUserProfile(current.copy(prayerNotificationsEnabled = enabled))
        }
    }

    fun toggleGeneralNotification(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value
            repository.saveUserProfile(current.copy(generalNotificationsEnabled = enabled))
        }
    }

    // Toggle Restaurant state
    fun toggleFavoriteRestaurant(restaurant: Restaurant) {
        viewModelScope.launch {
            repository.toggleFavorite(restaurant)
        }
    }

    // Toggle filter
    fun setCuisineFilter(cuisine: String) {
        _cuisineFilter.value = cuisine
    }

    fun setCertFilter(cert: String) {
        _certFilter.value = cert
    }

    fun setRestaurantSearch(query: String) {
        _restaurantSearchQuery.value = query
    }

    // Load selected product
    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun setProductSearch(query: String) {
        _productSearchQuery.value = query
    }

    // Submit a community product review
    fun submitProductReview(productId: String, productName: String, rating: Int, reviewText: String) {
        viewModelScope.launch {
            val currentProfile = userProfile.value
            val review = ProductReview(
                productId = productId,
                productName = productName,
                userName = currentProfile.name,
                userAvatar = currentProfile.avatarName,
                rating = rating,
                reviewText = reviewText
            )
            repository.addProductReview(review)
            // Re-select to prompt refresh
            val currentSelected = selectedProduct.value
            if (currentSelected?.id == productId) {
                _selectedProduct.value = null
                _selectedProduct.value = currentSelected
            }
        }
    }

    // Request analysis of ingredients
    fun performScan(productName: String, ingredientsText: String) {
        _scanUiState.value = ScanUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.analyzeIngredients(productName, ingredientsText)
                _scanUiState.value = ScanUiState.Success(result)
            } catch (e: Exception) {
                _scanUiState.value = ScanUiState.Error(e.message ?: "An unknown scan error occurred")
            }
        }
    }

    fun clearScan() {
        _scanUiState.value = ScanUiState.Idle
    }
}
