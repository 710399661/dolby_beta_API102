# === LSPosed XposedModule — §5 mandatory rules ===
-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

# === Keep Xposed metadata resources (critical) ===
-keep,allowoptimization class META-INF.xposed.** { *; }

# === Main module code: keep all, allow R8 to shrink unused & obfuscate ===
-keep,allowobfuscation,allowoptimization class com.raincat.dolby_beta.** { *; }

# === Legacy Xposed stub (delegates to Libxposed at runtime) ===
-keep class de.robv.android.xposed.** { *; }
-dontwarn de.robv.android.xposed.**

# === Gson model classes ===
-keep public class **.*model*.** { *; }

# === Third-party dontwarns ===
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-dontwarn com.google.common.util.concurrent.FuturesGetChecked**
-dontwarn javax.lang.model.element.Modifier
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn org.jf.dexlib2.dexbacked.**

# === Obfuscation dictionary (shorter names = smaller DEX) ===
-obfuscationdictionary proguard-class.txt
-classobfuscationdictionary proguard-class.txt
-packageobfuscationdictionary proguard-class.txt
-repackageclasses com.raincat.dolby_beta

# === Strip verbose & debug logs in release ===
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
