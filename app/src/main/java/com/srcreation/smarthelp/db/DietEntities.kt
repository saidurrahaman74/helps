package com.srcreation.smarthelp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_foods")
data class DietFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nameBangla: String,
    val category: String, // Rice, Protein, Vegetable, Snack, Drink, etc.
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double,
    val servingUnit: String, // cup, piece, bowl, g, ml
    val servingSize: Double, // e.g. 150g or 1 cup
    val isLocalFood: Boolean = true
)

@Entity(tableName = "diet_meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Int = 1,
    val date: String, // yyyy-MM-dd
    val time: String, // hh:mm a
    val mealType: String, // Breakfast, Lunch, Snack, Dinner, Other
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "diet_meal_items")
data class MealFoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val foodId: Long = 0,
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val estimatedCalories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val confidence: Float = 1.0f
)

@Entity(tableName = "user_diet_profiles")
data class UserDietProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Int = 1,
    val age: Int = 30,
    val sex: String = "Male",
    val height: Double = 170.0, // cm
    val weight: Double = 70.0, // kg
    val activityLevel: String = "Moderate", // Sedentary, Light, Moderate, Active, Very Active
    val goal: String = "Maintain", // Maintain, Weight Loss, Weight Gain, Improve Nutrition
    val dailyCalorieTarget: Int = 2000,
    val updatedAt: Long = System.currentTimeMillis()
)
