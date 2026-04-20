package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.content.Context;
import android.view.View;

import org.telegram.ui.Components.AnimatedLinearLayout;
import org.telegram.ui.Components.glass.GlassTabView;

import me.vkryl.android.animator.ListAnimator;

public class MainTabsLayout extends AnimatedLinearLayout {
    //DIVO--START
    private int maxWidth = -1;
    private int minWidth = -1;

    private float[] tabsLeftPosAnimated;
    private float[] tabsWidthAnimated;

    public void setMaxWidth(int value) {
        if (maxWidth != value) {
            maxWidth = value;
            requestLayout();
        }
    }

    public void setMinWidth(int value) {
        if (minWidth != value) {
            minWidth = value;
            requestLayout();
        }
    }
    //DIVO--END

    public MainTabsLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //DIVO--START
        if (maxWidth > 0 && width > maxWidth) {
            width = maxWidth;
        }
        //DIVO--END
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int tabHeight = height - getPaddingTop() - getPaddingBottom();

        measureTabTexts();

        final int maxTotalWidthForTabs = width - getPaddingLeft() - getPaddingRight();
        //DIVO--START
        int minTotalWidthForTabs;
        if (minWidth > 0) {
            minTotalWidthForTabs = minWidth - getPaddingLeft() - getPaddingRight();
        } else {
            minTotalWidthForTabs = Math.min(dp(320), maxTotalWidthForTabs);
        }

        final int tabPadding = dp(16);
        final int wideTabPadding = dp(8);

        final int maxTabTextWidthIfEq = (maxTotalWidthForTabs / visibleChildCount) - tabPadding * 2;

        float totalWidth = 0;
        int totalWeight = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (!isViewVisible(child)) {
                tabsTextWidth[a] = tabsTextWidthWithMargin[a] = 0;
                tabsWeight[a] = 0;
                continue;
            }

            final float w = tabsTextWidth[a];
            if (w > maxTabTextWidthIfEq) {
                tabsTextWidthWithMargin[a] = tabsTextWidth[a] + wideTabPadding * 2;
            } else {
                tabsTextWidthWithMargin[a] = tabsTextWidth[a] + tabPadding * 2;
            }
            tabsWeight[a] = tabsTextWidthWithMargin[a] > (maxTabTextWidthIfEq + tabPadding * 2) ? 0 : 1;

            totalWidth += tabsTextWidthWithMargin[a];
            totalWeight += tabsWeight[a];
        }

        if (totalWeight == 0) {
            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsWeight[a] = isViewVisible(getChildAt(a)) ? 1 : 0;
            }
            totalWeight = visibleChildCount;
        }

        if (totalWidth > maxTotalWidthForTabs) {
            final float m = maxTotalWidthForTabs / totalWidth;
            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsTextWidthWithMargin[a] *= m;
            }
        } else if (totalWidth < minTotalWidthForTabs) {
            final float growW = minTotalWidthForTabs - totalWidth;
            final float growP = growW / totalWeight;

            for (int a = 0, N = getChildCount(); a < N; a++) {
                tabsTextWidthWithMargin[a] += growP * tabsWeight[a];
            }
        }

        if (tabsWidthAnimated == null || tabsWidthAnimated.length < getChildCount()) {
            tabsWidthAnimated = new float[getChildCount()];
            tabsLeftPosAnimated = new float[getChildCount()];
        }

        float l = 0;
        for (int a = 0, N = getChildCount(); a < N; a++) {
            if (!isViewVisible(getChildAt(a))) {
                continue;
            }

            tabsWidth[a] = Math.round(tabsTextWidthWithMargin[a]);
            tabsLeftPos[a] = Math.round(l);

            tabsWidthAnimated[a] = lerp(tabsWidthAnimated[a], tabsTextWidthWithMargin[a], 0.3f);
            tabsLeftPosAnimated[a] = lerp(tabsLeftPosAnimated[a], l, 0.3f);

            l += tabsTextWidthWithMargin[a];
        }

        setMeasuredDimension(Math.round(l) + getPaddingLeft() + getPaddingRight(), height);

        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            child.measure(
                    MeasureSpec.makeMeasureSpec(Math.round(tabsWidthAnimated[a]), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(tabHeight, MeasureSpec.EXACTLY));
        }

        calculateTotalSizesAfterMeasure();
    }

    // fills tabsTextWidth[] and return visible child count;
    private float[] tabsTextWidth;
    private float[] tabsTextWidthWithMargin;
    private int[] tabsWeight;
    private int[] tabsWidth;
    private int[] tabsLeftPos;

    private int visibleChildCount;
    private int biggestTabTextWidth;

    private void measureTabTexts() {
        final int childCount = getChildCount();
        if (tabsTextWidth == null || tabsTextWidth.length < childCount) {
            tabsTextWidth = new float[childCount];
            tabsTextWidthWithMargin = new float[childCount];
            tabsWeight = new int[childCount];
            tabsLeftPos = new int[childCount];
            tabsWidth = new int[childCount];
        }

        float maxTabWidthF = 0;
        int index = 0;

        for (int a = 0; a < childCount; a++) {
            final View child = getChildAt(a);
            if (!isViewVisible(child)) {
                tabsTextWidth[a] = -1;
                continue;
            }

            final float tabWidth;
            if (child instanceof MainTabsLayout.Tab) {
                tabWidth = ((MainTabsLayout.Tab) child).measureTextWidth();
            } else {
                tabWidth = 0;
            }

            tabsTextWidth[a] = tabWidth;
            maxTabWidthF = Math.max(maxTabWidthF, tabWidth);
            index++;
        }

        biggestTabTextWidth = (int) Math.ceil(maxTabWidthF);
        visibleChildCount = index;
    }

    @Override
    protected void setChildVisibilityFactor(View view, float factor) {
        final float s = lerp(0.7f, 1f, factor);
        view.setAlpha(factor);
        view.setScaleX(s);
        view.setScaleY(s);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int a = 0, N = getChildCount(); a < N; a++) {
            final View child = getChildAt(a);
            if (!isViewVisible(child)) {
                continue;
            }

            final int top = getPaddingTop();
            final int left = getPaddingLeft() + Math.round(tabsLeftPosAnimated[a]);
            child.layout(left, top, left + Math.round(tabsWidthAnimated[a]), top + child.getMeasuredHeight());
        }

        checkVisualWidth();
    }

    @Override
    protected void onItemsChanged() {
        super.onItemsChanged();
        checkVisualWidth();
    }

    private void checkVisualWidth() {
        for (int a = 0, N = getEntriesCount(); a < N; a++) {
            final ListAnimator.Entry<Holder> entry = getEntry(a);
            final float width = entry.getRectF().width();
            ((GlassTabView) entry.item.view).setVisualWidth(width);
        }
    }

    public interface Tab {
        float measureTextWidth();
    }
}
