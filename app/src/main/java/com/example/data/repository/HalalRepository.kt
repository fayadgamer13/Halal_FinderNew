package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.*
import com.example.data.network.GeminiApiClient
import com.example.data.network.GeminiContent
import com.example.data.network.GeminiGenerationConfig
import com.example.data.network.GeminiPart
import com.example.data.network.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class IngredientAnalysis(
    val name: String,
    val status: String, // "HALAL", "HARAM", "MUSHBOOH"
    val reason: String
)

data class ScanResult(
    val productName: String,
    val overallStatus: String, // "HALAL", "HARAM", "MUSHBOOH"
    val confidence: String,
    val items: List<IngredientAnalysis>
)

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val address: String,
    val certification: String,
    val rating: Double,
    val distance: String,
    val isFavorite: Boolean = false,
    val reviewsCount: Int = 12
)

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val status: String,
    val barcode: String,
    val ingredients: String,
    val description: String,
    val rating: Double
)

class HalalRepository(private val context: Context) {

    private val db = HalalDatabase.getDatabase(context)
    private val profileDao = db.userProfileDao()
    private val favoriteDao = db.favoriteRestaurantDao()
    private val reviewDao = db.productReviewDao()

    // Baseline fallback keyword lists
    private val haramKeywords = listOf(
        "pork", "lard", "bacon", "ham", "gelatin (pork", "carmine", "cochineal", "e120", "e-120",
        "pepsin", "l-cysteine (human", "l-cysteine (duck", "byproduct (pork", "whey (pork", "rennet (animal",
        "alcohol", "beer", "wine", "ethanol", "rum", "whiskey", "brandy", "intoxicant"
    )

    private val mushboohKeywords = listOf(
        "gelatin", "e441", "e-441", "mono- and diglycerides", "e471", "e-471", "e472", "glycerin", "glycerol",
        "e422", "enzymes", "whey", "lecithin", "e322", "rennet", "vanilla extract", "natural flavor",
        "artificial flavor", "shortening", "emulsifier", "e124", "e102", "cochineal extract", "pepsin"
    )

    // Preset products database
    val presetProducts = listOf(
        Product(
            id = "prod_1",
            name = "Choco-Joy Chocolate Bar",
            category = "Snacks & Sweets",
            status = "HALAL",
            barcode = "501234567890",
            ingredients = "Sugar, Cocoa Butter, Whole Milk Powder, Soy Lecithin (E322), Vanilla Flavoring",
            description = "Rich premium milk chocolate crafted with 100% plant-based lecithins and natural vanilla.",
            rating = 4.8
        ),
        Product(
            id = "prod_2",
            name = "Gummy Bear Wonders",
            category = "Snacks & Sweets",
            status = "HARAM",
            barcode = "491234567890",
            ingredients = "Corn Syrup, Sugar, Pork Gelatin (E441), Citric Acid, Carmine Color (E120), Artificial Flavors",
            description = "Soft candies containing pork-derived gelatin and carmine (cochineal extract red color).",
            rating = 1.2
        ),
        Product(
            id = "prod_3",
            name = "Crispy Potato Waves - Hot Chilli",
            category = "Chips & Savory",
            status = "HALAL",
            barcode = "880123456789",
            ingredients = "Potatoes, Vegetable Palm Oil, Chilli Seasoning, Salt, Garlic Powder, Onion Powder, Citric Acid",
            description = "Crispy ridged potato chips baked in vegetable oil with synthetic spicy seasoning.",
            rating = 4.5
        ),
        Product(
            id = "prod_4",
            name = "Crunchy Cheese Puffs",
            category = "Chips & Savory",
            status = "MUSHBOOH",
            barcode = "761123456789",
            ingredients = "Corn Meal, Coconut Oil, Cheese Powder, Whey, Disodium Phosphate, Enzymes (Source Unspecified), E471 Emulsifier",
            description = "Fluffy cheese puffs containing animal-derived enzymes and E471 emusifiers without certified source.",
            rating = 3.6
        ),
        Product(
            id = "prod_5",
            name = "Tokyo Shoyu Ramen Cup",
            category = "Instant Meals",
            status = "MUSHBOOH",
            barcode = "490123456789",
            ingredients = "Noodle (Wheat Flour, Salt), Soy Sauce Soup Base, Hydrolyzed Pork Protein, Chicken Extract, Flavour Enhancers",
            description = "Instant soup bowl with chicken broth extracts and hydrolyzed proteins of unspecified origin.",
            rating = 3.1
        )
    )

