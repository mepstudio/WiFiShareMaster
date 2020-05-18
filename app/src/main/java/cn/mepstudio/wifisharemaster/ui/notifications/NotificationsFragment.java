package cn.mepstudio.wifisharemaster.ui.notifications;

import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.text.DecimalFormat;

import cn.mepstudio.wifisharemaster.R;

public class NotificationsFragment extends Fragment {

    private TextView mTotalBytes,mSendBytes,mReceiveBytes,mSpeed;
    private Chronometer mTime;
    private long rxtxTotal =0;
    private Handler mHandler;
    Runnable Speed;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        mTotalBytes = root.findViewById(R.id.totalBytes);
        mSendBytes = root.findViewById(R.id.sendBytes);
        mReceiveBytes = root.findViewById(R.id.receiveBytes);
        mSpeed= root.findViewById(R.id.speed);
        mTime = root.findViewById(R.id.time);

        //开始计数
        mTime.setBase(SystemClock.elapsedRealtime());//计时器清零
        int hour = (int) ((SystemClock.elapsedRealtime() - mTime.getBase()) / 1000 / 60);
        mTime.setFormat("0"+String.valueOf(hour)+":%s");
        mTime.start();

        long TotalBytes=TrafficStats.getMobileRxBytes()+TrafficStats.getMobileTxBytes();
        /** 获取手机通过 2G/3G 接收的字节流量总数 */
        mTotalBytes.setText("总消耗流量："+byteToMB(TotalBytes));
        /** 获取手机指定 UID 对应的应程序用通过所有网络方式发送的字节流量总数(包括 wifi) */
        mSendBytes.setText("发送流量："+byteToMB(TrafficStats.getMobileTxBytes()));
        /** 获取手机指定 UID 对应的应用程序通过所有网络方式接收的字节流量总数(包括 wifi) */
        mReceiveBytes.setText("接收流量："+byteToMB(TrafficStats.getMobileRxBytes()));

        mHandler = new Handler();
        mHandler.post(Speed=new Runnable() {
            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                updateViewData();
                mHandler.postDelayed(this, 1000);
            }
        });

        return root;
    }
    //将字节数转化为MB
    private String byteToMB(long size){
        long kb = 1024;
        long mb = kb*1024;
        long gb = mb*1024;
        if (size >= gb){
            return String.format("%.1f GB",(float)size/gb);
        }else if (size >= mb){
            float f = (float) size/mb;
            return String.format(f > 100 ?"%.0f MB":"%.1f MB",f);
        }else if (size > kb){
            float f = (float) size / kb;
            return String.format(f>100?"%.0f KB":"%.1f KB",f);
        }else {
            return String.format("%d B",size);
        }
    }
    private DecimalFormat showFloatFormat =new DecimalFormat("0.00");

    public void updateViewData() {
        long tempSum = TrafficStats.getTotalRxBytes()+ TrafficStats.getTotalTxBytes();
        long rxtxLast = tempSum -rxtxTotal;
        double totalSpeed= rxtxLast *1000 /2000d;
        rxtxTotal = tempSum;
        mSpeed.setText("当前网速:" + showSpeed(totalSpeed)); //设置显示当前网速
    }

    private String showSpeed(double speed) {
        String speedString;
        if (speed >=1048576d) {
            speedString = showFloatFormat.format(speed /1048576d) +"MB/s";
        }else {
            speedString = showFloatFormat.format(speed /1024d) +"KB/s";
        }
        return speedString;
    }
    /** 当其他活动获得焦点时调用 */
    @Override
    public void onPause() {
        super.onPause();
        //将接口从线程队列中移除
        mHandler.removeCallbacks(Speed);
    }

}
