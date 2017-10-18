package com.example.administrator.koyom_client;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.administrator.koyom_client.R.layout.toast;

//Viewpagerの中の要素
public class Fragment extends android.support.v4.app.Fragment implements View.OnClickListener, View.OnKeyListener {
    private final static String POSITION = "POSITION";
    private int mPosition;
    private String m_SagyoName = "";
    private ColorStateList mDefaultColor = null;
    private boolean mBarcodeFlg;
    int[] pages = { R.layout.activity_select_sagyo, R.layout.zenhan, R.layout.kouhan};
    ArrayList<EditText> editTexts = new ArrayList<EditText>();
    ArrayList<TextView> textViews = new ArrayList<TextView>();
    ArrayList<TextView> textViews_Hantei = new ArrayList<TextView>();
    ListView lv;
    EditText txtSagyo, txtKokan;
    TextView emptyTextView;

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

        int position = getArguments().getInt(POSITION);
        //position保持
        mPosition = position;
        View view = inflater.inflate(pages[position], null);

        //コントロール設定
        if (position == 0) {
            setListView(view);
        }
        else {
            setControls(view, position);
            //初期化
            initFragmentPage();
        }
        return view;
    }

    //手入力対応
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        try {
            if (v != null) {
                //イベントを取得するタイミングには、ボタンが押されてなおかつエンターキーだったときを指定
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //DEBUG
                    Log.d("OnKey", "Enter : " + Integer.toString(v.getId()));
                    //エンターの度にGetする（クラス変数とし、インスタンス化時にGetしたものを使ってもエラー）
                    MainActivity mainActivity = (MainActivity) getActivity();
                    //ページ分岐
                    switch (mPosition){
                        case 1:
                            switch (v.getId()) {
                                case R.id.txtKikai:
                                    pressedEnter(mainActivity, 0);
                                    break;
                                case R.id.txtKokan:
                                    pressedEnter(mainActivity, 1);
                                    break;
                            }
                            break;
                        case 2:
                            //枠網の手入力
                            for (int i = 0; i < 7; i++) {
                                if (checkFocused(i)) {
                                    pressedEnter(mainActivity, i);
                                    break;
                                }
                            }
                            break;
                    }
                    //キーボードをしまう
                    InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
            else {
                return false;
            }

        } catch (ClassCastException e) {
            throw new ClassCastException("activity が OnOkBtnClickListener を実装していません.");
        }
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
    }

    //手入力対応の本処理
    private void pressedEnter(MainActivity mainActivity, int num) {
        EditText editText = editTexts.get(num);
        String msg = editText.getText().toString();
        //フォーカス中のEditTextをクリア
        editText.setText("");
        //ページ分岐
        switch (mPosition){
            case 1:
                mainActivity.onEnterPushed(msg, num);
                break;
            case 2:
                mainActivity.onEnterPushed(msg, num + 10);
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

                //設定値が空（工程管理番号が未スキャン）の場合は処理を行わない
                if (txt_Set.equals("")) {
                    return false;
                }

                //判定処理
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
        //バーコードリード時の２重起動を防止
        if (mPosition == 1) {
            if (editText.getId() == R.id.txtKokan) {
                mBarcodeFlg = false;
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
        return editText.isFocused();
    }

    public boolean checkIsEmpty(int i) {
        if (i < 50) {
            EditText editText = editTexts.get(i);
            return TextUtils.isEmpty(editText.getText());
        }
        else {
            return TextUtils.isEmpty((txtSagyo.getText()));
        }
    }

    //TextViewに工程管理番号から取得した情報をセットする
    public void setKokanInfo(String[] info) {
        TextView textView;
        switch (mPosition) {
            case 1:
                for (int i = 0; i < textViews.size(); i++) {
                    textView = textViews.get(i);
                    textView.setText(info[i + 1]);
                }
                break;
            case 2:
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
    public String createUpdText() {
        String txt = "";

        switch (mPosition) {
            case 1:
                //mPosition = 1 : 機械No取得
                EditText editText = editTexts.get(0);
                txt = editText.getText().toString();
                break;

            case 2:
                //mPosition = 2 : 枠網取得
                for (EditText editText1 : editTexts) {
                    txt += "," + editText1.getText().toString();
                }
                break;
        }
        return txt;
    }

    //取得した作業者名をListViewにセット
    public void setListSagyoName(String names) {
        String[] members = names.split(",");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1, members);
        lv.setAdapter(adapter);
        m_SagyoName = names;
    }

    //選択された作業者名をセット
    public void setSelectedSagyoName(String name) {
        if (mPosition == 1) {
            txtSagyo.setText(name);
        }
    }

    //ListViewの設定
    private void setListView(View view) {
        lv = (ListView) view.findViewById(R.id.list);
        //ListView で表示リスト項目の無い場合のビューを指定
        emptyTextView = (TextView) view.findViewById(R.id.emptyTextView);
        lv.setEmptyView(emptyTextView);
        //リスト項目がクリックされた時の処理
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                String item = (String) listView.getItemAtPosition(position);
                //MainActivity経由で作業者名をセット
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.adaptSelectedSagyoName(item);
            }
        });
        if (!m_SagyoName.equals("")) {
            setListSagyoName(m_SagyoName);
        }
    }

    private void setControls(View view, int position){
        int[] txtId = null;
        int[] lblId = null;
        int[] lblId_Hantei = null;

        switch(position) {
            case 1:
                //txtId = new int[] {R.id.txtSagyo, R.id.txtKikai, R.id.txtKokan};
                txtId = new int[] {R.id.txtKikai, R.id.txtKokan};
                lblId = new int[] {R.id.lblSyori, R.id.lblSyoriNM};

                //txtSagyoのみ特別扱い
                txtSagyo = (EditText) view.findViewById(R.id.txtSagyo);
                txtSagyo.setFocusableInTouchMode(false);
                txtSagyo.setFocusable(false);

                //バーコードリーダー対応
                txtKokan = (EditText) view.findViewById(R.id.txtKokan);
                txtKokan.addTextChangedListener(new TextWatcher(){
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count){
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        //DEBUG
                        //Log.d("test", "afterTextChanged");
                        if (txtKokan.getText().length() >= 6) {
                            if (mBarcodeFlg) {
                                //工程管理Noが6文字以上になったら、Enter押下イベントを発生させる
                                MainActivity mainActivity = (MainActivity) getActivity();
                                pressedEnter(mainActivity, 1);
                                //キーボードをしまう
                                InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(txtKokan.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            }
                        }
                    }
                });

                break;
            case 2:
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

        switch(mPosition) {
            case 0:
                return;
            case 1:
                txtSagyo.setText("");
                mBarcodeFlg = true;
                break;
        }

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

        //ページ最初のコントロールにフォーカスをあてる
        editText = editTexts.get(0);
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
