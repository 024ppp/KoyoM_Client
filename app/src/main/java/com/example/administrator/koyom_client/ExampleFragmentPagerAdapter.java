package com.example.administrator.koyom_client;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by yuki on 2016/09/28.
 */
public class ExampleFragmentPagerAdapter extends FragmentPagerAdapter {
    public ExampleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        /*
        switch (position) {
            case 0:
                return ExampleFragment.newInstance(0);
            case 1:
                return ExampleFragment.newInstance(1);
            case 2:
                return ExampleFragment.newInstance(2);
        }
        return null;
        */
        return ExampleFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "ページ" + (position + 1);
    }
}