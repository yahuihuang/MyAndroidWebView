package com.myyhhuang.webview.config;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * WebView進度條，原作者: cenxiaozhong，在此基礎上修改優化：
 * 1. progress同時返回兩次100時進度條出現兩次
 * 2. 當一條進度沒跑完，又點擊其他鏈接開始第二次進度時，第二次進度不出現
 * 3. 修改消失動畫時長，使其消失時看到可以進度跑完
 */
public class WebProgress extends FrameLayout {

    /**
     * 默認勻速動畫最大的時長
     */
    public static final int MAX_UNIFORM_SPEED_DURATION = 8 * 1000;
    /**
     * 默認加速後減速動畫最大時長
     */
    public static final int MAX_DECELERATE_SPEED_DURATION = 450;
    /**
     * 95f-100f時，透明度1f-0f時長
     */
    public static final int DO_END_ALPHA_DURATION = 630;
    /**
     * 95f - 100f動畫時長
     */
    public static final int DO_END_PROGRESS_DURATION = 500;
    /**
     * 當前勻速動畫最大的時長
     */
    private static int CURRENT_MAX_UNIFORM_SPEED_DURATION = MAX_UNIFORM_SPEED_DURATION;
    /**
     * 當前加速後減速動畫最大時長
     */
    private static int CURRENT_MAX_DECELERATE_SPEED_DURATION = MAX_DECELERATE_SPEED_DURATION;
    /**
     * 默認的高度(dp)
     */
    public static int WEB_PROGRESS_DEFAULT_HEIGHT = 3;
    /**
     * 進度條顏色默認
     */
    public static String WEB_PROGRESS_COLOR = "#2483D9";
    /**
     * 進度條顏色
     */
    private int mColor;
    /**
     * 進度條的畫筆
     */
    private Paint mPaint;
    /**
     * 進度條動畫
     */
    private Animator mAnimator;
    /**
     * 控件的寬度
     */
    private int mTargetWidth = 0;
    /**
     * 控件的高度
     */
    private int mTargetHeight;
    /**
     * 標誌當前進度條的狀態
     */
    private int TAG = 0;
    /**
     * 第一次過來進度show，後面就是setProgress
     */
    private boolean isShow = false;
    public static final int UN_START = 0;
    public static final int STARTED = 1;
    public static final int FINISH = 2;
    private float mCurrentProgress = 0F;

    public WebProgress(Context context) {
        this(context, null);
    }

