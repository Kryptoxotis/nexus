# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Supabase models
-keep class com.kryptoxotis.nexus.data.remote.dto.** { *; }
-keep class com.kryptoxotis.nexus.domain.model.** { *; }

# Keep serialization annotations
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep NFC HCE service (referenced by AndroidManifest, must not be renamed/removed)
-keep class com.kryptoxotis.nexus.service.NFCPassService { *; }
-keep class com.kryptoxotis.nexus.service.NfcReader { *; }

# Suppress R8 warnings for ktor/JVM management classes
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# Keep Serializers
-keep,includedescriptorclasses class com.kryptoxotis.nexus.**$$serializer { *; }
-keepclassmembers class com.kryptoxotis.nexus.** {
    *** Companion;
}
-keepclasseswithmembers class com.kryptoxotis.nexus.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor — keep engine + call pipeline (R8 strips via ServiceLoader reflection)
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.call.** { *; }
-keep class io.ktor.client.plugins.** { *; }
-keepnames class io.ktor.client.HttpClient
-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class kotlinx.coroutines.android.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
