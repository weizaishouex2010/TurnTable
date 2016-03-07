package com.huangzhiwei.turntable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by huangzhiwei on 16/3/7.
 */
public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback,Runnable {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    private Thread t;
    private boolean isRunning;

    private String[] mStrs = new String[]{"单反相机","iPad","恭喜发财","iPhone","服装一套","恭喜发财"};

    private int[] mImgs = new int[]{R.drawable.danfan,R.drawable.ipad,R.drawable.f015,R.drawable.iphone,R.drawable.meizi,R.drawable.f040};
    private Bitmap[] mImgsBitmap;

    private int[] mColors = new int[]{0xFFFFC000,0xFFF17E01,0xFFFFC000,0xFFF17E01,0xFFFFC000,0xFFF17E01};

    private int mItemCount = 6;
    //盘快范围
    private RectF mRange = new RectF();
    //直径
    private int mRadius;
    //图画绘制
    private Paint mArcPaint;
    //文字绘制
    private Paint mTextPaint;
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.bg2);

    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,20,getResources().getDisplayMetrics());

    private double mSpeed;

    private volatile float mStartAngle = 0;
    //判断是否点击停止
    private boolean isShouldEnd;
    //中心位置
    private int mCenter;
    //以左侧padding为标准
    private int mPadding;

    public LuckyPan(Context context) {
        super(context);
    }

    public LuckyPan(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredHeight(),getMeasuredWidth());
        mPadding = getPaddingLeft();
        mRadius = width - mPadding*2;
        mCenter = width/2;
        setMeasuredDimension(width,width);


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //初始化画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);
        //圆盘范围
        mRange = new RectF(mPadding,mPadding,mPadding+mRadius,mPadding+mRadius);

        mImgsBitmap = new Bitmap[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(),mImgs[i]);
        }


        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }


    @Override
    public void run() {
        while(isRunning)
        {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if(end-start<50)
            {
                try {
                    Thread.sleep(50-(end-start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if(mCanvas!=null)
            {
                //绘制背景
                drawBg();
                //绘制盘快
                float tmpAngle = mStartAngle;
                float sweepAngle = 360/mItemCount;
                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColors[i]);
                    //盘快
                    mCanvas.drawArc(mRange,tmpAngle,sweepAngle,true,mArcPaint);
                    //文本
                    drawText(tmpAngle,sweepAngle,mStrs[i]);

                    drawIcon(tmpAngle,mImgsBitmap[i]);
                    tmpAngle += sweepAngle;
                }

                //旋转
                mStartAngle += mSpeed;
                //如果点击了停止
                if(isShouldEnd)
                {
                    mSpeed-=1;
                }
                if(mSpeed<=0)
                {
                    mSpeed = 0;
                    isShouldEnd = false;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(mCanvas!=null)
            {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        //设置图片的宽度 为直径的1/8
        int imgWidth = mRadius/8;
        //转成弧度制
        float angle = (float) ((tmpAngle+360/mItemCount/2)*Math.PI/180);
        int x = (int) (mCenter + mRadius/2/2*Math.cos(angle));
        int y = (int) (mCenter + mRadius/2/2* Math.sin(angle));
        Rect rect = new Rect(x-imgWidth/2,y-imgWidth/2,x+imgWidth/2,y+imgWidth/2);
        mCanvas.drawBitmap(bitmap,null,rect,null);
    }

    /**
     * 绘制每个文本
     * @param tmpAngle
     * @param sweepAngle
     * @param mStr
     */
    private void drawText(float tmpAngle, float sweepAngle, String mStr) {
        Path path = new Path();
        path.addArc(mRange,tmpAngle,sweepAngle);

        //水平偏移量
        float textWidth = mTextPaint.measureText(mStr);
        int hOffset = (int) (mRadius*Math.PI/mItemCount/2 - textWidth/2);
        int vOffset = mRadius/2/6;//垂直偏移量
        mCanvas.drawTextOnPath(mStr,path,hOffset,vOffset,mTextPaint);
    }

    private void drawBg() {
        mCanvas.drawColor(0xffffffff);
        mCanvas.drawBitmap(mBgBitmap,null,new Rect(mPadding/2,mPadding/2,getMeasuredWidth()-mPadding/2,getMeasuredHeight()-mPadding/2),null);
    }


    /**
     *点击启动
     */
    public void luckyStart(int index)
    {
        //计算每一项的角度
        float angle = 360/mItemCount;
        //计算每一项中奖范围
        // 0 -> 210-270
        // 1->  150 210
        float from = 270-(index+1)*angle;
        float end = from + angle;
        //设置停下来需要旋转的距离  区间
        float targetFrom = 4*360 + from;
        float targetEnd = 4*360 + end;

        /**
         * v->0 每次-1
         * 解二次方程
         */
        float v1 = (float) ((-1 +Math.sqrt(1+8*targetFrom))/2);
        float v2 = (float) ((-1 +Math.sqrt(1+8*targetEnd))/2);

        mSpeed = v1+Math.random()*(v2-v1);

        isShouldEnd = false;
    }

    /**
     * 点击停止
     */
    public void luckyEnd()
    {
        isShouldEnd = true;
        mStartAngle = 0;
    }

    /**
     * 是否在旋转
     */
    public boolean isStart()
    {
        return mSpeed!=0;
    }

    /**
     *是否停止
     * @return
     */
    public boolean isShouldEnd()
    {
        return isShouldEnd;
    }


}
