package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.fab;

import android.content.Context;
import android.graphics.Canvas;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.gordonwong.materialsheetfab.AnimatedFab;


public class MaterialSheetFab extends FloatingActionButton implements AnimatedFab {

    public MaterialSheetFab(Context context) {
        super(context);
    }

    public MaterialSheetFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialSheetFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public void show(float translationX, float translationY) {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void show() {
        show(0, 0);
    }

    @Override
    public void hide() {
        setVisibility(View.INVISIBLE);
    }
}
