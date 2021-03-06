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
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "作業者名";
            case 1:
                return "機械,工管No";
            case 2:
                return "枠網";
            default:
                return "ページ" + (position + 1);
        }
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

    //以下、Fragmentのメソッド本体へのアクセス部
    public void setTextOrder(String txt){
        mCurrentFragment.setTextOrder(txt);
    }

    public boolean checkHantei(String sWakuAmi){
        return mCurrentFragment.checkHantei(sWakuAmi);
    }

    public boolean checkFocused(int i) {
        return mCurrentFragment.checkFocused(i);
    }

    public boolean checkIsEmpty(int position, int i) {
        Fragment frg = fragments.get(position);
        return frg.checkIsEmpty(i);
    }

    public void setKokanInfo(String[] info) {
        Fragment frg;

        for (int i = 1; i < 3; i++) {
            frg = fragments.get(i);
            frg.setKokanInfo(info);
        }
    }

    public String createUpdText() {
        Fragment frg;
        String txt = "";

        for (int i = 1; i < 3; i++) {
            //i = 0 : 機械No取得 /i = 1 : 枠網取得
            frg = fragments.get(i);
            txt += frg.createUpdText();
        }
        return txt;
    }

    public void initFragmentPage() {
        Fragment frg;

        for (int i = 1; i < 3; i++) {
            frg = fragments.get(i);
            frg.initFragmentPage();
        }
    }

    //取得した作業者名をセットする
    public void setListSagyoName(String names) {
        Fragment frg = fragments.get(0);
        frg.setListSagyoName(names);
    }

    //選択された作業者名をセットする
    public void setSelectedSagyoName(String name) {
        Fragment frg = fragments.get(1);
        frg.setSelectedSagyoName(name);
    }
}