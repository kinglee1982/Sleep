package example.simpo.sleep.netswitch;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * 开启和关闭wifi，并连接到指定wifi的工具，针对6.0设备，未做适配
 **/
public class WifiConnectUtils {

    private static WifiConnectUtils wifiUtils;
    private Context context;
    private WifiManager wifiManager;
    private boolean hasOpen = false;

    public static WifiConnectUtils getInstance(Context context) {
        if (wifiUtils == null) {
            synchronized (WifiConnectUtils.class) {
                if (wifiUtils == null) {
                    wifiUtils = new WifiConnectUtils(context);
                }
            }
        }
        return wifiUtils;
    }

    public WifiConnectUtils(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    //连接到指定wifi，连接前必须调用开启wifi的方法，不然会崩溃，如果type不知道填什么，默认填2
    public WifiConfiguration configWifiInfo(String SSID, String password, int type) {
        //先检测也没有开启wifi
        if (!hasOpen && !isWifi(context)) {
            openWifi();
            return null;
        }
        if (isWifi(context)) {
            hasOpen = true;
        } else {
            hasOpen = false;
        }
        WifiConfiguration config = null;
        if (wifiManager != null) {
            List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig == null) continue;
                if (existingConfig.SSID.equals("\"" + SSID + "\"")  /*&&  existingConfig.preSharedKey.equals("\""  +  password  +  "\"")*/) {
                    config = existingConfig;
                    break;
                }
            }
        }
        if (config == null) {
            config = new WifiConfiguration();
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // 分为三种情况：0没有密码1用wep加密2用wpa加密
        if (type == 0) {// WIFICIPHER_NOPASSwifiCong.hiddenSSID = false;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == 1) {  //  WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == 2) {   // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        int netId = config.networkId;
        if (netId == -1) {
            netId = wifiManager.addNetwork(config);
        }
        wifiManager.enableNetwork(netId, true);
        return config;
    }

    // 打开wifi功能
    public boolean openWifi() {
        boolean bRet = true;
        bRet = wifiManager.setWifiEnabled(true);
        hasOpen = true;
        return bRet;
    }

    // 关闭WIFI
    public void closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            hasOpen = false;
        }
    }


    //检测当前是否已经连接上了wifi
    private static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null
                && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 获取热点的加密类型
     */
    private int getType(ScanResult scanResult) {
        int type;
        if (scanResult.capabilities.contains("WPA"))
            type = 2;
        else if (scanResult.capabilities.contains("WEP"))
            type = 1;
        else
            type = 0;
        return type;
    }

}
