# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class kotlin.coroutines.** { *; }

# Keep data classes used for localization
-keep class com.example.eggtimer.LocalizedStrings { *; }
-keep class com.example.eggtimer.AppLanguage { *; }

# Keep enum classes
-keep enum com.example.eggtimer.EggLevel { *; }
-keep enum com.example.eggtimer.CookingMethod { *; }

# Keep MainActivity
-keep class com.example.eggtimer.MainActivity { *; }