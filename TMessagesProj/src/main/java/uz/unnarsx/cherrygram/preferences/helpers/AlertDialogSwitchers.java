/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2025.
 */

package uz.unnarsx.cherrygram.preferences.helpers;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.LayoutHelper;

import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.preferences.cells.StickerSliderCell;
import uz.unnarsx.cherrygram.preferences.tgkit.preference.types.TGKitSliderPreference;

public class AlertDialogSwitchers {

    public static void showMessageSize(BaseFragment fragment) {
        if (fragment.getParentActivity() == null) {
            return;
        }
        Context context = fragment.getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.CP_Messages_Size));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = 3;
        for (int a = 0; a < count; a++) {
            HeaderCell headerCell = new HeaderCell(fragment.getContext(), fragment.getResourceProvider());
            TextInfoPrivacyCell textInfoPrivacyCell = new TextInfoPrivacyCell(fragment.getContext());
            StickerSliderCell stickerSliderCell = new StickerSliderCell(fragment.getContext());
            TGKitSliderPreference.TGSLContract contract;
            switch (a) {
                case 0: {
                    headerCell.setText(getString(R.string.CP_Slider_MediaAmplifier), false);
                    contract = new TGKitSliderPreference.TGSLContract() {
                        @Override
                        public void setValue(int value) {
                            CherrygramChatsConfig.INSTANCE.setSlider_mediaAmplifier(value);
                        }

                        @Override
                        public int getPreferenceValue() {
                            return CherrygramChatsConfig.INSTANCE.getSlider_mediaAmplifier();
                        }

                        @Override
                        public int getMin() {
                            return 50;
                        }

                        @Override
                        public int getMax() {
                            return 100;
                        }
                    };
                    contract.setValue(CherrygramChatsConfig.INSTANCE.getSlider_mediaAmplifier());
                    textInfoPrivacyCell.setText(getString(R.string.CP_Slider_MediaAmplifier_Hint));
                    stickerSliderCell.setContract(contract);

                    textInfoPrivacyCell.setTag(a);
                    textInfoPrivacyCell.setPadding(0, AndroidUtilities.dp(25), 0, 0);
                    stickerSliderCell.addView(textInfoPrivacyCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                    break;
                }
                case 1: {
                    headerCell.setText(getString(R.string.AccDescrStickers), false);
                    contract = new TGKitSliderPreference.TGSLContract() {
                        @Override
                        public void setValue(int value) {
                            CherrygramChatsConfig.INSTANCE.setSlider_stickerAmplifier(value);
                        }

                        @Override
                        public int getPreferenceValue() {
                            return CherrygramChatsConfig.INSTANCE.getSlider_stickerAmplifier();
                        }

                        @Override
                        public int getMin() {
                            return 50;
                        }

                        @Override
                        public int getMax() {
                            return 100;
                        }
                    };
                    contract.setValue(CherrygramChatsConfig.INSTANCE.getSlider_stickerAmplifier());
                    stickerSliderCell.setContract(contract);
                    break;
                }
                case 2: {
                    headerCell.setText(getString(R.string.AccDescrGIFs), false);
                    contract = new TGKitSliderPreference.TGSLContract() {
                        @Override
                        public void setValue(int value) {
                            CherrygramChatsConfig.INSTANCE.setSlider_gifsAmplifier(value);
                        }

                        @Override
                        public int getPreferenceValue() {
                            return CherrygramChatsConfig.INSTANCE.getSlider_gifsAmplifier();
                        }

                        @Override
                        public int getMin() {
                            return 50;
                        }

                        @Override
                        public int getMax() {
                            return 100;
                        }
                    };
                    contract.setValue(CherrygramChatsConfig.INSTANCE.getSlider_gifsAmplifier());
                    stickerSliderCell.setContract(contract);
                    break;
                }
            }
            headerCell.setTag(a);
            headerCell.setTextSize(16);
            linearLayoutInviteContainer.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            stickerSliderCell.setTag(a);
            stickerSliderCell.setBackground(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(stickerSliderCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        builder.setPositiveButton(getString(R.string.OK), null);
        builder.setView(linearLayout);
        fragment.showDialog(builder.create());
    }

}
