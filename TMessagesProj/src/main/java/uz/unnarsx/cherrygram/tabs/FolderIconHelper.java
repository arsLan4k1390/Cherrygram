package uz.unnarsx.cherrygram.tabs;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class FolderIconHelper {
    public static int[] icons = {
            R.drawable.filter_cat,
            R.drawable.filter_crown,
            R.drawable.filter_favorite,
            R.drawable.filter_flower,
            R.drawable.filter_game,
            R.drawable.filter_home,
            R.drawable.filter_love,
            R.drawable.filter_mask,
            R.drawable.filter_party,
            R.drawable.filter_sport,
            R.drawable.filter_study,
            R.drawable.filter_trade,
            R.drawable.filter_travel,
            R.drawable.filter_work,
            R.drawable.filter_all,
            R.drawable.filter_unread,
            R.drawable.filter_unmuted,
            R.drawable.filter_bot,
            R.drawable.filter_channel,
            R.drawable.filter_groups,
            R.drawable.filter_private,
            R.drawable.filter_custom,
            R.drawable.filter_setup,
    };

    public static String[] emojis = {
            "\uD83D\uDC31",
            "\uD83D\uDC51",
            "\u2B50",
            "\uD83C\uDF39",
            "\uD83C\uDFAE",
            "\uD83C\uDFE0",
            "\u2764",
            "\uD83C\uDFAD",
            "\uD83C\uDF78",
            "\u26BD",
            "\uD83C\uDF93",
            "\uD83D\uDCC8",
            "\u2708",
            "\uD83D\uDCBC",
            "\uD83D\uDCAC",
            "\u2705",
            "\uD83D\uDD14",
            "\uD83E\uDD16",
            "\uD83D\uDCE2",
            "\uD83D\uDC65",
            "\uD83D\uDC64",
            "\uD83D\uDCC1",
            "\uD83D\uDCCB",
    };

    public static int getIconWidth() {
        return AndroidUtilities.dp(28);
    }

    public static int getPadding() {
        if (CherrygramConfig.INSTANCE.getTabMode() == 1) {
            return AndroidUtilities.dp(6);
        }
        return 0;
    }

    public static int getTotalIconWidth() {
        int result = 0;
        if (CherrygramConfig.INSTANCE.getTabMode() != 0) {
            result = getIconWidth() + getPadding();
        }
        return result;
    }

    public static int getPaddingTab() {
        if (CherrygramConfig.INSTANCE.getTabMode() != 2) {
            return AndroidUtilities.dp(32);
        }
        return AndroidUtilities.dp(16);
    }

    public static int getTabIcon(String emoji, boolean active) {
        if (emoji != null) {
            switch (emoji) {
                case "\uD83D\uDCAC":
                    if (active) {
                        return R.drawable.filter_all_active;
                    } else {
                        return R.drawable.filter_all;
                    }
                case "\uD83D\uDC31":
                    if (active) {
                        return R.drawable.filter_cat_active;
                    } else {
                        return R.drawable.filter_cat;
                    }
                case "\uD83D\uDC51":
                    if (active) {
                        return R.drawable.filter_crown_active;
                    } else {
                        return R.drawable.filter_crown;
                    }
                case "\u2B50":
                    if (active) {
                        return R.drawable.filter_favorite_active;
                    } else {
                        return R.drawable.filter_favorite;
                    }
                case "\uD83C\uDF39":
                    if (active) {
                        return R.drawable.filter_flower_active;
                    } else {
                        return R.drawable.filter_flower;
                    }
                case "\uD83C\uDFAE":
                    if (active) {
                        return R.drawable.filter_game_active;
                    } else {
                        return R.drawable.filter_game;
                    }
                case "\uD83C\uDFE0":
                    if (active) {
                        return R.drawable.filter_home_active;
                    } else {
                        return R.drawable.filter_home;
                    }
                case "\u2764":
                    if (active) {
                        return R.drawable.filter_love_active;
                    } else {
                        return R.drawable.filter_love;
                    }
                case "\uD83C\uDF93":
                    if (active) {
                        return R.drawable.filter_study_active;
                    } else {
                        return R.drawable.filter_study;
                    }
                case "\uD83C\uDFAD":
                    if (active) {
                        return R.drawable.filter_mask_active;
                    } else {
                        return R.drawable.filter_mask;
                    }
                case "\uD83C\uDF78":
                    if (active) {
                        return R.drawable.filter_party_active;
                    } else {
                        return R.drawable.filter_party;
                    }
                case "\u26BD":
                    if (active) {
                        return R.drawable.filter_sport_active;
                    } else {
                        return R.drawable.filter_sport;
                    }
                case "\uD83D\uDCC8":
                    return R.drawable.filter_trade;
                case "\u2708":
                    if (active) {
                        return R.drawable.filter_travel_active;
                    } else {
                        return R.drawable.filter_travel;
                    }
                case "\uD83D\uDCBC":
                    if (active) {
                        return R.drawable.filter_work_active;
                    } else {
                        return R.drawable.filter_work;
                    }
                case "\u2705":
                    if (active) {
                        return R.drawable.filter_unread_active;
                    } else {
                        return R.drawable.filter_unread;
                    }
                case "\uD83D\uDD14":
                    if (active) {
                        return R.drawable.filter_unmuted_active;
                    } else {
                        return R.drawable.filter_unmuted;
                    }
                case "\uD83E\uDD16":
                    if (active) {
                        return R.drawable.filter_bot_active;
                    } else {
                        return R.drawable.filter_bot;
                    }
                case "\uD83D\uDCE2":
                    if (active) {
                        return R.drawable.filter_channel_active;
                    } else {
                        return R.drawable.filter_channel;
                    }
                case "\uD83D\uDC65":
                    if (active) {
                        return R.drawable.filter_groups_active;
                    } else {
                        return R.drawable.filter_groups;
                    }
                case "\uD83D\uDC64":
                    if (active) {
                        return R.drawable.filter_private_active;
                    } else {
                        return R.drawable.filter_private;
                    }
                case "\uD83D\uDCCB":
                    return R.drawable.filter_setup;
            }
        }
        if (active) {
            return R.drawable.filter_custom_active;
        } else {
            return R.drawable.filter_custom;
        }
    }
}
