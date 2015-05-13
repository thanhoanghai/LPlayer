
package com.baby.policy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.baby.cartoonnetwork.CategoryActivity;
import com.baby.cartoonnetwork.R;
import com.baby.cartoonnetwork.SearchActivity;
import com.baby.fragments.MenuFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class BaseActivity extends SlidingFragmentActivity {

    private int mTitleRes;
    protected Fragment mFrag;

    public BaseActivity(int titleRes) {
        mTitleRes = titleRes;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(mTitleRes);

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);

        if (savedInstanceState == null) {
            FragmentTransaction t = this.getSupportFragmentManager()
                    .beginTransaction();
            mFrag = new MenuFragment();
            t.replace(R.id.menu_frame, mFrag);
            t.commit();
        } else {
//            mFrag = (Fragment) this.getSupportFragmentManager()
//                    .findFragmentById(R.id.menu_frame);
        }

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Force to call this method in order to change the icon of ActionBar
        getSupportActionBar().setDisplayOptions(
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
            case android.R.id.home:
                toggle();
                return true;
            case R.id.action_search:
                Intent searchActivity = new Intent(this, SearchActivity.class);
                startActivity(searchActivity);
                break;
            case R.id.action_category:
                Intent categoryActivity = new Intent(this, CategoryActivity.class);
                startActivityForResult(categoryActivity, 100);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateTitle(String title) {
        setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(BaseActivity.this, "hello", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
