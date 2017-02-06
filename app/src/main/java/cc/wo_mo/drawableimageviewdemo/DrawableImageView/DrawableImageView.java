package cc.wo_mo.drawableimageviewdemo.DrawableImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by womo on 2017/2/2.
 */
public class DrawableImageView extends ImageView implements View.OnTouchListener {

    private boolean mHaveMove;

    private OnStartDrawingListener mOnStartDrawingListener;
    private OnStopDrawingListener mOnStopDrawingListener;

    private boolean mInEditMode;
    private List<DrawOperation> mDrawOperationList;
    private int mCurrentDrawOperationIndex;
    private DrawOperation.DrawingMode mDrawingMode;
    private DrawOperation.PenType mPenType;
    private int mPenColor, mPenAlpha, mPenWidth;
    private boolean mAntiAlias;
    private boolean mAllowPoint;


    /**
     * Instantiates a new Drawable image view.
     *
     * @param context the context
     */
    public DrawableImageView(Context context) {
        super(context);
        init(null, 0);
    }

    /**
     * Instantiates a new Drawable image view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public DrawableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    /**
     * Instantiates a new Drawable image view.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public DrawableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Initializes this class with default value or from xml definition
     *
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
//        final TypedArray a = getContext().obtainStyledAttributes(
//                attrs, R.styleable.DrawableImageView, defStyle, 0);
        mInEditMode = true;
        mDrawOperationList = new ArrayList<>();
        mDrawingMode = DrawOperation.DrawingMode.DRAW;
        mPenType = DrawOperation.PenType.NORMAL;
        mCurrentDrawOperationIndex = -1;
        mPenColor = Color.GREEN;
        mPenWidth = 15;
        mAntiAlias = true;
        mAllowPoint = true;
        setDrawingCacheEnabled(true);
        setOnTouchListener(this);
//        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCurrentDrawOperationIndex <= -1)
            return;
        Bitmap cacheBitmap;
        cacheBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        for (int i = 0; i <= mCurrentDrawOperationIndex; i++) {
            DrawOperation drawOperation = mDrawOperationList.get(i);
            draw(cacheBitmap, drawOperation);
        }
        canvas.drawBitmap(cacheBitmap,0, 0, null);
    }


    /**
     * Do a drawing operation on a bitmap
     *
     * @param bitmap the bitmap
     * @param drawOperation the draw operation
     */
    private void draw(Bitmap bitmap, DrawOperation drawOperation) {
        Canvas canvas = new Canvas(bitmap);
        switch (drawOperation.getDrawingMode()) {
            case DRAW:
                switch (drawOperation.getPenType()) {
                    case NORMAL:
                        canvas.drawPath(drawOperation.getPath(), drawOperation.getPaint());
                        break;
                    case LINE:
                        canvas.drawLine(
                                drawOperation.getStartX(), drawOperation.getStartY(),
                                drawOperation.getStopX(), drawOperation.getStopY(),
                                drawOperation.getPaint());
                        break;
                    case POINT:
                        canvas.drawPoint(drawOperation.getStartX(), drawOperation.getStartY(),
                                drawOperation.getPaint());
                }
                break;
            case ERASE:
                canvas.drawPath(drawOperation.getPath(), drawOperation.getPaint());
                break;
        }
    }


