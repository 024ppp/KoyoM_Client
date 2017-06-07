package com.example.administrator.koyom_client;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yuki on 2016/09/28.
 */

public class ExampleFragment extends Fragment {
    private final static String POSITION = "background_color";
    int[] pages = { R.layout.zenhan, R.layout.kouhan, R.layout.fragment_main};

    public static ExampleFragment newInstance(int position) {
        ExampleFragment frag = new ExampleFragment();
        Bundle b = new Bundle();
        b.putInt(POSITION, position);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int position = getArguments().getInt(POSITION);

        /*
        View view = inflater.inflate(R.layout.fragment_main, null);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.fragment_main_linearlayout);
        linearLayout.setBackgroundResource(getArguments().getInt(BACKGROUND_COLOR));
        */
        View view = inflater.inflate(pages[position], null);

        return view;
    }
}
