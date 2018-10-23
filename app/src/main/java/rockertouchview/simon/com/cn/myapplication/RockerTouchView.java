package rockertouchview.simon.com.cn.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author simon.k
 */
public class RockerTouchView extends View {
    private int fullWidth = 900;//绘图使用的宽,min
    private int fullHeight = 900;//绘图使用的高
    private float innerCenterX;
    private float innerCenterY;
    private float mOutRadius;//大圆半径
    private float mInnerRedius;
    private Paint mOuterPaint;
    private Paint mInnerPaint;

    private float touchDownX;
    private float touchDownY;

    private Bitmap mOuterBgBitmap;
    private Bitmap mRockerPointBitmap, defaultPointBitmap;

    private Paint mOuterBgPaint;
    private Paint mRockerPointPaint, deaultRockerPointPaint;
    private RectF outDst, innerDst, defaultDst;
    private Rect outSrc, innerSrc, defaultSrc;

    private Bitmap mDirectionBmp;
    /**
     * 判断落下位置是否可绘制(边界)
     */
    boolean keepDeading = true;
    /**
     * 触摸箭头模式 ,true 拥有箭头指示
     */
    boolean roockTouchMode = false;

    /**
     * 方向模式 (true:八方向,false:四方向 )
     */
    boolean allDirectionMode = false;

    public RockerTouchView(Context context) {
        this(context, null);
    }

