package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;

import org.telegram.divo.screen.event_list.FragmentEventList;
import org.telegram.divo.screen.models.FragmentModels;
import org.telegram.divo.screen.settings.FragmentSettings;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LiteMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.blur3.BlurredBackgroundDrawableViewFactory;
import org.telegram.ui.Components.blur3.BlurredBackgroundWithFadeDrawable;
import org.telegram.ui.Components.blur3.RenderNodeWithHash;
import org.telegram.ui.Components.blur3.capture.IBlur3Hash;
import org.telegram.ui.Components.blur3.drawable.BlurredBackgroundDrawable;
import org.telegram.ui.Components.blur3.drawable.color.impl.BlurredBackgroundProviderImpl;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceColor;
import org.telegram.ui.Components.blur3.source.BlurredBackgroundSourceRenderNode;
import org.telegram.ui.Components.chat.ViewPositionWatcher;
import org.telegram.ui.Components.glass.GlassTabView;

import java.util.ArrayList;

import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.animator.FactorAnimator;

public class MainTabsActivity extends ViewPagerActivity implements NotificationCenter.NotificationCenterDelegate, FactorAnimator.Target {
    public static final int TABS_COUNT = 4;
    private static final int POSITION_MODELS = 0;
    private static final int POSITION_EVENTS = 1;
    private static final int POSITION_CHATS = 2;
    private static final int POSITION_SETTINGS = 3;

    private static final int INDEX_MODELS = 0;
    private static final int INDEX_EVENTS = 1;
    private static final int INDEX_CHATS = 2;
    private static final int INDEX_SETTINGS = 3;



    private static final int ANIMATOR_ID_TABS_VISIBLE = 0;
    private final BoolAnimator animatorTabsVisible = new BoolAnimator(ANIMATOR_ID_TABS_VISIBLE,
        this, CubicBezierInterpolator.EASE_OUT_QUINT, 380, true);


    private IUpdateLayout updateLayout;

    private UpdateLayoutWrapper updateLayoutWrapper;
    private MainTabsLayout tabsView;
    private BlurredBackgroundDrawable tabsViewBackground;
    private View fadeView;

    //DIVO--START
    private LinearLayout bottomBarContainer;
    private FrameLayout modelsSearchButton;
    private boolean isModelsSearchVisible;
    //DIVO--END

    public MainTabsActivity() {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            iBlur3SourceTabGlass = new BlurredBackgroundSourceRenderNode(null);
            iBlur3SourceTabGlass.setupRenderer(new RenderNodeWithHash.Renderer() {
                @Override
                public void renderNodeCalculateHash(IBlur3Hash hash) {
                    hash.add(0xFFFFFFFF); // Force White
                    hash.add(SharedConfig.chatBlurEnabled());

                    for (int a = 0, N = fragmentsArr.size(); a < N; a++) {
                        final FragmentState state = fragmentsArr.valueAt(a);
                        final BaseFragment fragment = state.fragment;
                        if (fragment.fragmentView == null) {
                            continue;
                        }
                        if (!ViewPositionWatcher.computeRectInParent(fragment.fragmentView, contentView, fragmentPosition)) {
                            continue;
                        }
                        if (fragmentPosition.right <= 0 || fragmentPosition.left >= fragmentView.getMeasuredWidth()) {
                            continue;
                        }

                        if (fragment instanceof TabFragmentDelegate) {
                            TabFragmentDelegate delegate = (TabFragmentDelegate) fragment;
                            BlurredBackgroundSourceRenderNode source = delegate.getGlassSource();
                            if (source != null) {
                                hash.addF(fragmentPosition.left);
                                hash.addF(fragmentPosition.top);
                                hash.add(fragment.getClassGuid());
                            }
                        }
                    }
                }

                @Override
                public void renderNodeUpdateDisplayList(Canvas canvas) {
                    final int width = fragmentView.getMeasuredWidth();
                    final int height = fragmentView.getMeasuredHeight();

                    canvas.drawColor(0xFFFFFFFF); // Force White

                    for (int a = 0, N = fragmentsArr.size(); a < N; a++) {
                        final FragmentState state = fragmentsArr.valueAt(a);
                        final BaseFragment fragment = state.fragment;
                        if (fragment.fragmentView == null) {
                            continue;
                        }
                        if (!ViewPositionWatcher.computeRectInParent(fragment.fragmentView, contentView, fragmentPosition)) {
                            continue;
                        }
                        if (fragmentPosition.right <= 0 || fragmentPosition.left >= fragmentView.getMeasuredWidth()) {
                            continue;
                        }

                        if (fragment instanceof TabFragmentDelegate) {
                            TabFragmentDelegate delegate = (TabFragmentDelegate) fragment;
                            BlurredBackgroundSourceRenderNode source = delegate.getGlassSource();
                            if (source != null) {
                                canvas.save();
                                canvas.translate(fragmentPosition.left, fragmentPosition.top);
                                source.draw(canvas, 0, 0, width, height);
                                canvas.restore();
                            }
                        }
                    }
                }
            });
        } else {
            iBlur3SourceTabGlass = null;
        }

