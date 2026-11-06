# AVI proguard rules
# Semua kelas aplikasi disimpan (aman untuk kotlinx.serialization):
# serializer $$serializer berada dalam package com.anztdree.avi.**
-keep class com.anztdree.avi.** { *; }

# kotlinx.serialization (jaga-jaga)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
