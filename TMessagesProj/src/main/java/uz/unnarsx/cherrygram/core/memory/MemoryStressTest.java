/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.core.memory;

import java.util.ArrayList;
import java.util.List;

public class MemoryStressTest {

    private static final List<byte[]> memoryLeakList = new ArrayList<>();

    public static void fillHeapToTriggerMonitor() {
        new Thread(() -> {
            try {
                while (true) {
                    byte[] chunk = new byte[20 * 1024 * 1024];
                    memoryLeakList.add(chunk);

                    Thread.sleep(500);
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
