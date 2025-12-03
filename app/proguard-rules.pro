# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Retrofit interfaces and models
-keep interface tn.esprit.libraryapp.api.** { *; }
-keep class tn.esprit.libraryapp.models.** { *; }
-keep class tn.esprit.libraryapp.data.** { *; }

# Keep Room database classes
-keep class tn.esprit.libraryapp.database.** { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Keep Socket.IO classes
-keep class io.socket.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep Stream Chat classes
-keep class io.getstream.** { *; }

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Remove logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}