    public WebProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mPaint = new Paint();
        mColor = Color.parseColor(WEB_PROGRESS_COLOR);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);

        mTargetWidth = context.getResources().getDisplayMetrics().widthPixels;
        mTargetHeight = dip2px(WEB_PROGRESS_DEFAULT_HEIGHT);
    }

    /**
     * 設置單色進度條
     */
    public void setColor(int color) {
        this.mColor = color;
        mPaint.setColor(color);
    }

    public void setColor(String color) {
        this.setColor(Color.parseColor(color));
    }

    public void setColor(int startColor, int endColor) {
        LinearGradient linearGradient = new LinearGradient(0, 0, mTargetWidth, mTargetHeight, startColor, endColor, Shader.TileMode.CLAMP);
        mPaint.setShader(linearGradient);
    }

    /**
     * 設置漸變色進度條
     *
     * @param startColor 開始顏色
     * @param endColor   結束顏色
     */
    public void setColor(String startColor, String endColor) {
        this.setColor(Color.parseColor(startColor), Color.parseColor(endColor));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);

        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        if (wMode == MeasureSpec.AT_MOST) {
            w = Math.min(w, getContext().getResources().getDisplayMetrics().widthPixels);
        }
        if (hMode == MeasureSpec.AT_MOST) {
            h = mTargetHeight;
        }
        this.setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawRect(0, 0, mCurrentProgress / 100 * (float) this.getWidth(), this.getHeight(), mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mTargetWidth = getMeasuredWidth();
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        if (mTargetWidth >= screenWidth) {
            CURRENT_MAX_DECELERATE_SPEED_DURATION = MAX_DECELERATE_SPEED_DURATION;
            CURRENT_MAX_UNIFORM_SPEED_DURATION = MAX_UNIFORM_SPEED_DURATION;
        } else {
            //取比值
            float rate = this.mTargetWidth / (float) screenWidth;
            CURRENT_MAX_UNIFORM_SPEED_DURATION = (int) (MAX_UNIFORM_SPEED_DURATION * rate);
            CURRENT_MAX_DECELERATE_SPEED_DURATION = (int) (MAX_DECELERATE_SPEED_DURATION * rate);
        }
    }

    private void setFinish() {
        isShow = false;
        TAG = FINISH;
    }

    private void startAnim(boolean isFinished) {

        float v = isFinished ? 100 : 95;

        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
        mCurrentProgress = mCurrentProgress == 0 ? 0.00000001f : mCurrentProgress;
        // 可能由於透明度造成突然出現的問題
        setAlpha(1);

        if (!isFinished) {
            ValueAnimator mAnimator = ValueAnimator.ofFloat(mCurrentProgress, v);
            float residue = 1f - mCurrentProgress / 100 - 0.05f;
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration((long) (residue * CURRENT_MAX_UNIFORM_SPEED_DURATION));
            mAnimator.addUpdateListener(mAnimatorUpdateListener);
            mAnimator.start();
            this.mAnimator = mAnimator;
        } else {

            ValueAnimator segment95Animator = null;
            if (mCurrentProgress < 95) {
                segment95Animator = ValueAnimator.ofFloat(mCurrentProgress, 95);
                float residue = 1f - mCurrentProgress / 100f - 0.05f;
                segment95Animator.setInterpolator(new LinearInterpolator());
                segment95Animator.setDuration((long) (residue * CURRENT_MAX_DECELERATE_SPEED_DURATION));
                segment95Animator.setInterpolator(new DecelerateInterpolator());
                segment95Animator.addUpdateListener(mAnimatorUpdateListener);
            }

            ObjectAnimator mObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
            mObjectAnimator.setDuration(DO_END_ALPHA_DURATION);
            ValueAnimator mValueAnimatorEnd = ValueAnimator.ofFloat(95f, 100f);
            mValueAnimatorEnd.setDuration(DO_END_PROGRESS_DURATION);
            mValueAnimatorEnd.addUpdateListener(mAnimatorUpdateListener);

            AnimatorSet mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(mObjectAnimator, mValueAnimatorEnd);

            if (segment95Animator != null) {
                AnimatorSet mAnimatorSet1 = new AnimatorSet();
                mAnimatorSet1.play(mAnimatorSet).after(segment95Animator);
                mAnimatorSet = mAnimatorSet1;
            }
            mAnimatorSet.addListener(mAnimatorListenerAdapter);
            mAnimatorSet.start();
            mAnimator = mAnimatorSet;
        }

        TAG = STARTED;
    }

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float t = (float) animation.getAnimatedValue();
            WebProgress.this.mCurrentProgress = t;
            WebProgress.this.invalidate();
        }
    };

    private AnimatorListenerAdapter mAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            doEnd();
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        /**
         * animator cause leak , if not cancel;
         */
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    private void doEnd() {
        if (TAG == FINISH && mCurrentProgress == 100) {
            setVisibility(GONE);
            mCurrentProgress = 0f;
            this.setAlpha(1f);
        }
        TAG = UN_START;
    }

    public void reset() {
        mCurrentProgress = 0;
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
        }
    }

    public void setProgress(int newProgress) {
        setProgress(Float.valueOf(newProgress));
    }


    public LayoutParams offerLayoutParams() {
        return new LayoutParams(mTargetWidth, mTargetHeight);
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public WebProgress setHeight(int heightDp) {
        this.mTargetHeight = dip2px(heightDp);
        return this;
    }

    public void setProgress(float progress) {
        // fix 同时返回两个 100，产生两次进度条的问题；
        if (TAG == UN_START && progress == 100) {
            setVisibility(View.GONE);
            return;
        }

        if (getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
        }
        if (progress < 95) {
            return;
        }
        if (TAG != FINISH) {
            startAnim(true);
        }
    }

    /**
     * 顯示進度條
     */
    public void show() {
        isShow = true;
        setVisibility(View.VISIBLE);
        mCurrentProgress = 0f;
        startAnim(false);
    }

    /**
     * 進度完成後消失
     */
    public void hide() {
        setWebProgress(100);
    }

    /**
     * 為單獨處理WebView進度條
     */
    public void setWebProgress(int newProgress) {
        if (newProgress >= 0 && newProgress < 95) {
            if (!isShow) {
                show();
            } else {
                setProgress(newProgress);
            }
        } else {
            setProgress(newProgress);
            setFinish();
        }
    }
}

