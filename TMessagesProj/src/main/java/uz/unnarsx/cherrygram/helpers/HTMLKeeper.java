package uz.unnarsx.cherrygram.helpers;

import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;

import androidx.core.util.Pair;

import org.apache.commons.text.StringEscapeUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaDataController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.TextStyleSpan;

import java.util.ArrayList;
import java.util.Arrays;

public class HTMLKeeper {
    final private static String[] list_html_params = new String[]{"b", "i", "u", "s", "tt", "a", "q"};

    public static String entitiesToHtml(String text, ArrayList<TLRPC.MessageEntity> entities, boolean includeLink) {
        text = text.replace("\n", "\u2029");
        if (!includeLink) {
            text = text.replace("<", "\u2027");
        }
        SpannableStringBuilder messSpan = SpannableStringBuilder.valueOf(text);
        MediaDataController.addTextStyleRuns(entities, text, messSpan);
        CharacterStyle[] mSpans = messSpan.getSpans(0, messSpan.length(), CharacterStyle.class);
        for (CharacterStyle mSpan : mSpans) {
            int start = messSpan.getSpanStart(mSpan);
            int end = messSpan.getSpanEnd(mSpan);
            boolean isBold = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_BOLD) > 0;
            boolean isItalic = (((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_ITALIC) > 0;
            if (EntitiesHelper.isEmoji(text.substring(0, 1)) && mSpans[0] == mSpan && start > 0) {
                start = 0;
            }
            if (isBold && !isItalic || isBold && !includeLink) {
                messSpan.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (!isBold && isItalic || isItalic && !includeLink) {
                messSpan.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (isBold && isItalic && includeLink) {
                messSpan.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MONO) > 0) {
                messSpan.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_UNDERLINE) > 0) {
                messSpan.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_STRIKE) > 0) {
                messSpan.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_SPOILER) > 0) {
                messSpan.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_chat_messagePanelText)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_URL) > 0) {
                String url = ((TextStyleSpan) mSpan).getTextStyleRun().urlEntity.url;
                if (url != null || !includeLink) {
                    messSpan.setSpan(new URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            if ((((TextStyleSpan) mSpan).getStyleFlags() & TextStyleSpan.FLAG_STYLE_MENTION) > 0) {
                long id = ((TLRPC.TL_messageEntityMentionName) ((TextStyleSpan) mSpan).getTextStyleRun().urlEntity).user_id;
                messSpan.setSpan(new URLSpan("tg://user?id=" + id), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        String html_result = Html.toHtml(messSpan);
        html_result = html_result.replace("<p dir=\"ltr\">", "");
        html_result = html_result.replace("<p dir=\"rtl\">", "");
        html_result = html_result.replace("</p>", "");
        html_result = html_result.replaceAll("<span style=\"text-decoration:line-through;\">(.*?)</span>", "<s>$1</s>");
        if (!includeLink) {
            html_result = html_result.replaceAll("<a href=\".*?\">", "<a>");
            html_result = html_result.replaceAll("<span style=\"color:.*?;\">(.*?)</span>", "<q>$1</q>");
            html_result = StringEscapeUtils.unescapeHtml4(html_result);
        } else {
            html_result = html_result.replace("&#8233;", "\u2029");
        }
        html_result = html_result.replace("\n", "");
        html_result = html_result.replace("\u2029", "\n");
        return html_result;
    }

    public static Pair<String, ArrayList<TLRPC.MessageEntity>> htmlToEntities(String text, ArrayList<TLRPC.MessageEntity> entities, boolean internalLinks) {
        ArrayList<TLRPC.MessageEntity> returnEntities = new ArrayList<>();
        ArrayList<TLRPC.MessageEntity> copyEntities = null;
        if (entities != null) {
            copyEntities = new ArrayList<>(entities);
        }
        text = fixDoubleSpace(text);
        text = fixDoubleHtmlElement(text);
        text = fixStrangeSpace(text);
        text = fixHtmlCorrupted(text);
        text = text.replaceAll("[\n\r]$", "");
        text = text.replace("\n", "<br/>");
        text = text.replaceAll("\n", "<br/>");
        text = text.replace("<a>", "<a href=\"https://telegram.org/\">");
        text = text.replaceAll("<q>(.*?)</q>", "<span style=\"color:#000000;\">$1</span>");
        text = text.replace("\u2027", "&lt;");
        text = text.replace("\u0327", "<");
        SpannableString htmlParsed = new SpannableString(AndroidUtilities.fromHtml(text));
        if (internalLinks) {
            AndroidUtilities.addLinks(htmlParsed, Linkify.ALL);
        }
        CharacterStyle[] mSpans = htmlParsed.getSpans(0, htmlParsed.length(), CharacterStyle.class);
        for (CharacterStyle mSpan : mSpans) {
            int start = htmlParsed.getSpanStart(mSpan);
            int end = htmlParsed.getSpanEnd(mSpan);
            TLRPC.MessageEntity entity = null;
            if (mSpan instanceof URLSpan) {
                URLSpan urlSpan = (URLSpan) mSpan;
                if (copyEntities != null) {
                    for (int i = 0; i < copyEntities.size(); i++) {
                        TLRPC.MessageEntity old_entity = copyEntities.get(i);
                        boolean found = false;
                        if (old_entity instanceof TLRPC.TL_messageEntityMentionName) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityMentionName();
                            ((TLRPC.TL_messageEntityMentionName) entity).user_id = ((TLRPC.TL_messageEntityMentionName) old_entity).user_id;
                        } else if (old_entity instanceof TLRPC.TL_inputMessageEntityMentionName) {
                            found = true;
                            entity = new TLRPC.TL_inputMessageEntityMentionName();
                            ((TLRPC.TL_inputMessageEntityMentionName) entity).user_id = ((TLRPC.TL_inputMessageEntityMentionName) old_entity).user_id;
                        } else if (old_entity instanceof TLRPC.TL_messageEntityTextUrl) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityTextUrl();
                            entity.url = old_entity.url;
                        } else if (old_entity instanceof TLRPC.TL_messageEntityUrl) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityUrl();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityMention) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityMention();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityBotCommand) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityBotCommand();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityHashtag) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityHashtag();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityCashtag) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityCashtag();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityEmail) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityEmail();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityBankCard) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityBankCard();
                        } else if (old_entity instanceof TLRPC.TL_messageEntityPhone) {
                            found = true;
                            entity = new TLRPC.TL_messageEntityPhone();
                        }
                        if (found) {
                            copyEntities.remove(i);
                            break;
                        }
                    }
                } else {
                    entity = new TLRPC.TL_messageEntityTextUrl();
                    entity.url = urlSpan.getURL();
                }
            } else if (mSpan instanceof StyleSpan) {
                StyleSpan styleSpan = (StyleSpan) mSpan;
                switch (styleSpan.getStyle()) {
                    case 1:
                        entity = new TLRPC.TL_messageEntityBold();
                        break;
                    case 2:
                        entity = new TLRPC.TL_messageEntityItalic();
                        break;
                }
            } else if (mSpan instanceof TypefaceSpan) {
                entity = new TLRPC.TL_messageEntityCode();
            } else if (mSpan instanceof UnderlineSpan) {
                entity = new TLRPC.TL_messageEntityUnderline();
            } else if (mSpan instanceof StrikethroughSpan) {
                entity = new TLRPC.TL_messageEntityStrike();
            } else if (mSpan instanceof ForegroundColorSpan) {
                entity = new TLRPC.TL_messageEntitySpoiler();
            }
            if (entity != null) {
                entity.offset = start;
                entity.length = end - start;
                returnEntities.add(entity);
            }
        }
        return Pair.create(htmlParsed.toString(), returnEntities);
    }

    // VARIOUS HTML FIXERS
    private static String fixStrangeSpace(String string) {
        for (String list_param : list_html_params) {
            String fixedStart = String.format("<%s>", list_param);
            String fixedEnd = String.format("</%s>", list_param);
            string = string.replace(String.format("< %s>", list_param), fixedStart);
            string = string.replace(String.format("<%s >", list_param), fixedStart);
            string = string.replace(String.format("< %s >", list_param), fixedStart);
            string = string.replace(String.format("</ %s>", list_param), fixedEnd);
            string = string.replace(String.format("< / %s>", list_param), fixedEnd);
            string = string.replace(String.format("< /%s>", list_param), fixedEnd);
            string = string.replace(String.format("< /%s >", list_param), fixedEnd);
            string = string.replace(String.format("</%s >", list_param), fixedEnd);
            string = string.replace(String.format("< / %s >", list_param), fixedEnd);
        }
        return string;
    }

    private static String fixDoubleSpace(String string) {
        for (String list_param : list_html_params) {
            string = string.replace(" <" + list_param + "> ", " <" + list_param + ">");
            string = string.replace(" </" + list_param + "> ", "</" + list_param + "> ");
        }
        string = string.replace("<a> ", "<a>");
        string = string.replace(" </a>", "</a> ");
        return string;
    }

    private static String fixDoubleHtmlElement(String string) {
        for (String list_param : list_html_params) {
            for (String list_param2 : list_html_params) {
                string = string.replace("<" + list_param + "-" + list_param2 + ">", "<" + list_param + "><" + list_param2 + ">");
                string = string.replace("</" + list_param + "-" + list_param2 + ">", "</" + list_param2 + "></" + list_param + ">");
            }
        }
        return string;
    }

    private static class HTMLTagPosition {
        private final int start;
        private final int end;
        private final String tag;

        public HTMLTagPosition(int start, int end, String tag) {
            this.start = start;
            this.end = end;
            this.tag = tag;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getTag() {
            return tag;
        }
    }

    private static class HTMLTagStack {
        private final ArrayList<HTMLTagPosition> stack = new ArrayList<>();
        private String text;

        public void parse(String string) {
            stack.clear();
            int start;
            int end = 0;
            while (true) {
                start = string.indexOf("<", end);
                if (start == -1) {
                    break;
                }
                end = string.indexOf(">", start);
                if (end == -1) {
                    break;
                }
                String tag = string.substring(start + 1, end);
                stack.add(new HTMLTagPosition(start, end + 1, tag));
            }
            text = string;
        }

        public void replace(int start, int end, String string) {
            text = text.substring(0, start) + string + text.substring(end);
            parse(text);
        }
    }

    private static String fixHtmlCorrupted(String string) {
        ArrayList<String> listUnclosedTags = new ArrayList<>();
        ArrayList<String> listUnopenedTags = new ArrayList<>();
        HTMLTagStack stack = new HTMLTagStack();
        stack.parse(string);
        for (int i = 0; i < stack.stack.size(); i++) {
            HTMLTagPosition tagPosition = stack.stack.get(i);
            String tag = tagPosition.getTag();
            tag = tag.replace("<", "").replace(">", "").replace(" ", "");
            if (!tag.contains("/")) {
                listUnclosedTags.add(0, tag);
                listUnopenedTags.add(0, tag);
            } else {
                tag = tag.replace("/", "");
                if (listUnclosedTags.contains(tag)) {
                    listUnclosedTags.remove(0);
                    listUnopenedTags.remove(0);
                } else if (listUnclosedTags.size() > 0) {
                    boolean isValidData = new ArrayList<>(Arrays.asList(list_html_params)).contains(tag);
                    String tagToReplace;
                    if (listUnclosedTags.size() > 0) {
                        tagToReplace = "/" + listUnclosedTags.get(0);
                        listUnclosedTags.remove(0);
                    } else if (listUnopenedTags.size() > 0 && isValidData) {
                        tagToReplace = listUnopenedTags.get(0);
                        listUnopenedTags.remove(0);
                    } else {
                        continue;
                    }
                    stack.replace(tagPosition.getStart(), tagPosition.getEnd(), "<" + tagToReplace + ">");
                }
            }
        }
        return stack.text;
    }
}
