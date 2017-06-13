package com.example.administrator.koyom_client;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

//Viewpagerの中の要素
//public class Fragment extends android.support.v4.app.Fragment implements View.OnClickListener {
public class Fragment extends android.support.v4.app.Fragment {
    private final static String POSITION = "POSITION";
    int[] pages = { R.layout.zenhan, R.layout.kouhan};
    int cnt = 0;
    ArrayList<EditText> editTexts = new ArrayList<EditText>();

    public static Fragment newInstance(int position) {
        Fragment frag = new Fragment();
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
        /*
        View view = inflater.inflate(R.layout.fragment_main, null);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.fragment_main_linearlayout);
        linearLayout.setBackgroundResource(getArguments().getInt(BACKGROUND_COLOR));
        */
        int position = getArguments().getInt(POSITION);
        View view = inflater.inflate(pages[position], null);

        setControls(view, position);
        //初期化
        initFragmentPage();
        return view;
    }

    public void setTextOrder(String txt){
        EditText editText = null;

        //現在、空白のEditTextの中で一番上にあるものを取得する
        for (EditText edt : editTexts) {
            if (TextUtils.isEmpty(edt.getText().toString())) {
                editText = edt;
                break;
            }
        }
        //テキストをセット
        if (editText != null) {
            editText.setText(txt);
            //値が入ったものは、選択できないようにする
            editText.setFocusableInTouchMode(false);
            editText.setFocusable(false);
        }

        //次の空白のEditTextにフォーカスを移動する
        //todo 全て埋まった場合の動作を追加
        for (EditText edtNext : editTexts) {
            if (TextUtils.isEmpty(edtNext.getText().toString())) {
                edtNext.setFocusableInTouchMode(true);
                edtNext.setFocusable(true);
                edtNext.requestFocus();
                break;
            }
        }
    }

    public boolean checkFocused(int i) {
        EditText editText = editTexts.get(i);

        Boolean result = editText.isFocused();
        return result;
    }

    private void setControls(View view, int position){
        int[] txtId = null;

        switch(position) {
            case 0:
                txtId = new int[] {R.id.txtSagyo, R.id.txtKikai, R.id.txtKokan};
                break;
            case 1:
                txtId = new int[] {R.id.txtWaku1, R.id.txtAmi2, R.id.txtWaku3, R.id.txtAmi4
                                  , R.id.txtWaku5, R.id.txtAmi6, R.id.txtWaku7};
                break;
        }

        for (int id : txtId) {
            editTexts.add((EditText) view.findViewById(id));
        }
    }

    public void initFragmentPage() {
        EditText editText = null;

        for (int i = 0; i < editTexts.size(); i++) {
            editText = editTexts.get(i);
            editText.setText("");

            if (i == 0) {
                editText.setFocusableInTouchMode(true);
                editText.setFocusable(true);
            }
            else {
                editText.setFocusableInTouchMode(false);
                editText.setFocusable(false);
            }
        }

        editText.requestFocus();
    }
}