    // Preset Restaurants
    private val presetRestaurants = listOf(
        Restaurant(
            id = "rest_1",
            name = "Al-Barakah Kabab & Grill",
            cuisine = "Pakistani & Indian",
            address = "452 Halal Boulevard, Metropolis",
            certification = "100% Halal Certified (Muslim-Owned)",
            rating = 4.8,
            distance = "0.3 km"
        ),
        Restaurant(
            id = "rest_2",
            name = "Istanbul Grand Bazaar Bistro",
            cuisine = "Turkish Authentic",
            address = "78 Crescent Way, Metropolis",
            certification = "Halal Certified by HMC Board",
            rating = 4.6,
            distance = "0.8 km"
        ),
        Restaurant(
            id = "rest_3",
            name = "Zaitoon Mediterranean Grill",
            cuisine = "Arabian & Levantine",
            address = "12 Olive branch Court, Metropolis",
            certification = "Halal Friendly (Halal Meat / Alcohol-Free)",
            rating = 4.5,
            distance = "1.5 km"
        ),
        Restaurant(
            id = "rest_4",
            name = "Little Canton Dim Sum & Wok",
            cuisine = "Halal Chinese",
            address = "22 Noodle Dr, Eastside",
            certification = "100% Halal Certified by HFAA",
            rating = 4.4,
            distance = "2.1 km"
        ),
        Restaurant(
            id = "rest_5",
            name = "Madinah Gourmet Burger Lab",
            cuisine = "Gourmet Burgers",
            address = "104 Pioneer Ave, Metropolis",
            certification = "100% Halal Certified (Muslim-Owned)",
            rating = 4.7,
            distance = "1.2 km"
        ),
        Restaurant(
            id = "rest_6",
            name = "The Green Oasis Kitchen",
            cuisine = "Vegan & Seafood Custom",
            address = "90 Eco Square, Metropolis",
            certification = "Vegan & Seafood / Halal Friendly",
            rating = 4.3,
            distance = "3.2 km"
        )
    )