    public RockerTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
        initAttribute(context, attrs);
        initCircleParams();
    }

    private void initCircleParams() {
        mOutRadius = Math.min(Math.min(fullWidth / 4 - getPaddingLeft(), fullWidth / 4 - getPaddingRight()), Math.min
                (fullHeight / 4 - getPaddingTop(), fullHeight / 4 - getPaddingBottom()));
        mInnerRedius = mOutRadius * 0.2f;

        if (roockTouchMode) {
            Bitmap tmpDirectionBmp = BitmapFactory.decodeResource(getResources(), R.drawable.out_arrow);
            mDirectionBmp = Bitmap.createScaledBitmap(tmpDirectionBmp, (int) (fullWidth / 2 + mInnerRedius * 2),
                    (int) (fullWidth / 2 + mInnerRedius * 2), true);
        }
        outSrc = new Rect(0, 0, mOuterBgBitmap.getWidth(), mOuterBgBitmap.getHeight());
        outDst = new RectF(touchDownX - mOutRadius, touchDownY - mOutRadius,
                touchDownX + mOutRadius, touchDownY + mOutRadius);

        innerDst = new RectF(innerCenterX - mInnerRedius, innerCenterY - mInnerRedius,
                innerCenterX + mInnerRedius, innerCenterY + mInnerRedius);
        innerSrc = new Rect(0, 0, mRockerPointBitmap.getWidth(), mRockerPointBitmap.getHeight());

        //test default point bitmap
        float width = mInnerRedius * 3;
        float height = mInnerRedius * 3;
        defaultSrc = new Rect(0, 0, defaultPointBitmap.getWidth(), defaultPointBitmap.getHeight());
        defaultDst = new RectF(fullWidth / 2 - width / 2, fullWidth / 5 * 4 - height / 2,
                fullWidth / 2 + width, fullWidth / 5 * 4 + height);
    }

    private void initPaint() {
        mOuterPaint = new Paint();
        mInnerPaint = new Paint();
        mOuterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mInnerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mOuterBgPaint = new Paint();
        mOuterBgPaint.setAntiAlias(true);
        mOuterBgPaint.setFilterBitmap(true);

        mRockerPointPaint = new Paint();
        mRockerPointPaint.setAntiAlias(true);
        mRockerPointPaint.setFilterBitmap(true);

        deaultRockerPointPaint = new Paint();
        deaultRockerPointPaint.setAntiAlias(true);
        deaultRockerPointPaint.setFilterBitmap(true);
    }

    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RockerTouchView);
        allDirectionMode = typedArray.getBoolean(R.styleable.RockerTouchView_all_direction_mode, false);
        roockTouchMode = typedArray.getBoolean(R.styleable.RockerTouchView_roock_touch_mode, false);
        Drawable outCircleBackground = typedArray.getDrawable(R.styleable.RockerTouchView_out_circle_background);
        if (null != outCircleBackground) {
            if (outCircleBackground instanceof BitmapDrawable) {
                mOuterBgBitmap = ((BitmapDrawable) outCircleBackground).getBitmap();
            }
        }
        Drawable rockerPointBackground = typedArray.getDrawable(R.styleable.RockerTouchView_rocker_point_background);
        if (null != rockerPointBackground) {
            if (rockerPointBackground instanceof BitmapDrawable) {
                mRockerPointBitmap = ((BitmapDrawable) rockerPointBackground).getBitmap();
            }
        }
        Drawable defaultRockerPointBackground = typedArray.getDrawable(R.styleable
                .RockerTouchView_default_rocker_point_background);
        if (null != defaultRockerPointBackground) {
            if (defaultRockerPointBackground instanceof BitmapDrawable) {
                defaultPointBitmap = ((BitmapDrawable) defaultRockerPointBackground).getBitmap();
            }
        }
        typedArray.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        //处理三种模式
        if (widthMode == MeasureSpec.EXACTLY) {
            return widthVal + getPaddingLeft() + getPaddingRight();
        } else if (widthMode == MeasureSpec.UNSPECIFIED) {
            return fullWidth;
        } else {
            return Math.min(fullWidth, widthVal);
        }
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightVal = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) { //三种模式
            return heightVal + getPaddingTop() + getPaddingBottom();
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            return fullHeight;
        } else {
            return Math.min(fullHeight, heightVal);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fullWidth = w;
        fullHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (defaultPointBitmap == null) {
            return;
        }
        if (innerCenterX == 0 || innerCenterY == 0 || mOuterBgBitmap == null || keepDeading) {
            canvas.drawBitmap(defaultPointBitmap, defaultSrc, defaultDst, deaultRockerPointPaint);
            return;
        }
        //out
        outSrc.set(0, 0, mOuterBgBitmap.getWidth(), mOuterBgBitmap.getHeight());
        outDst.set(touchDownX - mOutRadius, touchDownY - mOutRadius,
                touchDownX + mOutRadius, touchDownY + mOutRadius);
        canvas.drawBitmap(mOuterBgBitmap, outSrc, outDst, mOuterBgPaint);

        //inner
        innerSrc.set(0, 0, mRockerPointBitmap.getWidth(), mRockerPointBitmap.getHeight());
        innerDst.set(innerCenterX - mInnerRedius, innerCenterY - mInnerRedius,
                innerCenterX + mInnerRedius, innerCenterY + mInnerRedius);
        canvas.drawBitmap(mRockerPointBitmap, innerSrc, innerDst, mRockerPointPaint);


        if (roockTouchMode && mDirectionBmp != null) {// out arrow
            float rotationDegree = (float) getPointsAngleDegree(touchDownX, touchDownY, innerCenterX, innerCenterY);
            drawRotateArrow(canvas, mDirectionBmp, 180 - rotationDegree,
                    touchDownX - mOutRadius - mInnerRedius,
                    touchDownY - mOutRadius - mInnerRedius);
        }
    }

    /**
     * @param rotation 旋转角度
     * @param posX     left
     * @param posY     top
     */
    private static void drawRotateArrow(Canvas canvas, Bitmap bitmap, float rotation, float posX, float posY) {
        Matrix matrix = new Matrix();
        int offsetX = bitmap.getWidth() / 2;
        int offsetY = bitmap.getHeight() / 2;
        matrix.postTranslate(-offsetX, -offsetY);
        matrix.postRotate(rotation);
        matrix.postTranslate(posX + offsetX, posY + offsetY);
        canvas.drawBitmap(bitmap, matrix, null);
    }

    /**
     * @return 计算(x1, y1)相对于(x0, y0)为圆点的圆的角度[0, 360]
     */
    public static double getPointsAngleDegree(double x0, double y0, double x1, double y1) {
        double z = getPointsDistant(x0, y0, x1, y1);
        double angle = Math.asin(Math.abs(y1 - y0) / z) * 180 / Math.PI;
        if (x1 < x0 && y1 < y0) {
            angle = 180 - angle;
        } else if (x1 < x0 && y1 >= y0) {
            angle = 180 + angle;
        } else if (x1 >= x0 && y1 >= y0) {
            angle = 360 - angle;
        }
        return angle;
    }

    /**
     * @return 计算(x1, y1)和(x0, y0)两点之间的直线距离
     */
    public static double getPointsDistant(double x0, double y0, double x1, double y1) {
        return Math.sqrt(Math.pow((x1 - x0), 2) + Math.pow((y1 - y0), 2));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                innerCenterX = touchDownX = event.getX();
                innerCenterY = touchDownY = event.getY();
                checkTouchDownPoint(touchDownX, touchDownY);
                break;
            case MotionEvent.ACTION_MOVE:
                updateMoveStatus(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                innerCenterX = 0;
                innerCenterY = 0;
                touchDownX = 0;
                touchDownY = 0;
                keepDeading = true;
                invalidate();
                break;
        }
        return true;
    }

    private void updateMoveStatus(MotionEvent e) {
        float moveX = e.getX();
        float moveY = e.getY();
        float distance = getDistance(touchDownX, touchDownY, moveX, moveY);
        double angle;
        boolean inIner = distance < mOutRadius - mInnerRedius;
        float touchPercent;//滑动距离的百分比
        if (inIner) {
            innerCenterX = moveX;
            innerCenterY = moveY;
            angle = getAngle(moveX, moveY, distance, true);

            touchPercent = distance / (mOutRadius - mInnerRedius) * 100;
        } else {
            angle = getAngle(moveX, moveY, distance, false);
            touchPercent = 100.f;
        }
        int direction;//具体那个方向
        direction = getDirection(angle, allDirectionMode);
        Log.e("rocker", "percent =" + touchPercent + "   distance =" + distance + "  far=" + (mOutRadius - mInnerRedius));
        if (mRockerViewChangeListener != null && direction != 0 && touchPercent >= 8.0f) {
            mRockerViewChangeListener.onRockerViewChange(allDirectionMode, (float) angle, direction, touchPercent);
        }
        invalidate();
    }

    private double getAngle(float moveX, float moveY, float distance, boolean inner) {
        float tempX = Math.abs(moveX - touchDownX);
        float tempY = Math.abs(moveY - touchDownY);

        float sinX = tempY / distance;
        float cosX = tempX / distance;

        float overX = (mOutRadius - mInnerRedius) * cosX;//(允许的最大x长度)
        float overY = (mOutRadius - mInnerRedius) * sinX;

        double angle = Math.acos(cosX) / Math.PI * 180;
        if (moveX >= touchDownX && moveY >= touchDownY) {
            if (!inner) {
                innerCenterX = touchDownX + overX;
                innerCenterY = touchDownY + overY;
            }
            angle = 90 - angle + 270;
        } else if (moveX < touchDownX && moveY >= touchDownY) {
            if (!inner) {
                innerCenterX = touchDownX - overX;
                innerCenterY = touchDownY + overY;
            }
            angle = angle + 180;
        } else if (moveX >= touchDownX && moveY < touchDownY) {
            if (!inner) {
                innerCenterX = touchDownX + overX;
                innerCenterY = touchDownY - overY;
            }
        } else {//2
            if (!inner) {
                innerCenterX = touchDownX - overX;
                innerCenterY = touchDownY - overY;
            }
            angle = 90 - angle + 90;
        }
//        Log.e("rocker", "angle=" + angle);
        return angle;
    }

    private int getDirection(double angle, boolean allDirectionMode) {
        int direction;
        if (allDirectionMode) {
            if (angle >= 22.5 && angle < 337.5) {
                angle = angle + 22.5;
                direction = (int) (angle / 45);
            } else {//right
                direction = 8;
            }
        } else {//四方向
            if (angle >= 45 && angle < 315) {
                angle = angle + 45;
                direction = (int) (angle / 90);
            } else {//right
                direction = 4;
            }
        }
//        Log.e("rocker", " allDirectionMode =" + allDirectionMode + "direction =" + direction);
        return direction;
    }

    /**
     * 判断按下位置点
     */
    private void checkTouchDownPoint(float x, float y) {
        float centerX = fullWidth / 2;
        float centerY = fullHeight / 2;
        if (x >= centerX && y <= centerY) {
            if (fullWidth - x >= mOutRadius && y - mOutRadius >= 0) {
                keepDeading = false;
            }
        } else if (x <= centerX && y <= centerY) {
            if (x - mOutRadius >= 0 && y - mOutRadius >= 0) {
                keepDeading = false;
            }
        } else if (x < centerX && y > centerY) {
            if (x - mOutRadius >= 0 && fullHeight - y >= mOutRadius) {
                keepDeading = false;
            }
        } else {
            if (fullWidth - x >= mOutRadius && fullHeight - y >= mOutRadius) {
                keepDeading = false;
            }
        }
    }

    private float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(Math.abs(x2 - x1), 2) + Math.pow(Math.abs(y2 - y1), 2));
    }


    public void setRockerViewChangeListener(RockerViewChangeListener listener) {
        mRockerViewChangeListener = listener;
    }


    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private RockerViewChangeListener mRockerViewChangeListener = null;

    public interface RockerViewChangeListener {
        /**
         * @param allDirectionMode 方向模式 false :四方向, true :八方向
         * @param angle            触摸角度
         * @param diretion         对应的方向
         *                         四方向  1:上, 2:左,3:下,4:右
         *                         八方向  1 :右上 ,2: 上,3: 左上,4 :左,5:左下,6 :下,7:右下 ,8 :下
         * @param percent          拖动距离的百分比(杆量)
         */
        void onRockerViewChange(boolean allDirectionMode, float angle, int diretion, float percent);
    }
}
