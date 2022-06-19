package uz.unnarsx.cherrygram.tabs;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

import uz.unnarsx.cherrygram.CherrygramConfig;

public class FolderIconHelper {
    public static int[] icons = {
            R.drawable.filter_cat,
            R.drawable.filter_book,
            R.drawable.filter_money,
            R.drawable.filter_game,
            R.drawable.filter_light,
            R.drawable.filter_like,
            R.drawable.filter_note,
            R.drawable.filter_palette,
            R.drawable.filter_travel,
            R.drawable.filter_sport,
            R.drawable.filter_favorite,
            R.drawable.filter_study,
            R.drawable.filter_airplane,
            R.drawable.filter_private,
            R.drawable.filter_groups,
            R.drawable.filter_all,
            R.drawable.filter_unread,
            R.drawable.filter_bot,
            R.drawable.filter_crown,
            R.drawable.filter_flower,
            R.drawable.filter_home,
            R.drawable.filter_love,
            R.drawable.filter_mask,
            R.drawable.filter_party,
            R.drawable.filter_trade,
            R.drawable.filter_work,
            R.drawable.filter_unmuted,
            R.drawable.filter_channel,
            R.drawable.filter_custom,
            R.drawable.filter_setup,
    };

    public static int[] icons_filled = {
            R.drawable.filter_cat_active,
            R.drawable.filter_book_active,
            R.drawable.filter_money_active,
            R.drawable.filter_game_active,
            R.drawable.filter_light_active,
            R.drawable.filter_like_active,
            R.drawable.filter_note_active,
            R.drawable.filter_palette_active,
            R.drawable.filter_travel_active,
            R.drawable.filter_sport_active,
            R.drawable.filter_favorite_active,
            R.drawable.filter_study_active,
            R.drawable.filter_airplane_active,
            R.drawable.filter_private_active,
            R.drawable.filter_groups_active,
            R.drawable.filter_all_active,
            R.drawable.filter_unread_active,
            R.drawable.filter_bot_active,
            R.drawable.filter_crown_active,
            R.drawable.filter_flower_active,
            R.drawable.filter_home_active,
            R.drawable.filter_love_active,
            R.drawable.filter_mask_active,
            R.drawable.filter_party_active,
            R.drawable.filter_trade,
            R.drawable.filter_work_active,
            R.drawable.filter_unmuted_active,
            R.drawable.filter_channel_active,
            R.drawable.filter_custom_active,
            R.drawable.filter_setup,
    };

    public static String[] emojis = {
            "\uD83D\uDC31",
            "\uD83D\uDCD5",
            "\uD83D\uDCB0",
            "\uD83C\uDFAE",
            "\uD83D\uDCA1",
            "\uD83D\uDC4C",
            "\uD83C\uDFB5",
            "\uD83C\uDFA8",
            "\u2708",
            "\u26BD",
            "\u2B50",
            "\uD83C\uDF93",
            "\uD83D\uDEEB",
            "\uD83D\uDC64",
            "\uD83D\uDC65",
            "\uD83D\uDCAC",
            "\u2705",
            "\uD83E\uDD16",
            "\uD83D\uDC51",
            "\uD83C\uDF39",
            "\uD83C\uDFE0",
            "\u2764",
            "\uD83C\uDFAD",
            "\uD83C\uDF78",
            "\uD83D\uDCC8",
            "\uD83D\uDCBC",
            "\uD83D\uDD14",
            "\uD83D\uDCE2",
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
                case "\uD83D\uDC31":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_cat_active;
                    } else {
                        return R.drawable.filter_cat;
                    }
                case "\uD83D\uDCD5":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_book_active;
                    } else {
                        return R.drawable.filter_book;
                    }
                case "\uD83D\uDCB0":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_money_active;
                    } else {
                        return R.drawable.filter_money;
                    }
                case "\uD83C\uDFAE":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_game_active;
                    } else {
                        return R.drawable.filter_game;
                    }
                case "\uD83D\uDCA1":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_light_active;
                    } else {
                        return R.drawable.filter_light;
                    }
                case "\uD83D\uDC4C":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_like_active;
                    } else {
                        return R.drawable.filter_like;
                    }
                case "\uD83C\uDFB5":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_note_active;
                    } else {
                        return R.drawable.filter_note;
                    }
                case "\uD83C\uDFA8":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_palette_active;
                    } else {
                        return R.drawable.filter_palette;
                    }
                case "\u2708":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_travel_active;
                    } else {
                        return R.drawable.filter_travel;
                    }
                case "\u26BD":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_sport_active;
                    } else {
                        return R.drawable.filter_sport;
                    }
                case "\u2B50":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_favorite_active;
                    } else {
                        return R.drawable.filter_favorite;
                    }
                case "\uD83C\uDF93":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_study_active;
                    } else {
                        return R.drawable.filter_study;
                    }
                case "\uD83D\uDEEB":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_airplane_active;
                    } else {
                        return R.drawable.filter_airplane;
                    }
                case "\uD83D\uDC64":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_private_active;
                    } else {
                        return R.drawable.filter_private;
                    }
                case "\uD83D\uDC65":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_groups_active;
                    } else {
                        return R.drawable.filter_groups;
                    }
                case "\uD83D\uDCAC":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_all_active;
                    } else {
                        return R.drawable.filter_all;
                    }
                case "\u2705":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_unread_active;
                    } else {
                        return R.drawable.filter_unread;
                    }
                case "\uD83E\uDD16":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_bot_active;
                    } else {
                        return R.drawable.filter_bot;
                    }
                case "\uD83D\uDC51":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_crown_active;
                    } else {
                        return R.drawable.filter_crown;
                    }
                case "\uD83C\uDF39":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_flower_active;
                    } else {
                        return R.drawable.filter_flower;
                    }
                case "\uD83C\uDFE0":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_home_active;
                    } else {
                        return R.drawable.filter_home;
                    }
                case "\u2764":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_love_active;
                    } else {
                        return R.drawable.filter_love;
                    }
                case "\uD83C\uDFAD":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_mask_active;
                    } else {
                        return R.drawable.filter_mask;
                    }
                case "\uD83C\uDF78":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_party_active;
                    } else {
                        return R.drawable.filter_party;
                    }
                case "\uD83D\uDCC8":
                    return R.drawable.filter_trade;
                case "\uD83D\uDCBC":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_work_active;
                    } else {
                        return R.drawable.filter_work;
                    }
                case "\uD83D\uDD14":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_unmuted_active;
                    } else {
                        return R.drawable.filter_unmuted;
                    }
                case "\uD83D\uDCE2":
                    if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
                        return R.drawable.filter_channel_active;
                    } else {
                        return R.drawable.filter_channel;
                    }
                case "\uD83D\uDCCB":
                    return R.drawable.filter_setup;
            }
        }
        if (active || CherrygramConfig.INSTANCE.getFilledIcons()) {
            return R.drawable.filter_custom_active;
        } else {
            return R.drawable.filter_custom;
        }
    }
}
