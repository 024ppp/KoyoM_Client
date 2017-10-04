package com.example.administrator.koyom_client;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    FPAdapter fpAdapter;
    TextView show;
    Button btnClear, btnUpd, btnCam;
    Handler handler;
    String ip;
    int myPort;
    // サーバと通信するスレッド
    ClientThread clientThread;
    NfcTags nfcTags = null;
    //インスタンス化無しで使える
    ProcessCommand pc;

    private int m_wakuamiNo;

    private static final int SETTING = 8888;
    private static final int RC_BARCODE_CAPTURE = 9001;

    //バイブ
    Vibrator vib;
    private long m_vibPattern_read[] = {0, 200};
    private long m_vibPattern_error[] = {0, 200, 200, 500};

    // BroadcastReceiver
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ハンドラー
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
        ip = prefs.getString("ip", "");
        myPort = prefs.getInt("myPort", 0);
        clientThread = new ClientThread(handler, ip, myPort, true);
        // サーバ接続スレッド開始
        new Thread(clientThread).start();

        //バイブ
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        // view取得
        setViews();

        //NFCタグ
        this.nfcTags = new NfcTags(this);

        // 受信する情報の種類を設定
        // 電源OFF,充電開始の通知を受け取る
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);

        // 受信した場合の処理の記述
        mReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                //アプリを終了
                finishAndRemoveTask();
            }
        };
        // BroadcastReceiverを登録する
        this.registerReceiver(mReceiver, filter);

        /*
        //Fragment切替時の振る舞い（未使用）
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
        */
    }

    //Fragmentからのエンターキー押下処理。手入力対応
    public void onEnterPushed(String msg, int num) {
        switch (num) {
            case 0:
                //機械No
                sendMsgToServer(pc.KIK.getString() + msg);
                break;
            case 1:
                //工程管理番号
                sendMsgToServer(pc.KOB.getString() + msg);
                break;
            default:
                //枠網
                if (fpAdapter.checkHantei(msg)){
                    fpAdapter.setTextOrder(msg);
                    setShowMessage(4);
                }
                break;
        }
    }

    //受信した文字列のコマンド値によって分岐（switch文ではenum使えず...if文汚し）
    private void selectMotionWhenReceiving(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);

        if (cmd.equals(pc.SAG.getString())) {
            //作業者名をセット
            if (!excmd.equals("")) {
                fpAdapter.setListSagyoName(excmd);
                setShowMessage(0);
            }
        }
        else if (cmd.equals(pc.KIK.getString())) {
            //作業者名が空白かどうかチェック
            if (fpAdapter.checkIsEmpty(1, 99)) {
                setShowMessage(0);
                //バイブ エラー
                vib.vibrate(m_vibPattern_error, -1);
            }
            else {
                //Vコン存在チェック後、検索結果が返ってくる
                fpAdapter.setTextOrder(excmd);
                //カメラ起動を有効化
                btnCam.setEnabled(true);
                setShowMessage(2);
            }
        }
        else if (cmd.equals(pc.KOB.getString())) {
            //工程管理番号の検索結果が返ってくる
            if (!excmd.equals("")) {
                //受信値を分解、各項目にセット
                String[] info = excmd.split(",");
                //工管Noセット
                fpAdapter.setTextOrder(info[0]);
                //処理粉情報、枠網情報をラベルにセット
                fpAdapter.setKokanInfo(info);

                //ページ遷移
                viewPager.setCurrentItem(2);
                //カメラ起動を無効化
                btnCam.setEnabled(false);
                //バイブ
                vib.vibrate(m_vibPattern_read, -1);
                setShowMessage(3);
            }
        }
        else if (cmd.equals(pc.UPD.getString())) {
            MyToast.makeText(this, "登録完了しました。", Toast.LENGTH_SHORT, 32f).show();
            initPage();
        }
        else if (cmd.equals(pc.MSG.getString())) {
            show.setText(excmd);
        }
        else if (cmd.equals(pc.ERR.getString())) {
            //作業者名が空白かどうかチェック
            if (fpAdapter.checkIsEmpty(1, 99)) {
                setShowMessage(0);
            }
            else {
                show.setText(excmd);
            }
            //バイブ エラー
            vib.vibrate(m_vibPattern_error, -1);
        }
    }

    //タグテキストのコマンド値によって分岐
    private void selectMotionTagText(String sMsg) {
        String cmd = pc.COMMAND_LENGTH.getCmdText(sMsg);
        String excmd  = pc.COMMAND_LENGTH.getExcludeCmdText(sMsg);
        int page = viewPager.getCurrentItem();

        if (cmd.equals(pc.KIK.getString())) {
            if (page == 1) {
                if (fpAdapter.checkFocused(0)) {
                    //TAGテキストをそのまま送信
                    sendMsgToServer(sMsg);
                }
            }
        }
        else if (cmd.equals(pc.WAK.getString()) ||
                  cmd.equals(pc.AMI.getString())) {
            if (page == 2) {
                if (fpAdapter.checkHantei(excmd)){
                    fpAdapter.setTextOrder(excmd);
                    setShowMessage(4);
                }
            }
        }
        else {
            show.setText("タグテキストエラー！");
        }
    }

    private void initPage() {
        //初期化
        fpAdapter.initFragmentPage();
        //登録ボタンを無効化
        btnUpd.setEnabled(false);
        setShowMessage(0);
    }

    @Override
    //クリック処理の実装
    public void onClick(View v) {
        if (v != null) {
            switch (v.getId()) {
                case R.id.btnCam :
                    // launch barcode activity.
                    show.setText("カメラ起動中・・・");
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
                                    String updText = fpAdapter.createUpdText();
                                    sendMsgToServer(pc.UPD.getString() + updText);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    break;

                case R.id.btnClear :
                    //Dialog(OK,Cancel Ver.)
                    new AlertDialog.Builder(this)
                            .setTitle("確認")
                            .setMessage("クリアしてよろしいですか？")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // OK button pressed
                                    initPage();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    break;
            }
        }
    }

    @Override
    //タグを読み込んだ時に実行される
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String tagText = "";
        tagText = this.nfcTags.getTagText(intent);
        if (!tagText.equals("")) {
            selectMotionTagText(tagText);
        }
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
            case 0:
                show.setText("作業者名を選択してください。");
                //1ページ目に戻る
                viewPager.setCurrentItem(0);
                break;
            case 1:
                show.setText("機械Noをタッチしてください。");
                break;
            case 2:
                show.setText("工管番号をスキャンしてください。");
                break;
            case 3:
                show.setText("1枠をタッチしてください。");
                //次の枠網番号をセット
                m_wakuamiNo = 2;
                break;
            case 4:
                String wakuamiNo = Integer.toString(m_wakuamiNo);

                if (m_wakuamiNo % 2 == 0) {
                    show.setText(wakuamiNo + "網をタッチしてください。");
                }
                else {
                    show.setText(wakuamiNo + "枠をタッチしてください。");
                }
                //次の枠網番号をセット
                m_wakuamiNo++;

                if (m_wakuamiNo > 8) {
                    show.setText("全てOKです。\n登録してください。");
                    btnUpd.setEnabled(true);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SETTING:
                Toast.makeText(this, "設定が完了しました。", Toast.LENGTH_SHORT).show();
                break;

            case RC_BARCODE_CAPTURE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        //戻るボタン対応
                        if (barcode == null){
                            setShowMessage(2);
                            break;
                        }
                        String value = barcode.displayValue;
                        int page = viewPager.getCurrentItem();

                        //ページ2で、工管Noにフォーカスがある場合のみ、サーバにメッセージ送信
                        if (page == 1) {
                            if (fpAdapter.checkFocused(1)) {
                                String msg = pc.KOB.getString() + value;
                                sendMsgToServer(msg);
                            }
                        }

                        Log.d("Barcode", "Barcode read: " + barcode.displayValue);
                    } else {
                        Log.d("Barcode", "No barcode captured, intent data is null");
                    }
                } else {
                    Log.d("Barcode", "Canceled");
                }

            default:
                break;
        }
    }

    private void setViews() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("Ｂ棟ふるい網目セット管理");
        setSupportActionBar(toolbar);

        FragmentManager manager = getSupportFragmentManager();
        fpAdapter = new FPAdapter(manager);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(fpAdapter);
        //3ページ以上になる場合は、フラグメントの再インスタンス化を防ぐため設定
        viewPager.setOffscreenPageLimit(fpAdapter.getCount() - 1);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        show = (TextView) findViewById(R.id.show);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnUpd = (Button) findViewById(R.id.btnUpd);
        btnCam = (Button) findViewById(R.id.btnCam);
        //クリックイベント
        btnClear.setOnClickListener(this);
        btnUpd.setOnClickListener(this);
        btnCam.setOnClickListener(this);
        //カメラ起動、登録ボタンを無効化
        btnCam.setEnabled(false);
        btnUpd.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PendingIntent pendingIntent = this.createPendingIntent();
        // Enable NFC adapter
        this.nfcTags.enable(this, pendingIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disable NFC adapter
        this.nfcTags.disable(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.nfcTags = null;
        try {
            clientThread.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            //Dialog(OK,Cancel Ver.)
            new AlertDialog.Builder(this)
                    .setTitle("確認")
                    .setMessage("終了してもよろしいですか？")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            finishAndRemoveTask();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
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
        else if (id == R.id.action_reconnection) {
            show.setText("再接続に失敗しました。\n無線LAN状況を確認してください。");
            //再接続を行う
            clientThread = new ClientThread(handler, ip, myPort, false);
            // サーバ接続スレッド開始
            new Thread(clientThread).start();
        }
        else if (id == R.id.action_finish) {
            //Dialog(OK,Cancel Ver.)
            new AlertDialog.Builder(this)
                    .setTitle("確認")
                    .setMessage("終了してもよろしいですか？")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // OK button pressed
                            finishAndRemoveTask();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    //作業者名をセットするための中継
    public void adaptSelectedSagyoName(String name) {
        fpAdapter.setSelectedSagyoName(name);
        //機械Noが空白かどうかチェック
        if (fpAdapter.checkIsEmpty(1, 0)) {
            setShowMessage(1);
        }
        //ページ遷移
        viewPager.setCurrentItem(1);
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
