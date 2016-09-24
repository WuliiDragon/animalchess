package com.dragon.animalchess.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dragon.animalchess.R;
import com.dragon.animalchess.utils.ImagePiece;
import com.dragon.animalchess.utils.ImageSplitterUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
 * Created by dragon on 16/7/17.
 * 翻拍模式View
 */
public class MyView extends RelativeLayout implements View.OnClickListener {
    private boolean once = false;//初始化标志，只进行一次
    private boolean isbBlue; //为true时表示蓝色走棋

    private int mColumn = 4;
    private int mMargin;
    private int mWidth;//屏幕的宽度
    public static int mItemWidth;//小图片的宽度

    private Bitmap mBitmap;
    private Bitmap mbgBitmap;
    private Bitmap mBeKillBitmap;

    private ArrayList<ImagePiece> mImagePieces = new ArrayList<>();//ImagePiece对象的集合
    private ImageView mImageViews[];//image数组

    private Paint mPaint;//笔

    private ImageView mFirst;//指向点击的第一张图片
    private ImageView mSecond;
    private boolean isAnimation;//表示是否在执行动画
    private RelativeLayout mAnimLayout;//动画层

    public changeListener mListener;

    public static SoundPool soundPool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);
    public static int kill = 2;
    public static int turn = 1;
    public static int bgm = 0;


    private static final int PLAYER_CHANGED = 1;
    private static final int GAME_OVER = 0;


    public interface changeListener {
        void playerChange(boolean isblue);
    }

    public void setmListener(MyView.changeListener mListener) {
        this.mListener = mListener;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {//匿名内部类
            switch (msg.what) {
                case PLAYER_CHANGED:
                    if (isbBlue) {
                        mListener.playerChange(isbBlue);
                    } else {
                        mListener.playerChange(isbBlue);
                    }
                case GAME_OVER:
                default:
                    break;
            }
        }
    };

    //两个参数的构造方法
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();//调用初始化方法
    }

    //测量给出布局大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //取宽高最小值
        mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
        //只执行一次初始化
        if (!once) {
            initBitmap();
            initItem();
            once = true;
        }
        setMeasuredDimension(mWidth, mWidth);
    }

    //画棋格
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = mWidth / 4;
        for (int i = 0; i <= 4; i++) {
            int startX = 0;
            int startY = i * w;
            int stopX = w * mColumn;
            canvas.drawLine(startX, startY, stopX, startY, mPaint);
            canvas.drawLine(startY, startX, startY, stopX, mPaint);
        }
    }

    //触摸事件处理
    @Override
    public void onClick(View v) {
        if (isAnimation)
            return;
        if (mFirst == v) {//表示两次都点同一张图片则取消高亮
            mFirst.setColorFilter(null);//取消高亮
            mFirst = null;
            return;
        }
        if (mFirst == null) {
            mFirst = (ImageView) v;
            if (!mImagePieces.get(getIdByTag((String) mFirst.getTag())).isTurn()) {
                turn();
                return;
            }
            ImagePiece imagePieceFirst = mImagePieces.get(getIdByTag((String) mFirst.getTag()));
            if (imagePieceFirst.isKill()) {
                mFirst = null;
                return;
            }
            mFirst.setColorFilter(Color.parseColor("#55FF0000"));
        } else {
            mSecond = (ImageView) v;
            ImagePiece imagePieceFirst = mImagePieces.get(getIdByTag((String) mFirst.getTag()));
            ImagePiece imagePieceSecond = mImagePieces.get(getIdByTag((String) mSecond.getTag()));
            if (!imagePieceSecond.isTurn()) return;
            if (((imagePieceFirst.getPoint().x + 1) == (imagePieceSecond.getPoint().x)) && ((imagePieceFirst.getPoint().y) == (imagePieceSecond.getPoint().y)) || ((imagePieceFirst.getPoint().x - 1) == (imagePieceSecond.getPoint().x)) && ((imagePieceFirst.getPoint().y) == (imagePieceSecond.getPoint().y)) || ((imagePieceFirst.getPoint().y + 1) == (imagePieceSecond.getPoint().y)) && ((imagePieceFirst.getPoint().x) == (imagePieceSecond.getPoint().x)) || ((imagePieceFirst.getPoint().y - 1) == (imagePieceSecond.getPoint().y)) && ((imagePieceFirst.getPoint().x) == (imagePieceSecond.getPoint().x))) {
                kill(imagePieceFirst, imagePieceSecond);
            }
        }
    }

    //吃棋动画
    private void kill(ImagePiece imagePieceFirst, ImagePiece imagePieceSecond) {
        //构建动画层
        setUpAnimLayout();
        mFirst.setColorFilter(null);
        if (!imagePieceSecond.isKill()) {
            if (imagePieceFirst.isRedOrBlue() == imagePieceSecond.isRedOrBlue()) {
                mFirst = mSecond = null;
                return;
            }
        }
        if (((imagePieceFirst.getIndex() == 0) && (imagePieceSecond.getIndex() == 7))) {
            mSecond = mFirst = null;
            return;
        }
        if ((imagePieceFirst.getIndex() <= (imagePieceSecond.getIndex())) || ((imagePieceFirst.getIndex() == 7) && (imagePieceSecond.getIndex() == 0)) || (imagePieceSecond.isKill())) {//如果第一张图图片位权大于第二张就执行吃棋动画 //或者第一张是鼠第二张是象
            if ((imagePieceFirst.getIndex() == (imagePieceSecond.getIndex()))) {
                killSame(imagePieceFirst, imagePieceSecond);
                return;
            }
            ImageView ImgFirst = new ImageView(getContext());
            final Bitmap BmpFirst = imagePieceFirst.getmBitmap();
            ImgFirst.setImageBitmap(BmpFirst);
            LayoutParams LpFirst = new LayoutParams(mItemWidth, mItemWidth);
            LpFirst.leftMargin = mFirst.getLeft();
            LpFirst.topMargin = mFirst.getTop();
            ImgFirst.setLayoutParams(LpFirst);
            mAnimLayout.addView(ImgFirst);

            imagePieceSecond.Kill();

            Point p = imagePieceFirst.getPoint();
            imagePieceFirst.setPoint(imagePieceSecond.getPoint());
            imagePieceSecond.setPoint(p);
            imagePieceSecond.setIndex(8);


            ImageView ImgSecond = new ImageView(getContext());
            final Bitmap BmpSecond = imagePieceSecond.getmBitmap();
            ImgSecond.setImageBitmap(BmpSecond);
            LayoutParams LpSecond = new LayoutParams(mItemWidth, mItemWidth);
            LpSecond.leftMargin = mSecond.getLeft();
            LpSecond.topMargin = mSecond.getTop();
            ImgSecond.setLayoutParams(LpSecond);
            mAnimLayout.addView(ImgSecond);
            //定义动画层参数
            TranslateAnimation TrAnFirst = new TranslateAnimation(0, mSecond.getLeft() - mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
            TrAnFirst.setDuration(100);
            TrAnFirst.setFillAfter(true);
            ImgFirst.startAnimation(TrAnFirst);

            TrAnFirst.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //当动画开始时
                    mFirst.setVisibility(View.INVISIBLE);
                    isAnimation = true;
                    soundPool.play(MyView.kill, 1, 1, 2, 0, 1);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    String firstTag = (String) mFirst.getTag();
                    String secondTag = (String) mSecond.getTag();

                    mFirst.setImageBitmap(BmpSecond);
                    mSecond.setImageBitmap(BmpFirst);

                    mFirst.setTag(secondTag);
                    mSecond.setTag(firstTag);

                    mFirst.setVisibility(View.VISIBLE);
                    //mSecond.setVisibility(View.VISIBLE);

                    mSecond = mFirst = null;
                    //移除动画层
                    mAnimLayout.removeAllViews();
                    //解除动画
                    isAnimation = false;
                    isbBlue = !isbBlue;
                    checkGameOver();
                    handler.sendEmptyMessage(PLAYER_CHANGED);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } else {
            mSecond = mFirst = null;
        }
    }

    //消除同位权的牌
    private void killSame(ImagePiece imagePieceFirst, ImagePiece imagePieceSecond) {
        imagePieceSecond.Kill();
        imagePieceFirst.Kill();

        Point p = imagePieceFirst.getPoint();
        imagePieceFirst.setPoint(imagePieceSecond.getPoint());
        imagePieceSecond.setPoint(p);
        imagePieceSecond.setIndex(8);

        ImageView ImgFirst = new ImageView(getContext());
        final Bitmap BmpFirst = imagePieceFirst.getmBitmap();
        ImgFirst.setImageBitmap(BmpFirst);
        LayoutParams LpFirst = new LayoutParams(mItemWidth, mItemWidth);
        LpFirst.leftMargin = mFirst.getLeft();
        LpFirst.topMargin = mFirst.getTop();
        ImgFirst.setLayoutParams(LpFirst);
        mAnimLayout.addView(ImgFirst);

        ImageView ImgSecond = new ImageView(getContext());
        final Bitmap BmpSecond = imagePieceSecond.getmBitmap();
        ImgSecond.setImageBitmap(BmpSecond);
        LayoutParams LpSecond = new LayoutParams(mItemWidth, mItemWidth);
        LpSecond.leftMargin = mSecond.getLeft();
        LpSecond.topMargin = mSecond.getTop();
        ImgSecond.setLayoutParams(LpSecond);
        mAnimLayout.addView(ImgSecond);
        //定义动画层参数
        TranslateAnimation TrAnFirst = new TranslateAnimation(0, (mSecond.getLeft() - mFirst.getLeft()), 0, (mSecond.getTop() - mFirst.getTop()));
        TrAnFirst.setDuration(10);
        TrAnFirst.setFillAfter(true);
        ImgFirst.startAnimation(TrAnFirst);

        TranslateAnimation TrAnSecond = new TranslateAnimation(0, (mSecond.getLeft() - mFirst.getLeft()), 0, (mSecond.getTop() - mFirst.getTop()));
        TrAnSecond.setDuration(10);
        TrAnSecond.setFillAfter(true);
        ImgSecond.startAnimation(TrAnSecond);

        TrAnFirst.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //当动画开始时
                mFirst.setVisibility(View.INVISIBLE);
                isAnimation = true;
                soundPool.play(MyView.kill, 1, 1, 2, 0, 1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                String firstTag = (String) mFirst.getTag();
                String secondTag = (String) mSecond.getTag();

                mFirst.setImageBitmap(BmpSecond);
                mSecond.setImageBitmap(BmpFirst);

                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);

                mFirst.setVisibility(View.VISIBLE);

                mSecond = mFirst = null;
                //移除动画层
                mAnimLayout.removeAllViews();
                //解除动画
                isAnimation = false;
                isbBlue = !isbBlue;
                checkGameOver();
                handler.sendEmptyMessage(PLAYER_CHANGED);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }


    private void checkGameOver() {
        boolean isSuccess = false;
        int n = 0, m = 0;
        for (int i = 0; i < 16; i++) {//先检查红旗
            if (!mImagePieces.get(i).isRedOrBlue()) {
                if (mImagePieces.get(i).isKill()) {
                    n++;
                    if (n == 8) isSuccess = true;
                }
            } else {
                if (mImagePieces.get(i).isKill()) {
                    m++;
                    if (m == 8) isSuccess = true;
                }
            }
        }
        if (isSuccess) {
            Toast.makeText(getContext(), "游戏结束", Toast.LENGTH_LONG).show();
        }
    }

    //翻拍动画
    private void turn() {
        setUpAnimLayout();//新建动画层
        ImageView ImgFirst = new ImageView(getContext());//在这动画层上new新的ImageView
        final Bitmap BmpFirst = mImagePieces.get(getIdByTag((String) mFirst.getTag())).getmBitmap();//拿到Bitmap
        ImgFirst.setImageBitmap(BmpFirst);//设置给这张图片
        LayoutParams LpFirst = new LayoutParams(mItemWidth, mItemWidth);//给图片设置属性
        LpFirst.leftMargin = mFirst.getLeft();//左边距
        LpFirst.topMargin = mFirst.getTop();//右边距
        ImgFirst.setLayoutParams(LpFirst);//把属性设置进去
        mAnimLayout.addView(ImgFirst);//加到动画层

        mImagePieces.get(getIdByTag((String) mFirst.getTag())).turn();//改变图片为动物

        ImageView ImgSecond = new ImageView(getContext());
        final Bitmap BmpSecond = mImagePieces.get(getIdByTag((String) mFirst.getTag())).getmBitmap();
        ImgSecond.setImageBitmap(BmpSecond);
        LayoutParams LpSecond = new LayoutParams(mItemWidth, mItemWidth);
        LpSecond.leftMargin = mFirst.getLeft();
        LpSecond.topMargin = mFirst.getTop();
        ImgSecond.setLayoutParams(LpSecond);
        mAnimLayout.addView(ImgSecond);


        //设置动画效果
        AnimationSet animation = new AnimationSet(true);
        final ScaleAnimation scale = new ScaleAnimation(1, 0f, 1, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.addAnimation(scale);
        animation.setDuration(10);
        mFirst.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimation = true;
                soundPool.play(MyView.turn, 1, 1, 2, 0, 1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mFirst.setImageBitmap(BmpSecond);
                mFirst.setVisibility(View.VISIBLE);
                mImagePieces.get(getIdByTag((String) mFirst.getTag())).setTurn(true);
                mSecond = mFirst = null;
                mAnimLayout.removeAllViews();
                isbBlue = !isbBlue;
                isAnimation = false;
                handler.sendEmptyMessage(PLAYER_CHANGED);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }


    private int getIdByTag(String s) {
        String[] str = s.split("_");
        return Integer.parseInt(str[0]);
    }

    //新建RelativeLayout动画层
    private void setUpAnimLayout() {
        if (mAnimLayout == null) {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
    }

    //整体初始化
    private void init() {
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
                getResources().getDisplayMetrics());
        //对笔的初始化
        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);//笔的颜色
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setDither(true);//防抖动。
        mPaint.setStyle(Paint.Style.STROKE);//填充样式
        mPaint.setStrokeWidth(8);//笔的宽度
        bgm = soundPool.load(getContext(), R.raw.bgm, 1);
        kill = soundPool.load(getContext(), R.raw.kill, 1);
        turn = soundPool.load(getContext(), R.raw.turn, 1);
    }

    //初始化ImageVIew 将其排列
    private void initItem() {
        mItemWidth = (mWidth - mMargin * (mColumn - 1)) / mColumn;
        mImageViews = new ImageView[mColumn * mColumn];
        for (int i = 0; i < mImageViews.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);
            item.setImageBitmap(mImagePieces.get(i).getmBitmap());
            mImageViews[i] = item;
            item.setId(i + 1);
            item.setTag(i + "_" + mImagePieces.get(i).getIndex());
            LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
            if ((i + 1) % mColumn != 0) {
                lp.rightMargin = mMargin;
            }
            if (i % mColumn != 0) {
                lp.addRule(RelativeLayout.RIGHT_OF, mImageViews[i - 1].getId());
            }
            if ((i + 1) > mColumn) {
                lp.topMargin = mMargin;
                lp.addRule(RelativeLayout.BELOW, mImageViews[i - mColumn].getId());
            }
            addView(item, lp);
        }
        for (int i = 0; i < mColumn; i++) {
            for (int j = 0; j < mColumn; j++) {
                ImagePiece imagePiece = mImagePieces.get(j + i * mColumn);
                int x = j * mItemWidth;
                int y = i * mItemWidth;
                imagePiece.setPoint(new Point(x / mItemWidth, y / mItemWidth));
            }
        }
        soundPool.play(MyView.bgm, 1, 1, 4, -1, 1);
    }

    //初始化bitmap
    private void initBitmap() {
        if (mBitmap == null) {//拿到资源
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.res);
            mbgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
            mBeKillBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kill);
        }//得到集合后乱序
        mImagePieces = (ArrayList<ImagePiece>) ImageSplitterUtils.splitte(mBitmap, 4, mbgBitmap, mBeKillBitmap);
        Collections.sort(mImagePieces, new Comparator<ImagePiece>() {
            @Override
            public int compare(ImagePiece a, ImagePiece b) {
                return Math.random() > 0.5 ? 1 : -1;
            }
        });
    }

    //暂停
    public void pauseSound() {
        soundPool.pause(bgm);
        soundPool.unload(kill);
        soundPool.unload(turn);
    }

    //恢复暂停
    public void resumeSound() {
        kill = soundPool.load(getContext(), R.raw.kill, 1);
        turn = soundPool.load(getContext(), R.raw.turn, 1);
        soundPool.resume(bgm);
    }
    /*
    private static final String INSTANCE = "instance";
    private static final String INSTANCE_IS_BLUE = "instance_is_blue";
    private static final String INSTANCE_IMAGE_PIECES = "instance_image_pieces";
    private static final String INSTANCE_IMAGE_VIEWS = "instance_image_views";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_IS_BLUE, isbBlue);
        bundle.putParcelableArrayList(INSTANCE_IMAGE_PIECES, mImagePieces);
        bundle.putSerializable(INSTANCE_IMAGE_VIEWS, mImageViews);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            isbBlue = bundle.getBoolean(INSTANCE_IS_BLUE);
            mImagePieces = bundle.getParcelableArrayList(INSTANCE_IMAGE_PIECES);
            mImageViews = (ImageView[]) bundle.getParcelableArray(INSTANCE_IMAGE_VIEWS);
            init();
            initItem();
            super.onRestoreInstanceState(bundle.getBundle(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }*/
}