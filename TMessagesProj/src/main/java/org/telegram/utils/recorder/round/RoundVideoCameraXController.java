/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package org.telegram.utils.recorder.round;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.TextureView;

import androidx.annotation.NonNull;

@SuppressLint({"UnsafeOptInUsageError", "RestrictedApi"})
final class RoundVideoCameraXController extends RoundVideoCameraSource {

    RoundVideoCameraXController(
            @NonNull Context context,
            @NonNull TextureView previewView,
            @NonNull RoundVideoSession.OutputResolution outputResolution,
            int videoBitrate,
            @NonNull RoundVideoSession.CameraResolution cameraResolution,
            @NonNull RoundVideoSession.FrameRate frameRate,
            boolean compositionEnabled,
            @NonNull RoundVideoDiagnostics diagnostics,
            @NonNull RoundVideoCameraController.Callback callback
    ) {

    }

    @Override
    void startSegment(
            @NonNull RoundVideoMp4Writer writer,
            long timelineOffsetUs,
            @NonNull RoundVideoSession.CameraFacing facing
    ) {

    }

    @Override
    boolean setCameraFacing(@NonNull RoundVideoSession.CameraFacing facing) {
        return false;
    }

    @Override
    boolean setZoom(float progress) {
        return false;
    }

    @Override
    boolean setTorchEnabled(boolean enabled) {
        return false;
    }

    @Override
    boolean stopSegment() {
        return false;
    }

    @Override
    void release() {

    }

}