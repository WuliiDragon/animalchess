package com.dragon.animalchess.utils;

import android.graphics.Bitmap;
import android.graphics.Point;

/*
 * Created by dragon on 16/7/12.
 */
public class ImagePiece {
    private int index;//牌的权值
    private Point point;//牌的坐标

    private boolean isTurn;//牌是否翻转
    private boolean IsRedOrBlue;//true表示红方 false表示蓝方
    private boolean isKill;//是否被kill

    private Bitmap bgBitmap;//存放背景
    private Bitmap mBitmap;//当前的bitmap
    private Bitmap bitmap;//存放动物的bitmap
    private Bitmap KillBitmap;//存放被杀死的bitmap

    public Bitmap getKillBitmap() {
        return KillBitmap;
    }

    public void setKillBitmap(Bitmap killBitmap) {
        KillBitmap = killBitmap;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isTurn() {
        return isTurn;
    }

    public void setTurn(boolean turn) {
        isTurn = turn;
    }

    public Bitmap getBgBitmap() {
        return bgBitmap;
    }

    public void setBgBitmap(Bitmap bgBitmap) {
        this.bgBitmap = bgBitmap;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public boolean isRedOrBlue() {
        return IsRedOrBlue;
    }

    public void setRedOrBlue(boolean redOrBlue) {
        IsRedOrBlue = redOrBlue;
    }

    public boolean isKill() {
        return isKill;
    }

    public void setKill(boolean kill) {
        isKill = kill;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public ImagePiece() {
    }

    public String toString() {
        return "ImagePiece [index=" + index + ", bitmap=" + bitmap + "]";
    }

    public void init() {
        mBitmap = bgBitmap;
        isTurn = false;
        isKill = false;
    }

    public void turn() {
        mBitmap = bitmap;
    }

    public void Kill() {
        mBitmap = KillBitmap;
        isKill = true;
    }

}
