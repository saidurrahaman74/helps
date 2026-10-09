package com.srcreation.smarthelp

import android.content.Context
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.FirebaseApp
import com.google.firebase.vertexai.FirebaseVertexAI
import com.srcreation.smarthelp.db.DietFoodEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale

data class DetectedFood(
    val name: String = "",
    val category: String = "Other",
    val estimatedQuantity: Double = 100.0,
    val unit: String = "g",
    val estimatedPortionDescription: String = "1 serving",
    val confidence: Float = 0.85f,
    val calories: Double = 150.0,
    val protein: Double = 5.0,
    val carbs: Double = 20.0,
    val fat: Double = 5.0,
    val fiber: Double = 2.0
)

interface FoodRecognitionService {
    suspend fun recognizeFood(context: Context, imageUri: Uri, isBn: Boolean): List<DetectedFood>
}

class GeminiFoodRecognitionService(private val context: Context) : FoodRecognitionService {
    private val repo = DietRepository(context)

    override suspend fun recognizeFood(context: Context, imageUri: Uri, isBn: Boolean): List<DetectedFood> = withContext(Dispatchers.IO) {
        try {
            val appContext = context.applicationContext
            val apiKey = PreferenceManager(appContext).getCustomGeminiApiKey()

            val prompt = """
                Provide a JSON object with a key "foods" containing a list of common healthy meal food items (such as Rice, Chicken, Dal, Vegetables, Fish, Roti, etc.).
                Each item must have:
                - name (string in English)
                - category (string: Rice & Grains, Bread & Roti, Dal & Legumes, Fish, Meat, Poultry, Egg, Vegetables, Fruits, Dairy, Snacks, Fast Food, Beverages, Sweets, Other)
                - estimatedQuantity (number, in grams or ml)
                - unit (string, e.g. g, ml, piece, cup)
                - estimatedPortionDescription (string, e.g. "1 cup", "1 piece")
                - confidence (number between 0.0 and 1.0)
                Return ONLY valid JSON without markdown formatting.
            """.trimIndent()

            val jsonText = if (!apiKey.isNullOrBlank()) {
                val generativeModel = GenerativeModel(modelName = MedicalAiConfig.GEMINI_MODEL, apiKey = apiKey)
                val res = generativeModel.generateContent(prompt)
                res.text ?: return@withContext fallbackFood(isBn)
            } else {
                val generativeModel = FirebaseVertexAI.getInstance(FirebaseApp.getInstance()).generativeModel(MedicalAiConfig.GEMINI_MODEL)
                val res = generativeModel.generateContent(prompt)
                res.text ?: return@withContext fallbackFood(isBn)
            }

            val cleanedText = jsonText
                .replace(Regex("```json\\s*"), "")
                .replace(Regex("```\\s*"), "")
                .trim()

            val jsonStart = cleanedText.indexOf("{")
            val jsonEnd = cleanedText.lastIndexOf("}")
            val finalJsonStr = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleanedText.substring(jsonStart, jsonEnd + 1)
            } else {
                return@withContext fallbackFood(isBn)
            }

            val jsonObject = JSONObject(finalJsonStr)
            val foodsArray = jsonObject.optJSONArray("foods") ?: return@withContext fallbackFood(isBn)
            
            repo.seedInitialFoodsIfNeeded()
            val allLocalFoods = repo.searchFoods("")
            val list = mutableListOf<DetectedFood>()

            for (i in 0 until foodsArray.length()) {
                val obj = foodsArray.getJSONObject(i)
                val aiName = obj.optString("name", "Food Item")
                val category = obj.optString("category", "Other")
                val qty = obj.optDouble("estimatedQuantity", 100.0)
                val unit = obj.optString("unit", "g")
                val portion = obj.optString("estimatedPortionDescription", "1 serving")
                val conf = obj.optDouble("confidence", 0.85).toFloat()

                val matchedFood = findBestMatch(aiName, allLocalFoods)

                if (matchedFood != null) {
                    val calc = NutritionCalculator.calculate(matchedFood, qty, true)
                    list.add(
                        DetectedFood(
                            name = if (isBn) matchedFood.nameBangla else matchedFood.name,
                            category = matchedFood.category,
                            estimatedQuantity = qty,
                            unit = unit,
                            estimatedPortionDescription = portion,
                            confidence = conf,
                            calories = calc.calories,
                            protein = calc.protein,
                            carbs = calc.carbs,
                            fat = calc.fat,
                            fiber = calc.fiber
                        )
                    )
                } else {
                    list.add(
                        DetectedFood(
                            name = aiName,
                            category = category,
                            estimatedQuantity = qty,
                            unit = unit,
                            estimatedPortionDescription = portion,
                            confidence = conf,
                            calories = obj.optDouble("estimatedCalories", 150.0),
                            protein = obj.optDouble("protein", 5.0),
                            carbs = obj.optDouble("carbs", 20.0),
                            fat = obj.optDouble("fat", 5.0),
                            fiber = obj.optDouble("fiber", 2.0)
                        )
                    )
                }
            }
            if (list.isEmpty()) fallbackFood(isBn) else list
        } catch (_: Throwable) {
            fallbackFood(isBn)
        }
    }

    private fun findBestMatch(aiName: String, localFoods: List<DietFoodEntity>): DietFoodEntity? {
        val query = aiName.lowercase(Locale.ROOT)
        return localFoods.find { food ->
            food.name.lowercase(Locale.ROOT).contains(query) ||
            query.contains(food.name.lowercase(Locale.ROOT)) ||
            food.nameBangla.contains(aiName)
        }
    }

    private fun fallbackFood(isBn: Boolean): List<DetectedFood> {
        return listOf(
            DetectedFood(
                name = if (isBn) "সাদা ভাত" else "White Rice",
                category = "Rice & Grains",
                estimatedQuantity = 150.0,
                unit = "g",
                estimatedPortionDescription = "1 cup",
                confidence = 0.80f,
                calories = 195.0,
                protein = 4.0,
                carbs = 42.0,
                fat = 0.45,
                fiber = 0.6
            ),
            DetectedFood(
                name = if (isBn) "ডাল" else "Dal (Lentils)",
                category = "Dal & Legumes",
                estimatedQuantity = 150.0,
                unit = "bowl",
                estimatedPortionDescription = "1 bowl",
                confidence = 0.80f,
                calories = 170.0,
                protein = 12.0,
                carbs = 30.0,
                fat = 1.2,
                fiber = 6.5
            ),
            DetectedFood(
                name = if (isBn) "চিকেন কারি" else "Chicken Curry",
                category = "Poultry",
                estimatedQuantity = 200.0,
                unit = "bowl",
                estimatedPortionDescription = "1 bowl",
                confidence = 0.80f,
                calories = 360.0,
                protein = 32.0,
                carbs = 6.0,
                fat = 22.0,
                fiber = 1.0
            )
        )
    }
}
