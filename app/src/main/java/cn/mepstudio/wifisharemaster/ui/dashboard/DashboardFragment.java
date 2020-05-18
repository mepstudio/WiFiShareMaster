package cn.mepstudio.wifisharemaster.ui.dashboard;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import cn.mepstudio.wifisharemaster.ClientScanResult;
import cn.mepstudio.wifisharemaster.FinishScanListener;
import cn.mepstudio.wifisharemaster.R;
import cn.mepstudio.wifisharemaster.WifiAPManager;

public class DashboardFragment extends Fragment {
    WifiAPManager wifiApManager;
    TextView mDeviceInfo;
    private String apName = "AndroidAP";
    private String apPwd = "147258369";
    private int apMode = WifiConfiguration.KeyMgmt.WPA_PSK;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mDeviceInfo =  root.findViewById(R.id.deviceInfo);

        wifiApManager = new WifiAPManager(getContext(), apName,apPwd,apMode);
        // force to show the settings page for demonstration purpose of this method
        wifiApManager.showWritePermissionSettings(true);

        //扫描连接上的设备并获取信息
        scan();

        return root;
    }
    private void scan() {
        wifiApManager.getClientList(false, new FinishScanListener() {

            @Override
            public void onFinishScan(final ArrayList<ClientScanResult> clients) {

                mDeviceInfo.setText("热点状态: " + wifiApManager.getWifiApState() + "\n\n");
                mDeviceInfo.append("已连接: \n");
                for (ClientScanResult clientScanResult : clients) {
                    mDeviceInfo.append("####################\n");
                    mDeviceInfo.append("Ip地址: " + clientScanResult.getIpAddr() + "\n");
                    mDeviceInfo.append("设备名: " + clientScanResult.getDevice() + "\n");
                    mDeviceInfo.append("Mac地址: " + clientScanResult.getHWAddr() + "\n");
                    mDeviceInfo.append("isReachable: " + clientScanResult.isReachable() + "\n");
                }
            }
        });
    }
}
