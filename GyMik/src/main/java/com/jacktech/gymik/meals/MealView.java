package com.jacktech.gymik.meals;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by toor on 11.1.14.
 */
public class MealView extends TextView {

    private boolean ordered = false;

    public MealView(Context context) {
        super(context);
    }

    public MealView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MealView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isOrdered(){
        return ordered;
    }

    public void setOrdered(boolean ordered){
        this.ordered = ordered;
    }

}
