/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package org.telegram.utils.recorder.round;

import androidx.annotation.NonNull;

abstract class RoundVideoCameraSource {

    abstract void startSegment(
            @NonNull RoundVideoMp4Writer writer,
            long timelineOffsetUs,
            @NonNull RoundVideoSession.CameraFacing facing
    );

    abstract boolean setCameraFacing(@NonNull RoundVideoSession.CameraFacing facing);

    abstract boolean setZoom(float progress);

    abstract boolean setTorchEnabled(boolean enabled);

    abstract boolean stopSegment();

    abstract void release();

}
