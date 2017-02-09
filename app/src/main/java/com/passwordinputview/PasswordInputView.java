package com.passwordinputview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pjj on 2017/2/9.
 */

public class PasswordInputView extends View {

    private static final String TAG = "PasswordInputView";
    private Context mContext;
    private int mScreenWidth;
    private int mScreenHeight;
    private List<ItemInfo> mItemList = new ArrayList<>();
    private Paint mCommentPaint;
    private TextPaint mTextPaint;
    private Paint mLinePaint;

    private static final int DEFAULT_ITEM_HEIGHT = 200;
    private static final int DEFAULT_LINE_WIDTH = 3;
    private int mItemWidth = 0;

    private int mCurrFocusItem = -1;
    private float mMotionDownX, mMotionDownY;

    private String[] mItemStringList = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "·", "0", "×"};
    private OnGetInputListener mOnGetInputListener;
    private StringBuilder sb = new StringBuilder();

    public PasswordInputView(Context context) {
        this(context, null);
    }

    public PasswordInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initItemInfo();
    }

    private void init(Context context) {
        this.mContext = context;
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        this.mScreenWidth = metrics.widthPixels;
        this.mScreenHeight = metrics.heightPixels;
        this.mItemWidth = mScreenWidth / 3;

        this.mCommentPaint = new Paint();
        this.mCommentPaint.setColor(Color.WHITE);
        this.mCommentPaint.setStyle(Paint.Style.FILL);
        this.mCommentPaint.setStrokeWidth(8);

        this.mTextPaint = new TextPaint();
        this.mTextPaint.setTextSize(100);
        this.mTextPaint.setStrokeWidth(6);
        this.mTextPaint.setColor(Color.BLACK);
        this.mTextPaint.setStyle(Paint.Style.FILL);
        this.mTextPaint.setSubpixelText(true);
        this.mTextPaint.setAntiAlias(true);

        this.mLinePaint = new Paint();
        this.mLinePaint.setColor(mContext.getResources().getColor(R.color.light_gray));
        this.mLinePaint.setStyle(Paint.Style.FILL);
    }

    private void initItemInfo() {
        int x = 0;
        int y = 0;
        int targetX = 0;
        int targetY = 0;
        for (int n = 0; n < mItemStringList.length; n++) {
            ItemInfo itemInfo = new ItemInfo();
            itemInfo.setText(mItemStringList[n]);
            itemInfo.setPosition(n);
            //靠近屏幕右边的矩形不用绘制竖线
            if ((n + 1) % 3 == 0) {
                targetX = (x + mItemWidth);
            } else {
                targetX = (x + mItemWidth - DEFAULT_LINE_WIDTH);
            }
            targetY = (y + DEFAULT_ITEM_HEIGHT - DEFAULT_LINE_WIDTH);
            itemInfo.setLocation(new Rect(x, y, targetX, targetY));
            x += mItemWidth;
            mItemList.add(itemInfo);
            if ((n + 1) % 3 == 0) {
                x = 0;
                y += DEFAULT_ITEM_HEIGHT;
            }
        }
    }

    public void setOnGetInputListener(OnGetInputListener mOnGetInputListener) {
        this.mOnGetInputListener = mOnGetInputListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_ITEM_HEIGHT * 4, MeasureSpec.UNSPECIFIED);
        setMeasuredDimension(widthMeasureSpec, measureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mItemList != null && mItemList.size() > 0) {
            for (ItemInfo itemInfo : mItemList) {
                drawItem(canvas, itemInfo);
            }
        }
    }

    private void drawItem(Canvas canvas, ItemInfo itemInfo) {
        if (mCurrFocusItem != -1 && itemInfo.position == mCurrFocusItem) {
            mCommentPaint.setColor(mContext.getResources().getColor(R.color.light_gray));
        } else {
            mCommentPaint.setColor(Color.WHITE);
        }
        canvas.drawRect(itemInfo.location, mCommentPaint);

        if ((itemInfo.position + 1) % 3 != 0) {
            //绘制竖线
            canvas.drawRect(itemInfo.location.right,
                    itemInfo.location.top,
                    itemInfo.location.right + DEFAULT_LINE_WIDTH,
                    itemInfo.location.top + DEFAULT_ITEM_HEIGHT, mLinePaint);
        }

        //绘制横线
        canvas.drawRect(itemInfo.location.left,
                itemInfo.location.bottom,
                itemInfo.location.left + mItemWidth,
                itemInfo.location.bottom + DEFAULT_LINE_WIDTH, mLinePaint);

        float baseX = (itemInfo.location.width() - mTextPaint.measureText(itemInfo.text)) / 2;
        float baseY = (itemInfo.location.height() - (mTextPaint.descent() + mTextPaint.ascent())) / 2;
        canvas.drawText(itemInfo.text, itemInfo.location.left + baseX, itemInfo.location.top + baseY, mTextPaint);
    }

    private int findFocusItem(int x, int y) {
        for (ItemInfo itemInfo : mItemList) {
            if (itemInfo.location.contains(x, y)) {
                return itemInfo.position;
            }
        }
        return -1;
    }

    private void resetFocusItem() {
        mCurrFocusItem = -1;
        invalidate();
        if (mOnGetInputListener != null) {
            mOnGetInputListener.getInput(sb.toString());
        }
    }

    private void onItemClick() {
        if (mCurrFocusItem == (mItemStringList.length - 1)) {
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
        } else {
            sb.append(mItemStringList[mCurrFocusItem]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mMotionDownX = event.getX();
                mMotionDownY = event.getY();
                int focusItem = findFocusItem((int) mMotionDownX, (int) mMotionDownY);
                if (focusItem != -1) {
                    mCurrFocusItem = focusItem;
                    invalidate();
                    onItemClick();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetFocusItem();
                break;
        }
        return true;
    }

    public interface OnGetInputListener {
        void getInput(String result);
    }

    class ItemInfo {
        public Rect location;
        public String text;
        public int position;

        public ItemInfo() {
        }

        public void setLocation(Rect mLocation) {
            this.location = mLocation;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}
