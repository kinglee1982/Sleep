package example.simpo.sleep.netswitch;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 开启关闭GPRS，需满足是系统应用，或者拥有系统签名
 * 需要添加配置
 * <uses-permission android:name="android.permission.WRITE_APN_SETTINGS" />
 * android:sharedUserId="android.uid.system"
 * **/
public class NetSwitchUtils {


    public static void switchNetStatus(Context context, boolean status) {
        try {
            TelephonyManager mConnectivityManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class ownerClass = mConnectivityManager.getClass();
            Method method =  ownerClass.getMethod("setDataEnabled", boolean.class);
            method.invoke(mConnectivityManager, status);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }




}
