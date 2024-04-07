-keep public class com.google.android.gms.* { public *; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keep class org.webrtc.* { *; }
-keep class org.webrtc.audio.* { *; }
-keep class org.webrtc.voiceengine.* { *; }
-keep class org.telegram.messenger.* { *; }
-keep class org.telegram.messenger.camera.* { *; }
-keep class org.telegram.messenger.secretmedia.* { *; }
-keep class org.telegram.messenger.support.* { *; }
-keep class org.telegram.messenger.support.* { *; }
-keep class org.telegram.messenger.time.* { *; }
-keep class org.telegram.messenger.video.* { *; }
-keep class org.telegram.messenger.voip.* { *; }
-keep class org.telegram.SQLite.** { *; }
-keep class org.telegram.tgnet.ConnectionsManager { *; }
-keep class org.telegram.tgnet.NativeByteBuffer { *; }
-keep class org.telegram.tgnet.RequestTimeDelegate { *; }
-keep class org.telegram.tgnet.RequestDelegate { *; }
-keep class com.google.android.exoplayer2.ext.** { *; }
-keep class com.google.android.exoplayer2.extractor.FlacStreamMetadata { *; }
-keep class com.google.android.exoplayer2.metadata.flac.PictureFrame { *; }
-keep class com.google.android.exoplayer2.decoder.SimpleDecoderOutputBuffer { *; }
-keep class org.telegram.ui.Stories.recorder.FfmpegAudioWaveformLoader { *; }

# https://developers.google.com/ml-kit/known-issues#android_issues
-keep class com.google.mlkit.nl.languageid.internal.LanguageIdentificationJni { *; }

# Constant folding for resource integers may mean that a resource passed to this method appears to be unused. Keep the method to prevent this from happening.
-keep class com.google.android.exoplayer2.upstream.RawResourceDataSource {
  public static android.net.Uri buildRawResourceUri(int);
}

# Methods accessed via reflection in DefaultExtractorsFactory
-dontnote com.google.android.exoplayer2.ext.flac.FlacLibrary
-keepclassmembers class com.google.android.exoplayer2.ext.flac.FlacLibrary {

}

# Some members of this class are being accessed from native methods. Keep them unobfuscated.
-keep class com.google.android.exoplayer2.decoder.VideoDecoderOutputBuffer {
  *;
}

-dontnote com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.opus.LibopusAudioRenderer {
  <init>(android.os.Handler, com.google.android.exoplayer2.audio.AudioRendererEventListener, com.google.android.exoplayer2.audio.AudioProcessor[]);
}
-dontnote com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer {
  <init>(android.os.Handler, com.google.android.exoplayer2.audio.AudioRendererEventListener, com.google.android.exoplayer2.audio.AudioProcessor[]);
}
-dontnote com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer
-keepclassmembers class com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer {
  <init>(android.os.Handler, com.google.android.exoplayer2.audio.AudioRendererEventListener, com.google.android.exoplayer2.audio.AudioProcessor[]);
}

# Constructors accessed via reflection in DefaultExtractorsFactory
-dontnote com.google.android.exoplayer2.ext.flac.FlacExtractor
-keepclassmembers class com.google.android.exoplayer2.ext.flac.FlacExtractor {
  <init>();
}

# Constructors accessed via reflection in DefaultDownloaderFactory
-dontnote com.google.android.exoplayer2.source.dash.offline.DashDownloader
-keepclassmembers class com.google.android.exoplayer2.source.dash.offline.DashDownloader {
  <init>(android.net.Uri, java.util.List, com.google.android.exoplayer2.offline.DownloaderConstructorHelper);
}
-dontnote com.google.android.exoplayer2.source.hls.offline.HlsDownloader
-keepclassmembers class com.google.android.exoplayer2.source.hls.offline.HlsDownloader {
  <init>(android.net.Uri, java.util.List, com.google.android.exoplayer2.offline.DownloaderConstructorHelper);
}
-dontnote com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloader
-keepclassmembers class com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloader {
  <init>(android.net.Uri, java.util.List, com.google.android.exoplayer2.offline.DownloaderConstructorHelper);
}

# Constructors accessed via reflection in DownloadHelper
-dontnote com.google.android.exoplayer2.source.dash.DashMediaSource$Factory
-keepclasseswithmembers class com.google.android.exoplayer2.source.dash.DashMediaSource$Factory {
  <init>(com.google.android.exoplayer2.upstream.DataSource$Factory);
}
-dontnote com.google.android.exoplayer2.source.hls.HlsMediaSource$Factory
-keepclasseswithmembers class com.google.android.exoplayer2.source.hls.HlsMediaSource$Factory {
  <init>(com.google.android.exoplayer2.upstream.DataSource$Factory);
}
-dontnote com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource$Factory
-keepclasseswithmembers class com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource$Factory {
  <init>(com.google.android.exoplayer2.upstream.DataSource$Factory);
}

# Huawei Services
-keep class com.huawei.hianalytics.**{ *; }
-keep class com.huawei.updatesdk.**{ *; }
-keep class com.huawei.hms.**{ *; }

-keep class com.fasterxml.jackson.**{ *; }

-keep class org.telegram.messenger.voip.* { *; }
-keep class org.telegram.messenger.AnimatedFileDrawableStream { <methods>; }
-keep class org.telegram.SQLite.SQLiteException { <methods>; }
-keep class org.telegram.tgnet.ConnectionsManager { <methods>; }
-keep class org.telegram.tgnet.NativeByteBuffer { <methods>; }
-keepnames class org.telegram.tgnet.TLRPC$TL_* {}
-keepclassmembernames class org.telegram.ui.* { <fields>; }
-keepclassmembernames class org.telegram.ui.Cells.* { <fields>; }
-keepclassmembernames class org.telegram.ui.Components.* { <fields>; }
-keep class org.telegram.ui.Components.RLottieDrawable$LottieMetadata { <fields>; }
-keep,allowshrinking,allowobfuscation class org.telegram.ui.Components.GroupCreateSpan {
    public void updateColors();
 }
-keep,allowshrinking,allowobfuscation class org.telegram.ui.Components.Premium.GLIcon.ObjLoader {
    public <init>();
 }

# Keep Cherrygram fields name
-keepnames class uz.unnarsx.cherrygram.CherrygramConfig { <fields>; }

# Keep all class member names of CameraX
-keep class androidx.camera.extensions.** { *; }
-keep class androidx.camera.camera2.internal.** { *; }
-keep class androidx.camera.camera2.interop.** { *; }
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.core.impl.** { *; }
-keep class androidx.camera.video.** { *; }

-keepclassmembernames class androidx.core.widget.NestedScrollView {
    private android.widget.OverScroller mScroller;
    private void abortAnimatedScroll();
}

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}

