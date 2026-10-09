package com.srcreation.smarthelp

import com.srcreation.smarthelp.db.DietFoodEntity

data class CalculatedNutrition(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double
)

object NutritionCalculator {
    fun calculate(food: DietFoodEntity, quantity: Double, isGram: Boolean = true): CalculatedNutrition {
        val grams = if (isGram) quantity else quantity * food.servingSize
        val factor = grams / 100.0
        return CalculatedNutrition(
            calories = food.caloriesPer100g * factor,
            protein = food.proteinPer100g * factor,
            carbs = food.carbsPer100g * factor,
            fat = food.fatPer100g * factor,
            fiber = food.fiberPer100g * factor
        )
    }
}
