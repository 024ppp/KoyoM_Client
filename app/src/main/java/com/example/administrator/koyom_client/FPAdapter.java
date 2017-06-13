package com.example.administrator.koyom_client;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by yuki on 2016/09/28.
 */
public class FPAdapter extends FragmentPagerAdapter {
    private Fragment mCurrentFragment;
    ArrayList<Fragment> fragments = new ArrayList<Fragment>();

    public FPAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        Fragment fragment = Fragment.newInstance(position);
        fragments.add(fragment);

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "ページ" + (position + 1);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentFragment != object) {
            mCurrentFragment = (Fragment) object;
        }
        super.setPrimaryItem(container, position, object);
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public Fragment getSelectFragment(int position) {
        Fragment frg = fragments.get(position);
        return frg;
    }
}