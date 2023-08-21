package com.Bureau.Achivki;


import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;


public class WindowCalculation extends Activity {
    DisplayMetrics displayMetrics;
    int screenHeight;
    int screenWeight;
    private final Context context;

    public WindowCalculation(Context context) {
        this.context = context;
    }

    public int WindowCalculationHeight() {
    displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    screenHeight = displayMetrics.heightPixels;
    Log.d("РАЗРЕШЕНИЕ ПЕРВОЕ", String.valueOf(screenHeight));
    return screenHeight;
    }
    public int WindowCalculationWeight() {
        displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWeight = displayMetrics.widthPixels;
        Log.d("РАЗРЕШЕНИЕ ВТОРОЕ", String.valueOf(screenWeight));
        return screenWeight;
    }
}
