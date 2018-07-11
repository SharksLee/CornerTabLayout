package com.example.admin.cornertablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

/**
 * 圆角的TabLayout
 *
 * @author lishaojie
 * create at 2018/7/9 16:07
 */
public class CornerTabLayout extends LinearLayout {

    private GradientDrawable mIndicatorDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.bg_corners_029b7a);
    private int mViewWidth;
    private int mDrawableId;
    private int mIndicatorDrawableWidth = 0;

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
     * 文本的尺寸
     */
    private int mTextSize;
    private TabOnClickListener mTabClickListener;

    public CornerTabLayout(Context context) {
        this(context, null);
    }

    public CornerTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {

        setOrientation(HORIZONTAL);
        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.CornerTabLayout);
        mNormalColor = attrArray.getColor(R.styleable.CornerTabLayout_text_normal_color, getResources().getColor(R.color.gray_darkest));
        mSelectedColor = attrArray.getColor(R.styleable.CornerTabLayout_text_selected_color, getResources().getColor(R.color.orange));
        mTextSize = (int) attrArray.getDimension(R.styleable.CornerTabLayout_text_size, DisplayUtil.sp2px(14));
        mBgNormalColor = attrArray.getDrawable(R.styleable.CornerTabLayout_text_bg_normal_color);
        mBgSelectedColor = attrArray.getDrawable(R.styleable.CornerTabLayout_text_bg_selected_color);
        mTabWidth = (int) attrArray.getDimension(R.styleable.CornerTabLayout_xlTab_width, -1);
        attrArray.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        setWillNotDraw(false);
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
        initIndicatorDrawable();
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
            mTabWidth = getMeasuredWidth()/ tabs.size();
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
            mTabClickListener.onTabClicked(j);
        }
        if (mViewPager != null)
            mViewPager.setCurrentItem(j);
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
        }


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

    public void setIndicatorDrawableId(int resId) {
        mDrawableId = resId;
        initIndicatorDrawable();
        invalidate();
    }

    public void initIndicatorDrawable() {
        if (mDrawableId > 0) {
            mIndicatorDrawable = (GradientDrawable) getResources().getDrawable(mDrawableId);
            mIndicatorDrawable.setBounds(0, 0, mIndicatorDrawableWidth, getHeight() - getPaddingBottom() - getPaddingTop());
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initIndicatorDrawable();
        initTab();
        if (mIndicatorTravelOffset == 0) {
            scroll(0, 0);
        }
    }


    private void initTab() {

        if (mViewPager == null) return;
        if (mViewWidth == getMeasuredWidth()) return;
        mViewWidth = getMeasuredWidth();
        PagerAdapter mPagerAdapter = mViewPager.getAdapter();
        int adapterCount = mPagerAdapter.getCount();
        List<String> dataList = new ArrayList<>(adapterCount);

        for (int i = 0; i < adapterCount; i++) {
            dataList.add(mPagerAdapter.getPageTitle(i).toString());
        }
        bindData(dataList);

        int currentPos = mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(currentPos);

        highLightTextView(currentPos);
    }


    protected void initTextAttribute(TextView tv, LayoutParams lp) {
        tv.setClickable(true);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        tv.setTextColor(mNormalColor);
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.weight = 1;
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIndicatorVerticalPos(canvas);
    }


    protected void drawIndicatorVerticalPos(Canvas canvas) {
        if (mIndicatorDrawable != null) {
            canvas.save();
            canvas.translate(mIndicatorTravelOffset, getPaddingTop());
            mIndicatorDrawable.draw(canvas);
            canvas.restore();
        }
    }


    public void scroll(int position, float offset) {
        if (getChildCount() == 0) return;
        View selectView = getChildAt(position);
        View nextView = getChildAt(position + 1);
        int width = selectView.getMeasuredWidth();
        if (width == 0) return;
        int nextWidth = nextView == null ? 0 : nextView.getWidth();
        mIndicatorDrawableWidth = (int) ((nextWidth - width) * offset) + width;

        if (mIndicatorDrawable != null)
            mIndicatorDrawable.setBounds(0, 0, mIndicatorDrawableWidth, getHeight() - getPaddingBottom() - getPaddingTop());

        mIndicatorTravelOffset = (int) (selectView.getX() + width * offset);
        invalidate();
    }

    public void setTabClickListener(TabOnClickListener tabClickListener) {
        mTabClickListener = tabClickListener;
    }

    public interface TabOnClickListener {
        void onTabClicked(int position);
    }
}
