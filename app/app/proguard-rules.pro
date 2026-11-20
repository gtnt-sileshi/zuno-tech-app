# ============================================================
#  Zuno Tech — R8 / ProGuard rules
#  (identical strategy to Fanos Pro — same ad SDK stack)
# ============================================================

-repackageclasses ''
-allowaccessmodification
-overloadaggressively

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ── App entry points ─────────────────────────────────────────
-keep class com.zunotech.app.activity.MyApplication { <init>(); }
-keep class com.zunotech.app.service.ZunoTechMessagingService { <init>(); }

# ── Gson ─────────────────────────────────────────────────────
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation class * implements com.google.gson.TypeAdapterFactory
-keep,allowobfuscation class * implements com.google.gson.JsonSerializer
-keep,allowobfuscation class * implements com.google.gson.JsonDeserializer

# ── EventBus ─────────────────────────────────────────────────
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# ── Parcelable ───────────────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ── AdMob ────────────────────────────────────────────────────
-keepclassmembers,allowobfuscation class com.google.android.gms.ads.** { public *; }
-dontwarn com.google.android.gms.**
-dontnote com.google.android.gms.**

# ── Firebase ─────────────────────────────────────────────────
-dontwarn com.google.firebase.**
-dontnote com.google.firebase.**

# ── WorkManager ──────────────────────────────────────────────
-keep class androidx.work.impl.WorkDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { <init>(); }
-keepclassmembers,allowobfuscation class androidx.work.** { *; }
-keepclassmembers class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontnote androidx.work.**

# ── Room ─────────────────────────────────────────────────────
-keep,allowobfuscation class * extends androidx.room.RoomDatabase { <init>(); }
-keep,allowobfuscation @androidx.room.Database class * { <init>(); }
-keepclassmembers,allowobfuscation @androidx.room.Dao class * { *; }
-keepclassmembers,allowobfuscation @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**
-dontnote androidx.room.**

# ── Misc ─────────────────────────────────────────────────────
-dontwarn com.squareup.okhttp.**
-dontwarn org.apache.http.**
-dontwarn sun.misc.**
-dontwarn org.jetbrains.annotations.**
-dontnote com.google.ads.mediation.**
