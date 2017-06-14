package com.example.administrator.koyom_client;

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
                    case R.id.txtKokan:
                        EditText editText = editTexts.get(2);
                        editText.setText("OK");
                        break;
                }
            }
        }
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {}

    //手入力対応の本処理
    private void pressedEnter(int id) {
        switch (id) {
            case R.id.txtKokan:
                EditText editText = editTexts.get(2);
                editText.setText("Enter");
                break;
        }
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
        int[] lblId = null;

        switch(position) {
            case 0:
                txtId = new int[] {R.id.txtSagyo, R.id.txtKikai, R.id.txtKokan};
                break;
            case 1:
                txtId = new int[] {R.id.txtWaku1, R.id.txtAmi2, R.id.txtWaku3, R.id.txtAmi4
                                  , R.id.txtWaku5, R.id.txtAmi6, R.id.txtWaku7};
                //lblの幅崩れ対策
                lblId = new int[] {R.id.lblSet1, R.id.lblHantei1,
                                    R.id.lblSet2, R.id.lblHantei2,
                                    R.id.lblSet3, R.id.lblHantei3,
                                    R.id.lblSet4, R.id.lblHantei4,
                                    R.id.lblSet5, R.id.lblHantei5,
                                    R.id.lblSet6, R.id.lblHantei6,
                                    R.id.lblSet7, R.id.lblHantei7 };
                for (int id : lblId) {
                    TextView textView = (TextView) view.findViewById(id);
                    textView.setWidth(textView.getWidth());
                }
                break;
        }

        for (int id : txtId) {
            EditText editText = (EditText) view.findViewById(id);
            //タグスキャン時の幅崩れ対策
            editText.setWidth(editText.getWidth());

            if (position == 0) {
                //FocusChange対応
                editText.setOnFocusChangeListener(this);
                //手入力対応
                editText.setOnKeyListener(this);
            }

            editTexts.add(editText);
        }
    }

    public void initFragmentPage() {
        EditText editText = null;

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
    }
}
