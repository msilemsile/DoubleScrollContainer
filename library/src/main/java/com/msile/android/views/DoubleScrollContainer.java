package com.msile.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * 上下翻页 包含两个view视图
 *
 * @author msile
 */
public class DoubleScrollContainer extends ScrollView {

    private int mTopContentHeight;                         //可滑动topview的高度
    private int mBottomContentHeight;                      //可滑动bottomview的高度
    private int mCanScrollNextHeight = 100;                //可滑动到下一个view的高度
    private View mScrollTopView;
    private View mScrollBottomView;
    private int mCurrentScrollView = SCROLL_TOP_VIEW;       //当前滑动的view
    public static final int SCROLL_TOP_VIEW = 0;
    public static final int SCROLL_BOTTOM_VIEW = 1;
    private DoubleScrollListener listener;
    private boolean canPullUp, canPullDown;                 //是否可以上拉、下拉
    private float yDown;                                    //触摸Y轴坐标

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
            contentBottomHeight = t.getDimensionPixelSize(R.styleable.DoubleScrollContainer_content_bottom_height, 0);
        } finally {
            t.recycle();
        }
        mCanScrollNextHeight = commonHeight / 10;
        mCanScrollNextHeight = mCanScrollNextHeight > 0 ? mCanScrollNextHeight : 100;
        mTopContentHeight = contentTopHeight > 0 ? contentTopHeight : commonHeight;
        mBottomContentHeight = contentBottomHeight > 0 ? contentBottomHeight : commonHeight;
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
                mScrollTopView = container.getChildAt(0);
                mScrollBottomView = container.getChildAt(1);
                if (mTopContentHeight <= 0) {
                    mTopContentHeight = getMeasuredHeight();
                }
                if (mBottomContentHeight <= 0) {
                    mBottomContentHeight = getMeasuredHeight();
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
                        if (mCurrentScrollView == SCROLL_BOTTOM_VIEW) {
                            if (!mScrollBottomView.canScrollVertically(-1)) {
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
                        if (mCurrentScrollView == SCROLL_TOP_VIEW) {
                            if (!mScrollTopView.canScrollVertically(1)) {
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

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mScrollTopView != null && mScrollBottomView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    int scrollY = getScrollY();
                    if (mCurrentScrollView == SCROLL_TOP_VIEW) {
                        if (scrollY <= mCanScrollNextHeight) {
                            smoothScrollTo(0, 0);
                            if (listener != null) {
                                listener.smoothToTop();
                            }
                        } else {
                            smoothScrollTo(0, mTopContentHeight);
                            mCurrentScrollView = SCROLL_BOTTOM_VIEW;
                            if (listener != null) {
                                listener.smoothToBottom();
                            }
                        }
                    } else {
                        int scrollDistance = mBottomContentHeight - scrollY;
                        if (scrollDistance >= mCanScrollNextHeight) {
                            smoothScrollTo(0, 0);
                            mCurrentScrollView = SCROLL_TOP_VIEW;
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
        mCurrentScrollView = SCROLL_TOP_VIEW;
    }

    /**
     * 滑到底部view带动画
     */
    public void smoothToBottom() {
        smoothScrollTo(0, mTopContentHeight);
        mCurrentScrollView = SCROLL_BOTTOM_VIEW;
    }

    /**
     * 顶部scrollview滑到顶部
     */
    public void scrollContentToTop() {
        smoothScrollTo(0, 0);
        if (mScrollTopView != null) {
            mScrollTopView.scrollTo(0, 0);
        }
        mCurrentScrollView = SCROLL_TOP_VIEW;
    }

    public void setDoubleScrollListener(DoubleScrollListener listener) {
        this.listener = listener;
    }

    public interface DoubleScrollListener {
        void smoothToBottom();

        void smoothToTop();
    }
}
