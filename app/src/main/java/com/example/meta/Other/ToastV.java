package com.example.meta.Other;

import static androidx.core.content.res.ResourcesCompat.getFont;

import android.app.Activity;
import android.content.Context;

import com.example.meta.R;

import www.sanju.motiontoast.MotionToast;

public class ToastV {
    public void Success(Context context,String title,String message){
        MotionToast.Companion.darkToast((Activity) context,
                title,
                message,
                MotionToast.TOAST_SUCCESS,
                MotionToast.GRAVITY_CENTER,
                MotionToast.SHORT_DURATION,
                getFont(context, R.font.helvetica_regular));
    }
    public void Failed(Context context,String title,String message){
        MotionToast.Companion.darkToast((Activity) context,
                title,
                message,
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_CENTER,
                MotionToast.SHORT_DURATION,
                getFont(context, R.font.helvetica_regular));
    }
    public void Warning(Context context,String title,String message){
        MotionToast.Companion.darkToast((Activity) context,
                title,
                message,
                MotionToast.TOAST_WARNING,
                MotionToast.GRAVITY_CENTER,
                MotionToast.SHORT_DURATION,
                getFont(context, R.font.helvetica_regular));
    }
    public void Info(Context context,String title,String message){
        MotionToast.Companion.darkToast((Activity) context,
                title,
                message,
                MotionToast.TOAST_INFO,
                MotionToast.GRAVITY_CENTER,
                MotionToast.SHORT_DURATION,
                getFont(context, R.font.helvetica_regular));
    }
}
