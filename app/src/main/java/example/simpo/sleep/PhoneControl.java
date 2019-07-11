package example.simpo.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import static android.content.Context.KEYGUARD_SERVICE;

public class PhoneControl {

    private static PhoneControl phoneControl;
    private Context context;

    public static PhoneControl getInstance(Context c) {
        if (phoneControl == null) {
            synchronized (PhoneControl.class) {
                phoneControl = new PhoneControl(c);
            }
        }
        return phoneControl;
    }

    private PhoneControl(Context context) {
        this.context = context;
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
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakup");
        wl.acquire(10000);
        wl.release();

        KeyguardManager keyguardManager = (KeyguardManager) context
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
        PowerManager pm = (PowerManager) context
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
                ActivityCompat.requestPermissions((Activity) context, permissions, 321);
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
}
