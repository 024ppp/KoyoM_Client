package com.example.administrator.koyom_client;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.administrator.koyom_client.R.layout.toast;

//Viewpagerの中の要素
//public class Fragment extends android.support.v4.app.Fragment implements View.OnClickListener {
public class Fragment extends android.support.v4.app.Fragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {
    private final static String POSITION = "POSITION";
    private int mPosition;
    private ColorStateList mDefaultColor = null;
    int[] pages = { R.layout.zenhan, R.layout.kouhan};
    ArrayList<EditText> editTexts = new ArrayList<EditText>();
    ArrayList<TextView> textViews = new ArrayList<TextView>();
    ArrayList<TextView> textViews_Hantei = new ArrayList<TextView>();

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

        //editTexts作成
        setControls(view, position);
        //初期化
        initFragmentPage();

        //position保持
        mPosition = position;

        return view;
    }

    //手入力対応
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v != null) {
            //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Log.d("OnKey", "Enter : " + Integer.toString(v.getId()));
                pressedEnter(v.getId());
                return true;
            }
            return false;
        }
        else {
            return false;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v != null) {
            if (mPosition == 0) {
                switch (v.getId()) {
                    /*
                    case R.id.txtKokan:
                        EditText editText = editTexts.get(2);
                        editText.setText("OK");
                        break;
                    */
                }
            }
        }
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
    }

    //手入力対応の本処理
    //todo  MainActivityに情報を送れないものか...
    private void pressedEnter(int id) {
        switch (id) {
            case R.id.txtKokan:
                EditText editText = editTexts.get(2);
                editText.setText("Enter");

                break;
        }
    }

    public boolean checkHantei(String sWakuAmi){
        //
        TextView textView_Hantei;
        String txt_Hantei;
        //
        TextView textView_Set;
        String txt_Set;

        for (int i = 0; i < textViews_Hantei.size(); i++) {
            textView_Hantei = textViews_Hantei.get(i);
            txt_Hantei = textView_Hantei.getText().toString();

            if (TextUtils.isEmpty(txt_Hantei) || txt_Hantei.equals("NG") ) {
                textView_Set = textViews.get(i);
                txt_Set = textView_Set.getText().toString();

                if (sWakuAmi.equals(txt_Set)){
                    textView_Hantei.setText("OK");
                    textView_Hantei.setTextColor(mDefaultColor);
                    return true;
                }
                else {
                    textView_Hantei.setText("NG");
                    textView_Hantei.setTextColor(Color.RED);
                    return false;
                }
            }
        }

        return false;
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
        //todo 全て埋まった場合の動作を追加(要らない？)
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

    //TextViewに工程管理番号から取得した情報をセットする
    public void setKokanInfo(String[] info) {
        TextView textView;

        switch (mPosition) {
            case 0:
                for (int i = 0; i < textViews.size(); i++) {
                    textView = textViews.get(i);
                    textView.setText(info[i + 1]);
                }
                break;
            case 1:
                for (int i = 0; i < textViews.size(); i++) {
                    textView = textViews.get(i);
                    textView.setText(info[i + 3]);
                }

                //カメラから戻ったときに、カーソルがでない不具合の修正
                EditText editText = editTexts.get(0);
                editText.requestFocus();
                break;
        }
    }

    //各EditTextから、更新時に必要な情報を取得する
    public String getForUpdateText() {
        String txt = "";

        switch (mPosition) {
            case 0:
                EditText editText = editTexts.get(1);
                txt = editText.getText().toString();
                break;

            case 1:
                for (EditText editText1 : editTexts) {
                    txt += "," + editText1.getText().toString();
                }
                break;
        }

        return txt;
    }

    private void setControls(View view, int position){
        int[] txtId = null;
        int[] lblId = null;
        int[] lblId_Hantei = null;

        switch(position) {
            case 0:
                txtId = new int[] {R.id.txtSagyo, R.id.txtKikai, R.id.txtKokan};

                lblId = new int[] {R.id.lblSyori, R.id.lblSyoriNM};

                break;
            case 1:
                txtId = new int[] {R.id.txtWaku1, R.id.txtAmi2, R.id.txtWaku3, R.id.txtAmi4
                                  , R.id.txtWaku5, R.id.txtAmi6, R.id.txtWaku7};

                lblId = new int[] {R.id.lblSet1, R.id.lblSet2, R.id.lblSet3, R.id.lblSet4
                                  , R.id.lblSet5, R.id.lblSet6, R.id.lblSet7};

                lblId_Hantei = new int[] {R.id.lblHantei1, R.id.lblHantei2, R.id.lblHantei3, R.id.lblHantei4
                                         , R.id.lblHantei5, R.id.lblHantei6, R.id.lblHantei7};
                break;
        }

        //EditText
        for (int id : txtId) {
            EditText editText = (EditText) view.findViewById(id);
            //タグスキャン時の幅崩れ対策
            editText.setWidth(editText.getWidth());
            //FocusChange対応
            editText.setOnFocusChangeListener(this);
            //手入力対応
            editText.setOnKeyListener(this);

            editTexts.add(editText);
        }

        //TextView
        for (int id : lblId) {
            TextView textView = (TextView) view.findViewById(id);
            //幅崩れ対策
            textView.setWidth(textView.getWidth());

            //TextViewのデフォルト色を保持
            if (mDefaultColor == null) {
                mDefaultColor = textView.getTextColors();
            }

            textViews.add(textView);
        }

        //TextView_Hantei（2ページ目のみ）
        if (lblId_Hantei != null) {
            for (int id : lblId_Hantei) {
                TextView textView = (TextView) view.findViewById(id);
                //幅崩れ対策
                textView.setWidth(textView.getWidth());

                textViews_Hantei.add(textView);
            }
        }
    }

    public void initFragmentPage() {
        EditText editText = null;

        //EditText初期化
        for (int i = 0; i < editTexts.size(); i++) {
            editText = editTexts.get(i);
            editText.setText("");
            /*
            //保留：手入力の必要あり
            //タップされてもキーボードを出さなくする
            editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
            editText.setTextIsSelectable(true);
            */

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

        //TextView初期化
        for (TextView textView : textViews) {
            textView.setText("");
        }
        for (TextView textView : textViews_Hantei) {
            textView.setText("");
        }
    }
}
