# Keep ARCore classes accessed via JNI/reflection.
-keep class com.google.ar.core.** { *; }
-dontwarn com.google.ar.core.**

# Keep Filament classes used by native code.
-keep class com.google.android.filament.** { *; }
-dontwarn com.google.android.filament.**

# Keep ML Kit barcode scanning models and internal APIs.
-keep class com.google.mlkit.vision.barcode.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-dontwarn com.google.mlkit.vision.barcode.**
-dontwarn com.google.android.gms.internal.mlkit_vision_barcode.**
