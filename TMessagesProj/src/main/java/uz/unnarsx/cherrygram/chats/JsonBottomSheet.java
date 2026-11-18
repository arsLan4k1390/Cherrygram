/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.chats;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_iv;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.LoadingDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;
import org.telegram.utils.code.highlight.CodeHighlighting;
import org.telegram.utils.code.highlight.PrismaTokenStyles;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uz.unnarsx.cherrygram.chats.helpers.ChatsHelper2;
import uz.unnarsx.cherrygram.core.CherrygramLogger;
import uz.unnarsx.cherrygram.core.configs.CherrygramChatsConfig;
import uz.unnarsx.cherrygram.core.configs.CherrygramMessagesConfig;
import uz.unnarsx.cherrygram.core.helpers.CGResourcesHelper;

public class JsonBottomSheet extends BottomSheet {

    private static final String LOADING_PLACEHOLDER =
            """
                    {
                        "_": "TL_message",
                        "id": 0000000,
                        "date": 0000000000,
                        "from_id": {
                            "user_id": 0000000000
                        },
                        "message": "..............",
                        "flags": 000000
                    }""";

    private final HeaderView headerView;
    private final LoadingTextView loadingTextView;
    private final FrameLayout textViewContainer;
    private final TextView textView;

    private boolean sheetTopNotAnimate;
    private final RecyclerListView listView;
    private final PaddedAdapter adapter;

    private final View buttonShadowView;

    public Theme.ResourcesProvider resourcesProvider;
    public BaseFragment fragment;

    private int colorizeRequestId;
    private int displayRequestId;

    private LoadedJson json;
    private boolean[] collapsed;
    private DisplayState displayState;

    private String searchQuery = "";
    private final ArrayList<int[]> searchMatches = new ArrayList<>();
    private int searchIndex = -1;

    private static final Pattern DATE_FIELD_PATTERN = Pattern.compile("\"[a-z_]*(?:date|expires|until|time)[a-z_]*\"\\s*:\\s*(\\d{9,10})(?!\\d)");

    private record DateHint(int position, String text) {}

    private static final class FoldNode {
        final int open;
        final boolean isArray;
        int close;
        int commas;
        boolean hasContent;

        FoldNode(int open, boolean isArray) {
            this.open = open;
            this.isArray = isArray;
        }

        int childCount() {
            return hasContent ? commas + 1 : 0;
        }
    }

    private static final class LoadedJson {
        final String text;
        final int[] styledRanges;
        final ArrayList<FoldNode> nodes;
        final ArrayList<DateHint> dateHints;

        LoadedJson(String text, int[] styledRanges, ArrayList<FoldNode> nodes, ArrayList<DateHint> dateHints) {
            this.text = text;
            this.styledRanges = styledRanges;
            this.nodes = nodes;
            this.dateHints = dateHints;
        }
    }

    private static final class DisplayState {
        final SpannableStringBuilder text;
        final boolean[] collapsed;
        final int[] nodeOpen;
        final int[] nodeClose;
        final int currentMatchStart;

        DisplayState(SpannableStringBuilder text, boolean[] collapsed, int[] nodeOpen, int[] nodeClose, int currentMatchStart) {
            this.text = text;
            this.collapsed = collapsed;
            this.nodeOpen = nodeOpen;
            this.nodeClose = nodeClose;
            this.currentMatchStart = currentMatchStart;
        }
    }

    private static final class BulkSpanned implements Spanned {
        private final CharSequence text;
        private Object[] spans = new Object[256];
        private int[] starts = new int[256];
        private int[] ends = new int[256];
        private int count;
        private final IdentityHashMap<Object, Integer> indexes = new IdentityHashMap<>();

        BulkSpanned(CharSequence text) {
            this.text = text;
        }

        void add(Object span, int start, int end) {
            if (start >= end) {
                return;
            }
            if (count == spans.length) {
                spans = Arrays.copyOf(spans, count * 2);
                starts = Arrays.copyOf(starts, count * 2);
                ends = Arrays.copyOf(ends, count * 2);
            }
            spans[count] = span;
            starts[count] = start;
            ends[count] = end;
            indexes.put(span, count++);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            long[] order = new long[count];
            int found = 0;
            for (int i = 0; i < count; i++) {
                if (type.isInstance(spans[i]) && starts[i] < end && ends[i] > start) {
                    order[found++] = (long) starts[i] << 32 | i;
                }
            }
            Arrays.sort(order, 0, found);
            T[] result = (T[]) Array.newInstance(type, found);
            for (int i = 0; i < found; i++) {
                result[i] = type.cast(spans[(int) order[i]]);
            }
            return result;
        }

        @Override
        public int getSpanStart(Object span) {
            Integer index = indexes.get(span);
            return index == null ? -1 : starts[index];
        }

        @Override
        public int getSpanEnd(Object span) {
            Integer index = indexes.get(span);
            return index == null ? -1 : ends[index];
        }

