package project.xplat.launcher.pxprpcapi.androidhelper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import project.xplat.launcher.pxprpcapi.ApiServer;
import pursuer.pxprpc.AsyncReturn;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Wifi2 extends PxprpcBroadcastReceiverAdapter{
    WifiManager wm;
    WifiP2pManager wpm;
    ConnectivityManager cm;
    public Wifi2(){
        wm=(WifiManager) ApiServer.defaultAndroidContext.getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            wpm=(WifiP2pManager) ApiServer.defaultAndroidContext.getSystemService(Context.WIFI_P2P_SERVICE);
        }
        cm=(ConnectivityManager) ApiServer.defaultAndroidContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }
    public void scan(){
        IntentFilter if2 = new IntentFilter();
        if2.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        ApiServer.defaultAndroidContext.registerReceiver(this, if2);
        wm.startScan();
    }
    public List<ScanResult> getScanResult(){
        return wm.getScanResults();
    }
    public String scanResultsToString(List<ScanResult> l){
        StringBuilder info1=new StringBuilder();
        for(ScanResult r:l){
            info1.append(r.SSID).append(",").append(r.level).append("\n");
        }
        return info1.toString();
    }
    public String getState(){
        StringBuilder sb=new StringBuilder();
        sb.append("enable:").append(wm.isWifiEnabled()).append("\n");
        return sb.toString();
    }
    public void setWifiEnable(boolean enable){
        wm.setWifiEnabled(enable);
    }
    public void disconnect(){
        wm.disconnect();
    }
    public void registerWifiListener(){
        IntentFilter if2 = new IntentFilter();
        if2.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        if2.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        if2.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        if2.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        if2.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        ApiServer.defaultAndroidContext.registerReceiver(this, if2);
    }
    public void unregisterAllListener(){
        ApiServer.defaultAndroidContext.unregisterReceiver(this);
    }

    public void connectTo(String ssid,String psk){
        WifiConfiguration conf=new WifiConfiguration();
        conf.SSID=ssid;
        conf.preSharedKey=psk;
        conf.hiddenSSID=true;
        conf.status=WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        int netid=wm.addNetwork(conf);
        wm.enableNetwork(netid,true);
    }
    Method fnSetWifiApEnabled;
    protected WifiConfiguration lastApConf;
    public void startWifiAp(String ssid,String psk) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(fnSetWifiApEnabled==null)
            fnSetWifiApEnabled=wm.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID=ssid;
        conf.preSharedKey=psk;
        conf.hiddenSSID=false;
        lastApConf=conf;
        fnSetWifiApEnabled.invoke(wm, conf, true);

    }
    public void stopWifiAp() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if(fnSetWifiApEnabled==null)
            fnSetWifiApEnabled=wm.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
        fnSetWifiApEnabled.invoke(wm,lastApConf,false);
    }

    protected WifiP2pManager.Channel defaultChannel;
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public WifiP2pManager.Channel p2pInit(){
        this.defaultChannel=wpm.initialize(ApiServer.defaultAndroidContext,ApiServer.handlerThread.getLooper(), new WifiP2pManager.ChannelListener(){
            @Override
            public void onChannelDisconnected() {
            }
        });
        return this.defaultChannel;
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void p2pStartDiscover(final AsyncReturn<Exception> aret){
        wpm.discoverPeers(defaultChannel,new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                aret.result(null);
            }
            @Override
            public void onFailure(int reason) {
                aret.result(new IOException("wifip2p manager return  reason:"+reason));
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void p2pStopDiscover(final AsyncReturn<Exception> aret){
        wpm.stopPeerDiscovery(defaultChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                aret.result(null);
            }

            @Override
            public void onFailure(int reason) {
                aret.result(new IOException("wifip2p manager return  reason:"+reason));
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    //return ArrayList<WifiP2pDevice>
    public void p2pGetPeersList(final AsyncReturn<Object> aret){
        wpm.requestPeers(defaultChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                ArrayList<WifiP2pDevice> peers=new ArrayList<WifiP2pDevice>();
                peers.addAll(peerList.getDeviceList());
                aret.result(peers);
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void p2pPeersInfo(ArrayList<WifiP2pDevice> peers){
        StringBuilder sb=new StringBuilder();
        for(WifiP2pDevice p:peers){
            sb.append(p.deviceAddress).append(",")
                    .append(p.deviceName).append(",")
                    .append(p.status).append("\n");
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void p2pConnect(final AsyncReturn<Object> aret,String addr){
        WifiP2pConfig conf = new WifiP2pConfig();
        conf.deviceAddress=addr;
        wpm.connect(defaultChannel,conf, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                aret.result(null);
            }
            @Override
            public void onFailure(int reason) {
                aret.result(new IOException("wifip2p manager return  reason:"+reason));
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void p2pCancelConnect(final AsyncReturn<Object> aret){
        wpm.cancelConnect(defaultChannel, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                aret.result(null);
            }
            @Override
            public void onFailure(int reason) {
                aret.result(new IOException("wifip2p manager return  reason:"+reason));
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void p2pDisconnect(final AsyncReturn<Object> aret) {
        wpm.removeGroup(defaultChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                aret.result(null);
            }

            @Override
            public void onFailure(int reason) {
                aret.result(new IOException("wifip2p manager return  reason:" + reason));
            }
        });
    }

}
