
package com.baby.policy;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityHelper;

public class SlidingFragmentActivity extends ActionBarActivity implements SlidingActivityBase {
    private SlidingActivityHelper mHelper;

    public SlidingFragmentActivity() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHelper = new SlidingActivityHelper(this);
        this.mHelper.onCreate(savedInstanceState);
    }

    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mHelper.onPostCreate(savedInstanceState);
    }

    public View findViewById(int id) {
        View v = super.findViewById(id);
        return v != null ? v : this.mHelper.findViewById(id);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mHelper.onSaveInstanceState(outState);
    }

    public void setContentView(int id) {
        this.setContentView(this.getLayoutInflater().inflate(id, (ViewGroup) null));
    }

    public void setContentView(View v) {
        this.setContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setContentView(View v, ViewGroup.LayoutParams params) {
        super.setContentView(v, params);
        this.mHelper.registerAboveContentView(v, params);
    }

    @Override
    public void setBehindContentView(int id) {
        setBehindContentView(getLayoutInflater().inflate(id, null));
    }

    @Override
    public void setBehindContentView(View v) {
        setBehindContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setBehindContentView(View v, ViewGroup.LayoutParams params) {
        this.mHelper.setBehindContentView(v, params);
    }

    public SlidingMenu getSlidingMenu() {
        return this.mHelper.getSlidingMenu();
    }

    public void toggle() {
        this.mHelper.toggle();
    }

    public void showContent() {
        this.mHelper.showContent();
    }

    public void showMenu() {
        this.mHelper.showMenu();
    }

    public void showSecondaryMenu() {
        this.mHelper.showSecondaryMenu();
    }

    public void setSlidingActionBarEnabled(boolean b) {
        this.mHelper.setSlidingActionBarEnabled(b);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean b = this.mHelper.onKeyUp(keyCode, event);
        return b ? b : super.onKeyUp(keyCode, event);
    }
}
