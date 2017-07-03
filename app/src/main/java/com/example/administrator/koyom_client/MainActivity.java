package com.example.administrator.koyom_client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  //todo Fragmentへの直接アクセスは禁止。FPAdapter越しに関数を呼び出すよう改修
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    FPAdapter fpAdapter;
    Fragment fragment;
    TextView show;
    Button btnClear, btnUpd, btnCam;

    Handler handler;
    // サーバと通信するスレッド
    ClientThread clientThread;
    NfcWriter nfcWriter = null;
    //インスタンス化無しで使える
    ProcessCommand pc;

    String sSagyoName = "";
    private int mWakuamiNo;

    private static final int SETTING = 8888;
    private static final int RC_BARCODE_CAPTURE = 9001;

    //バイブ
    Vibrator vib;
    private long m_vibPattern_read[] = {0, 200};
    private long m_vibPattern_error[] = {0, 200, 200, 500};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //バイブ
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        setViews();

        // view取得
        show = (TextView) findViewById(R.id.show);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnUpd = (Button) findViewById(R.id.btnUpd);
        btnCam = (Button) findViewById(R.id.btnCam);

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

        //接続先を取得
        SharedPreferences prefs = getSharedPreferences("ConnectionData", Context.MODE_PRIVATE);
        final String ip = prefs.getString("ip", "");
        final int myPort = prefs.getInt("myPort", 0);
        clientThread = new ClientThread(handler, ip, myPort);
        // サーバ接続スレッド開始
        new Thread(clientThread).start();

        this.nfcWriter = new NfcWriter(this);

        btnClear.setOnClickListener(this);
        btnUpd.setOnClickListener(this);
        btnCam.setOnClickListener(this);

        //登録ボタンを無効化
        btnUpd.setEnabled(false);

        //Fragment切替時の振る舞い
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            //スクロール状態が変化したときに呼び出される
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_SETTLING) {
                    //int page = viewPager.getCurrentItem();
                    //show.append("" + page);
                }
            }
            @Override
            //ページが切り替わった時に呼び出される
            public void onPageSelected(int position) {
                //show.append("" + position);
            }
        });
    }

    private void setViews() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("Ｂ棟ふるい網目セット管理");
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
        //todo fpAdapterにsetTextOrderを実装。引数にページ数とメッセージを渡してやる
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
        else if (cmd.equals(pc.KOB.getString())) {
            //工程管理番号の検索結果が返ってくる
            if (!excmd.equals("")) {
                //受信値を分解、各項目にセット
                String[] info = excmd.split(",");
                //工管Noセット
                fragment.setTextOrder(info[0]);

                //処理粉情報、枠網情報をラベルにセット
                //1ページ目
                fragment.setKokanInfo(info);
                //2ページ目
                fragment = fpAdapter.getSelectFragment(1);
                fragment.setKokanInfo(info);

                setShowMessage(3);
            }
        }
        else if (cmd.equals(pc.UPD.getString())) {
            MyToast.makeText(this, "登録完了しました。", Toast.LENGTH_SHORT, 32f).show();
            initPage();
        }
        //TODO err時の振る舞い
        else if (cmd.equals(pc.ERR.getString())) {
            //バイブ
            vib.vibrate(m_vibPattern_error, -1);
            show.setText(excmd);
        }

    }

    //タグテキストのコマンド値によって分岐
    private void selectMotionTagText(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);
        //todo fpAdapterにsetTextOrderを実装。引数にページ数とメッセージを渡してやる
        fragment = fpAdapter.getCurrentFragment();
        int page = viewPager.getCurrentItem();

        if (cmd.equals(pc.KIK.getString())) {
            if (page == 0) {
                if (fragment.checkFocused(1)) {
                    //TAGテキストをそのまま送信
                    sendMsgToServer(sMsg);
                }
            }
        }
        else if (cmd.equals(pc.WAK.getString()) ||
                  cmd.equals(pc.AMI.getString())) {

            if (page == 1) {
                if (fragment.checkHantei(excmd)){
                    fragment.setTextOrder(excmd);
                    setShowMessage(4);
                }
            }
        }
    }

    //登録ボタン押下時にサーバに送る更新値の生成
    private String createUpdText() {
        Fragment frg;
        String txt = "";

        for (int i = 0; i < 2; i++) {
            //i = 0 : 機械No取得 /i = 1 : 枠網取得
            frg = fpAdapter.getSelectFragment(i);
            txt += frg.getForUpdateText();
        }
        return txt;
    }

    private void initPage() {
        Fragment frg;

        for (int i = 0; i < 2; i++) {
            frg = fpAdapter.getSelectFragment(i);
            frg.initFragmentPage();
        }

        //1ページ目に戻る
        viewPager.setCurrentItem(0);
        //登録ボタンを無効化
        btnUpd.setEnabled(false);

        showSelectSagyo();
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
        if (v != null) {

            //todo fpAdapterにsetTextOrderを実装。引数にページ数とメッセージを渡してやる
            fragment = fpAdapter.getCurrentFragment();

            switch (v.getId()) {
                case R.id.btnCam :
                    // launch barcode activity.
                    Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                    startActivityForResult(intent, RC_BARCODE_CAPTURE);
                    break;

                case R.id.btnUpd :
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

                    break;

                case R.id.btnClear :
                    initPage();
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
        //バイブ
        vib.vibrate(m_vibPattern_read, -1);
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
                break;
            case 3:
                //viewPager.setCurrentItem(1);
                show.setText("1枠をスキャンしてください。");
                //次の枠網番号をセット
                mWakuamiNo = 2;
                break;
            case 4:
                String wakuamiNo = Integer.toString(mWakuamiNo);

                if (mWakuamiNo % 2 == 0) {
                    show.setText(wakuamiNo + "網をスキャンしてください。");
                }
                else {
                    show.setText(wakuamiNo + "枠をスキャンしてください。");
                }
                //次の枠網番号をセット
                mWakuamiNo++;

                if (mWakuamiNo > 8) {
                    show.setText("登録ボタンを押してください。");
                    btnUpd.setEnabled(true);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        //todo fpAdapterにsetTextOrderを実装。引数にページ数とメッセージを渡してやる
        fragment = fpAdapter.getCurrentFragment();

        switch (requestCode) {
            case 1001:
                if (resultCode == RESULT_OK) {
                    //txtSagyo.setText(bundle.getString("key.StringData"));
                    String value = bundle.getString("key.StringData");
                    fragment.setTextOrder(value);
                    setShowMessage(1);

                } else if (resultCode == RESULT_CANCELED) {
                    show.setText(
                            "requestCode:" + requestCode
                                    + "\nresultCode:" + resultCode
                                    + "\ndata:" + bundle.getString("key.canceledData"));
                }
                break;

            case SETTING:
                Toast.makeText(this, "Setting has been completed.", Toast.LENGTH_SHORT).show();
                break;

            case RC_BARCODE_CAPTURE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        String value = barcode.displayValue;
                        int page = viewPager.getCurrentItem();

                        //ページ１で、工管Noにフォーカスがある場合のみ、サーバにメッセージ送信
                        if (page == 0) {
                            if (fragment.checkFocused(2)) {
                                String msg = pc.KOB.getString() + value;
                                sendMsgToServer(msg);
                            }
                            else {
                                //工管No以外でカメラ使用を許可するかどうか
                                //現時点では許可しない
                                //fragment.setTextOrder(value);
                            }
                        }
                        else {
                            //工管No以外でカメラ使用を許可するかどうか
                            //現時点では許可しない
                            //fragment.setTextOrder(value);
                        }

                        Log.d("Barcode", "Barcode read: " + barcode.displayValue);
                    } else {
                        Log.d("Barcode", "No barcode captured, intent data is null");
                    }
                } else {

                }

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
            //設定画面呼び出し
            Intent intent = new Intent(this, Setting.class);
            int requestCode = SETTING;
            startActivityForResult(intent, requestCode);
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
