# Apache POI
-keep class org.apache.poi.** { *; }
-keepclassmembers class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Material
-dontwarn com.google.android.material.**

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**