    // Live Flow for User Profile with standard setup if empty
    val userProfile: Flow<UserProfile> = profileDao.getUserProfile().map { profile ->
        profile ?: UserProfile().also {
            // Lazy prepopulate
            withContext(Dispatchers.IO) {
                profileDao.insertProfile(it)
            }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        profileDao.insertProfile(profile)
    }

    // Favorite restaurants flow, mapped with status
    val favoritesList: Flow<List<FavoriteRestaurant>> = favoriteDao.getAllFavorites()

    fun getRestaurants(favorites: List<FavoriteRestaurant>): List<Restaurant> {
        return presetRestaurants.map { rest ->
            rest.copy(isFavorite = favorites.any { it.id == rest.id })
        }
    }

    suspend fun toggleFavorite(restaurant: Restaurant) = withContext(Dispatchers.IO) {
        val exists = favoriteDao.getAllFavorites().map { list -> list.any { it.id == restaurant.id } }.firstOrNull() ?: false
        if (exists) {
            favoriteDao.deleteFavoriteById(restaurant.id)
        } else {
            favoriteDao.insertFavorite(
                FavoriteRestaurant(
                    id = restaurant.id,
                    name = restaurant.name,
                    cuisine = restaurant.cuisine,
                    address = restaurant.address,
                    certification = restaurant.certification,
                    rating = restaurant.rating,
                    distance = restaurant.distance
                )
            )
        }
    }

    // Product reviews flow
    fun getProductReviews(productId: String): Flow<List<ProductReview>> = flow {
        // Pre-populate standard reviews first if empty
        val dbReviews = reviewDao.getReviewsForProduct(productId).firstOrNull() ?: emptyList()
        if (dbReviews.isEmpty()) {
            val presetReviews = when (productId) {
                "prod_1" -> listOf(
                    ProductReview(productId = "prod_1", productName = "Choco-Joy Chocolate Bar", userName = "Adil K.", userAvatar = "Avatar 3", rating = 5, reviewText = "Mashallah, very creamy chocolate! Great to know E322 is soy-derived."),
                    ProductReview(productId = "prod_1", productName = "Choco-Joy Chocolate Bar", userName = "Fatima R.", userAvatar = "Avatar 2", rating = 4, reviewText = "My kids love this. Verified directly with manufacturers as well.")
                )
                "prod_2" -> listOf(
                    ProductReview(productId = "prod_2", productName = "Gummy Bear Wonders", userName = "Zayd S.", userAvatar = "Avatar 1", rating = 1, reviewText = "Haram! Uses Gelatin E441 without specifying cow/halal source. Avoid!"),
                    ProductReview(productId = "prod_2", productName = "Gummy Bear Wonders", userName = "Sara N.", userAvatar = "Avatar 4", rating = 1, reviewText = "Contains Carmine E120 too. Astaghfirullah, steer clear!")
                )
                "prod_4" -> listOf(
                    ProductReview(productId = "prod_4", productName = "Crunchy Cheese Puffs", userName = "Omar H.", userAvatar = "Avatar 5", rating = 3, reviewText = "Mushbooh because of unspecified enzymes. I suggest buying halal-certified brands.")
                )
                else -> emptyList()
            }
            presetReviews.forEach { reviewDao.insertReview(it) }
        }
        reviewDao.getReviewsForProduct(productId).collect { emit(it) }
    }

    suspend fun addProductReview(review: ProductReview) = withContext(Dispatchers.IO) {
        reviewDao.insertReview(review)
    }

    // Core Scanner Logic — analyze elements
    suspend fun analyzeIngredients(productNameInput: String, ingredientsInput: String): ScanResult = withContext(Dispatchers.IO) {
        val name = productNameInput.ifBlank { "Scanned Product" }
        val rawInput = ingredientsInput.trim()

        if (rawInput.isBlank()) {
            return@withContext ScanResult(name, "MUSHBOOH", "10%", listOf(IngredientAnalysis("No text detected", "MUSHBOOH", "Please enter some ingredients or scan a valid list.")))
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (hasKey) {
            try {
                val prompt = """
                    Classify the ingredients list of the product "$name" for Halal status.
                    Ingredients: "$rawInput"
                    
                    Return a concise analysis for each distinct ingredient. Explain why each is Halal, Haram (pork, alcohol, etc.), or Mushbooh (doubtful, or needs animal/plant source verification like E471, Gelatin, etc.). 
                    Format the output strictly as a structured JSON object:
                    {
                      "productName": "$name",
                      "overallStatus": "HALAL" or "HARAM" or "MUSHBOOH",
                      "confidence": "X%",
                      "items": [
                        {
                          "name": "ingredient name",
                          "status": "HALAL" or "HARAM" or "MUSHBOOH",
                          "reason": "short explanation"
                        }
                      ]
                    }
                    Provide ONLY the valid minified JSON object inside no markdown wrappers.
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(responseMimeType = "application/json", temperature = 0.2f),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are an expert Islamic Halal food analyst. You analyze recipes, E-numbers, and additives. Be precise and strict.")))
                )

                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!textResponse.isNullOrBlank()) {
                    // Simple regex/parsing or Moshi loading to decode json
                    val cleanJson = textResponse.trim().removeSurrounding("```json", "```").trim()
                    // Try parsing using Moshi
                    val adapter = com.squareup.moshi.Moshi.Builder().build().adapter(ScanResult::class.java)
                    val result = adapter.fromJson(cleanJson)
                    if (result != null) {
                        return@withContext result
                    }
                }
            } catch (e: Exception) {
                Log.e("HalalRepository", "Gemini call failed, defaulting to local engine: ${e.message}")
            }
        }

        // --- Offline fallback classification ---
        val detectedIngredients = rawInput.split(Regex("[,;\\(\\)\\n]+")).map { it.trim() }.filter { it.isNotBlank() }
        val itemsList = mutableListOf<IngredientAnalysis>()
        var overallHaramCount = 0
        var overallMushboohCount = 0

        for (ingredient in detectedIngredients) {
            val lowerName = ingredient.lowercase()
            var status = "HALAL"
            var reason = "Generally recognized as plant-based, synthetic, or non-animal ingredient."

            // Check HARAM
            val haramMatch = haramKeywords.firstOrNull { lowerName.contains(it) }
            if (haramMatch != null) {
                status = "HARAM"
                overallHaramCount++
                reason = "Contains or derived from $haramMatch (known forbidden substance in Islamic dietary law)."
            } else {
                // Check MUSHBOOH
                val mushboohMatch = mushboohKeywords.firstOrNull { lowerName.contains(it) }
                if (mushboohMatch != null) {
                    status = "MUSHBOOH"
                    overallMushboohCount++
                    reason = "Derived from $mushboohMatch, which might be sourced from animal fats (haram) or plants (halal). Certified verification required."
                }
            }
            itemsList.add(IngredientAnalysis(ingredient, status, reason))
        }

        val overallStatus = when {
            overallHaramCount > 0 -> "HARAM"
            overallMushboohCount > 0 -> "MUSHBOOH"
            else -> "HALAL"
        }

        return@withContext ScanResult(
            productName = name,
            overallStatus = overallStatus,
            confidence = if (hasKey) "50%" else "Local Scan Database (95%)",
            items = itemsList
        )
    }
}
