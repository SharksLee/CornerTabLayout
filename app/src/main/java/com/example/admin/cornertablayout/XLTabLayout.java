package com.example.admin.cornertablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

 /**
  * 方块的Tablayout
   *@author lishaojie
   *create at 2018/7/9 16:27
  */
public class XLTabLayout extends LinearLayout {


    /**
     * tab选项卡的宽度
     */
    protected int mTabWidth;
    /**
     * 指示器已经移动的位置
     */
    protected int mIndicatorTravelOffset;
    /**
     * 关联的内容页面
     */
    protected ViewPager mViewPager;
    /**
     * 默认颜色
     */
    protected int mNormalColor;
    /**
     * 默认背景颜色
     */
    protected Drawable mBgNormalColor;
    /**
     * 默认背景颜色
     */
    protected Drawable mBgSelectedColor;
    /**
     * 选中颜色
     */
    protected int mSelectedColor;
    protected int mCurrentPosition;
    protected ArrayList<String> dataList;
    /**
     * 指示器的画笔
     */
    private Paint mPaint;
    /**
     * 指示器颜色
     */
    private int mIndicatorColor;
    /**
     * 文本的尺寸
     */
    private int mTextSize;
    /**
     * 指示器绘图位置
     */
    private int mIndicatorHeight;
    private int mIndicatorVerticalPos = 0;
    /**
     * 圆点的索引
     */
    /**
     * 圆点颜色
     */
    private float mLastClickX;
    private float mLastClickY;
    private ITabClickListener mTabClickListener;

    public XLTabLayout(Context context) {
        this(context, null);
    }

