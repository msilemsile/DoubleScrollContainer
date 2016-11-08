import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * doublescroll头部滑动view
 */
public class DoubleTopScrollView extends ScrollView {

    private DoubleScrollContainer container;

    public DoubleTopScrollView(Context context) {
        this(context, null);
    }

    public DoubleTopScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    public void setContainer(DoubleScrollContainer container) {
        this.container = container;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (container != null && !container.isCanPullUp() && getChildAt(0).getMeasuredHeight() - t == container.getTopContentHeight()) {
            container.setCanPullUp(true);
        }
    }
}
