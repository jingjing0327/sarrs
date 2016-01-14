package com.chaojishipin.sarrs.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.chaojishipin.sarrs.interfaces.INetWorkObServe;
import com.chaojishipin.sarrs.utils.ConstantUtils;
import com.chaojishipin.sarrs.utils.LogUtil;
import com.chaojishipin.sarrs.utils.NetWorkUtils;
import com.chaojishipin.sarrs.utils.Utils;

import java.util.ArrayList;

/**
 * 网络状态观察者 add by zhangshuo
 */
public class NetWorkStateReceiver extends BroadcastReceiver {

    private INetWorkObServe mNetWorkObserve;
    private ArrayList<INetWorkObServe> observers = new ArrayList<>();

    public NetWorkStateReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //刚进入界面会读取网络状态
         if(intent.getAction().equalsIgnoreCase( ConnectivityManager.CONNECTIVITY_ACTION)){

            try {
                NetworkInfo networkInfo = NetWorkUtils.getAvailableNetWorkInfo();
                if (null != context && null != networkInfo) {
                    //获取网络名称、网络类型、以及网络是否连接
                    mNetWorkObserve.observeNetWork(networkInfo.getTypeName(), networkInfo.getType(), networkInfo.isAvailable());
                } else {
                    mNetWorkObserve.observeNetWork(ConstantUtils.NET_TYPE_NAME, ConstantUtils.NET_TYPE_ERROR, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(intent.getAction().equalsIgnoreCase(WifiManager.NETWORK_STATE_CHANGED_ACTION)){

//                observeNetwork(ConstantUtils.NET_TYPE_NAME, ConstantUtils.NET_TYPE_ERROR, false);
        }


    }

    public void setmNetWorkObserve(INetWorkObServe mNetWorkObserve) {
        this.mNetWorkObserve = mNetWorkObserve;
    }

    public void addNetworkObserver(INetWorkObServe mNetWorkObserve)
    {
        observers.add(mNetWorkObserve);
    }

    public void removeNetworkOberver(INetWorkObServe mNetWorkObserve)
    {
        observers.remove(mNetWorkObserve);
    }

    private void observeNetwork(String netName, int netType, boolean isHasNetWork)
    {
        for (INetWorkObServe observer: observers
             ) {
            mNetWorkObserve.observeNetWork(netName, netType, isHasNetWork);
        }
    }
}
