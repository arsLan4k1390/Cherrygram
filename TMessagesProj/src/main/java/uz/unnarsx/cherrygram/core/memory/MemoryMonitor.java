/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.memory;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.SharedConfig;

import java.lang.ref.WeakReference;

public class MemoryMonitor implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    @Nullable
    private static volatile MemoryMonitor instance;

    @Nullable
    public static MemoryMonitor getInstance() {
        return instance;
    }

    public interface OnOomWarningListener {
        void onOomWarning(@NonNull Activity activity);
    }

    public interface OnCleanupListener {
        void onCleanupRequested(double currentUsageRatio);
    }

    private final Application application;

    private final double cleanupThresholdRatio;
    private float warningThresholdRatio;
    private final long checkIntervalMs;
    private final long cleanupCooldownMs;

    private final OnCleanupListener cleanupListener;
    private final OnOomWarningListener warningListener;

    private final HandlerThread workerThread = new HandlerThread("MemoryMonitorWorker");
    private Handler workerHandler;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile WeakReference<Activity> currentActivityRef;
    private volatile boolean isMonitoring = false;

    private long lastCleanupAt = 0L;

    private volatile long lastMaxMemory = 0L;
    private volatile long lastUsedMemory = 0L;
    private volatile double lastUsageRatio = 0.0;

    private final Runnable memoryCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkMemoryUsage();
            if (isMonitoring) {
                workerHandler.postDelayed(this, checkIntervalMs);
            }
        }
    };

    public MemoryMonitor(
            @NonNull Application application,
            double cleanupThresholdRatio,
            float warningThresholdRatio,
            long checkIntervalMs,
            long cleanupCooldownMs,
            @Nullable OnCleanupListener cleanupListener,
            @NonNull OnOomWarningListener warningListener
    ) {
        this.application = application;
        this.cleanupThresholdRatio = cleanupThresholdRatio;
        this.warningThresholdRatio = warningThresholdRatio;
        this.checkIntervalMs = checkIntervalMs;
        this.cleanupCooldownMs = cleanupCooldownMs;
        this.cleanupListener = cleanupListener;
        this.warningListener = warningListener;
    }

    public void start() {
        if (isMonitoring) return;

        if (!workerThread.isAlive()) {
            workerThread.start();
        }
        workerHandler = new Handler(workerThread.getLooper());

        application.registerActivityLifecycleCallbacks(this);
        application.registerComponentCallbacks(this);

        isMonitoring = true;
        lastCleanupAt = 0L;

        instance = this;
        workerHandler.post(memoryCheckRunnable);
    }

    // --- Геттеры для отображения на отдельном экране (таблица метрик и т.п.) ---
    public long getMaxMemoryMb() {
        return lastMaxMemory / (1024 * 1024);
    }

    public long getUsedMemoryMb() {
        return lastUsedMemory / (1024 * 1024);
    }

    public double getCurrentUsagePercent() {
        return lastUsageRatio * 100.0;
    }

    public void updateWarningThresholdRatio(float value) {
        warningThresholdRatio = value / 100;
    }

    public void stop() {
        isMonitoring = false;
        if (workerHandler != null) {
            workerHandler.removeCallbacks(memoryCheckRunnable);
        }
        application.unregisterActivityLifecycleCallbacks(this);
        application.unregisterComponentCallbacks(this);
        workerThread.quitSafely();
    }

    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double currentUsageRatio = (double) usedMemory / (double) maxMemory;

        lastMaxMemory = maxMemory;
        lastUsedMemory = usedMemory;
        lastUsageRatio = currentUsageRatio;

        if (currentUsageRatio >= warningThresholdRatio) {
            System.gc();

            long postGcUsed = runtime.totalMemory() - runtime.freeMemory();
            double postGcRatio = (double) postGcUsed / (double) maxMemory;

            if (postGcRatio >= warningThresholdRatio) {
                showWarning();
            }
            return;
        }

        if (currentUsageRatio >= cleanupThresholdRatio) {
            maybeRequestCleanup(currentUsageRatio);
        }
    }

    private void maybeRequestCleanup(double currentUsageRatio) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastCleanupAt < cleanupCooldownMs) return;
        lastCleanupAt = now;

        if (cleanupListener != null) {
            cleanupListener.onCleanupRequested(currentUsageRatio);
        } else {
            System.gc();
        }
    }

    private void showWarning() {
        Activity activity = currentActivityRef != null ? currentActivityRef.get() : null;
        if (activity == null) return;

        mainHandler.post(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                warningListener.onOomWarning(activity);
            }
        });
    }

    @Override
    public void onTrimMemory(int level) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL && workerHandler != null) {
            workerHandler.post(this::showWarning);
        }
    }

    @Override
    public void onLowMemory() {
        if (workerHandler != null) {
            workerHandler.post(this::showWarning);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivityRef = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (currentActivityRef != null && currentActivityRef.get() == activity) {
            currentActivityRef = null;
        }
    }

    @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}
    @Override public void onActivityStarted(@NonNull Activity activity) {}
    @Override public void onActivityStopped(@NonNull Activity activity) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}
    @Override public void onActivityDestroyed(@NonNull Activity activity) {}

    public static float getWarningThresholdRatio() {
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW) {
            return 85;
        } else if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_AVERAGE) {
            return 90;
        } else {
            return 95;
        }
    }

    public static double getCleanupThresholdRatio() {
        if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_LOW) {
            return 0.65;
        } else if (SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_AVERAGE) {
            return 0.70;
        } else {
            return 0.75;
        }
    }

}