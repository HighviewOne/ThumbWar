# Android default ProGuard rules
-include ${android.sdk.home}/tools/proguard/proguard-android-optimize.txt

# Application-specific ProGuard rules

# Jetpack/AndroidX rules
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Jetpack Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep DataStore classes
-keep class androidx.datastore.** { *; }

# Keep navigation components
-keep class androidx.navigation.** { *; }

# Keep all Android lifecycle components
-keep class androidx.lifecycle.** { *; }

# Application classes
-keep class com.thumbwar.** { *; }
-keepclasseswithmembers class com.thumbwar.** {
    public static void main(java.lang.String[]);
}

# Keep ThumbWar MainActivity
-keep class com.thumbwar.MainActivity { *; }
-keep class com.thumbwar.ThumbWarApplication { *; }

# Keep game engine and core logic
-keep class com.thumbwar.engine.** { *; }
-keep class com.thumbwar.ai.** { *; }
-keep class com.thumbwar.data.** { *; }

# Keep enum classes used by game logic
-keepclasseswithmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep data classes
-keep class com.thumbwar.engine.GameState { *; }
-keep class com.thumbwar.engine.ThumbEntity { *; }
-keep class com.thumbwar.input.InputEvent { *; }

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize aggressively
-optimizationpasses 5
-allowaccessmodification

# Enable obfuscation
-obfuscate
-useuniqueclassmembernames

