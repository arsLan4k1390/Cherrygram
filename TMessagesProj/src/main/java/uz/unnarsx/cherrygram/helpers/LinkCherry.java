package uz.unnarsx.cherrygram.helpers;

import androidx.annotation.NonNull;
import java.util.regex.Pattern;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.Components.Bulletin;

public class LinkCherry {

  public static final Pattern REGEX_CHERRY_WEB_PATH = Pattern.compile(
    "^(?:cherry|cherrygram)[^/]*?/?$"
  );

  @SuppressWarnings("unused")
  public static boolean isсherryPath(@NonNull String path) {
    return path.matches(REGEX_CHERRY_WEB_PATH.pattern());
  }

  public static boolean isсherryTgLink(@NonNull String url) {
    return (
      url.startsWith("tg:cherry") ||
      url.startsWith("tg://cherry") ||
      url.startsWith("tg:cherrygram") ||
      url.startsWith("tg://cherrygram")
    );
  }

  public static void сherry() {
    NotificationCenter
      .getGlobalInstance()
      .postNotificationName(
        NotificationCenter.showBulletin,
        Bulletin.TYPE_ERROR,
        LocaleController.getString(R.string.CherryLink)
      );
  }
}