    /**
     * Instantiates a new paint with current parameters.
     *
     * @return the new instance of paint
     */
    private Paint newPaint() {
        Paint paint = new Paint();
        paint.setColor(mPenColor);
        paint.setStrokeWidth(mPenWidth);
        paint.setAntiAlias(mAntiAlias);
        paint.setStyle(Paint.Style.STROKE);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        if (mDrawingMode == DrawOperation.DrawingMode.ERASE) {
            paint.setAntiAlias(false);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        }
        return paint;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Todo: fixed bug for two points touch
        boolean result = false;
        if (isInEditMode()) {
            DrawOperation curOperation;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    getParent().requestDisallowInterceptTouchEvent(true);
                    Log.d("onTouch", "action down");
                    if (mCurrentDrawOperationIndex >= -1
                            && mCurrentDrawOperationIndex < mDrawOperationList.size()-1) {
                        mDrawOperationList =
                                mDrawOperationList.subList(0, mCurrentDrawOperationIndex+1);
                    }
                    curOperation = new DrawOperation()
                            .setDrawingMode(mDrawingMode)
                            .setPenType(mPenType)
                            .setPaint(newPaint())
                            .setStartX(event.getX())
                            .setStartY(event.getY())
                            .setStopX(event.getX())
                            .setStopY(event.getY());
                    if (mPenType == DrawOperation.PenType.NORMAL) {
                        Path path = new Path();
                        path.moveTo(event.getX(), event.getY());
                        curOperation.setPath(path);
                    }
                    mDrawOperationList.add(curOperation);
                    mCurrentDrawOperationIndex++;
                    if (mOnStartDrawingListener != null) {
                        mOnStartDrawingListener.onStartDrawing();
                    }
                    mHaveMove = false;
                    result = false;
                    break;
                case MotionEvent.ACTION_MOVE:
//                    Log.d("onTouch", "ACTION_MOVE");
                    curOperation = mDrawOperationList.get(mDrawOperationList.size()-1);
                    // touch point should move more than a threshold distance
                    if (Math.abs(event.getX()-curOperation.getStopX())+
                            Math.abs(event.getX()-curOperation.getStopX())> 0.01f) {
                        mHaveMove = true;
                        cancelLongPress(); //
                        // 在涂鸦的过程中转化为画直线模式（比如通过长按触发）
                        if (mPenType == DrawOperation.PenType.LINE
                                && curOperation.getPenType() == DrawOperation.PenType.NORMAL) {
                            curOperation.setPenType(DrawOperation.PenType.LINE);
                            mPenType = DrawOperation.PenType.NORMAL;
                        }
                        // in normal mode, path should line to the new point
                        if (mPenType == DrawOperation.PenType.NORMAL) {
                            curOperation.getPath().lineTo(event.getX(), event.getY());
                        }
                        curOperation.setStopX(event.getX()).setStopY(event.getY());
                        invalidate();
                    }
                    result = true;
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("onTouch", "action up");
                    curOperation = mDrawOperationList.get(mDrawOperationList.size()-1);
                    cancelLongPress();
                    getParent().requestDisallowInterceptTouchEvent(false);
                    if (mOnStopDrawingListener != null) {
                        mOnStopDrawingListener.onStopDrawing();
                    }
                    curOperation.setCompleted(true);
                    if (!mHaveMove) {
                        if (mPenType == DrawOperation.PenType.LINE
                                && curOperation.getPenType() == DrawOperation.PenType.NORMAL) {
                            mPenType = DrawOperation.PenType.NORMAL;
                        }
                        if (mAllowPoint) {
                            curOperation.setPenType(DrawOperation.PenType.POINT);
                        } else {
                            mDrawOperationList.remove(mDrawOperationList.size()-1);
                            mCurrentDrawOperationIndex--;
                        }
                    }
                    invalidate();
                    result = true;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.d("onTouch", "ACTION_POINTER_DOWN");
                    cancelLongPress();
                    getParent().requestDisallowInterceptTouchEvent(false);
                    result = true;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    Log.d("onTouch", "ACTION_POINTER_UP");
                    result = true;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    Log.d("onTouch", "ACTION_CANCEL");
                    mDrawOperationList.remove(mDrawOperationList.size()-1);
                    mCurrentDrawOperationIndex--;
                    result = true;
                    break;
                default:
                    Log.d("onTouch", "default"+event.getActionMasked());
                    result = true;
                    break;
            }
        }
        return result;
    }

    /**
     * Create capture of current image as a bitmap.
     *
     * @return the bitmap
     */
    public Bitmap createCaptureBitmap() {
        setDrawingCacheEnabled(false);
        setDrawingCacheEnabled(true);
        return getDrawingCache(true);
    }

    /**
     * Create a capture of current image as a byte[]
     *
     * @return the byte [ ]
     */
    public byte[] createCaptureBytes() {
        setDrawingCacheEnabled(false);
        setDrawingCacheEnabled(true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        getDrawingCache(true).compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Undo.
     */
    public void undo() {
        if (canUndo()) {
            mCurrentDrawOperationIndex--;
            invalidate();
        }
    }

    /**
     * Can undo boolean.
     *
     * @return the boolean
     */
    public boolean canUndo() {
        return mCurrentDrawOperationIndex > -1;
    }

    /**
     * Redo.
     */
    public void redo() {
        if (canRedo()) {
            mCurrentDrawOperationIndex++;
            invalidate();
        }
    }

    /**
     * Can redo boolean.
     * return true if can do redo operation.
     * @return the boolean
     */
    public boolean canRedo() {
        return mCurrentDrawOperationIndex < mDrawOperationList.size()-1;
    }

    /**
     * Clear all drawing.
     */
    public void clearAll() {
        Log.d("clearAll", "called");
        if (mDrawOperationList != null) {
            mDrawOperationList.clear();
        }
        mCurrentDrawOperationIndex = -1;
    }


    /**
     * The interface On start drawing listener.
     */
// interface
    public interface OnStartDrawingListener {
        /**
         * On start drawing.
         */
        void onStartDrawing();
    }

    /**
     * The interface On end drawing listener.
     */
    public interface OnStopDrawingListener {
        /**
         * On end drawing.
         */
        void onStopDrawing();
    }

    // getter and setter

    /**
     * Gets pen type.
     *
     * @return the pen type
     */
    public DrawOperation.PenType getPenType() {
        return mPenType;
    }

    /**
     * Sets pen type.
     *
     * @param penType the pen type
     * @return the pen type
     */
    public DrawableImageView setPenType(DrawOperation.PenType penType) {
        mPenType = penType;
        return this;
    }

    /**
     * Gets draw mode.
     *
     * @return the draw mode
     */
    public DrawOperation.DrawingMode getDrawingMode() {
        return mDrawingMode;
    }

    /**
     * Sets draw mode.
     *
     * @param drawingMode the draw mode
     * @return the draw mode
     */
    public DrawableImageView setDrawingMode(DrawOperation.DrawingMode drawingMode) {
        mDrawingMode = drawingMode;
        return this;
    }

    /**
     * Gets pen color.
     *
     * @return the pen color
     */
    public int getPenColor() {
        return mPenColor;
    }

    /**
     * Sets pen color.
     *
     * @param penColor the pen color
     */
    public void setPenColor(int penColor) {
        mPenColor = penColor;
    }

    /**
     * Gets pen alpha.
     *
     * @return the pen alpha
     */
    public int getPenAlpha() {
        return mPenAlpha;
    }

    /**
     * Sets pen alpha.
     *
     * @param penAlpha the pen alpha
     */
    public void setPenAlpha(int penAlpha) {
        mPenAlpha = penAlpha;
    }

    /**
     * Gets pen width.
     *
     * @return the pen width
     */
    public int getPenWidth() {
        return mPenWidth;
    }

    /**
     * Sets pen width.
     *
     * @param penWidth the pen width
     */
    public void setPenWidth(int penWidth) {
        mPenWidth = penWidth;
    }

    /**
     * Is anti alias boolean.
     *
     * @return the boolean
     */
    public boolean isAntiAlias() {
        return mAntiAlias;
    }

    /**
     * Sets anti alias.
     *
     * @param antiAlias the anti alias
     */
    public void setAntiAlias(boolean antiAlias) {
        mAntiAlias = antiAlias;
    }

    @Override
    public boolean isInEditMode() {
        return mInEditMode;
    }

    /**
     * Sets in edit mode.
     *
     * @param inEditMode the in edit mode
     */
    public void setInEditMode(boolean inEditMode) {
        mInEditMode = inEditMode;
    }


    /**
     * Gets on start drawing listener.
     *
     * @return the on start drawing listener
     */
    public OnStartDrawingListener getOnStartDrawingListener() {
        return mOnStartDrawingListener;
    }

    /**
     * Sets on start drawing listener.
     *
     * @param onStartDrawingListener the on start drawing listener
     */
    public void setOnStartDrawingListener(OnStartDrawingListener onStartDrawingListener) {
        mOnStartDrawingListener = onStartDrawingListener;
    }

    /**
     * Gets on end drawing listener.
     *
     * @return the on end drawing listener
     */
    public OnStopDrawingListener getOnStopDrawingListener() {
        return mOnStopDrawingListener;
    }

    /**
     * Sets on end drawing listener.
     *
     * @param onStopDrawingListener the on end drawing listener
     */
    public void setOnStopDrawingListener(OnStopDrawingListener onStopDrawingListener) {
        mOnStopDrawingListener = onStopDrawingListener;
    }


    @Override
    public void setImageResource(int resId) {
        clearAll();
        super.setImageResource(resId);
    }

    @Override
    public void setImageURI(Uri uri) {
        clearAll();
        super.setImageURI(uri);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        clearAll();
        super.setImageBitmap(bm);
    }


    @Override
    public void setImageDrawable(Drawable drawable) {
        clearAll();
        super.setImageDrawable(drawable);
    }
}
