# ── Kotlin & Coroutines ──────────────────────────────────────
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
-keepattributes Signature, Exceptions
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ── Kotlin Serialization ─────────────────────────────────────
-keepattributes *Annotation*
-keep @kotlinx.serialization.Serializable class * { *; }
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# ── Room ─────────────────────────────────────────────────────
# Room generates _Impl classes at KSP time; keep them all
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**

# ── Proto DataStore ──────────────────────────────────────────
# Protobuf lite uses reflection for field access
-keep class com.google.protobuf.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn com.google.protobuf.**

# ── Koin ─────────────────────────────────────────────────────
# Koin uses class names to resolve modules; keep all @KoinComponent
# and anything declared in KSP-generated modules
-keep class org.koin.** { *; }
-keep @org.koin.core.annotation.* class * { *; }
-keep class **.*Module { *; }
-dontwarn org.koin.**

# ── Accessibility Service ────────────────────────────────────
# Service class names are declared in the manifest and referenced
# by the OS — never rename them
-keep class com.vocallock.service.VocalLockAccessibilityService { *; }

# ── Compose ──────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── General Android ──────────────────────────────────────────
# Preserve Parcelable implementations (used by Navigation args)
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
# Preserve enum values (used in Room and DataStore schemas)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
