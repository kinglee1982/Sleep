package example.simpo.sleep;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * 用于定时开启系统黑屏和唤醒的工具类
 * 使用前需要让应用永不休眠，setContentView前添加
 * getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 * Manifest中添加<uses-permission android:name="android.permission.WAKE_LOCK" />
 * **/

public class SleepControl {

    private static SleepControl sleepControl;
    private static Handler handler;
    private List<SleepBean> sleepBeans = new ArrayList<>();
    private List<OnSleepListener> sleepListeners = new ArrayList<>();
    private static boolean hasInit = false;

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

    public void add(SleepBean sleepBean) {
        if (sleepBean != null) {
            if (!sleepBeans.contains(sleepBean)) {
                sleepBeans.add(sleepBean);
            }
        }
    }

    private void checkTime() {
        if (sleepBeans == null || sleepBeans.size() == 0) {
            return;
        }
        for (int i = 0; i < sleepBeans.size(); i++) {
            Log.d("sleepBeans", getCurrentTime() + " : " + sleepBeans.get(i).getActiveTime());
            if (getCurrentTime().equals(sleepBeans.get(i).getActiveTime())) {
                if (sleepListeners == null || sleepListeners.size() == 0) {
                    return;
                }
                for (int j = 0; j < sleepListeners.size(); j++) {
                    sleepListeners.get(j).sleep(sleepBeans.get(i));
                }
            }
        }
    }

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

    public void setOnSleepListener(OnSleepListener onSleepListener) {
        this.sleepListeners.add(onSleepListener);
    }

    public interface OnSleepListener {
        void sleep(SleepBean sleepBean);
    }

}
