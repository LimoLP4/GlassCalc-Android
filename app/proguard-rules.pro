# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# mXparser - Mathematical Expression Parser
# The library uses complex internal logic and reflection-like behavior for expression evaluation.
-keep class org.mariuszgromada.math.mxparser.** { *; }
-dontwarn org.mariuszgromada.math.mxparser.**

# OSS Licenses - Google Play Services
# Ensuring the license activities and their dependencies are not stripped.
-keep class com.google.android.gms.oss.licenses.** { *; }
-dontwarn com.google.android.gms.oss.licenses.**

# Liquid Glass - Shader-based UI components
# AGSL and custom graphics layers often require keeping classes to avoid shader linkage issues.
-keep class io.github.nadeemiqbal.liquidglass.** { *; }
-dontwarn io.github.nadeemiqbal.liquidglass.**

# Compose and Kotlin specific rules (usually handled by default, but keeping common ones)
-keep class androidx.compose.runtime.Composer { *; }
-keep class androidx.compose.runtime.CompositionLocal { *; }
-keep class androidx.compose.material3.** { *; }
