package io.github.gregoryconrad.chitchat.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A modified view pager that allows swipes from the second item to the first
 */
public class SelectiveSwipeViewPager extends ViewPager {
    public SelectiveSwipeViewPager(Context context) {
        super(context);
    }

    public SelectiveSwipeViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return getCurrentItem() == 1 && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return getCurrentItem() == 1 && super.onInterceptTouchEvent(event);
    }
}
