package cn.mepstudio.wifisharemaster.ui.home;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import cn.mepstudio.wifisharemaster.R;
import cn.mepstudio.wifisharemaster.WifiAPManager;

public class HomeFragment extends Fragment{

    private HomeViewModel homeViewModel;

    private Switch mAPSwitch,mOpenNet,mShowPWD;
    private EditText mSSID,mPWD;
    private Toast mToast;
    private WifiAPManager mWifiAPMgr;

    private String apName = "AndroidAP";
    private String apPwd = "147258369";
    private int apMode = WifiConfiguration.KeyMgmt.WPA_PSK;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mSSID = root.findViewById(R.id.ssid);
        mPWD = root.findViewById(R.id.pwd);
        //显示默认的名称和密码
        mSSID.setText(apName);
        mPWD.setText(apPwd);

        //获取热点信息
        mWifiAPMgr = new WifiAPManager(getContext(), apName,apPwd,apMode);

        //将变量关联到界面id上
        mAPSwitch = root.findViewById(R.id.ap_switch);
        mOpenNet= root.findViewById(R.id.openNet);
        mShowPWD = root.findViewById(R.id.showPWD);
        //获取wifi开启、是否开放网络的状态并显示在界面
        mAPSwitch.setChecked(mWifiAPMgr.isApOn());
        //默认不开放
        mOpenNet.setChecked(false);
        //热点开启的时候，你可以修改热点参数
        if(mWifiAPMgr.isApOn()){
            mSSID.setEnabled(false);
            mPWD.setEnabled(false);
            mOpenNet.setEnabled(false);
        }

        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
       /*
        if(mWifiAPMgr.isWifiApEnabled()){
            mSSID.setText(apName);
            mPWD.setText(apPwd);
        }
*/
        //开放网络
        mOpenNet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    // 每次 setChecked 时会触发onCheckedChanged 监听回调，而有时我们在设置setChecked后不想去自动触发 onCheckedChanged 里的具体操作, 即想屏蔽掉onCheckedChanged;加上此判断
                    apMode = WifiConfiguration.KeyMgmt.NONE;
                }else {
                    apMode = WifiConfiguration.KeyMgmt.WPA_PSK;
                }
            }
        });

        //显示密码

        mShowPWD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //显示
                    mPWD.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    }else {
                    //隐藏
                    mPWD.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }
                    // 每次 setChecked 时会触发onCheckedChanged 监听回调，而有时我们在设置setChecked后不想去自动触发 onCheckedChanged 里的具体操作, 即想屏蔽掉onCheckedChanged;加上此判断
                }

        });
        //热点开关
        mAPSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isOn,isSuccess;
                isOn = mWifiAPMgr.isApOn();

                if (isChecked){
                    //应用名称和密码
                    apName = mSSID.getText().toString();
                    apPwd = mPWD.getText().toString();
                    //重新生成一个改变后的mWifiAPMgr对象
                    mWifiAPMgr = new WifiAPManager(getContext(), apName,apPwd,apMode);
                    //开启热点
                    isSuccess = mWifiAPMgr.setWifiApEnabled(!isOn);
                    mSSID.setEnabled(false);
                    mPWD.setEnabled(false);
                    mOpenNet.setEnabled(false);
                }else {
                    isSuccess = mWifiAPMgr.setWifiApEnabled(!isOn);
                }
                //显示土司提示
                StringBuilder msg = new StringBuilder();
                msg.append(isOn ? "关闭" : "开启").append("热点").append(isSuccess ? "成功" : "失败");
                mToast.setText(msg);
                mToast.show();

                mSSID.setEnabled(true);
                mPWD.setEnabled(true);
                mOpenNet.setEnabled(true);
            }
        });

        return root;
    }
}
