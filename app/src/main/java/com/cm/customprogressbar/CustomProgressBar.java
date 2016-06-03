package com.cm.customprogressbar;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Cmad on 2016/6/1.
 */
public class CustomProgressBar extends View {

    private static final int DEFAULT_ITEM_WIDTH = 40;
    private static final int DEFAULT_ITEM_HEIGHT = 40;

    private Drawable mItemDrawable;
    private ArgbEvaluator mArgbEvaluator;
    private int mStartColor = Color.WHITE;
    private int mEndColor = Color.RED;
    private float mProgress ;
    private int mItemWidth;
    private int mItemCount;


    /**
     * 是否固定item的宽度,如果固定则不会去平均整个progressBar的宽度
     */
    private boolean mIsFixationItemWidth;

    /**
     * 是否整个ProgressBar渐变,true为整个Progress渐变,false为当前进度渐变
     */
    private boolean mIsFullGradient ;

    /**
     * 是否渐变
     */
    private boolean mIsGradient ;

    private Rect mDrawableRect;
    private Rect mClipRect;


    public CustomProgressBar(Context context) {
        super(context);
        init();
    }

    public CustomProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public CustomProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    private void initAttr(AttributeSet attrs) {
        TypedArray attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.CustomProgressBar);
        mProgress = attrArray.getFraction(R.styleable.CustomProgressBar_progress,1,1,0);
        mStartColor = attrArray.getColor(R.styleable.CustomProgressBar_startColor,Color.WHITE);
        mEndColor = attrArray.getColor(R.styleable.CustomProgressBar_endColor,Color.RED);
        mItemWidth = (int) attrArray.getDimension(R.styleable.CustomProgressBar_itemWidth,0);
        mIsFullGradient = attrArray.getBoolean(R.styleable.CustomProgressBar_isFullGradient,true);
        mIsFixationItemWidth = attrArray.getBoolean(R.styleable.CustomProgressBar_isFixationItemWidth,false);
        mItemDrawable = attrArray.getDrawable(R.styleable.CustomProgressBar_itemDrawable);
        mIsGradient = attrArray.getBoolean(R.styleable.CustomProgressBar_isGradient,true);
        mItemCount = attrArray.getInteger(R.styleable.CustomProgressBar_itemCount,0);
        attrArray.recycle();
    }

    private void init() {
        if(mItemDrawable == null){
            mItemDrawable = getContext().getResources().getDrawable(R.drawable.progressbar_item_drawable);
        }
        initItemWidth();
        mArgbEvaluator = new ArgbEvaluator();
        mDrawableRect = new Rect();
        mClipRect = new Rect();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);


        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            if (mItemCount <= 0 || mItemWidth <= 0) {
                setMeasuredDimension(width, height);
            } else {
                width = (mItemWidth * mItemCount + getWidthGap());
                height = mItemDrawable.getIntrinsicHeight() + getHeightGap();
                setMeasuredDimension(width, height);
            }

        } else if (widthMode == MeasureSpec.AT_MOST) {
            if (mItemCount != 0) {
                width = mItemWidth*mItemCount + getWidthGap();
            }
            setMeasuredDimension(width, height);

        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = mItemDrawable.getIntrinsicHeight() + getHeightGap();

            setMeasuredDimension(width, height);

        } else {
            setMeasuredDimension(width, height);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mItemWidth = getItemWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable bg = getBackground();
        if(bg != null){
            bg.draw(canvas);
        }

        int count = 0;
        int indexColor = mStartColor;
        int index = getPaddingLeft();

        float gradientWidth = mIsFullGradient ? getRealWidth() : getRealWidth() * mProgress;
        int n = (int) (gradientWidth / mItemWidth);
        while (index < getPaddingLeft()+gradientWidth){
            count ++;
            mDrawableRect.set(index,getPaddingTop(),index+mItemWidth,getHeight()-getPaddingBottom());
            mItemDrawable.setBounds(mDrawableRect);
            int color = drawGradient(count, indexColor, n);
            mItemDrawable.draw(canvas);
            index += mItemWidth;
            indexColor = color;
        }

        //裁剪,绘制进度背景覆盖不需要显示的进度
        mClipRect.set((int)(getPaddingLeft()+getRealWidth()*mProgress),0,getWidth(),getHeight());
        canvas.clipRect(mClipRect);
        if(bg != null){
            bg.draw(canvas);
        }

    }

    /**
     * 绘制Drawable渐变
     * @param count 整体item的个数
     * @param indexColor 当前渐变开始色
     * @param n 当前item个数
     * @return
     */
    private int drawGradient(int count, int indexColor, int n) {
        GradientDrawable drawable;
        int color = 0;
        //判断当前Drawable是否是渐变的Drawable,且允许渐变
        if(mItemDrawable instanceof  GradientDrawable && mIsGradient){
            drawable = (GradientDrawable) mItemDrawable;
            float f = 1.0f*count / n;
            color = (int) mArgbEvaluator.evaluate(f,mStartColor,mEndColor);
            drawable.setColors(new int[]{indexColor,color});
        }

        return color;
    }

    private void initItemWidth(){
        //mItemWidth的优先级别大于mItemDrawable的宽度
        mItemWidth = mItemWidth <= 0 ? mItemDrawable.getIntrinsicWidth() : mItemWidth;
    }

    /**
     * 计算每个item的宽度
     * @return
     */
    private int getItemWidth() {


        int dw = mItemWidth ;

        //mItemCount的优先级别大于mItemWidth;
        if(mItemCount > 0){
            dw = getRealWidth() / mItemCount;
        }

        //微调item宽度使其平均整体宽度
        while (getRealWidth() % dw != 0 && !mIsFixationItemWidth){
            dw ++;
        }
        return dw;
    }

    private int getRealWidth(){
        return getWidth() - getWidthGap();
    }


    private int getWidthGap(){
        return getPaddingLeft() + getPaddingRight();
    }

    private int getHeightGap(){
        return getPaddingTop() + getPaddingBottom();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float mProgress) {
        this.mProgress = mProgress;
        invalidate();
    }

    public int getStartColor() {
        return mStartColor;
    }

    public void setStartColor(int startColor) {
        this.mStartColor = startColor;
        invalidate();
    }

    public int getEndColor() {
        return mEndColor;
    }

    public void setEndColor(int endColor) {
        this.mEndColor = endColor;
        invalidate();
    }

    public int getmItemWidth() {
        return mItemWidth;
    }

    public void setItemWidth(int itemWidth) {
        this.mItemWidth = itemWidth;
        invalidate();
    }

    public Drawable getItemDrawable() {
        return mItemDrawable;
    }

    public void setItemDrawable(Drawable mItemDrawable) {
        this.mItemDrawable = mItemDrawable;
    }

    public boolean isFixationItemWidth() {
        return mIsFixationItemWidth;
    }

    public void setIsFixationItemWidth(boolean mIsFixationItemWidth) {
        this.mIsFixationItemWidth = mIsFixationItemWidth;
    }

    public boolean isFullGradient() {
        return mIsFullGradient;
    }

    public void setIsFullGradient(boolean mIsFullGradient) {
        this.mIsFullGradient = mIsFullGradient;
    }

    public boolean isGradient() {
        return mIsGradient;
    }

    public void setIsGradient(boolean mIsGradient) {
        this.mIsGradient = mIsGradient;
    }
}