        iBlur3SourceColor = new BlurredBackgroundSourceColor();
    }

    @Override
    protected FrameLayout createContentView(Context context) {
        return new FrameLayout(context) {
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                checkUi_tabsPosition();
                checkUi_fadeView();
            }

            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                super.dispatchDraw(canvas);
                blur3_invalidateBlur();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        blur3_updateColors();
        checkUnreadCount(true);

        Bulletin.Delegate delegate = new Bulletin.Delegate() {
            @Override
            public int getBottomOffset(int tag) {
                return navigationBarHeight + dp(DialogsActivity.MAIN_TABS_HEIGHT + DialogsActivity.MAIN_TABS_MARGIN);
            }
        };

        Bulletin.addDelegate(this, delegate);
        Bulletin.addDelegate(contentView, delegate);
    }

    @Override
    public void onPause() {
        super.onPause();
        Bulletin.removeDelegate(this);
        Bulletin.removeDelegate(contentView);
    }

    @Override
    public View createView(Context context) {
        super.createView(context);

        tabsView = new MainTabsLayout(context);
        tabsView.setClipChildren(false);
        tabsView.setPadding(dp(DialogsActivity.MAIN_TABS_MARGIN + 4), dp(DialogsActivity.MAIN_TABS_MARGIN + 4), dp(DialogsActivity.MAIN_TABS_MARGIN + 4), dp(DialogsActivity.MAIN_TABS_MARGIN + 4));

        tabs = new GlassTabView[TABS_COUNT];
        tabs[INDEX_MODELS] = GlassTabView.createMainTabWithIcon(context, resourceProvider, R.drawable.baseline_models, R.string.DivoMainTabsModels);
        tabs[INDEX_EVENTS] = GlassTabView.createMainTabWithIcon(context, resourceProvider, R.drawable.baseline_calendar_item, R.string.DivoMainTabsEvents);
        tabs[INDEX_CHATS] = GlassTabView.createMainTab(context, resourceProvider, GlassTabView.TabAnimation.CHATS, R.string.DivoMainTabsChats);
        tabs[INDEX_SETTINGS] = GlassTabView.createAvatar(context, resourceProvider, currentAccount, R.string.DivoMainTabsSettings);

        for (int index = 0; index < tabs.length; index++) {
            final GlassTabView view = tabs[index];

            final int position = index;
            tabs[index].setOnClickListener(v -> {
                if (viewPager.isManualScrolling() || viewPager.isTouch()) {
                    return;
                }

                if (viewPager.getCurrentPosition() == position) {
                    final BaseFragment fragment = getCurrentVisibleFragment();
                    if (fragment instanceof MainTabsActivity.TabFragmentDelegate) {
                        ((MainTabsActivity.TabFragmentDelegate) fragment).onParentScrollToTop();
                    }
                    return;
                }

                selectTab(position, true);
                viewPager.scrollToPosition(position);
            });

            tabsView.addView(tabs[index]);
            tabsView.setViewVisible(view, true, false);
        }

        selectTab(viewPager.getCurrentPosition(), false);

        iBlur3SourceColor.setColor(0xFFFFFFFF); // Force White


        ViewPositionWatcher viewPositionWatcher = new ViewPositionWatcher(contentView);


        BlurredBackgroundDrawableViewFactory iBlur3FactoryGlass = new BlurredBackgroundDrawableViewFactory(iBlur3SourceTabGlass != null ? iBlur3SourceTabGlass : iBlur3SourceColor);
        iBlur3FactoryGlass.setSourceRootView(viewPositionWatcher, contentView);
        iBlur3FactoryGlass.setLiquidGlassEffectAllowed(LiteMode.isEnabled(LiteMode.FLAG_LIQUID_GLASS));

        tabsViewBackground = iBlur3FactoryGlass.create(tabsView, BlurredBackgroundProviderImpl.mainTabs(resourceProvider));
        tabsViewBackground.setRadius(dp(DialogsActivity.MAIN_TABS_HEIGHT / 2f));
        tabsViewBackground.setPadding(dp(DialogsActivity.MAIN_TABS_MARGIN - 0.334f));
        tabsView.setBackground(tabsViewBackground);

        BlurredBackgroundDrawableViewFactory iBlur3FactoryFade = new BlurredBackgroundDrawableViewFactory(iBlur3SourceColor);
        iBlur3FactoryFade.setSourceRootView(viewPositionWatcher, contentView);

        fadeView = new View(context);
        BlurredBackgroundWithFadeDrawable fadeDrawable = new BlurredBackgroundWithFadeDrawable(iBlur3FactoryFade.create(fadeView, null));
        fadeDrawable.setFadeHeight(dp(60), true);
        fadeView.setBackground(fadeDrawable);

        //DIVO--START
        modelsSearchButton = new FrameLayout(context);
        BlurredBackgroundDrawable searchButtonBackground = iBlur3FactoryGlass.create(modelsSearchButton, BlurredBackgroundProviderImpl.mainTabs(resourceProvider));
        searchButtonBackground.setRadius(dp(28));
        searchButtonBackground.setPadding(dp(DialogsActivity.MAIN_TABS_MARGIN - 0.334f));
        modelsSearchButton.setBackground(searchButtonBackground);
        modelsSearchButton.setOnClickListener(v -> {
            BaseFragment fragment = getCurrentVisibleFragment();
            if (fragment instanceof FragmentModels) {
                ((FragmentModels) fragment).openSearchFromBottomBar();
            }
        });
        modelsSearchButton.setVisibility(View.GONE);
        modelsSearchButton.setAlpha(0f);

        ImageView searchIcon = new ImageView(context);
        searchIcon.setScaleType(ImageView.ScaleType.CENTER);
        searchIcon.setImageResource(R.drawable.ic_divo_search_24);
        modelsSearchButton.addView(searchIcon, LayoutHelper.createFrame(24, 24, Gravity.CENTER));

        bottomBarContainer = new LinearLayout(context);
        bottomBarContainer.setOrientation(LinearLayout.HORIZONTAL);
        bottomBarContainer.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        bottomBarContainer.setClipChildren(false);

        bottomBarContainer.addView(tabsView, LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT, DialogsActivity.MAIN_TABS_HEIGHT_WITH_MARGINS));
        bottomBarContainer.addView(modelsSearchButton, LayoutHelper.createLinear(
                DialogsActivity.MAIN_TABS_HEIGHT_WITH_MARGINS,
                DialogsActivity.MAIN_TABS_HEIGHT_WITH_MARGINS,
                0, -4, 0, 0, 0));

        contentView.addView(fadeView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 0, Gravity.BOTTOM));
        contentView.addView(bottomBarContainer, LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,   // ← БЫЛО WRAP_CONTENT, СТАЛО MATCH_PARENT
                LayoutHelper.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
        //DIVO--END

        updateLayoutWrapper = new UpdateLayoutWrapper(context);
        contentView.addView(updateLayoutWrapper, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        updateLayout = ApplicationLoader.applicationLoaderInstance.takeUpdateLayout(getParentActivity(), updateLayoutWrapper);
        if (updateLayout != null) {
            updateLayout.updateAppUpdateViews(currentAccount, false);
        }

        //AndroidUtilities.cancelRunOnUIThread(justForTestR);
        //AndroidUtilities.runOnUIThread(justForTestR, 2000);

        checkUnreadCount(false);
        return contentView;
    }

    private void checkUnreadCount(boolean animated) {
        if (tabsView == null || tabs[INDEX_CHATS] == null) {
            return;
        }

        final int unreadCount = MessagesStorage.getInstance(currentAccount).getMainUnreadCount();
        if (unreadCount > 0) {
            final String unreadCountFmt = LocaleController.formatNumber(unreadCount, ',');
            tabs[INDEX_CHATS].setCounter(unreadCountFmt, false, animated);
        } else {
            tabs[INDEX_CHATS].setCounter(null, false, animated);
        }
    }



    @Override
    protected void onViewPagerScrollEnd() {
        if (tabsView != null) {
            selectTab(viewPager.getCurrentPosition(), true);
            setGestureSelectedOverride(0, false);
        }
        blur3_invalidateBlur();
    }

    @Override
    protected void onViewPagerTabAnimationUpdate(boolean manual) {
        final boolean isDragByGesture = !manual;

        if (tabsView != null) {
            final float position = viewPager.getPositionAnimated();
            setGestureSelectedOverride(position, isDragByGesture);
            if (isDragByGesture) {
                selectTab(Math.round(position), true);
            }
        }

        checkUi_fadeView();
        blur3_invalidateBlur();
    }


    @Override
    protected int getFragmentsCount() {
        return TABS_COUNT;
    }

    @Override
    protected int getStartPosition() {
        return POSITION_MODELS;
    }

    private DialogsActivity dialogsActivity;

    @Override
    public boolean onBackPressed(boolean invoked) {
        final boolean result = super.onBackPressed(invoked);
        if (result) {
            final int startPosition = getStartPosition();
            if (viewPager.getCurrentPosition() != startPosition) {
                if (invoked) {
                    viewPager.scrollToPosition(startPosition);
                }
                return false;
            }
        }
        return result;
    }

    public DialogsActivity prepareDialogsActivity(Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }

        bundle.putBoolean("hasMainTabs", true);
        dialogsActivity = new DialogsActivity(bundle);
        dialogsActivity.setMainTabsActivityController(new MainTabsActivityControllerImpl());
        putFragmentAtPosition(POSITION_CHATS, dialogsActivity); // position 2
        return dialogsActivity;
    }

    @Override
    protected BaseFragment createBaseFragmentAt(int position) {
        if (position == POSITION_MODELS) {
            FragmentModels fragment = new FragmentModels();
            fragment.setMainTabsActivityController(new MainTabsActivityControllerImpl());
            return fragment;
        } else if (position == POSITION_EVENTS) {
            FragmentEventList fragment = new FragmentEventList();
            fragment.setMainTabsActivityController(new MainTabsActivityControllerImpl());
            return fragment;
        } else if (position == POSITION_CHATS) {
            Bundle args = new Bundle();
            args.putBoolean("hasMainTabs", true);
            dialogsActivity = new DialogsActivity(args);
            dialogsActivity.setMainTabsActivityController(new MainTabsActivityControllerImpl());
            return dialogsActivity;
        } else if (position == POSITION_SETTINGS) {
            FragmentSettings fragment = new FragmentSettings();
            fragment.setMainTabsActivityController(new MainTabsActivityControllerImpl());
            return fragment;
        }
        return null;
    }

    public DialogsActivity getDialogsActivity() {
        return dialogsActivity;
    }

    /* */

    public GlassTabView[] tabs;

    public void selectTab(int position, boolean animated) {
        for (int a = 0; a < tabs.length; a++) {
            GlassTabView tab = tabs[a];
            tab.setSelected(a == position, animated);
        }
    }

    public void switchToTabPosition(int position) {
        if (viewPager != null) {
            selectTab(position, false);
            viewPager.scrollToPosition(position);
        }
    }

    public void setGestureSelectedOverride(float animatedPosition, boolean allow) {
        for (int index = 0; index < tabs.length; index++) {
            final float visibility = Math.max(0, 1f - Math.abs(index - animatedPosition));
            tabs[index].setGestureSelectedOverride(visibility, allow);
        }
        tabsView.invalidate();
    }


    /* * */

    public interface TabFragmentDelegate {
        default boolean canParentTabsSlide(MotionEvent ev, boolean forward) {
            return false;
        }

        default void onParentScrollToTop() {

        }

        default BlurredBackgroundSourceRenderNode getGlassSource() {
            return null;
        }
    }

    @Override
    protected boolean canScrollForward(MotionEvent ev) {
        return canScrollInternal(ev, true);
    }

    @Override
    protected boolean canScrollBackward(MotionEvent ev) {
        return canScrollInternal(ev, false);
    }

    private boolean canScrollInternal(MotionEvent ev, boolean forward) {
        final BaseFragment fragment = getCurrentVisibleFragment();
        if (fragment instanceof TabFragmentDelegate) {
            final TabFragmentDelegate delegate = (TabFragmentDelegate) fragment;
            return delegate.canParentTabsSlide(ev, forward);

        }

        return false;
    }


    /* * */

    private int navigationBarHeight;

    @NonNull
    @Override
    protected WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
        navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
        final boolean isUpdateLayoutVisible = updateLayoutWrapper.isUpdateLayoutVisible();
        final int updateLayoutHeight = isUpdateLayoutVisible ? dp(UpdateLayoutWrapper.HEIGHT) : 0;
        updateLayoutWrapper.setPadding(0, 0, 0, navigationBarHeight);

        ViewGroup.MarginLayoutParams lp;
        {
            final int height = navigationBarHeight + updateLayoutHeight + dp(DialogsActivity.MAIN_TABS_HEIGHT_WITH_MARGINS);
            lp = (ViewGroup.MarginLayoutParams) fadeView.getLayoutParams();
            if (lp.height != height) {
                lp.height = height;
                fadeView.setLayoutParams(lp);
            }
        }
        {
            final int bottomMargin = isUpdateLayoutVisible ? (navigationBarHeight + updateLayoutHeight) : 0;
            lp = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
            if (lp.bottomMargin != bottomMargin) {
                lp.bottomMargin = bottomMargin;
                viewPager.setLayoutParams(lp);
            }
        }

        final WindowInsetsCompat consumed = isUpdateLayoutVisible ?
            insets.inset(0, 0, 0, navigationBarHeight) : insets;

        checkUi_tabsPosition();
        checkUi_fadeView();

        return super.onApplyWindowInsets(v, consumed);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.notificationsCountUpdated || id == NotificationCenter.updateInterfaces) {
            checkUnreadCount(fragmentView != null && fragmentView.isAttachedToWindow());
        } else if (id == NotificationCenter.appUpdateLoading) {
            if (updateLayout != null) {
                updateLayout.updateFileProgress(null);
                updateLayout.updateAppUpdateViews(currentAccount, true);
            }
        } else if (id == NotificationCenter.fileLoaded) {
            String path = (String) args[0];
            if (SharedConfig.isAppUpdateAvailable()) {
                String name = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
                if (name.equals(path) && updateLayout != null) {
                    updateLayout.updateAppUpdateViews(currentAccount, true);
                }
            }
        } else if (id == NotificationCenter.fileLoadFailed) {
            String path = (String) args[0];
            if (SharedConfig.isAppUpdateAvailable()) {
                String name = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
                if (name.equals(path) && updateLayout != null) {
                    updateLayout.updateAppUpdateViews(currentAccount, true);
                }
            }
        } else if (id == NotificationCenter.fileLoadProgressChanged) {
            if (updateLayout != null) {
                updateLayout.updateFileProgress(args);
            }
        } else if (id == NotificationCenter.appUpdateAvailable) {
            if (updateLayout != null) {
                updateLayout.updateAppUpdateViews(currentAccount, LaunchActivity.getMainFragmentsStackSize() == 1);
            }
        } else if (id == NotificationCenter.needSetDayNightTheme) {
            clearAllHiddenFragments();
        }
    }

    /* Just For Test */

    //private final Runnable justForTestR = this::justForTest;

    //private void justForTest() {
    //    getUserConfig().setShowCallsTab(!getUserConfig().showCallsTab);
    //    NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.callTabsVisibleToggled);
    //    AndroidUtilities.cancelRunOnUIThread(justForTestR);
    //    AndroidUtilities.runOnUIThread(justForTestR, 3000);
    //}



    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.fileLoadFailed);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.notificationsCountUpdated);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.appUpdateAvailable);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.appUpdateLoading);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.needSetDayNightTheme);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoaded);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoadProgressChanged);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.fileLoadFailed);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.notificationsCountUpdated);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.appUpdateAvailable);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.appUpdateLoading);
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.needSetDayNightTheme);

        super.onFragmentDestroy();
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        super.onTransitionAnimationEnd(isOpen, backward);
        if (isOpen) {
            org.telegram.divo.common.utils.DivoDeeplinkDispatcher.INSTANCE.processPendingDeeplink(parentLayout);
        }
    }

    @Override
    public void onFactorChanged(int id, float factor, float fraction, FactorAnimator callee) {
        if (id == ANIMATOR_ID_TABS_VISIBLE) {
            checkUi_tabsPosition();
            checkUi_fadeView();
        }
    }

    private void checkUi_fadeView() {
        if (viewPager == null || fadeView == null) {
            return;
        }

        final float alpha = animatorTabsVisible.getFloatValue();

        fadeView.setAlpha(alpha);
        fadeView.setTranslationY(0);
        fadeView.setVisibility(alpha > 0 ? View.VISIBLE : View.GONE);
        //DIVO--START
        if (modelsSearchButton != null && bottomBarContainer != null) {
            final boolean canShow = viewPager.getCurrentPosition() == POSITION_MODELS
                    && (isModelsSearchVisible || getCurrentVisibleFragment() instanceof FragmentModels);

            final float searchAlpha = canShow ? alpha : 0f;
            modelsSearchButton.setAlpha(searchAlpha);
            modelsSearchButton.setVisibility(searchAlpha > 0 ? View.VISIBLE : View.GONE);
            modelsSearchButton.setTranslationY(lerp(dp(28), 0, searchAlpha));
            modelsSearchButton.setClickable(searchAlpha >= 1f);
        }

        checkUi_bottomBarLayout();
        if (bottomBarContainer != null) {
            bottomBarContainer.requestLayout();
        }
        //DIVO--END
    }

    private void checkUi_tabsPosition() {
        final boolean isUpdateLayoutVisible = updateLayoutWrapper.isUpdateLayoutVisible();
        final int updateLayoutHeight = isUpdateLayoutVisible ? dp(UpdateLayoutWrapper.HEIGHT) : 0;
        final int normalY = -(navigationBarHeight + updateLayoutHeight);
        final int hiddenY = normalY + dp(40);

        final float factor = animatorTabsVisible.getFloatValue();
        final float scale = lerp(0.85f, 1f, factor);

        //DIVO--START
        bottomBarContainer.setTranslationY(lerp(hiddenY, normalY, factor));
        bottomBarContainer.setScaleX(scale);
        bottomBarContainer.setScaleY(scale);
        bottomBarContainer.setClickable(factor > 1);
        bottomBarContainer.setEnabled(factor > 1);
        bottomBarContainer.setAlpha(factor);
        bottomBarContainer.setVisibility(factor > 0 ? View.VISIBLE : View.GONE);

        checkUi_bottomBarLayout();
    }

    private void checkUi_bottomBarLayout() {
        if (bottomBarContainer == null || tabsView == null || modelsSearchButton == null || contentView == null) {
            return;
        }

        final int contentWidth = contentView.getMeasuredWidth();
        if (contentWidth == 0) return;

        final float searchAlpha = modelsSearchButton.getAlpha();
        final boolean isSearchVisible = searchAlpha > 0f;

        final int minMargin = dp(12);
        final int availableWidth = contentWidth - 2 * minMargin;

        int preferredTabsW = isSearchVisible
                ? dp(256) + (int) dp(DialogsActivity.MAIN_TABS_MARGIN * 2)
                : dp(328) + (int) dp(DialogsActivity.MAIN_TABS_MARGIN * 2);

        int currentTabsW = preferredTabsW;

        if (preferredTabsW > availableWidth) {
            currentTabsW = availableWidth;
            currentTabsW = Math.max(currentTabsW, dp(220));
        }

        tabsView.setMaxWidth(currentTabsW);

        if (isSearchVisible) {
            tabsView.setMinWidth(currentTabsW);
        } else {
            tabsView.setMinWidth(-1);
        }

        final int tabsPadding = dp(DialogsActivity.MAIN_TABS_MARGIN + 4);
        tabsView.setPadding(tabsPadding, tabsPadding, tabsPadding, tabsPadding);

        tabsView.requestLayout();
        bottomBarContainer.requestLayout();
        //DIVO--END
    }



    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = super.getThemeDescriptions();

        ThemeDescription.ThemeDescriptionDelegate cellDelegate = this::blur3_updateColors;
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_dialogBackground));

        return themeDescriptions;
    }

    /* * */

    private class MainTabsActivityControllerImpl implements MainTabsActivityController {
        @Override
        public void setTabsVisible(boolean visible) {
            animatorTabsVisible.setValue(visible, true);
        }
        //DIVO--START
        @Override
        public void setModelsSearchVisible(boolean visible) {
            isModelsSearchVisible = visible;
            checkUi_fadeView();
        }
        //DIVO--END
    }


    /* Slide */

    @Override
    public boolean canBeginSlide() {
        final BaseFragment fragment = getCurrentVisibleFragment();
        return fragment != null && fragment.canBeginSlide();
    }

    @Override
    public void onBeginSlide() {
        super.onBeginSlide();
        final BaseFragment fragment = getCurrentVisibleFragment();
        if (fragment != null) {
            fragment.onBeginSlide();
        }
    }

    @Override
    public void onSlideProgress(boolean isOpen, float progress) {
        final BaseFragment fragment = getCurrentVisibleFragment();
        if (fragment != null) {
            fragment.onSlideProgress(isOpen, progress);
        }
    }

    @Override
    public Animator getCustomSlideTransition(boolean topFragment, boolean backAnimation, float distanceToMove) {
        final BaseFragment fragment = getCurrentVisibleFragment();
        return fragment != null ? fragment.getCustomSlideTransition(topFragment, backAnimation, distanceToMove) : null;
    }

    @Override
    public void prepareFragmentToSlide(boolean topFragment, boolean beginSlide) {
        final BaseFragment fragment = getCurrentVisibleFragment();
        if (fragment != null) {
            fragment.prepareFragmentToSlide(topFragment, beginSlide);
        }
    }



    /* * */

    private final @NonNull BlurredBackgroundSourceColor iBlur3SourceColor;
    private final @Nullable BlurredBackgroundSourceRenderNode iBlur3SourceTabGlass;

    private final RectF fragmentPosition = new RectF();
    private void blur3_invalidateBlur() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || iBlur3SourceTabGlass == null || fragmentView == null) {
            return;
        }

        final int width = fragmentView.getMeasuredWidth();
        final int height = fragmentView.getMeasuredHeight();

        iBlur3SourceTabGlass.setSize(width, height);
        iBlur3SourceTabGlass.updateDisplayListIfNeeded();
    }

    private void blur3_updateColors() {
        iBlur3SourceColor.setColor(0xFFFFFFFF); // Force White
        tabsViewBackground.updateColors();
        blur3_invalidateBlur();
        fadeView.invalidate();
        tabsView.invalidate();
        for (GlassTabView tabView : tabs) {
            tabView.updateColorsLottie();
        }
    }
}
