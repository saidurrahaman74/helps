package com.srcreation.smarthelp

import android.content.Context
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.DietFoodEntity
import com.srcreation.smarthelp.db.MealEntity
import com.srcreation.smarthelp.db.MealFoodItemEntity
import com.srcreation.smarthelp.db.UserDietProfileEntity
import kotlinx.coroutines.flow.Flow

class DietRepository(context: Context) {
    private val dietDao = AppDatabase.getDatabase(context).dietDao()

    val allFoods: Flow<List<DietFoodEntity>> = dietDao.getAllFoods()

    suspend fun searchFoods(query: String): List<DietFoodEntity> = dietDao.searchFoods(query)

    fun getFoodsByCategory(category: String): Flow<List<DietFoodEntity>> = dietDao.getFoodsByCategory(category)

    suspend fun getFoodById(id: Long): DietFoodEntity? = dietDao.getFoodById(id)

    fun getMealsForDate(profileId: Int, date: String): Flow<List<MealEntity>> = dietDao.getMealsByDate(profileId, date)

    suspend fun getItemsForMeal(mealId: Long): List<MealFoodItemEntity> = dietDao.getItemsForMeal(mealId)

    suspend fun saveMealWithItems(meal: MealEntity, items: List<MealFoodItemEntity>): Long {
        val mealId = dietDao.insertMeal(meal)
        val itemsWithMealId = items.map { it.copy(mealId = mealId) }
        dietDao.insertMealItems(itemsWithMealId)
        return mealId
    }

    suspend fun deleteMeal(meal: MealEntity) {
        dietDao.deleteItemsForMeal(meal.id)
        dietDao.deleteMeal(meal)
    }

    suspend fun clearMealsForDate(profileId: Int, date: String) {
        val meals = dietDao.getMealsListByDate(profileId, date)
        for (meal in meals) {
            deleteMeal(meal)
        }
    }

    fun getProfile(profileId: Int): Flow<UserDietProfileEntity?> = dietDao.getDietProfile(profileId)

    suspend fun saveProfile(profile: UserDietProfileEntity) = dietDao.saveDietProfile(profile)

