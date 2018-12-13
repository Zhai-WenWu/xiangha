package amodule.lesson.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class VerticalViewPager extends ViewPager {

    private double transform;
    public int showPosition;
    private float mDownPosX;
    private float mDownPosY;
    private float deltaY;
    int dealtX = 0;
    int dealtY = 0;
    int lastX = 0;
    int lastY = 0;
    boolean intercepted;

    public int getShowPosition() {
        return showPosition;
    }

    public VerticalViewPager(Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setPageTransformer(true, new VerticalPageTransformer());
        setOverScrollMode(OVER_SCROLL_NEVER);
        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mOnScrollDistance.scrollDistance(positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                showPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mOnScrollDistance.scrollEnd(state);
            }
        });
    }

    public void setScale(double scale) {
        transform = (1 - scale);
    }

    private class VerticalPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {
            if ((position > -transform && position < 0) || position > (1 - transform)) {
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);
            } else if (position > 0 && position <= 1 - transform) {
                float yPosition = (float) ((1 - transform) * view.getHeight());
                view.setTranslationY(yPosition);
            } else if (position <= -transform) {
                float yPosition = (float) (-transform * view.getHeight());
                view.setTranslationY(yPosition);
            }
            view.setTranslationX(view.getWidth() * -position);
        }
    }

    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;
        ev.setLocation(newX, newY);
        return ev;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        super.onInterceptTouchEvent(swapXY(ev));
        int y = (int) ev.getX();
        int x = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dealtX = x;
                dealtY = y;
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                lastX = Math.abs(x - dealtX);
                lastY = Math.abs(y - dealtY);
                int y2 = y - dealtY;
                if (lastY > lastX && ((getShowPosition() == 0) || getShowPosition() == 1 && y2 > 0 && mOnWebScrollTop != null && mOnWebScrollTop.canScroll())) {
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
        }
        swapXY(ev);
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(swapXY(ev));
    }

    private OnWebScrollTop mOnWebScrollTop;

    public void setWebScrollTop(OnWebScrollTop mOnWebScrollTop) {
        this.mOnWebScrollTop = mOnWebScrollTop;
    }

    public interface OnWebScrollTop {
        boolean canScroll();
    }

    private OnScrollDistance mOnScrollDistance;

    public void setScrollDistance(OnScrollDistance onScrollDistance) {
        this.mOnScrollDistance = onScrollDistance;
    }

    public interface OnScrollDistance {
        void scrollDistance(float distance);
        void scrollEnd(int state);
    }

}
