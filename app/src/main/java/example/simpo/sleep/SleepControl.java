package example.simpo.sleep;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * 用于定时开启系统黑屏和唤醒的工具类，只是简单的view覆盖，非系统休眠
 * 使用前需要让应用永不休眠，setContentView前添加
 * getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 * Manifest中添加<uses-permission android:name="android.permission.WAKE_LOCK" />
 **/

public class SleepControl {

    private static SleepControl sleepControl;
    private static Handler handler;
    private List<SleepBean> sleepBeans = new ArrayList<>();
    private List<OnSleepListener> sleepListeners = new ArrayList<>();
    private static boolean hasInit = false;
    private static String correspondime = "";

    public static SleepControl getInstance() {
        if (sleepControl == null) {
            synchronized (SleepControl.class) {
                if (sleepControl == null) {
                    sleepControl = new SleepControl();
                }
            }
        }
        return sleepControl;
    }

    private SleepControl() {
        if (hasInit) {
            return;
        }
        init();
    }

    /**
     * 每隔一秒检查一次时间表和此时时间
     **/
    private void init() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handler.sendMessageDelayed(handler.obtainMessage(), 1000);
                checkTime();
            }
        };
        handler.sendMessageDelayed(handler.obtainMessage(), 1000);
        hasInit = true;
    }

    /**
     * 向时间表中添加数据，并依次遍历时间表
     **/
    public void add(SleepBean sleepBean) {
        if (sleepBean != null) {
            if (!sleepBeans.contains(sleepBean)) {
                sleepBeans.add(sleepBean);
            }
        }
    }

    /**
     * 遍历时间表，对比此时时间是否被包含于时间表中，是，则启用回调
     **/
    private void checkTime() {
        if (sleepBeans == null || sleepBeans.size() == 0) {
            return;
        }
        for (int i = 0; i < sleepBeans.size(); i++) {
            if (getCurrentTime().equals(sleepBeans.get(i).getActiveTime())) {
                //避免一分钟内被重复调用60次
                if (sleepBeans.get(i).getActiveTime().equals(correspondime) && sleepBeans.size() > 1) {
                    return;
                }
                correspondime = sleepBeans.get(i).getActiveTime();
                if (sleepListeners == null || sleepListeners.size() == 0) {
                    return;
                }
                for (int j = 0; j < sleepListeners.size(); j++) {
                    sleepListeners.get(j).sleep(sleepBeans.get(i));
                }
            }
        }
    }

    /**
     * 获取此时的时间，小时/分钟
     **/
    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        String hour = calendar.get(Calendar.HOUR_OF_DAY) + "";
        String minute = calendar.get(Calendar.MINUTE) + "";
        if (hour.length() == 0) {
            hour = "00";
        } else if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 0) {
            minute = "00";
        } else if (minute.length() == 1) {
            minute = "0" + minute;
        }
        return hour + ":" + minute;
    }

    /**
     * 添加时间表的监听器，当满足睡眠或唤醒条件时，回调被调用
     **/
    public void setOnSleepListener(OnSleepListener onSleepListener) {
        if (onSleepListener == null) {
            return;
        }
        this.sleepListeners.add(onSleepListener);
    }

    public interface OnSleepListener {
        void sleep(SleepBean sleepBean);
    }

}
