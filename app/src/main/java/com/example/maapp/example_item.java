package com.example.maapp;

import android.graphics.Bitmap;

public class example_item {
    private String mText1;
    private String mText2;
    private int mHOLDER_SELECTOR;
    private Bitmap mBitmap;
    public example_item(String text1, String text2, int HOLDER_SELECTOR) {
        mText1 = text1;
        mText2 = text2;
        mHOLDER_SELECTOR = HOLDER_SELECTOR;
    }
    public String getText1() {
        return mText1;
    }
    public String getText2() {
        return mText2;
    }
    public Bitmap getBitmap(){ return mBitmap; };
    public int getHOLDER_SELECTOR(){return mHOLDER_SELECTOR;};

    /// case 2

    public example_item(String text1, String text2, Bitmap bitmap, int HOLDER_SELECTOR){
        mText1 = text1;
        mText2 = text2;
        mBitmap = bitmap;
        mHOLDER_SELECTOR = HOLDER_SELECTOR;
    }
}
