package uz.unnarsx.cherrygram.preferences.drawer;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;

import org.telegram.messenger.FileLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class DrawerBitmapHelperJ {

    public static BitmapDrawable currentAccountBitmap;

    public static void setAccountBitmap(TLRPC.User user) {
        if (user.photo != null) {
            File photo = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(user.photo.photo_big, true);
            try (FileInputStream photoIn = new FileInputStream(photo); DataInputStream data = new DataInputStream (photoIn)) {
                byte[] photoData = new byte[(int)photo.length()];
                data.readFully(photoData);
                Bitmap bg = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                if (CherrygramConfig.INSTANCE.getDrawerBlur()) bg = blurDrawerWallpaper(bg);
                currentAccountBitmap = new BitmapDrawable(Resources.getSystem(), bg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap darkenBitmap(Bitmap bm) {
        Canvas canvas = new Canvas(bm);
        Paint p = new Paint(Color.RED);
        ColorFilter filter = new LightingColorFilter(-0x808081, 0x00000000);
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);
        return bm;
    }

    public static Bitmap blurDrawerWallpaper(Bitmap src) {
        if (src == null) {
            return null;
        }
        Bitmap b;
        if (src.getHeight() > src.getWidth()) {
            b = Bitmap.createBitmap(Math.round(640f * src.getWidth() / src.getHeight()), 640, Bitmap.Config.ARGB_8888);
        } else {
            b = Bitmap.createBitmap(640, Math.round(640f * src.getHeight() / src.getWidth()), Bitmap.Config.ARGB_8888);
        }
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        Rect rect = new Rect(0, 0, b.getWidth(), b.getHeight());
        new Canvas(b).drawBitmap(src, null, rect, paint);
        Utilities.stackBlurBitmap(b, CherrygramConfig.INSTANCE.getDrawerBlurIntensity());
        return b;
    }

}