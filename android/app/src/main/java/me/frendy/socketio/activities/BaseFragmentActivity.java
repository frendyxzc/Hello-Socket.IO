package me.frendy.socketio.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import me.frendy.socketio.R;

/**
 * Created by iimedia on 2017/3/23.
 */
public class BaseFragmentActivity extends AppCompatActivity {
    protected Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_fragment);
    }

    protected void setDefaultFragment(Fragment fragment) {
        // 开启Fragment事务
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // 使用fragment的布局替代frame_layout的控件并提交事务
        fragmentTransaction.replace(R.id.frame_layout, fragment).commit();
        currentFragment = fragment;
    }

    protected void switchFragment(Fragment fragment) {
        if (fragment != currentFragment) {
            if (!fragment.isAdded()) {
                getSupportFragmentManager().beginTransaction().hide(currentFragment)
                        .add(R.id.frame_layout, fragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction().hide(currentFragment)
                        .show(fragment).commit();
            }
            currentFragment = fragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentFragment.setUserVisibleHint(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentFragment.setUserVisibleHint(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        currentFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
