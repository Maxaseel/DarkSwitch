############################################
# General
############################################

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable

# Keep annotations (important for Android framework & tooling)
-keepattributes *Annotation*

############################################
# Jetpack Compose
############################################

# Required for Compose runtime
-dontwarn androidx.compose.**

############################################
# Kotlin / Coroutines / Flow
############################################

-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

-keepclassmembers class kotlinx.coroutines.** {
    *;
}
-dontwarn kotlinx.coroutines.**

############################################
# Android Services
############################################

# AccessibilityService must not be obfuscated
-keep class * extends android.accessibilityservice.AccessibilityService {
    <init>(...);
    *;
}

# Foreground / Lifecycle services
-keep class * extends android.app.Service {
    <init>(...);
    *;
}

############################################
# Shizuku
############################################

# Keep Shizuku APIs
-keep class rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**

############################################
# AIDL / Binder interfaces
############################################

# Keep AIDL-generated interfaces
-keep interface **.I*Service
-keep class **.I*Service$Stub { *; }
-keep class **.I*Service$Stub$Proxy { *; }

############################################
# DataStore
############################################

# Preferences DataStore
-dontwarn androidx.datastore.**

############################################
# Logging (optional safety)
############################################

# Keep logging utility (DsLog)
-keep class com.alpwarestudio.darkswitch.core.log.** { *; }

############################################
# App packages (safety for internal wiring)
############################################

# Keep core app structure (services, trackers, controllers)
-keep class com.alpwarestudio.darkswitch.core.** { *; }
-keep class com.alpwarestudio.darkswitch.worker.** { *; }

############################################
# Remove warnings for hidden / internal APIs
############################################

-dontwarn android.hidden.**
-dontwarn dalvik.system.**