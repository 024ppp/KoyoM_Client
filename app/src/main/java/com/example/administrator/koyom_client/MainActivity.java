package com.example.administrator.koyom_client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    FPAdapter fpAdapter;
    Fragment fragment;
    TextView show;
    Button btnClear, btnUpd;
    EditText txtHoge;

    Handler handler;
    // サーバと通信するスレッド
    ClientThread clientThread;
    NfcWriter nfcWriter = null;
    //インスタンス化無しで使える
    ProcessCommand pc;

    String sSagyoName = "";
    boolean focusFlg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();

        // view取得
        show = (TextView) findViewById(R.id.show);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnUpd = (Button) findViewById(R.id.btnUpd);
        txtHoge = (EditText) findViewById(R.id.txtHoge);

        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                // サブスレッドからのメッセージ
                if (msg.what == 0x123) {
                    // 表示する
                    String sMsg = msg.obj.toString();
                    //show.append("\n PCから受信-" + sMsg);
                    selectMotionWhenReceiving(sMsg);
                }
            }
        };

        //IP & Port取得
        final String ip = getString(R.string.ip);
        final int myPort = getResources().getInteger(R.integer.myPort);
        clientThread = new ClientThread(handler, ip, myPort);
        // サーバ接続スレッド開始
        new Thread(clientThread).start();

        this.nfcWriter = new NfcWriter(this);

        btnClear.setOnClickListener(this);
        btnUpd.setOnClickListener(this);

        //Fragment切替時の振る舞い
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            //スクロール状態が変化したときに呼び出される
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_SETTLING) {
                    int page = viewPager.getCurrentItem();
                    show.append("" + page);
                }
            }
            @Override
            //ページが切り替わった時に呼び出される
            public void onPageSelected(int position) {
                show.append("" + position);
            }
        });
    }

    private void setViews() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        FragmentManager manager = getSupportFragmentManager();
        fpAdapter = new FPAdapter(manager);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(fpAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    //受信した文字列のコマンド値によって分岐（switch文ではenum使えず...if文汚し）
    private void selectMotionWhenReceiving(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);
        //todo fragmentアクセス時に取得するしかないのか？
        fragment = fpAdapter.getCurrentFragment();

        if (cmd.equals(pc.SAG.getString())) {
            sSagyoName = excmd;
            showSelectSagyo();
        }
        else if (cmd.equals(pc.KIK.getString())) {
            //Vコン存在チェック後、検索結果が返ってくる
            fragment.setTextOrder(excmd);
            setShowMessage(2);
        }
        else if (cmd.equals(pc.UPD.getString())) {
            //Toast.makeText(this, "登録完了しました。", Toast.LENGTH_SHORT).show();
            MyToast.makeText(this, "登録完了しました。", Toast.LENGTH_SHORT, 32f).show();
        }
        //TODO err時の振る舞い

    }

    //タグテキストのコマンド値によって分岐
    private void selectMotionTagText(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);
        //todo fragmentアクセス時に取得するしかないのか？
        fragment = fpAdapter.getCurrentFragment();

        if (cmd.equals(pc.KIK.getString())) {
            if (fragment.checkFocused(1)) {
                //TAGテキストをそのまま送信
                sendMsgToServer(sMsg);
            }
        }
        else if (cmd.equals(pc.AM1.getString())) {

        }
    }

    //登録ボタン押下時にサーバに送る更新値の生成
    private String createUpdText() {
        String txt = "";
        /*
        ●更新値のフォーマット
        各コマンド文字列 + 値, 各コマンド文字列 + 値,･･･
        ex:VKO11,AM1123
        */
        txt += pc.KIK.getString();
        txt += ",";
        txt += pc.AM1.getString();


        return txt;
    }

    private void initPage() {
        Fragment frg;
        frg = fpAdapter.getSelectFragment(0);
        frg.initFragmentPage();
        frg = fpAdapter.getSelectFragment(1);
        frg.initFragmentPage();
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
        if (v != null) {

            //todo fragmentアクセス時に取得するしかないのか？
            fragment = fpAdapter.getCurrentFragment();

            switch (v.getId()) {
                case R.id.btnUpd :
                    /*
                    if (!inputCheck()) {
                        break;
                    }
                    //Dialog(OK,Cancel Ver.)
                    new AlertDialog.Builder(this)
                            .setTitle("確認")
                            .setMessage("登録しますか？")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // OK button pressed
                                    //更新値の生成
                                    String updText = createUpdText();
                                    sendMsgToServer(pc.UPD.getString() + updText);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    */
                    show.append("upd");
                    MyToast.makeText(this, "upd", Toast.LENGTH_SHORT, 32f).show();
                    fragment.setTextOrder("upd");
                    //viewPager.setCurrentItem(0);
                    break;

                case R.id.btnClear :
                    initPage();
                    break;
            }
        }
    }
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.txtSagyo :
                    if (hasFocus) {
                        showSelectSagyo();
                    }
                    break;

            }
        }
    }


    @Override
    //タグを読み込んだ時に実行される
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String tagText = "";
        tagText = this.nfcWriter.getTagText(intent);

        selectMotionTagText(tagText);
    }

    //サーバへメッセージを送信する
    private void sendMsgToServer(String sMsg) {
        try {
            // メッセージ送信
            Message msg = new Message();
            msg.what = 0x345;   //？
            msg.obj = sMsg;
            clientThread.revHandler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setShowMessage(int order) {
        switch (order) {
            case 1:
                show.setText("機械Noをスキャンしてください。");
                break;
            case 2:
                show.setText("工管番号をスキャンしてください。");
                //test
                viewPager.setCurrentItem(1);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();

        switch (requestCode) {
            case 1001:
                if (resultCode == RESULT_OK) {
                    //txtSagyo.setText(bundle.getString("key.StringData"));

                    //todo fragmentアクセス時に取得するしかないのか？
                    fragment = fpAdapter.getCurrentFragment();
                    fragment.setTextOrder(bundle.getString("key.StringData"));
                    setShowMessage(1);

                } else if (resultCode == RESULT_CANCELED) {
                    show.setText(
                            "requestCode:" + requestCode
                                    + "\nresultCode:" + resultCode
                                    + "\ndata:" + bundle.getString("key.canceledData"));
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent = this.createPendingIntent();
        // Enable NFC adapter
        this.nfcWriter.enable(this, pendingIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable NFC adapter
        this.nfcWriter.disable(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.nfcWriter = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //作業者選択画面呼び出し
    private void showSelectSagyo() {
        if (sSagyoName.equals("")) {
            show.setText("作業者名取得エラー。");
            return;
        }
        Intent intent = new Intent(this, SelectSagyo.class);
        intent.putExtra("name", sSagyoName);
        int requestCode = pc.SAG.getInt();
        startActivityForResult(intent, requestCode);
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