    suspend fun seedInitialFoodsIfNeeded() {
        val count = dietDao.getFoodCount()
        if (count > 0) return

        val foods = listOf(
            // Rice & Grains
            DietFoodEntity(name = "White Rice", nameBangla = "সাদা ভাত", category = "Rice & Grains", caloriesPer100g = 130.0, proteinPer100g = 2.7, carbsPer100g = 28.0, fatPer100g = 0.3, fiberPer100g = 0.4, servingUnit = "cup", servingSize = 150.0),
            DietFoodEntity(name = "Brown Rice", nameBangla = "ব্রাউন রাইস", category = "Rice & Grains", caloriesPer100g = 111.0, proteinPer100g = 2.6, carbsPer100g = 23.0, fatPer100g = 0.9, fiberPer100g = 1.8, servingUnit = "cup", servingSize = 150.0),
            DietFoodEntity(name = "Polao", nameBangla = "পোলাও", category = "Rice & Grains", caloriesPer100g = 200.0, proteinPer100g = 3.5, carbsPer100g = 32.0, fatPer100g = 6.0, fiberPer100g = 0.8, servingUnit = "plate", servingSize = 200.0),
            DietFoodEntity(name = "Khichuri", nameBangla = "খিচুড়ি", category = "Rice & Grains", caloriesPer100g = 160.0, proteinPer100g = 4.5, carbsPer100g = 26.0, fatPer100g = 4.0, fiberPer100g = 2.0, servingUnit = "bowl", servingSize = 250.0),
            DietFoodEntity(name = "Biryani", nameBangla = "বিরিয়ানি", category = "Rice & Grains", caloriesPer100g = 220.0, proteinPer100g = 8.0, carbsPer100g = 25.0, fatPer100g = 9.0, fiberPer100g = 1.0, servingUnit = "plate", servingSize = 300.0),

            // Bread & Roti
            DietFoodEntity(name = "Roti", nameBangla = "রুটি", category = "Bread & Roti", caloriesPer100g = 290.0, proteinPer100g = 9.0, carbsPer100g = 55.0, fatPer100g = 3.5, fiberPer100g = 6.0, servingUnit = "piece", servingSize = 40.0),
            DietFoodEntity(name = "Paratha", nameBangla = "পরোটা", category = "Bread & Roti", caloriesPer100g = 330.0, proteinPer100g = 7.0, carbsPer100g = 42.0, fatPer100g = 15.0, fiberPer100g = 3.0, servingUnit = "piece", servingSize = 60.0),
            DietFoodEntity(name = "Naan", nameBangla = "নান রুটি", category = "Bread & Roti", caloriesPer100g = 310.0, proteinPer100g = 8.5, carbsPer100g = 50.0, fatPer100g = 8.0, fiberPer100g = 2.2, servingUnit = "piece", servingSize = 90.0),
            DietFoodEntity(name = "White Bread", nameBangla = "সাদা পাউরুটি", category = "Bread & Roti", caloriesPer100g = 265.0, proteinPer100g = 9.0, carbsPer100g = 49.0, fatPer100g = 3.2, fiberPer100g = 2.7, servingUnit = "slice", servingSize = 30.0),

            // Dal & Legumes
            DietFoodEntity(name = "Masoor Dal", nameBangla = "মসুর ডাল", category = "Dal & Legumes", caloriesPer100g = 115.0, proteinPer100g = 9.0, carbsPer100g = 20.0, fatPer100g = 0.4, fiberPer100g = 7.9, servingUnit = "bowl", servingSize = 150.0),
            DietFoodEntity(name = "Moong Dal", nameBangla = "মুগ ডাল", category = "Dal & Legumes", caloriesPer100g = 105.0, proteinPer100g = 7.0, carbsPer100g = 19.0, fatPer100g = 0.2, fiberPer100g = 5.0, servingUnit = "bowl", servingSize = 150.0),
            DietFoodEntity(name = "Chana Dal", nameBangla = "ছোলা ডাল", category = "Dal & Legumes", caloriesPer100g = 164.0, proteinPer100g = 8.9, carbsPer100g = 27.4, fatPer100g = 2.6, fiberPer100g = 7.6, servingUnit = "bowl", servingSize = 150.0),

            // Fish
            DietFoodEntity(name = "Ilish Fish", nameBangla = "ইলিশ মাছ", category = "Fish", caloriesPer100g = 250.0, proteinPer100g = 21.0, carbsPer100g = 0.0, fatPer100g = 18.0, fiberPer100g = 0.0, servingUnit = "piece", servingSize = 100.0),
            DietFoodEntity(name = "Rui Fish", nameBangla = "রুই মাছ", category = "Fish", caloriesPer100g = 140.0, proteinPer100g = 18.0, carbsPer100g = 0.0, fatPer100g = 7.0, fiberPer100g = 0.0, servingUnit = "piece", servingSize = 100.0),
            DietFoodEntity(name = "Katla Fish", nameBangla = "কাতলা মাছ", category = "Fish", caloriesPer100g = 145.0, proteinPer100g = 19.0, carbsPer100g = 0.0, fatPer100g = 7.5, fiberPer100g = 0.0, servingUnit = "piece", servingSize = 100.0),
            DietFoodEntity(name = "Tilapia Fish", nameBangla = "তেলাপিয়া মাছ", category = "Fish", caloriesPer100g = 128.0, proteinPer100g = 26.0, carbsPer100g = 0.0, fatPer100g = 2.7, fiberPer100g = 0.0, servingUnit = "piece", servingSize = 100.0),
            DietFoodEntity(name = "Shrimp / Prawn", nameBangla = "চিংড়ি মাছ", category = "Fish", caloriesPer100g = 99.0, proteinPer100g = 24.0, carbsPer100g = 0.2, fatPer100g = 0.3, fiberPer100g = 0.0, servingUnit = "cup", servingSize = 85.0),
            DietFoodEntity(name = "Salmon", nameBangla = "স্যালমন মাছ", category = "Fish", caloriesPer100g = 208.0, proteinPer100g = 20.0, carbsPer100g = 0.0, fatPer100g = 13.0, fiberPer100g = 0.0, servingUnit = "fillet", servingSize = 120.0),
            DietFoodEntity(name = "Tuna", nameBangla = "টুনা মাছ", category = "Fish", caloriesPer100g = 132.0, proteinPer100g = 28.0, carbsPer100g = 0.0, fatPer100g = 1.3, fiberPer100g = 0.0, servingUnit = "can", servingSize = 100.0),

            // Meat & Poultry
            DietFoodEntity(name = "Chicken Curry", nameBangla = "চিকেন কারি", category = "Poultry", caloriesPer100g = 180.0, proteinPer100g = 16.0, carbsPer100g = 3.0, fatPer100g = 11.0, fiberPer100g = 0.5, servingUnit = "bowl", servingSize = 200.0),
            DietFoodEntity(name = "Grilled Chicken", nameBangla = "গ্রিলড চিকেন", category = "Poultry", caloriesPer100g = 165.0, proteinPer100g = 31.0, carbsPer100g = 0.0, fatPer100g = 3.6, fiberPer100g = 0.0, servingUnit = "breast", servingSize = 150.0),
            DietFoodEntity(name = "Beef Curry", nameBangla = "গরুর মাংস", category = "Meat", caloriesPer100g = 250.0, proteinPer100g = 22.0, carbsPer100g = 2.0, fatPer100g = 17.0, fiberPer100g = 0.0, servingUnit = "bowl", servingSize = 150.0),
            DietFoodEntity(name = "Mutton Curry", nameBangla = "খাসির মাংস", category = "Meat", caloriesPer100g = 294.0, proteinPer100g = 25.0, carbsPer100g = 1.5, fatPer100g = 21.0, fiberPer100g = 0.0, servingUnit = "bowl", servingSize = 150.0),

            // Egg
            DietFoodEntity(name = "Boiled Egg", nameBangla = "ডিম সিদ্ধ", category = "Egg", caloriesPer100g = 155.0, proteinPer100g = 13.0, carbsPer100g = 1.1, fatPer100g = 11.0, fiberPer100g = 0.0, servingUnit = "piece", servingSize = 50.0),
            DietFoodEntity(name = "Fried Egg", nameBangla = "ভাজা ডিম", category = "Egg", caloriesPer100g = 196.0, proteinPer100g = 13.6, carbsPer100g = 0.8, fatPer100g = 14.8, fiberPer100g = 0.0, servingUnit = "piece", servingSize = 55.0),

            // Vegetables
            DietFoodEntity(name = "Mixed Vegetables", nameBangla = "সবজি ভাজি", category = "Vegetables", caloriesPer100g = 70.0, proteinPer100g = 2.5, carbsPer100g = 10.0, fatPer100g = 2.5, fiberPer100g = 3.5, servingUnit = "bowl", servingSize = 150.0),
            DietFoodEntity(name = "Potato", nameBangla = "আলু", category = "Vegetables", caloriesPer100g = 77.0, proteinPer100g = 2.0, carbsPer100g = 17.0, fatPer100g = 0.1, fiberPer100g = 2.2, servingUnit = "piece", servingSize = 150.0),
            DietFoodEntity(name = "Alu Bhorta", nameBangla = "আলু ভর্তা", category = "Vegetables", caloriesPer100g = 120.0, proteinPer100g = 2.0, carbsPer100g = 18.0, fatPer100g = 4.5, fiberPer100g = 2.0, servingUnit = "serving", servingSize = 100.0),
            DietFoodEntity(name = "Begun Bhaji", nameBangla = "বেগুন ভাজা", category = "Vegetables", caloriesPer100g = 130.0, proteinPer100g = 1.5, carbsPer100g = 10.0, fatPer100g = 9.5, fiberPer100g = 3.0, servingUnit = "piece", servingSize = 80.0),
            DietFoodEntity(name = "Spinach / Shak", nameBangla = "শাক", category = "Vegetables", caloriesPer100g = 35.0, proteinPer100g = 3.0, carbsPer100g = 5.0, fatPer100g = 0.5, fiberPer100g = 3.0, servingUnit = "bowl", servingSize = 100.0),
            DietFoodEntity(name = "Broccoli", nameBangla = "ব্রকলি", category = "Vegetables", caloriesPer100g = 34.0, proteinPer100g = 2.8, carbsPer100g = 7.0, fatPer100g = 0.4, fiberPer100g = 2.6, servingUnit = "cup", servingSize = 90.0),

            // Fruits
            DietFoodEntity(name = "Banana", nameBangla = "কলা", category = "Fruits", caloriesPer100g = 89.0, proteinPer100g = 1.1, carbsPer100g = 23.0, fatPer100g = 0.3, fiberPer100g = 2.6, servingUnit = "piece", servingSize = 118.0),
            DietFoodEntity(name = "Apple", nameBangla = "আপেল", category = "Fruits", caloriesPer100g = 52.0, proteinPer100g = 0.3, carbsPer100g = 14.0, fatPer100g = 0.2, fiberPer100g = 2.4, servingUnit = "piece", servingSize = 180.0),
            DietFoodEntity(name = "Mango", nameBangla = "আম", category = "Fruits", caloriesPer100g = 60.0, proteinPer100g = 0.8, carbsPer100g = 15.0, fatPer100g = 0.4, fiberPer100g = 1.6, servingUnit = "piece", servingSize = 200.0),
            DietFoodEntity(name = "Guava", nameBangla = "পেয়ারা", category = "Fruits", caloriesPer100g = 68.0, proteinPer100g = 2.6, carbsPer100g = 14.3, fatPer100g = 0.9, fiberPer100g = 5.4, servingUnit = "piece", servingSize = 100.0),

            // Dairy
            DietFoodEntity(name = "Milk", nameBangla = "দুধ", category = "Dairy", caloriesPer100g = 60.0, proteinPer100g = 3.2, carbsPer100g = 4.8, fatPer100g = 3.3, fiberPer100g = 0.0, servingUnit = "glass", servingSize = 200.0),
            DietFoodEntity(name = "Yogurt / Doi", nameBangla = "দই", category = "Dairy", caloriesPer100g = 61.0, proteinPer100g = 3.5, carbsPer100g = 4.7, fatPer100g = 3.3, fiberPer100g = 0.0, servingUnit = "cup", servingSize = 150.0),

            // Snacks & Fast Food
            DietFoodEntity(name = "Singara", nameBangla = "সিঙ্গাড়া", category = "Snacks", caloriesPer100g = 280.0, proteinPer100g = 5.0, carbsPer100g = 32.0, fatPer100g = 14.0, fiberPer100g = 2.0, servingUnit = "piece", servingSize = 60.0),
            DietFoodEntity(name = "Fuchka", nameBangla = "ফুচকা", category = "Snacks", caloriesPer100g = 250.0, proteinPer100g = 6.0, carbsPer100g = 35.0, fatPer100g = 10.0, fiberPer100g = 3.0, servingUnit = "plate", servingSize = 150.0),
            DietFoodEntity(name = "Pizza", nameBangla = "পিজ্জা", category = "Fast Food", caloriesPer100g = 266.0, proteinPer100g = 11.0, carbsPer100g = 33.0, fatPer100g = 10.0, fiberPer100g = 2.3, servingUnit = "slice", servingSize = 100.0),
            DietFoodEntity(name = "Burger", nameBangla = "বার্গার", category = "Fast Food", caloriesPer100g = 295.0, proteinPer100g = 14.0, carbsPer100g = 24.0, fatPer100g = 15.0, fiberPer100g = 1.5, servingUnit = "piece", servingSize = 180.0),

            // Beverages
            DietFoodEntity(name = "Tea with Milk", nameBangla = "চা (দুধ চা)", category = "Beverages", caloriesPer100g = 60.0, proteinPer100g = 2.0, carbsPer100g = 8.0, fatPer100g = 2.2, fiberPer100g = 0.0, servingUnit = "cup", servingSize = 150.0),
            DietFoodEntity(name = "Black Tea", nameBangla = "লাল চা", category = "Beverages", caloriesPer100g = 2.0, proteinPer100g = 0.1, carbsPer100g = 0.3, fatPer100g = 0.0, fiberPer100g = 0.0, servingUnit = "cup", servingSize = 150.0),

            // Sweets
            DietFoodEntity(name = "Mishti Doi", nameBangla = "মিষ্টি দই", category = "Sweets", caloriesPer100g = 110.0, proteinPer100g = 4.0, carbsPer100g = 18.0, fatPer100g = 3.0, fiberPer100g = 0.0, servingUnit = "cup", servingSize = 100.0),
            DietFoodEntity(name = "Rasgulla", nameBangla = "রসগোল্লা", category = "Sweets", caloriesPer100g = 185.0, proteinPer100g = 4.0, carbsPer100g = 42.0, fatPer100g = 0.5, fiberPer100g = 0.0, servingUnit = "piece", servingSize = 50.0),

            // International & Fitness Foods
            DietFoodEntity(name = "Oatmeal", nameBangla = "ওটমিল (Oatmeal)", category = "Rice & Grains", caloriesPer100g = 68.0, proteinPer100g = 2.4, carbsPer100g = 12.0, fatPer100g = 1.4, fiberPer100g = 1.7, servingUnit = "bowl", servingSize = 240.0),
            DietFoodEntity(name = "Quinoa", nameBangla = "কুইনোয়া (Quinoa)", category = "Rice & Grains", caloriesPer100g = 120.0, proteinPer100g = 4.4, carbsPer100g = 21.3, fatPer100g = 1.9, fiberPer100g = 2.8, servingUnit = "cup", servingSize = 185.0),
            DietFoodEntity(name = "Greek Yogurt", nameBangla = "গ্রিক দই (Greek Yogurt)", category = "Dairy", caloriesPer100g = 97.0, proteinPer100g = 10.0, carbsPer100g = 3.6, fatPer100g = 5.0, fiberPer100g = 0.0, servingUnit = "cup", servingSize = 150.0),
            DietFoodEntity(name = "Avocado Toast", nameBangla = "অ্যাভোকাডো টোস্ট", category = "Snacks", caloriesPer100g = 220.0, proteinPer100g = 5.0, carbsPer100g = 20.0, fatPer100g = 14.0, fiberPer100g = 6.0, servingUnit = "slice", servingSize = 120.0),
            DietFoodEntity(name = "Chicken Breast", nameBangla = "চিকেন ব্রেস্ট (Lean Protein)", category = "Poultry", caloriesPer100g = 165.0, proteinPer100g = 31.0, carbsPer100g = 0.0, fatPer100g = 3.6, fiberPer100g = 0.0, servingUnit = "breast", servingSize = 150.0),
            DietFoodEntity(name = "Caesar Salad", nameBangla = "সিজার সালাদ", category = "Vegetables", caloriesPer100g = 150.0, proteinPer100g = 5.0, carbsPer100g = 6.0, fatPer100g = 12.0, fiberPer100g = 2.0, servingUnit = "bowl", servingSize = 200.0),
            DietFoodEntity(name = "Pasta Carbonara", nameBangla = "পাস্তা কার্বোনারা", category = "Fast Food", caloriesPer100g = 210.0, proteinPer100g = 8.0, carbsPer100g = 24.0, fatPer100g = 9.0, fiberPer100g = 1.5, servingUnit = "plate", servingSize = 250.0),
            DietFoodEntity(name = "Beef Steak", nameBangla = "বিফ স্টেক (Beef Steak)", category = "Meat", caloriesPer100g = 271.0, proteinPer100g = 25.0, carbsPer100g = 0.0, fatPer100g = 19.0, fiberPer100g = 0.0, servingUnit = "serving", servingSize = 200.0),
            DietFoodEntity(name = "Protein Shake", nameBangla = "প্রোটিন শেক (Whey)", category = "Beverages", caloriesPer100g = 120.0, proteinPer100g = 24.0, carbsPer100g = 3.0, fatPer100g = 1.5, fiberPer100g = 1.0, servingUnit = "glass", servingSize = 300.0),
            DietFoodEntity(name = "Almonds", nameBangla = "আমন্ড বাদাম (Almonds)", category = "Snacks", caloriesPer100g = 579.0, proteinPer100g = 21.0, carbsPer100g = 22.0, fatPer100g = 50.0, fiberPer100g = 12.5, servingUnit = "handful", servingSize = 30.0),
            DietFoodEntity(name = "Chia Seeds", nameBangla = "চিয়া সিডস (Chia Seeds)", category = "Snacks", caloriesPer100g = 486.0, proteinPer100g = 16.5, carbsPer100g = 42.1, fatPer100g = 30.7, fiberPer100g = 34.4, servingUnit = "tablespoon", servingSize = 15.0),
            DietFoodEntity(name = "Sushi Roll", nameBangla = "সুশি রোল (Sushi)", category = "Fast Food", caloriesPer100g = 145.0, proteinPer100g = 5.5, carbsPer100g = 26.0, fatPer100g = 2.0, fiberPer100g = 1.0, servingUnit = "roll", servingSize = 120.0),
            DietFoodEntity(name = "Pancakes", nameBangla = "প্যানকেক (Pancakes)", category = "Snacks", caloriesPer100g = 227.0, proteinPer100g = 6.0, carbsPer100g = 28.0, fatPer100g = 10.0, fiberPer100g = 1.0, servingUnit = "piece", servingSize = 60.0),
            DietFoodEntity(name = "French Fries", nameBangla = "ফ্রেঞ্চ ফ্রাই (French Fries)", category = "Fast Food", caloriesPer100g = 312.0, proteinPer100g = 3.4, carbsPer100g = 41.0, fatPer100g = 15.0, fiberPer100g = 3.8, servingUnit = "serving", servingSize = 100.0),
            DietFoodEntity(name = "Peanut Butter Toast", nameBangla = "পিনাট বাটার টোস্ট", category = "Snacks", caloriesPer100g = 380.0, proteinPer100g = 14.0, carbsPer100g = 35.0, fatPer100g = 20.0, fiberPer100g = 4.5, servingUnit = "slice", servingSize = 70.0),
            DietFoodEntity(name = "Tuna Sandwich", nameBangla = "টুনা স্যান্ডউইচ", category = "Fast Food", caloriesPer100g = 220.0, proteinPer100g = 15.0, carbsPer100g = 22.0, fatPer100g = 8.0, fiberPer100g = 2.0, servingUnit = "sandwich", servingSize = 150.0),
            DietFoodEntity(name = "Hummus with Pita", nameBangla = "হামুমাস ও পিটা ব্রেড", category = "Snacks", caloriesPer100g = 240.0, proteinPer100g = 7.5, carbsPer100g = 30.0, fatPer100g = 10.0, fiberPer100g = 5.0, servingUnit = "serving", servingSize = 120.0)
        )
        dietDao.insertAllFoods(foods)
    }
}
