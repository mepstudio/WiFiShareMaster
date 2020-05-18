package cn.mepstudio.wifisharemaster;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * wifi AP 管理类
 * 提供 AP 状态，开关 AP
 * <p>
 * Created by zhenguo on 1/11/17.
 */
public class WifiAPManager {
    // 热点的配置类
    private WifiConfiguration apConfig = new WifiConfiguration();

    private WifiManager mWifiManager;
    private Context context;
    // 密码必须大于8位数
    private static String apName,apPwd;
    private static int apMode;

    public WifiAPManager(Context context, String apName, String apPwd, int apMode) {
        this.context = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiAPManager.apName = apName;
        WifiAPManager.apPwd = apPwd;
        WifiAPManager.apMode = apMode;
    }

    /**
     * 判断热点开启状态
     */
    public boolean isWifiApEnabled() {
        return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    public WIFI_AP_STATE getWifiApState() {
        int tmp;
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            tmp = ((Integer) method.invoke(mWifiManager));
            // Fix for Android 4
            if (tmp > 10) {
                tmp = tmp - 10;
            }
            return WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (Exception e) {
            e.printStackTrace();
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING,
        WIFI_AP_STATE_DISABLED,
        WIFI_AP_STATE_ENABLING,
        WIFI_AP_STATE_ENABLED,
        WIFI_AP_STATE_FAILED
    }

    /**
     * 检查是否打开或关闭wifi热点
     */
    public boolean isApOn() {
        try {
            Method method = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        }
        catch (Throwable ignored) {}
        return false;
    }

    /**
     * wifi热点开关
     *
     * @param enabled true：打开  false：关闭
     * @return true：成功  false：失败
     */
    public boolean setWifiApEnabled(boolean enabled) {
         Log.d("WifiAPMgr", "开启热点");
        if (enabled) {
            // wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            mWifiManager.setWifiEnabled(false);

             Log.d("WifiAPMgr", "关闭wifi");
        } else {
            // wifiManager.setWifiEnabled(true);
        }
        try {

            // 配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = apName;
            // 配置热点的密码
            apConfig.preSharedKey = apPwd;
            // 安全：默认WPA2_PSK
            apConfig.allowedKeyManagement.set(apMode);
            // 通过反射调用设置热点
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            // 返回热点打开状态
            return (Boolean) method.invoke(mWifiManager, apConfig, enabled);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Show write permission settings page to user if necessary or forced
     * @param force show settings page even when rights are already granted
     */
    public void showWritePermissionSettings(boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (force || !Settings.System.canWrite(this.context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.context.startActivity(intent);
            }
        }
    }

    /**
     * Start AccessPoint mode with the specified
     * configuration. If the radio is already running in
     * AP mode, update the new configuration
     * Note that starting in access point mode disables station
     * mode operation
     *
     * @param wifiConfig SSID, security and channel details as part of WifiConfiguration
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
    public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
        try {
            if (enabled) { // disable WiFi in any case
                mWifiManager.setWifiEnabled(false);
            }

            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig, enabled);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    /**
     * Gets the Wi-Fi AP Configuration.
     *
     * @return AP details in {@link WifiConfiguration}
     */
    public WifiConfiguration getWifiApConfiguration() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return null;
        }
    }

    /**
     * Sets the Wi-Fi AP Configuration.
     *
     * @return {@code true} if the operation succeeded, {@code false} otherwise
     */
    public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, wifiConfig);
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            return false;
        }
    }

    /**
     * Gets a list of the clients connected to the Hotspot, reachable timeout is 300
     *
     * @param onlyReachables  {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @param finishListener, Interface called when the scan method finishes
     */
    public void getClientList(boolean onlyReachables, FinishScanListener finishListener) {
        getClientList(onlyReachables, 300, finishListener);
    }

    /**
     * Gets a list of the clients connected to the Hotspot
     *
     * @param onlyReachables   {@code false} if the list should contain unreachable (probably disconnected) clients, {@code true} otherwise
     * @param reachableTimeout Reachable Timout in miliseconds
     * @param finishListener,  Interface called when the scan method finishes
     */
    public void getClientList(final boolean onlyReachables, final int reachableTimeout, final FinishScanListener finishListener) {
        Runnable runnable = new Runnable() {
            public void run() {

                BufferedReader br = null;
                final ArrayList<ClientScanResult> result = new ArrayList<ClientScanResult>();

                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] splitted = line.split(" +");

                        if ((splitted != null) && (splitted.length >= 4)) {
                            // Basic sanity check
                            String mac = splitted[3];

                            if (mac.matches("..:..:..:..:..:..")) {
                                boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

                                if (!onlyReachables || isReachable) {
                                    result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(this.getClass().toString(), e.toString());
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e(this.getClass().toString(), e.getMessage());
                    }
                }

                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(context.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        finishListener.onFinishScan(result);
                    }
                };
                mainHandler.post(myRunnable);
            }
        };

        Thread mythread = new Thread(runnable);
        mythread.start();
    }
}
