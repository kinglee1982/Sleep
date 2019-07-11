package example.simpo.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import example.simpo.sleep.netswitch.NetSwitchUtils;
import example.simpo.sleep.netswitch.NetSignUtils;
import example.simpo.sleep.netswitch.WifiConnectUtils;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private int time = 0;
    private boolean set = false;
    private Button imei, connectWifi, openWifi, closeWifi, strongSign;
    private EditText wifiName, wifiPwd, wifiType;
    public TelephonyManager mTelephonyManager;
    public PhoneStatListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        wifiName = findViewById(R.id.wifi_name);
        wifiPwd = findViewById(R.id.wifi_pwd);
        wifiType = findViewById(R.id.wifi_type);
        connectWifi = findViewById(R.id.connect_wifi);
        openWifi = findViewById(R.id.open_wifi);
        closeWifi = findViewById(R.id.close_wifi);
        strongSign = findViewById(R.id.strong_sign);
        imei = findViewById(R.id.imei);
        imei.setText("IMEI:  " + getIMEI(this));
    }

    private void initData() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (time != 7200) {
                    Log.d("fdsafsad", "not" + time);
                    time++;
                    handler.sendMessageDelayed(handler.obtainMessage(), 1000);
                } else {
                    Log.d("fdsafsad", "yes" + time);
                    wakeUpAndUnlock();
                    setScreenSleepTime(24 * 3600 * 1000, MainActivity.this);
                }
            }
        };
        if (!Settings.System.canWrite(MainActivity.this)) {     //获取修改系统属性的权限,用来开启关闭网络
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("刷新");
            builder.setMessage("我们的应用需要您授权\"修改系统设置\"的权限,请点击\"设置\"确认开启");
            // 拒绝, 退出应用
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.setPositiveButton("设置",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 0);
                        }
                    });

            builder.setCancelable(false);
            builder.show();
        }
        //获取telephonyManager
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //开始监听
        mListener = new PhoneStatListener();
    }

    private void initListener() {
        //监听信号强度
        mTelephonyManager.listen(mListener, PhoneStatListener.LISTEN_SIGNAL_STRENGTHS);
        findViewById(R.id.sleep).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!set) {
                    set = true;
                    setScreenSleepTime(5000, MainActivity.this);
                    handler.sendMessageDelayed(handler.obtainMessage(), 1000);
                }
            }
        });
        findViewById(R.id.open_g).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetSwitchUtils.switchNetStatus(MainActivity.this, true);
            }
        });
        findViewById(R.id.close_g).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetSwitchUtils.switchNetStatus(MainActivity.this, false);
            }
        });
        connectWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiConnectUtils.getInstance(MainActivity.this).configWifiInfo(wifiName.getText().toString(), wifiPwd.getText().toString(), Integer.parseInt(wifiType.getText().toString()));
            }
        });
        openWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiConnectUtils.getInstance(MainActivity.this).openWifi();
            }
        });
        closeWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiConnectUtils.getInstance(MainActivity.this).closeWifi();
            }
        });
    }

    /**
     * 设置休眠时间
     *
     * @param millisecond
     * @param context
     */
    public void setScreenSleepTime(int millisecond, Context context) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                    millisecond);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * 唤醒手机屏幕并解锁
     */
    public void wakeUpAndUnlock() {
        PowerManager pm = (PowerManager) MainActivity.this
                .getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakup");
        wl.acquire(10000);
        wl.release();

        KeyguardManager keyguardManager = (KeyguardManager) MainActivity.this
                .getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");

        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard();
    }

    /**
     * 判断当前屏幕是否亮
     *
     * @return
     */
    public boolean isScreenOn() {
        PowerManager pm = (PowerManager) MainActivity.this
                .getSystemService(Context.POWER_SERVICE);
        boolean screenOn = pm.isScreenOn();

        return screenOn;
    }

    /**
     * 获取手机IMEI
     *
     * @param context
     * @return
     */
    public final String getIMEI(Context context) {
        try {
            //实例化TelephonyManager对象
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMEI号
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = {Manifest.permission.READ_PHONE_STATE};
                ActivityCompat.requestPermissions(this, permissions, 321);
                return "";
            }
            String imei = telephonyManager.getDeviceId();
            //在次做个验证，也不是什么时候都能获取到的啊
            if (imei == null) {
                imei = "";
            }
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    @SuppressWarnings("deprecation")
    private class PhoneStatListener extends PhoneStateListener {
        //获取信号强度


        @Override
        public void onSignalStrengthChanged(int asu) {
            super.onSignalStrengthChanged(asu);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            //获取网络信号强度
            //获取0-4的5种信号级别，越大信号越好,但是api23开始才能用
            int level = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                level = signalStrength.getLevel();
                System.out.println("level====" + level);
            }
            int cdmaDbm = signalStrength.getCdmaDbm();
            int evdoDbm = signalStrength.getEvdoDbm();
            System.out.println("cdmaDbm=====" + cdmaDbm);
            System.out.println("evdoDbm=====" + evdoDbm);

            int gsmSignalStrength = signalStrength.getGsmSignalStrength();
            int dbm = -113 + 2 * gsmSignalStrength;
            System.out.println("dbm===========" + dbm);

            //获取网络类型
            int netWorkType = NetSignUtils.getNetworkState(MainActivity.this);
            switch (netWorkType) {
                case NetSignUtils.NETWORK_WIFI:
                    strongSign.setText("当前网络为wifi,信号强度为：" + gsmSignalStrength + " : " + dbm + " : " + level);
                    break;
                case NetSignUtils.NETWORK_2G:
                    strongSign.setText("当前网络为2G移动网络,信号强度为：" + gsmSignalStrength + " : " + dbm + " : " + level);
                    break;
                case NetSignUtils.NETWORK_3G:
                    strongSign.setText("当前网络为3G移动网络,信号强度为：" + gsmSignalStrength + " : " + dbm + " : " + level);
                    break;
                case NetSignUtils.NETWORK_4G:
                    strongSign.setText("当前网络为4G移动网络,信号强度为：" + gsmSignalStrength + " : " + dbm + " : " + level);
                    break;
                case NetSignUtils.NETWORK_NONE:
                    strongSign.setText("当前没有网络,信号强度为：" + gsmSignalStrength + " : " + dbm + " : " + level);
                    break;
                case -1:
                    strongSign.setText("当前网络错误,信号强度为：" + gsmSignalStrength + " : " + dbm + " : " + level);
                    break;
            }
        }
    }


}
