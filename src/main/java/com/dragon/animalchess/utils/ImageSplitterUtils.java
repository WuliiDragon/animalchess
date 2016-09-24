package com.dragon.animalchess.utils;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dragon on 16/7/12.
 * 切图工具类
 */
public class ImageSplitterUtils {

    public static List<ImagePiece> splitte(Bitmap bitmap, int piece, Bitmap bgbitmap,Bitmap killBitmap) {
        List<ImagePiece> imagePieces = new ArrayList<ImagePiece>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pieceWidth = Math.min(width, height) / piece;
        for (int i = 0; i < piece; i++) {
            for (int j = 0; j < piece; j++) {
                ImagePiece imagePiece = new ImagePiece();
                if (j + i * piece < 8) {
                    imagePiece.setIndex(j + i * piece);
                    imagePiece.setRedOrBlue(true);
                } else {
                    imagePiece.setIndex(-(8 - (j + i * piece)));
                    imagePiece.setRedOrBlue(false);
                }
                int x = j * pieceWidth;
                int y = i * pieceWidth;
                imagePiece.setBitmap(Bitmap.createBitmap(bitmap, x, y, pieceWidth, pieceWidth));
                imagePiece.setBgBitmap(bgbitmap);
                imagePiece.setKillBitmap(killBitmap);
                imagePiece.init();
                imagePieces.add(imagePiece);
            }
        }
        return imagePieces;
    }

}
