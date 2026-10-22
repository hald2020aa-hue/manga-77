package com.mangaxw.clean;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public final class Ui {
    private Ui() {}
    public static final int BG = Color.rgb(12,17,27), CARD = Color.rgb(25,33,46);
    public static final int TEXT = Color.rgb(242,242,244), MUTED = Color.rgb(160,172,192);
    public static final int GOLD = Color.rgb(231,169,75);
    public static int dp(Activity a, float value) { return (int)(value*a.getResources().getDisplayMetrics().density+0.5f); }
    public static GradientDrawable shape(int color, int radius, Activity a) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(a,radius)); return d;
    }
    public static LinearLayout root(Activity a) {
        LinearLayout v = new LinearLayout(a); v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(dp(a,14),dp(a,10),dp(a,14),dp(a,10));v.setBackgroundColor(BG);
        if(android.os.Build.VERSION.SDK_INT>=30){
            v.setOnApplyWindowInsetsListener((view,insets)->{
                android.graphics.Insets b=insets.getInsets(android.view.WindowInsets.Type.systemBars());
                view.setPadding(dp(a,14)+b.left,dp(a,10)+b.top,dp(a,14)+b.right,dp(a,10)+b.bottom);
                return insets;
            });
        }
        return v;
    }
    public static LinearLayout row(Activity a) {
        LinearLayout v = new LinearLayout(a);v.setOrientation(LinearLayout.HORIZONTAL);v.setGravity(Gravity.CENTER_VERTICAL);return v;
    }
    public static TextView text(Activity a, String content, int sp, boolean bold) {
        TextView v=new TextView(a);v.setText(content);v.setTextColor(TEXT);v.setTextSize(sp);
        v.setGravity(Gravity.CENTER_VERTICAL);v.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        v.setPadding(dp(a,5),dp(a,9),dp(a,5),dp(a,9));return v;
    }
    public static Button button(Activity a, String text) {
        Button b = new Button(a);b.setText(text);b.setTextSize(13);b.setTextColor(TEXT);
        b.setAllCaps(false);b.setBackground(shape(CARD,12,a));
        b.setPadding(dp(a,10),0,dp(a,10),0);b.setMinHeight(dp(a,46));b.setMinimumHeight(dp(a,46));return b;
    }
    public static Button accent(Activity a,String text){
        Button b=button(a,text);b.setBackground(shape(GOLD,12,a));b.setTextColor(Color.rgb(17,21,28));return b;
    }
    public static LinearLayout.LayoutParams layout(Activity a,int w,int h,int margin) {
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(w,h);lp.setMargins(dp(a,margin),dp(a,margin),dp(a,margin),dp(a,margin));return lp;
    }
    public static EditText edit(Activity a, String hint) {
        EditText e = new EditText(a);e.setSingleLine(true);e.setHint(hint);e.setTextColor(TEXT);e.setHintTextColor(MUTED);
        e.setTextSize(15);e.setPadding(dp(a,12),dp(a,9),dp(a,12),dp(a,9));e.setBackground(shape(CARD,12,a));return e;
    }
    public static ScrollView scroll(Activity a, LinearLayout parent) {
        ScrollView s=new ScrollView(a);s.setFillViewport(false);parent.addView(s,new LinearLayout.LayoutParams(-1,0,1));return s;
    }
    public static void weight(LinearLayout row,View child){ row.addView(child,new LinearLayout.LayoutParams(0,-2,1)); }
    public static void message(Activity a, String s){Toast.makeText(a,s,Toast.LENGTH_LONG).show();}
}