    public XLTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(HORIZONTAL);
        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.XLTabLayoutV2);
        mNormalColor = attrArray.getColor(R.styleable.XLTabLayoutV2_text_normal_color, getResources().getColor(R.color.gray_darkest));
        mSelectedColor = attrArray.getColor(R.styleable.XLTabLayoutV2_text_selected_color, getResources().getColor(R.color.orange));
        mIndicatorColor = attrArray.getColor(R.styleable.XLTabLayoutV2_tab_indicator_color, mSelectedColor);
        mTextSize = (int) attrArray.getDimension(R.styleable.XLTabLayoutV2_text_size, DisplayUtil.sp2px(14));
        mBgNormalColor = attrArray.getDrawable(R.styleable.XLTabLayoutV2_text_bg_normal_color);
        mBgSelectedColor = attrArray.getDrawable(R.styleable.XLTabLayoutV2_text_bg_selected_color);
        mTabWidth = (int) attrArray.getDimension(R.styleable.XLTabLayoutV2_xlTab_width, -1);
        mIndicatorHeight = (int) attrArray.getDimension(R.styleable.XLTabLayoutV2_tab_indicator_height, DisplayUtil.dip2px(2));
        attrArray.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        setWillNotDraw(false);
    }

    /**
     * 绑定tab选项卡的数据
     *
     * @param tabs 标题栏数据
     */
    protected void bindData(List<String> tabs) {
        if (CollectionUtil.isEmpty(tabs)) {
            return;
        }

        removeAllViews();

        if (mTabWidth <= 0) {
            mTabWidth = DisplayUtil.getScreenWidth() / tabs.size();
        }

        LayoutParams lp = new LayoutParams(mTabWidth, LayoutParams.MATCH_PARENT);
        TypedArray typedArray = null;
        for (String title : tabs) {
            TextView tv = new TextView(getContext());
            tv.setText(title);
            tv.setSingleLine(true);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setBackgroundResource(R.drawable.selector_transparent_gray_circle);
            initTextAttribute(tv, lp);
            addView(tv, lp);
        }
        if (typedArray != null) {
            typedArray.recycle();
        }

        setItemClickEvent();
    }

    protected void initTextAttribute(TextView tv, LayoutParams lp) {
        tv.setClickable(true);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        tv.setTextColor(mNormalColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicatorVerticalPos(canvas);
    }

    /**
     * 绘制指示器
     */
    protected void drawIndicatorVerticalPos(Canvas canvas) {
        if (mIndicatorVerticalPos == 0) {
            //+1是为了避免底部有空隙存在
            mIndicatorVerticalPos = getHeight() + 1 - mIndicatorHeight / 2;
        }

        //画指示器
        mPaint.setColor(mIndicatorColor);
        mPaint.setStrokeWidth(mIndicatorHeight);
        canvas.drawLine(mIndicatorTravelOffset, mIndicatorVerticalPos, mTabWidth + mIndicatorTravelOffset, mIndicatorVerticalPos, mPaint);

        canvas.save();
    }



    /**
     * 设置关联的ViewPager
     *
     * @param viewPager
     */
    public void setupWithViewPager(ViewPager viewPager) {
        switchViewPager(viewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                if (isEnabled()) {
                    highLightTextView(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scroll(position, positionOffset);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 已经绑定过多个ViewPager后，切换ViewPager时用
     *
     * @param viewPager
     */
    public void switchViewPager(ViewPager viewPager) {
        if (viewPager == null) {
            return;
        }

        mViewPager = viewPager;
        PagerAdapter mPagerAdapter = mViewPager.getAdapter();

        final int adapterCount = mPagerAdapter.getCount();

        dataList = new ArrayList<>(adapterCount);

        for (int i = 0; i < adapterCount; i++) {
            String title = mPagerAdapter.getPageTitle(i) == null ? "" : mPagerAdapter.getPageTitle(i).toString();
            dataList.add(title);
        }

        bindData(dataList);
        int currentPos = viewPager.getCurrentItem();
        mViewPager.setCurrentItem(currentPos);
        mCurrentPosition = currentPos;

        highLightTextView(currentPos);
        scroll(currentPos, 0);
    }

    /**
     * 重置TAB文本颜色
     */
    public void resetTextViewColor() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(mNormalColor);
                if (mBgNormalColor != null) {
                    view.setBackgroundDrawable(mBgNormalColor);
                }

            }
        }
    }

    /**
     * 高亮某个Tab的文本
     *
     * @param pos
     */
    public void highLightTextView(int pos) {
        resetTextViewColor();
        View view = getChildAt(pos);
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(mSelectedColor);
            if (mBgNormalColor != null) {
                view.setBackgroundDrawable(mBgSelectedColor);
            }
            //根据选中文本 移动tabLayout
            moveLayout(((TextView) view));
        }


    }

    private void moveLayout(TextView v) {
        if (!(getParent() instanceof HorizontalScrollView)) return;


        HorizontalScrollView mParentView = (HorizontalScrollView) getParent();
        mParentView.setOverScrollMode(OVER_SCROLL_NEVER);


        if (mParentView.getScrollX() + DisplayUtil.getScreenWidth() - v.getWidth() < v.getX()) {
            int scrollToX = (int) (v.getX() - DisplayUtil.getScreenWidth() + v.getWidth());
            mParentView.smoothScrollTo(scrollToX > 0 ? scrollToX : 0, 0);
        }

        if (mParentView.getScrollX() > v.getX()) {
            mParentView.smoothScrollTo((int) v.getX(), 0);
        }
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }


    /**
     * 设置Tab的点击事件
     */
    private void setItemClickEvent() {
        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickTab(j);
                }
            });
        }
    }

    protected void onClickTab(int j) {
        if (mTabClickListener != null) {
            mTabClickListener.onTabClicked(j, mLastClickX, mLastClickY);
        }
        if (mViewPager != null)
            mViewPager.setCurrentItem(j);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mLastClickX = ev.getX();
            mLastClickY = ev.getY();
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 指示器跟随手指进行滚动
     *
     * @param position
     * @param offset
     */
    public void scroll(int position, float offset) {
        mIndicatorTravelOffset = (int) (mTabWidth * (offset + position));
        invalidate();
    }

    public void setTabClickListener(ITabClickListener tabClickListener) {
        mTabClickListener = tabClickListener;
    }

    public void removeViewAtPosition(int index, boolean isNeedReset) {
        if (isNeedReset) {

        }
        if (index < getChildCount()) {
            if (isNeedReset) {
                mTabWidth = 0;
                dataList.remove(index);
                bindData(dataList);
            } else {
                removeViewAt(index);
            }

        }

    }

    public void resetTabWidth() {
        mTabWidth = 0;
    }


    public interface ITabClickListener {
        void onTabClicked(int position, float x, float y);
    }
}
