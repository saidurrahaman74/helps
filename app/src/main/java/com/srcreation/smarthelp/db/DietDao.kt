package com.srcreation.smarthelp.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {
    @Query("SELECT * FROM diet_foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<DietFoodEntity>>

    @Query("SELECT * FROM diet_foods WHERE name LIKE '%' || :query || '%' OR nameBangla LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchFoods(query: String): List<DietFoodEntity>

    @Query("SELECT * FROM diet_foods WHERE category = :category ORDER BY name ASC")
    fun getFoodsByCategory(category: String): Flow<List<DietFoodEntity>>

    @Query("SELECT * FROM diet_foods WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: Long): DietFoodEntity?

    @Query("SELECT COUNT(*) FROM diet_foods")
    suspend fun getFoodCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: DietFoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFoods(foods: List<DietFoodEntity>)

    @Query("SELECT * FROM diet_meals WHERE profileId = :profileId AND date = :date ORDER BY id DESC")
    fun getMealsByDate(profileId: Int, date: String): Flow<List<MealEntity>>

    @Query("SELECT * FROM diet_meals WHERE profileId = :profileId AND date = :date")
    suspend fun getMealsListByDate(profileId: Int, date: String): List<MealEntity>

    @Query("SELECT * FROM diet_meals")
    suspend fun getFullMealList(): List<MealEntity>

    @Query("SELECT * FROM diet_meal_items WHERE mealId = :mealId")
    suspend fun getItemsForMeal(mealId: Long): List<MealFoodItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealItems(items: List<MealFoodItemEntity>)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("DELETE FROM diet_meal_items WHERE mealId = :mealId")
    suspend fun deleteItemsForMeal(mealId: Long)

    @Query("SELECT * FROM user_diet_profiles WHERE profileId = :profileId LIMIT 1")
    fun getDietProfile(profileId: Int): Flow<UserDietProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDietProfile(profile: UserDietProfileEntity)
}
