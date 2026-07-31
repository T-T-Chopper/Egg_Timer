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
-keep class com.ferhatcangeyik.eggtimer.LocalizedStrings { *; }
-keep class com.ferhatcangeyik.eggtimer.AppLanguage { *; }

# Keep enum classes
-keep enum com.ferhatcangeyik.eggtimer.EggLevel { *; }
-keep enum com.ferhatcangeyik.eggtimer.CookingMethod { *; }

# Keep MainActivity
-keep class com.ferhatcangeyik.eggtimer.MainActivity { *; }

# Keep the alarm receiver: it is instantiated by the system from the manifest
-keep class com.ferhatcangeyik.eggtimer.AlarmReceiver { *; }