# Smart Help - ProGuard & R8 Keep Rules

# Keep Kotlin Data Classes & Models
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Room Database & Entities
-keep class * extends androidx.room.RoomDatabase
-keep class com.srcreation.smarthelp.db.** { *; }
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Entity *;
    @androidx.room.TypeConverter *;
}
-dontwarn androidx.room.paging.**

# Application Domain Models & Entities
-keep class com.srcreation.smarthelp.Models** { *; }
-keep class com.srcreation.smarthelp.CaregiverModels** { *; }
-keepclassmembers class com.srcreation.smarthelp.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Gson Serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Retrofit & OkHttp
-keepclassmembers enum com.squareup.retrofit2.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Firebase (Auth, Firestore, Messaging, Vertex AI)
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Jetpack Compose & Material 3
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ML Kit Text Recognition
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ZXing QR Code
-keep class com.google.zxing.** { *; }

# Health Connect
-keep class androidx.health.connect.** { *; }
-dontwarn androidx.health.connect.**

# Biometric & Coroutines
-keep class androidx.biometric.** { *; }
-keep class kotlinx.coroutines.** { *; }