        @Override
        public int getSpanFlags(Object span) {
            return indexes.containsKey(span) ? Spanned.SPAN_EXCLUSIVE_EXCLUSIVE : 0;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int nextSpanTransition(int start, int limit, Class type) {
            for (int i = 0; i < count; i++) {
                if (type == null || type.isInstance(spans[i])) {
                    if (starts[i] > start && starts[i] < limit) {
                        limit = starts[i];
                    }
                    if (ends[i] > start && ends[i] < limit) {
                        limit = ends[i];
                    }
                }
            }
            return limit;
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public char charAt(int index) {
            return text.charAt(index);
        }

        @NonNull
        @Override
        public CharSequence subSequence(int start, int end) {
            return text.subSequence(start, end);
        }

        @NonNull
        @Override
        public String toString() {
            return text.toString();
        }
    }

    public static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(TLObject.class, new TLObjectTypeAdapter())
            .create();
    public static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    private JsonBottomSheet(Context context, Theme.ResourcesProvider resourcesProvider, MessageObject messageObject, TLRPC.Chat currentChat) {
        super(context, true, resourcesProvider);
        this.resourcesProvider = resourcesProvider;

        backgroundPaddingLeft = 0;

        fixNavigationBar();

        MessagesController mc = MessagesController.getInstance(UserConfig.selectedAccount);
        boolean isNoForwards = mc.isPeerNoForwards(Math.abs(messageObject.getDialogId())) || currentChat != null && currentChat.noforwards || messageObject.messageOwner.noforwards;

        containerView = new ContainerView(context);
        sheetTopAnimated = new AnimatedFloat(containerView, 320, CubicBezierInterpolator.EASE_OUT_QUINT);

        loadingTextView = new LoadingTextView(context);
        loadingTextView.setPadding(dp(40), dp(12), dp(22), dp(6));
        loadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        loadingTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        loadingTextView.setText(LOADING_PLACEHOLDER);

        textViewContainer = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        };

        textView = new TextView(context) {
            private final int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            private final Path arrowPath = new Path();
            private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Rect clipRect = new Rect();
            private float downX;
            private float downY;
            private boolean hadSelectionOnDown;

            {
                arrowPath.moveTo(-dp(4), -dp(2));
                arrowPath.lineTo(0, dp(2));
                arrowPath.lineTo(dp(4), -dp(2));
                arrowPaint.setStyle(Paint.Style.STROKE);
                arrowPaint.setStrokeWidth(dp(1.5f));
                arrowPaint.setStrokeCap(Paint.Cap.ROUND);
                arrowPaint.setStrokeJoin(Paint.Join.ROUND);
                arrowPaint.setColor(Theme.multAlpha(getThemedColor(Theme.key_dialogTextBlack), .45f));
                guidePaint.setStyle(Paint.Style.STROKE);
                guidePaint.setStrokeWidth(dp(1));
                guidePaint.setColor(Theme.multAlpha(getThemedColor(Theme.key_dialogTextBlack), .15f));
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Layout layout = getLayout();
                if (layout == null || displayState == null || !canvas.getClipBounds(clipRect)) {
                    return;
                }
                int firstVisibleLine = layout.getLineForVertical(clipRect.top - getTotalPaddingTop());
                int lastVisibleLine = layout.getLineForVertical(clipRect.bottom - getTotalPaddingTop());
                int lastArrowLine = -1;
                for (int i = 0; i < displayState.nodeOpen.length; i++) {
                    if (displayState.nodeOpen[i] < 0) {
                        continue;
                    }
                    int line = layout.getLineForOffset(displayState.nodeOpen[i]);
                    if (line > lastVisibleLine) {
                        break;
                    }
                    if (!displayState.collapsed[i]) {
                        int closeLine = layout.getLineForOffset(displayState.nodeClose[i]);
                        if (closeLine - line >= 2 && closeLine >= firstVisibleLine) {
                            float x = getTotalPaddingLeft() + (layout.getPrimaryHorizontal(displayState.nodeClose[i]) + layout.getPrimaryHorizontal(displayState.nodeClose[i] + 1)) / 2f;
                            canvas.drawLine(x, getTotalPaddingTop() + layout.getLineBottom(line) + dp(2), x, getTotalPaddingTop() + layout.getLineTop(closeLine) - dp(2), guidePaint);
                        }
                    }
                    if (line < firstVisibleLine || line == lastArrowLine) {
                        continue;
                    }
                    lastArrowLine = line;
                    canvas.save();
                    canvas.translate(getPaddingLeft() - dp(14), getTotalPaddingTop() + (layout.getLineTop(line) + layout.getLineBottom(line)) / 2f);
                    if (displayState.collapsed[i]) {
                        canvas.rotate(-90);
                    }
                    canvas.drawPath(arrowPath, arrowPaint);
                    canvas.restore();
                }
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = event.getX();
                    downY = event.getY();
                    hadSelectionOnDown = hasSelection();
                }
                boolean handled = super.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP && !hadSelectionOnDown && displayState != null && getLayout() != null
                        && Math.abs(event.getX() - downX) < touchSlop && Math.abs(event.getY() - downY) < touchSlop
                        && event.getEventTime() - event.getDownTime() < ViewConfiguration.getLongPressTimeout()) {
                    Layout layout = getLayout();
                    int line = layout.getLineForVertical((int) (event.getY() - getTotalPaddingTop()));
                    int offset = layout.getOffsetForHorizontal(line, event.getX() - getTotalPaddingLeft());
                    boolean inGutter = event.getX() < getPaddingLeft();
                    int target = -1;
                    for (int i = 0; i < displayState.nodeOpen.length; i++) {
                        int open = displayState.nodeOpen[i];
                        if (open < 0 || layout.getLineForOffset(open) != line) {
                            continue;
                        }
                        if (target < 0 || !inGutter && Math.abs(open - offset) < Math.abs(displayState.nodeOpen[target] - offset)) {
                            target = i;
                        }
                    }
                    if (target >= 0) {
                        collapsed[target] = !collapsed[target];
                        updateDisplay(false);
                        performClick();
                        return true;
                    }
                }
                return handled || displayState != null && displayState.nodeOpen.length > 0;
            }

