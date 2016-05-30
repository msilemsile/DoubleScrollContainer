package com.msile.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * 上下翻页 包含两个view视图
 * create 5/29 2016
 *
 * @author msile
 */
public class DoubleScrollContainer extends ScrollView {

    private int mTopContentHeight;                          //可滑动TopView的高度
    private int mBottomContentHeight;                       //可滑动BottomView的高度
    private int mTopOffset;                                 //TopView偏移量
    private int mBottomOffset;                              //BottomView偏移量
    private int mCanScrollNextHeight = 100;                 //可滑动到下一个view的高度
    private View mScrollTopView;
    private View mScrollBottomView;
    private View mCurrentScrollView;                        //当前滚动的view
    private int mScrollState = SCROLL_TOP_VIEW;             //当前滑动的状态
    public static final int SCROLL_TOP_VIEW = 0;
    public static final int SCROLL_BOTTOM_VIEW = 1;
    private DoubleScrollListener listener;
    private boolean canPullUp, canPullDown;                 //是否可以上拉、下拉
    private float yDown;                                    //触摸Y轴坐标
    private boolean hasFixedInnerScroll;

    public DoubleScrollContainer(Context context) {
        this(context, null);
    }

    public DoubleScrollContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleScrollContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray t = context.getResources().obtainAttributes(attrs, R.styleable.DoubleScrollContainer);
        int contentTopHeight = 0;
        int contentBottomHeight = 0;
        int commonHeight = 0;
        try {
            commonHeight = t.getDimensionPixelSize(R.styleable.DoubleScrollContainer_content_common_height, 0);
            contentTopHeight = t.getDimensionPixelSize(R.styleable.DoubleScrollContainer_content_top_height, 0);
            mTopOffset = t.getDimensionPixelSize(R.styleable.DoubleScrollContainer_content_top_offset, 0);
            contentBottomHeight = t.getDimensionPixelSize(R.styleable.DoubleScrollContainer_content_bottom_height, 0);
            mBottomOffset = t.getDimensionPixelSize(R.styleable.DoubleScrollContainer_content_bottom_offset, 0);
            mCanScrollNextHeight = t.getDimensionPixelSize(R.styleable.DoubleScrollContainer_scroll_next_height, 0);
        } finally {
            t.recycle();
        }
        mCanScrollNextHeight = mCanScrollNextHeight > 0 ? mCanScrollNextHeight : 100;
        mTopContentHeight = contentTopHeight > 0 ? (contentTopHeight - mTopOffset) : (commonHeight - mTopOffset);
        mBottomContentHeight = contentBottomHeight > 0 ? (contentBottomHeight - mBottomOffset) : (commonHeight - mBottomOffset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setNestedScrollingEnabled(false);
        }
    }

    /**
     * 设置上下视图的高度
     */
    public void setContentHeight(int topHeight, int bottomHeight) {
        this.mTopContentHeight = topHeight;
        if (mScrollTopView != null) {
            mScrollTopView.getLayoutParams().height = topHeight;
        }
        this.mBottomContentHeight = bottomHeight;
        if (mScrollBottomView != null) {
            mScrollBottomView.getLayoutParams().height = bottomHeight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mScrollTopView == null || mScrollBottomView == null) {
            ViewGroup container = (ViewGroup) getChildAt(0);
            if (container.getChildCount() >= 2) {
                mScrollTopView = findViewById(R.id.double_scroll_top_view);
                mScrollBottomView = findViewById(R.id.double_scroll_bottom_view);
                if (mScrollTopView != null && mScrollBottomView != null) {
                    if (mTopContentHeight <= 0) {
                        mTopContentHeight = getMeasuredHeight() - mTopOffset;
                    }
                    if (mBottomContentHeight <= 0) {
                        mBottomContentHeight = getMeasuredHeight() - mBottomOffset;
                    }
                    mScrollTopView.getLayoutParams().height = mTopContentHeight;
                    mScrollBottomView.getLayoutParams().height = mBottomContentHeight;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mScrollTopView.setNestedScrollingEnabled(false);
                        mScrollBottomView.setNestedScrollingEnabled(false);
                    }
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (canPullDown || canPullUp) {
            return super.onInterceptTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mScrollTopView != null && mScrollBottomView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    yDown = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float yDistance = ev.getY() - yDown;
                    if (yDistance > 0) {
                        if (mScrollState == SCROLL_BOTTOM_VIEW) {
                            if (!hasFixedInnerScroll) {
                                getInnerScrollView(mScrollBottomView);
                            }
                            if (mCurrentScrollView != null && !mCurrentScrollView.canScrollVertically(-1)) {
                                canPullDown = true;
                                canPullUp = false;
                            } else {
                                canPullDown = false;
                            }
                        } else {
                            canPullDown = false;
                            canPullUp = false;
                        }
                    } else {
                        if (mScrollState == SCROLL_TOP_VIEW) {
                            if (!hasFixedInnerScroll) {
                                getInnerScrollView(mScrollTopView);
                            }
                            if (mCurrentScrollView != null && !mCurrentScrollView.canScrollVertically(1)) {
                                canPullUp = true;
                                canPullDown = false;
                            } else {
                                canPullUp = false;
                            }
                        } else {
                            canPullUp = false;
                            canPullDown = false;
                        }
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 获取子view的滚动视图
     */
    private void getInnerScrollView(View view) {
        if (view instanceof ViewPager) {
            getViewPagerInnerScroll((ViewPager) view);
        } else {
            mCurrentScrollView = view;
        }
    }

    /**
     * 获取子view是ViewPager里面的滚动视图
     */
    private void getViewPagerInnerScroll(ViewPager viewPager) {
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter != null) {
            if (adapter instanceof FragmentPagerAdapter) {
                FragmentPagerAdapter pagerAdapter = (FragmentPagerAdapter) adapter;
                Fragment item = (Fragment) pagerAdapter.instantiateItem(viewPager,
                        viewPager.getCurrentItem());
                mCurrentScrollView = item.getView().findViewById(R.id.double_scroll_inner_scroll);
            } else if (adapter instanceof FragmentStatePagerAdapter) {
                FragmentStatePagerAdapter statePagerAdapter = (FragmentStatePagerAdapter) adapter;
                Fragment item = (Fragment) statePagerAdapter.instantiateItem(viewPager,
                        viewPager.getCurrentItem());
                mCurrentScrollView = item.getView().findViewById(R.id.double_scroll_inner_scroll);
            }
        } else {
            mCurrentScrollView = viewPager;
        }
    }

    /**
     * 设置当前滚动视图
     */
    public void setCurrentScrollView(View view) {
        this.mCurrentScrollView = view;
        hasFixedInnerScroll = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mScrollTopView != null && mScrollBottomView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    int scrollY = getScrollY();
                    if (mScrollState == SCROLL_TOP_VIEW) {
                        if (scrollY <= mCanScrollNextHeight) {
                            smoothScrollTo(0, 0);
                            if (listener != null) {
                                listener.smoothToTop();
                            }
                        } else {
                            smoothScrollTo(0, mTopContentHeight);
                            mScrollState = SCROLL_BOTTOM_VIEW;
                            if (listener != null) {
                                listener.smoothToBottom();
                            }
                        }
                    } else {
                        int scrollDistance = mBottomContentHeight - scrollY;
                        if (scrollDistance >= mCanScrollNextHeight) {
                            smoothScrollTo(0, 0);
                            mScrollState = SCROLL_TOP_VIEW;
                            if (listener != null) {
                                listener.smoothToTop();
                            }
                        } else {
                            smoothScrollTo(0, mTopContentHeight);
                            if (listener != null) {
                                listener.smoothToBottom();
                            }
                        }
                    }
                    return true;
            }
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 滑动顶部view带动画
     */
    public void smoothToTop() {
        smoothScrollTo(0, 0);
        mScrollState = SCROLL_TOP_VIEW;
    }

    /**
     * 滑到底部view带动画
     */
    public void smoothToBottom() {
        smoothScrollTo(0, mTopContentHeight);
        mScrollState = SCROLL_BOTTOM_VIEW;
    }

    /**
     * 顶部scrollview滑到顶部
     */
    public void scrollContentToTop() {
        smoothScrollTo(0, 0);
        if (mScrollTopView != null) {
            mScrollTopView.scrollTo(0, 0);
        }
        mScrollState = SCROLL_TOP_VIEW;
    }

    public void setDoubleScrollListener(DoubleScrollListener listener) {
        this.listener = listener;
    }

    public interface DoubleScrollListener {
        void smoothToBottom();

        void smoothToTop();
    }
}
