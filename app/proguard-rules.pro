# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Navigation classes
-keep class androidx.navigation.** { *; }

# Keep Material3 classes
-keep class androidx.compose.material3.** { *; }

# Suppress warnings for known issues
-dontwarn kotlinx.coroutines.flow.**