-keepclassmembers enum * {
     public static **[] values();
     public static ** valueOf(java.lang.String);
}

-keepnames class androidx.recyclerview.widget.RecyclerView
-keepclassmembers class androidx.recyclerview.widget.RecyclerView {
    public void suppressLayout(boolean);
    public boolean isLayoutSuppressed();
}

-dontwarn org.jetbrains.annotations.NotNull
-dontwarn org.jetbrains.annotations.Nullable
-dontwarn androidx.camera.extensions.**
-dontwarn javax.script.**

-dontwarn a.a.a.a.a.a
-dontwarn android.telephony.HwTelephonyManager
-dontwarn com.huawei.android.os.BuildEx$VERSION
-dontwarn com.huawei.android.telephony.ServiceStateEx
-dontwarn com.huawei.hianalytics.process.HiAnalyticsConfig$Builder
-dontwarn com.huawei.hianalytics.process.HiAnalyticsConfig
-dontwarn com.huawei.hianalytics.process.HiAnalyticsInstance$Builder
-dontwarn com.huawei.hianalytics.process.HiAnalyticsInstance
-dontwarn com.huawei.hianalytics.process.HiAnalyticsManager
-dontwarn com.huawei.hianalytics.util.HiAnalyticTools
-dontwarn com.huawei.hms.commonkit.config.Config
-dontwarn com.huawei.hms.config.Server
-dontwarn com.huawei.hms.maps.auth.AuthClient
-dontwarn com.huawei.hms.maps.provider.inhuawei.IDistanceCalculatorDelegate
-dontwarn com.huawei.hms.maps.provider.inhuawei.IHuaweiMapDelegate
-dontwarn com.huawei.hms.maps.provider.inhuawei.MapFragmentDelegate
-dontwarn com.huawei.hms.maps.provider.inhuawei.MapViewDelegate
-dontwarn com.huawei.hms.network.NetworkKit$Callback
-dontwarn com.huawei.hms.network.NetworkKit
-dontwarn com.huawei.hms.network.httpclient.HttpClient$Builder
-dontwarn com.huawei.hms.network.httpclient.HttpClient
-dontwarn com.huawei.hms.network.httpclient.Response
-dontwarn com.huawei.hms.network.httpclient.ResponseBody
-dontwarn com.huawei.hms.network.httpclient.Submit
-dontwarn com.huawei.hms.network.restclient.RestClient$Builder
-dontwarn com.huawei.hms.network.restclient.RestClient
-dontwarn com.huawei.hms.network.restclient.anno.Body
-dontwarn com.huawei.hms.network.restclient.anno.GET
-dontwarn com.huawei.hms.network.restclient.anno.HeaderMap
-dontwarn com.huawei.hms.network.restclient.anno.POST
-dontwarn com.huawei.hms.network.restclient.anno.Url
-dontwarn com.huawei.hms.tss.inner.TssCallback
-dontwarn com.huawei.hms.tss.inner.TssInnerAPI
-dontwarn com.huawei.hms.tss.inner.TssInnerClient
-dontwarn com.huawei.hms.tss.inner.entity.GetCertificationKeyReq
-dontwarn com.huawei.hms.tss.inner.entity.GetCertifiedCredentialReq
-dontwarn com.huawei.libcore.io.ExternalStorageFile
-dontwarn com.huawei.libcore.io.ExternalStorageFileInputStream
-dontwarn com.huawei.libcore.io.ExternalStorageFileOutputStream
-dontwarn com.huawei.libcore.io.ExternalStorageRandomAccessFile

-repackageclasses
-allowaccessmodification
-overloadaggressively
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Use -keep to explicitly keep any other classes shrinking would remove
#-dontoptimize
-dontobfuscate