            @Override
            public boolean performClick() {
                return super.performClick();
            }
        };
        textView.setPadding(dp(40), dp(12), dp(22), dp(6));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        textView.setTextIsSelectable(!isNoForwards);
        textView.setSpannableFactory(new Spannable.Factory() {
            @NonNull
            @Override
            public Spannable newSpannable(CharSequence source) {
                return source instanceof Spannable ? (Spannable) source : super.newSpannable(source);
            }
        });

        textViewContainer.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(context, resourcesProvider) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getY() < getSheetTop() - getTop()) {
                    dismiss();
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            @Override
            protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                return true;
            }

            @Override
            public void requestChildFocus(View child, View focused) {}

            @Override
            public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
                return false;
            }
        };
        listView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        listView.setPadding(0, AndroidUtilities.statusBarHeight + dp(56), 0, dp(80));
        listView.setClipToPadding(true);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(adapter = new PaddedAdapter(context, loadingTextView));
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                containerView.invalidate();
                updateButtonShadow(listView.canScrollVertically(1));
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    sheetTopNotAnimate = false;
                }
                if ((newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) && getSheetTop(false) > 0 && getSheetTop(false) < dp(64 + 32) && listView.canScrollVertically(1) && hasEnoughHeight()) {
                    sheetTopNotAnimate = true;
                    listView.smoothScrollBy(0, (int) getSheetTop(false));
                }
            }
        });
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onChangeAnimationUpdate(RecyclerView.ViewHolder holder) {
                containerView.invalidate();
            }

            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                containerView.invalidate();
            }
        };
        itemAnimator.setDurations(180);
        itemAnimator.setInterpolator(new LinearInterpolator());
        listView.setItemAnimator(itemAnimator);
        containerView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        headerView = new HeaderView(context, messageObject);
        containerView.addView(headerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 78, Gravity.TOP | Gravity.FILL_HORIZONTAL));

        FrameLayout buttonView = new FrameLayout(context);
        buttonView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

        buttonShadowView = new View(context);
        buttonShadowView.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
        buttonShadowView.setAlpha(0);
        buttonView.addView(buttonShadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL));

        ButtonWithCounterView buttonTextView = new ButtonWithCounterView(context, getResourcesProvider()).setRound();
        buttonTextView.setFilled(true);
        buttonTextView.setText(getString(R.string.Close), false);
        buttonTextView.setOnClickListener(e -> dismiss());
        buttonView.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 72, 16));

        ButtonWithCounterView copyButton = new ButtonWithCounterView(context, getResourcesProvider()).setRound();
        SpannableStringBuilder sb = new SpannableStringBuilder(" ");
        sb.setSpan(new ColoredImageSpan(ContextCompat.getDrawable(getContext(), R.drawable.msg_copy_filled_solar)), 0, 1, 0);
        copyButton.setText(sb, false);
        copyButton.setOnClickListener(v -> {
            if (isNoForwards) {
                if (ChatObject.isChannel(currentChat) && !currentChat.megagroup) {
                    BulletinFactory.of(getContainer(), getResourcesProvider()).createErrorBulletin(getString(R.string.ForwardsRestrictedInfoChannel))
                            .setDuration(Bulletin.DURATION_LONG)
                            .show();
                } else {
                    BulletinFactory.of(getContainer(), getResourcesProvider()).createErrorBulletin(getString(R.string.ForwardsRestrictedInfoGroup))
                            .setDuration(Bulletin.DURATION_LONG)
                            .show();
                }
            } else if (json != null) {
                AndroidUtilities.addToClipboard(json.text);
                BulletinFactory.of(getContainer(), getResourcesProvider()).createCopyBulletin(getString(R.string.TextCopied))
                        .setDuration(Bulletin.DURATION_LONG)
                        .show();
            }
        });
        buttonView.addView(copyButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16, 16));

        containerView.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        colorizeJson(messageObject);
    }

    private boolean hasEnoughHeight() {
        float height = 0;
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (listView.getChildAdapterPosition(child) == 1)
                height += child.getHeight();
        }
        return height >= listView.getHeight() - listView.getPaddingTop() - listView.getPaddingBottom();
    }

    public void colorizeJson(MessageObject messageObject) {
        final boolean useGson = !isJacksonSupportedAndEnabled();
        final int requestId = ++colorizeRequestId;

        Utilities.globalQueue.postRunnable(() -> {
            String serialized;
            try {
                serialized = useGson
                        ? toPrettyJson(messageObject.messageOwner)
                        : new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(messageObject.messageOwner);
            } catch (Exception e) {
                CherrygramLogger.e(e);
                CherrygramMessagesConfig.INSTANCE.setJacksonJSON_Provider(useGson);
                serialized = getString(R.string.SafetyNetErrorOccurred);
            }
            final String jsonString = serialized;

            final ArrayList<FoldNode> nodes = new ArrayList<>();
            ArrayDeque<FoldNode> stack = new ArrayDeque<>();
            boolean inString = false;
            for (int i = 0; i < jsonString.length(); i++) {
                char c = jsonString.charAt(i);
                if (inString) {
                    if (c == '\\') {
                        i++;
                    } else if (c == '"') {
                        inString = false;
                    }
                    continue;
                }
                if (Character.isWhitespace(c)) {
                    continue;
                }
                FoldNode top = stack.peek();
                if (c == '}' || c == ']') {
                    if (top == null) {
                        break;
                    }
                    stack.pop();
                    top.close = i;
                    int newLine = jsonString.indexOf('\n', top.open);
                    if (newLine != -1 && newLine < i) {
                        nodes.add(top);
                    }
                    continue;
                }
                if (top != null) {
                    if (c == ',') {
                        top.commas++;
                    } else {
                        top.hasContent = true;
                    }
                }
                if (c == '"') {
                    inString = true;
                } else if (c == '{' || c == '[') {
                    stack.push(new FoldNode(i, c == '['));
                }
            }
            nodes.sort(Comparator.comparingInt(node -> node.open));

            final ArrayList<DateHint> hints = new ArrayList<>();
            Matcher matcher = DATE_FIELD_PATTERN.matcher(jsonString);
            while (matcher.find()) {
                long seconds = Long.parseLong(matcher.group(1));
                if (seconds < 946684800L || seconds > 4102444800L) {
                    continue;
                }
                int lineEnd = jsonString.indexOf('\n', matcher.end());
                hints.add(new DateHint(lineEnd == -1 ? jsonString.length() : lineEnd, " (" + CGResourcesHelper.INSTANCE.createDateAndTimeForJSON(seconds) + ")"));
            }

            CodeHighlighting.requestStyledRanges(jsonString, "json", ranges -> AndroidUtilities.runOnUIThread(() -> {
                if (requestId != colorizeRequestId || !isShowing()) {
                    return;
                }
                json = new LoadedJson(jsonString, ranges, nodes, hints);
                collapsed = new boolean[nodes.size()];
                displayState = null;
                setSearchQuery(searchQuery);
            }));
        });
    }

    private void updateDisplay(boolean scrollToMatch) {
        if (json == null) {
            return;
        }
        final int requestId = ++displayRequestId;
        final LoadedJson loaded = json;
        final boolean[] collapsedSnapshot = collapsed.clone();
        final ArrayList<int[]> matches = new ArrayList<>(searchMatches);
        final int matchIndex = searchIndex;
        final int commentColor = getThemedColor(Theme.key_code_comment);
        final int dateColor = Theme.multAlpha(getThemedColor(Theme.key_dialogTextBlack), .45f);
        final int matchColor = getThemedColor(Theme.key_featuredStickers_addButton);

        Utilities.globalQueue.postRunnable(() -> {
            int nodeCount = loaded.nodes.size();
            int[] nodeOpen = new int[nodeCount];
            int[] nodeClose = new int[nodeCount];
            Arrays.fill(nodeOpen, -1);
            int[] shiftPositions = new int[nodeCount + loaded.dateHints.size()];
            int[] shiftTotals = new int[shiftPositions.length];
            int shiftCount = 0;
            int[] collapsedOpens = new int[nodeCount];
            int[] collapsedCloses = new int[nodeCount];
            int collapsedCount = 0;

            StringBuilder text = new StringBuilder(loaded.text.length() + loaded.dateHints.size() * 32);
            BulkSpanned spanned = new BulkSpanned(text);
            int cursor = 0;
            int skipUntil = -1;
            int total = 0;
            int nodeIndex = 0;
            int hintIndex = 0;
            while (nodeIndex < nodeCount || hintIndex < loaded.dateHints.size()) {
                boolean takeNode = hintIndex >= loaded.dateHints.size()
                        || nodeIndex < nodeCount && loaded.nodes.get(nodeIndex).open < loaded.dateHints.get(hintIndex).position();
                if (takeNode) {
                    int i = nodeIndex++;
                    FoldNode node = loaded.nodes.get(i);
                    if (node.open < skipUntil) {
                        continue;
                    }
                    nodeOpen[i] = node.open;
                    if (!collapsedSnapshot[i]) {
                        continue;
                    }
                    text.append(loaded.text, cursor, node.open + 1);
                    int start = text.length();
                    int count = node.childCount();
                    text.append(' ').append(count).append(node.isArray ? (count == 1 ? " item " : " items ") : (count == 1 ? " key " : " keys "));
                    spanned.add(new ForegroundColorSpan(commentColor), start, text.length());
                    total += text.length() - start - (node.close - node.open - 1);
                    shiftPositions[shiftCount] = node.close;
                    shiftTotals[shiftCount++] = total;
                    collapsedOpens[collapsedCount] = node.open;
                    collapsedCloses[collapsedCount++] = node.close;
                    cursor = node.close;
                    skipUntil = node.close;
                } else {
                    DateHint hint = loaded.dateHints.get(hintIndex++);
                    if (hint.position() < skipUntil) {
                        continue;
                    }
                    text.append(loaded.text, cursor, hint.position());
                    int start = text.length();
                    text.append(hint.text());
                    spanned.add(new ForegroundColorSpan(dateColor), start, text.length());
                    total += hint.text().length();
                    shiftPositions[shiftCount] = hint.position();
                    shiftTotals[shiftCount++] = total;
                    cursor = hint.position();
                }
            }
            text.append(loaded.text, cursor, loaded.text.length());

            for (int i = 0; i < nodeCount; i++) {
                if (nodeOpen[i] >= 0) {
                    nodeOpen[i] = toDisplayPosition(shiftPositions, shiftTotals, shiftCount, nodeOpen[i]);
                    nodeClose[i] = toDisplayPosition(shiftPositions, shiftTotals, shiftCount, loaded.nodes.get(i).close);
                }
            }

            for (int r = 0; r < loaded.styledRanges.length; r += 3) {
                int group = loaded.styledRanges[r + 2] & PrismaTokenStyles.COLOR_MASK;
                int start = loaded.styledRanges[r];
                if (group == 0 || findCollapsedRange(collapsedOpens, collapsedCloses, collapsedCount, start) >= 0) {
                    continue;
                }
                int end = loaded.styledRanges[r + 1];
                int hidden = findCollapsedRange(collapsedOpens, collapsedCloses, collapsedCount, end - 1);
                if (hidden >= 0) {
                    end = collapsedOpens[hidden] + 1;
                }
                spanned.add(new CodeHighlighting.ColorSpan(group),
                        toDisplayPosition(shiftPositions, shiftTotals, shiftCount, start),
                        toDisplayPosition(shiftPositions, shiftTotals, shiftCount, end - 1) + 1);
            }

            int currentMatchStart = -1;
            for (int m = 0; m < matches.size(); m++) {
                int[] match = matches.get(m);
                if (findCollapsedRange(collapsedOpens, collapsedCloses, collapsedCount, match[0]) >= 0
                        || findCollapsedRange(collapsedOpens, collapsedCloses, collapsedCount, match[1] - 1) >= 0) {
                    continue;
                }
                int start = toDisplayPosition(shiftPositions, shiftTotals, shiftCount, match[0]);
                spanned.add(new BackgroundColorSpan(Theme.multAlpha(matchColor, m == matchIndex ? .6f : .25f)), start,
                        toDisplayPosition(shiftPositions, shiftTotals, shiftCount, match[1] - 1) + 1);
                if (m == matchIndex) {
                    currentMatchStart = start;
                }
            }

            DisplayState state = new DisplayState(new SpannableStringBuilder(spanned), collapsedSnapshot, nodeOpen, nodeClose, currentMatchStart);
            AndroidUtilities.runOnUIThread(() -> {
                if (requestId != displayRequestId || !isShowing()) {
                    return;
                }
                displayState = state;
                textView.setText(state.text, TextView.BufferType.SPANNABLE);
                adapter.updateMainView(textViewContainer);

                Layout layout = textView.getLayout();
                if (!scrollToMatch || state.currentMatchStart < 0 || layout == null || textViewContainer.getParent() != listView) {
                    return;
                }
                int line = layout.getLineForOffset(state.currentMatchStart);
                int y = textViewContainer.getTop() + textView.getTop() + textView.getTotalPaddingTop() + layout.getLineTop(line);
                int visibleTop = listView.getPaddingTop();
                int visibleBottom = listView.getHeight() - listView.getPaddingBottom();
                if (y < visibleTop + dp(16) || y + layout.getLineBottom(line) - layout.getLineTop(line) > visibleBottom - dp(16)) {
                    listView.smoothScrollBy(0, y - visibleTop - (visibleBottom - visibleTop) / 3);
                }
            });
        });
    }

    private static int toDisplayPosition(int[] shiftPositions, int[] shiftTotals, int shiftCount, int position) {
        int low = 0;
        int high = shiftCount;
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (shiftPositions[mid] <= position) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low == 0 ? position : position + shiftTotals[low - 1];
    }

    private static int findCollapsedRange(int[] opens, int[] closes, int count, int position) {
        int low = 0;
        int high = count - 1;
        int found = -1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (opens[mid] < position) {
                found = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return found >= 0 && position < closes[found] ? found : -1;
    }

    private void setSearchQuery(String query) {
        searchQuery = query;
        searchMatches.clear();
        if (json != null && !query.isEmpty()) {
            Matcher matcher = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(json.text);
            while (matcher.find()) {
                searchMatches.add(new int[]{matcher.start(), matcher.end()});
            }
        }
        searchIndex = searchMatches.isEmpty() ? -1 : 0;
        showSearchMatch();
    }

    private void moveSearch(int direction) {
        if (searchMatches.isEmpty()) {
            return;
        }
        searchIndex = (searchIndex + direction + searchMatches.size()) % searchMatches.size();
        showSearchMatch();
    }

    private void showSearchMatch() {
        boolean hasMatches = !searchMatches.isEmpty();
        headerView.searchCounterTextView.setText(hasMatches ? (searchIndex + 1) + "/" + searchMatches.size() : searchQuery.isEmpty() ? "" : getString(R.string.NoResult));
        headerView.searchUpButton.setEnabled(hasMatches);
        headerView.searchUpButton.setAlpha(hasMatches ? 1f : .4f);
        headerView.searchDownButton.setEnabled(hasMatches);
        headerView.searchDownButton.setAlpha(hasMatches ? 1f : .4f);
        if (json == null) {
            return;
        }
        if (hasMatches) {
            int[] match = searchMatches.get(searchIndex);
            for (int i = 0; i < json.nodes.size(); i++) {
                FoldNode node = json.nodes.get(i);
                if (collapsed[i] && match[0] < node.close && match[1] > node.open + 1) {
                    collapsed[i] = false;
                }
            }
        }
        updateDisplay(true);
    }

    @Override
    public void onBackPressed() {
        if (headerView.searching) {
            headerView.closeSearch();
            return;
        }
        super.onBackPressed();
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    private static class LoadingTextView extends TextView {

        private final LinkPath path = new LinkPath(true);
        private final LoadingDrawable loadingDrawable = new LoadingDrawable();

        public LoadingTextView(Context context) {
            super(context);
            loadingDrawable.usePath(path);
            loadingDrawable.setSpeed(.65f);
            loadingDrawable.setRadiiDp(4);
            setBackground(loadingDrawable);
        }

        @Override
        public void setTextColor(int color) {
            super.setTextColor(Theme.multAlpha(color, .2f));
            loadingDrawable.setColors(
                    Theme.multAlpha(color, 0.03f),
                    Theme.multAlpha(color, 0.175f),
                    Theme.multAlpha(color, 0.2f),
                    Theme.multAlpha(color, 0.45f)
            );
        }

        private void updateDrawable() {
            if (path == null || loadingDrawable == null) {
                return;
            }

            path.rewind();
            if (getLayout() != null && getLayout().getText() != null) {
                path.setCurrentLayout(getLayout(), 0, getPaddingLeft(), getPaddingTop());
                getLayout().getSelectionPath(0, getLayout().getText().length(), path);
            }
            loadingDrawable.updateBounds();
        }

        @Override
        public void setText(CharSequence text, BufferType type) {
            super.setText(text, type);
            updateDrawable();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            updateDrawable();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            loadingDrawable.reset();
        }
    }

    private static class PaddedAdapter extends RecyclerListView.Adapter {

        private final Context mContext;
        private View mMainView;

        public PaddedAdapter(Context context, View mainView) {
            mContext = context;
            mMainView = mainView;
        }

        private int mainViewType = 1;

        public void updateMainView(View newMainView) {
            if (mMainView == newMainView) {
                return;
            }
            mainViewType++;
            mMainView = newMainView;
            notifyItemChanged(1);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new RecyclerListView.Holder(new View(mContext) {
                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(
                                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec((int) (AndroidUtilities.displaySize.y * .4f), MeasureSpec.EXACTLY)
                        );
                    }
                });
            } else {
                return new RecyclerListView.Holder(mMainView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return 0;
            } else {
                return mainViewType;
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private final AnimatedFloat sheetTopAnimated;
    private float getSheetTop() {
        return getSheetTop(true);
    }
    private float getSheetTop(boolean animated) {
        float top = listView.getTop();
        if (listView.getChildCount() >= 1) {
            top += Math.max(0, listView.getChildAt(listView.getChildCount() - 1).getTop());
        }
        top = Math.max(0, top - dp(78));
        if (animated && sheetTopAnimated != null) {
            if (!listView.scrollingByUser && !sheetTopNotAnimate) {
                top = sheetTopAnimated.set(top);
            } else {
                sheetTopAnimated.set(top, true);
            }
        }
        return top;
    }

    private class HeaderView extends FrameLayout {

        private final ImageView backButton;
        private final ImageView searchIconImageView;
        private final ImageView menuIconImageView;
        private final TextView titleTextView;
        private final LinearLayout subtitleView;
        public TextView messageIdTextView;
        private final TextView libraryTextView;

        private final LinearLayout searchRow;
        private final EditTextBoldCursor searchField;
        private final TextView searchCounterTextView;
        private final ImageView searchUpButton;
        private final ImageView searchDownButton;
        private boolean searching;

        private final View shadow;

        public HeaderView(Context context, MessageObject messageObject) {
            super(context);

            backButton = new ImageView(context);
            backButton.setScaleType(ImageView.ScaleType.CENTER);
            BackDrawable backDrawable = new BackDrawable(true);
            backDrawable.setShowStick(!CherrygramChatsConfig.INSTANCE.getCenterChatTitle());
            backButton.setImageDrawable(backDrawable);
            backButton.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            backButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector)));
            backButton.setAlpha(0f);
            backButton.setOnClickListener(e -> dismiss());
            addView(backButton, LayoutHelper.createFrame(54, 54, Gravity.TOP, 1, 1, 1, 1));

            searchIconImageView = new ImageView(context);
            searchIconImageView.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector, resourcesProvider), 1));
            searchIconImageView.setScaleType(ImageView.ScaleType.CENTER);
            searchIconImageView.setImageResource(R.drawable.outline_header_search);
            searchIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon, resourcesProvider), PorterDuff.Mode.MULTIPLY));
            addView(searchIconImageView, LayoutHelper.createFrame(36, 36, Gravity.RIGHT | Gravity.TOP, 0, 11, 56, 0));

            menuIconImageView = new ImageView(context);
            menuIconImageView.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector, resourcesProvider), 1));
            menuIconImageView.setScaleType(ImageView.ScaleType.CENTER);
            menuIconImageView.setImageResource(R.drawable.ic_ab_other);
            menuIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon, resourcesProvider), PorterDuff.Mode.MULTIPLY));
            menuIconImageView.setOnClickListener((v) -> ChatsHelper2.INSTANCE.showJsonMenu(JsonBottomSheet.this, headerView, messageObject));
            addView(menuIconImageView, LayoutHelper.createFrame(36, 36, Gravity.RIGHT | Gravity.TOP, 0, 11, 16, 0));

            titleTextView = new TextView(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    if (LocaleController.isRTL) {
                        titleTextView.setPivotX(getMeasuredWidth());
                    }
                }
            };
            titleTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            titleTextView.setTypeface(AndroidUtilities.bold());
            titleTextView.setText("JSON");
            titleTextView.setPivotX(0);
            titleTextView.setPivotY(0);
            addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 22, 20, 22, 0));

            subtitleView = new LinearLayout(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    if (LocaleController.isRTL) {
                        subtitleView.setPivotX(getMeasuredWidth());
                    }
                }
            };
            subtitleView.setOrientation(LinearLayout.VERTICAL);
            if (LocaleController.isRTL) {
                subtitleView.setGravity(Gravity.RIGHT);
            }
            subtitleView.setPivotX(0);
            subtitleView.setPivotY(0);

            int messageId = messageObject.messageOwner.id;

            messageIdTextView = new TextView(context);
            messageIdTextView.setSingleLine(true);
            messageIdTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
            messageIdTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            messageIdTextView.setText("message ID: " + messageId);
            messageIdTextView.setPadding(0, dp(2), 0, 0);
            messageIdTextView.setOnClickListener(v -> {
                AndroidUtilities.addToClipboard(String.valueOf(messageId));
                BulletinFactory.of(getContainer(), null).createCopyBulletin(getString(R.string.TextCopied)).show();
            });
            subtitleView.addView(messageIdTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 5, 0, 0));

            libraryTextView = new TextView(context);
            libraryTextView.setSingleLine(true);
            libraryTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
            libraryTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            libraryTextView.setText("JSON Library: " + (isJacksonSupportedAndEnabled() ? "Jackson" : "Google GSON"));
            subtitleView.addView(libraryTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

            addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 22, 34, 22, 0));

            searchRow = new LinearLayout(context);
            searchRow.setOrientation(LinearLayout.HORIZONTAL);
            searchRow.setGravity(Gravity.CENTER_VERTICAL);
            searchRow.setVisibility(GONE);

            ImageView closeSearchButton = new ImageView(context);
            closeSearchButton.setScaleType(ImageView.ScaleType.CENTER);
            closeSearchButton.setImageDrawable(new BackDrawable(false));
            closeSearchButton.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            closeSearchButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector), 1));
            closeSearchButton.setOnClickListener(v -> closeSearch());
            searchRow.addView(closeSearchButton, LayoutHelper.createLinear(36, 36));

            searchField = new EditTextBoldCursor(context);
            searchField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            searchField.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            searchField.setHintTextColor(getThemedColor(Theme.key_dialogTextHint));
            searchField.setHint(getString(R.string.Search));
            searchField.setCursorColor(getThemedColor(Theme.key_dialogTextBlack));
            searchField.setCursorSize(dp(20));
            searchField.setCursorWidth(1.5f);
            searchField.setBackground(null);
            searchField.setPadding(0, 0, 0, 0);
            searchField.setSingleLine(true);
            searchField.setGravity(Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
            searchField.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            searchField.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    moveSearch(1);
                    return true;
                }
                return false;
            });
            searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    setSearchQuery(s.toString());
                }
            });
            searchRow.addView(searchField, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f, 8, 0, 4, 0));

            searchCounterTextView = new TextView(context);
            searchCounterTextView.setSingleLine(true);
            searchCounterTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
            searchCounterTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            searchRow.addView(searchCounterTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 0, 4, 0));

            searchUpButton = new ImageView(context);
            searchUpButton.setScaleType(ImageView.ScaleType.CENTER);
            searchUpButton.setImageResource(R.drawable.msg_go_up);
            searchUpButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon, resourcesProvider), PorterDuff.Mode.MULTIPLY));
            searchUpButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector, resourcesProvider), 1));
            searchUpButton.setOnClickListener(v -> moveSearch(-1));
            searchRow.addView(searchUpButton, LayoutHelper.createLinear(36, 36));

            searchDownButton = new ImageView(context);
            searchDownButton.setScaleType(ImageView.ScaleType.CENTER);
            searchDownButton.setImageResource(R.drawable.msg_go_down);
            searchDownButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon, resourcesProvider), PorterDuff.Mode.MULTIPLY));
            searchDownButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector, resourcesProvider), 1));
            searchDownButton.setOnClickListener(v -> moveSearch(1));
            searchRow.addView(searchDownButton, LayoutHelper.createLinear(36, 36));

            addView(searchRow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.TOP | Gravity.FILL_HORIZONTAL, 10, 11, 12, 0));

            searchIconImageView.setOnClickListener(v -> {
                searching = true;
                titleTextView.setVisibility(INVISIBLE);
                subtitleView.setVisibility(INVISIBLE);
                backButton.setVisibility(INVISIBLE);
                searchIconImageView.setVisibility(INVISIBLE);
                menuIconImageView.setVisibility(INVISIBLE);
                searchRow.setVisibility(VISIBLE);
                searchRow.setAlpha(0f);
                searchRow.animate().alpha(1f).setDuration(150).start();
                searchField.requestFocus();
                AndroidUtilities.showKeyboard(searchField);
            });

            shadow = new View(context);
            shadow.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
            shadow.setAlpha(0);
            addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 56, 0, 0));
        }

        private void closeSearch() {
            searching = false;
            AndroidUtilities.hideKeyboard(searchField);
            searchField.setText("");
            searchRow.setVisibility(GONE);
            titleTextView.setVisibility(VISIBLE);
            subtitleView.setVisibility(VISIBLE);
            backButton.setVisibility(VISIBLE);
            searchIconImageView.setVisibility(VISIBLE);
            menuIconImageView.setVisibility(VISIBLE);
        }

        @Override
        public void setTranslationY(float translationY) {
            super.setTranslationY(translationY);

            float t = MathUtils.clamp((translationY - AndroidUtilities.statusBarHeight) / dp(64), 0, 1);
            if (!hasEnoughHeight()) {
                t = 1;
            }
            t = CubicBezierInterpolator.EASE_OUT.getInterpolation(t);

            titleTextView.setScaleX(AndroidUtilities.lerp(.85f, 1f, t));
            titleTextView.setScaleY(AndroidUtilities.lerp(.85f, 1f, t));
            titleTextView.setTranslationY(AndroidUtilities.lerp(dpf2(-12), 0, t));
            if (!LocaleController.isRTL) {
                titleTextView.setTranslationX(AndroidUtilities.lerp(dpf2(50), 0, t));
                subtitleView.setTranslationX(AndroidUtilities.lerp(dpf2(50), 0, t));
            }

            subtitleView.setTranslationY(AndroidUtilities.lerp(dpf2(-12), 0, t));
            libraryTextView.setAlpha(t);

            backButton.setTranslationX(AndroidUtilities.lerp(0, dpf2(-25), t));
            backButton.setAlpha(1f - t);

            shadow.setTranslationY(AndroidUtilities.lerp(0, dpf2(22), t));
            shadow.setAlpha(1f - t);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(78), MeasureSpec.EXACTLY)
            );
        }
    }

    private class ContainerView extends FrameLayout {
        public ContainerView(Context context) {
            super(context);

            bgPaint.setColor(getThemedColor(Theme.key_dialogBackground));
            Theme.applyDefaultShadow(bgPaint);
        }

        private final Path bgPath = new Path();
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override
        protected void dispatchDraw(Canvas canvas) {

            float top = getSheetTop();
            final float R = AndroidUtilities.lerp(0, dp(12), MathUtils.clamp(top / dpf2(24), 0, 1));
            headerView.setTranslationY(Math.max(AndroidUtilities.statusBarHeight, top));
            updateLightStatusBar(top <= AndroidUtilities.statusBarHeight / 2f);

            bgPath.rewind();
            AndroidUtilities.rectTmp.set(0, top, getWidth(), getHeight() + R);
            bgPath.addRoundRect(AndroidUtilities.rectTmp, R, R, Path.Direction.CW);
            canvas.drawPath(bgPath, bgPaint);

            super.dispatchDraw(canvas);
        }

        private Boolean lightStatusBarFull;
        private void updateLightStatusBar(boolean full) {
            if (lightStatusBarFull == null || lightStatusBarFull != full) {
                lightStatusBarFull = full;
                AndroidUtilities.setLightStatusBar(getWindow(), AndroidUtilities.computePerceivedBrightness(
                        full ?
                                getThemedColor(Theme.key_dialogBackground) :
                                Theme.blendOver(
                                        getThemedColor(Theme.key_actionBarDefault),
                                        0x33000000
                                )
                ) > .721f);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            Bulletin.addDelegate(this, new Bulletin.Delegate() {
                @Override
                public int getBottomOffset(int tag) {
                    return dp(16 + 48 + 16);
                }
            });
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            Bulletin.removeDelegate(this);
        }
    }

    private Boolean buttonShadowShown;
    private void updateButtonShadow(boolean show) {
        if (buttonShadowShown == null || buttonShadowShown != show) {
            buttonShadowShown = show;
            buttonShadowView.animate().cancel();
            buttonShadowView.animate().alpha(show ? 1f : 0f).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(320).start();
        }
    }

    public boolean isJacksonSupportedAndEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && CherrygramMessagesConfig.INSTANCE.getJacksonJSON_Provider()) {
            CherrygramMessagesConfig.INSTANCE.setJacksonJSON_Provider(false);
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && CherrygramMessagesConfig.INSTANCE.getJacksonJSON_Provider();
    }

    public static JsonBottomSheet showAlert(Context context, Theme.ResourcesProvider resourcesProvider, BaseFragment fragment, MessageObject messageObject, TLRPC.Chat currentChat) {
        JsonBottomSheet alert = new JsonBottomSheet(context, resourcesProvider, messageObject, currentChat);
        if (fragment.getParentActivity() != null) {
            fragment.showDialog(alert);
        }
        alert.dimBehindAlpha = 140;
        alert.setFragment(fragment);
        return alert;
    }

    private static String toPrettyJson(Object object) {
        JsonElement jsonElement = JsonParser.parseString(gson.toJson(object));
        return prettyGson.toJson(jsonElement);
    }

    private static class TLObjectTypeAdapter implements JsonSerializer<TLObject> {
        @Override
        public JsonElement serialize(TLObject src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("_type", src.getClass().getSimpleName());
            HashSet<String> names = new HashSet<>();
            for (Class<?> clazz = src.getClass(); clazz != null && TLObject.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    int modifiers = field.getModifiers();
                    if (field.isSynthetic() || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                        continue;
                    }
                    String name = field.getName();
                    if (!names.add(name) || shouldSkipJsonField(field.getDeclaringClass(), name)) {
                        continue;
                    }
                    try {
                        field.setAccessible(true);
                        Object value = field.get(src);
                        if (value != null) {
                            object.add(name, context.serialize(value));
                        }
                    } catch (Exception e) {
                        CherrygramLogger.e(e);
                    }
                }
            }
            return object;
        }
    }

    private static boolean shouldSkipJsonField(Class<?> declaringClass, String name) {
        if ("parentRichText".equals(name) || "bitmap".equals(name) || "mChangingConfigurations".equals(name)) {
            return true;
        }
        if (TL_iv.Page.class.isAssignableFrom(declaringClass)) {
            return "web".equals(name) || "local".equals(name);
        }
        if (TL_iv.textMath.class.isAssignableFrom(declaringClass)) {
            return "w".equals(name) || "h".equals(name) || "depth".equals(name) || "tried".equals(name);
        }
        if (TL_iv.PageBlock.class.isAssignableFrom(declaringClass)) {
            return "first".equals(name) || "bottom".equals(name) || "level".equals(name) || "quoteLevels".equals(name) ||
                    "mid".equals(name) || "groupId".equals(name) || "thumb".equals(name) || "thumbObject".equals(name) ||
                    "cachedWidth".equals(name) || "cachedHeight".equals(name);
        }
        return false;
    }

}