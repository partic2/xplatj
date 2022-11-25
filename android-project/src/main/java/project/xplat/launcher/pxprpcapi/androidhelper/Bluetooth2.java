package project.xplat.launcher.pxprpcapi.androidhelper;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import project.xplat.launcher.pxprpcapi.ApiServer;
import pursuer.pxprpc.EventDispatcher;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Bluetooth2 extends BroadcastReceiver implements BluetoothAdapter.LeScanCallback {
    public BluetoothAdapter adapter;
    public EventDispatcher eventDispatcher;
    public void init(){
        this.eventDispatcher=new EventDispatcher();
        if(adapter==null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                adapter=((BluetoothManager) ApiServer.defaultAndroidContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                ApiServer.defaultAndroidContext.registerReceiver(this,
                        new IntentFilter(BluetoothDevice.ACTION_FOUND));
                ApiServer.defaultAndroidContext.registerReceiver(this,
                        new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
                ApiServer.defaultAndroidContext.registerReceiver(this,
                        new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
            }else{
                throw new UnsupportedOperationException("Not support yet");
            }
        }
    }
    public Bluetooth2 self(){
        return this;
    }
    public BluetoothAdapter.LeScanCallback asLeScanListener(){
        return this;
    }
    public BluetoothAdapter bluetoothAdapter(){
        return bluetoothAdapter();
    }
    public EventDispatcher getEventDispatcher(){
        return this.eventDispatcher;
    }
    public void deinig(){
        ApiServer.defaultAndroidContext.unregisterReceiver(this);
        adapter=null;
    }

    public void requestBluetoothDicoverable(int durationSec){
        ((Intent2)ApiServer.getModule("AndroidHelper-Intent")).requestBluetoothDicoverable(durationSec);
    }

    public void requestEnableBluetooth(){
        ((Intent2)ApiServer.getModule("AndroidHelper-Intent")).requestEnableBluetooth();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Map2 m = ((SysBase) ApiServer.getModule("AndroidHelper-SysBase")).bundleToMap2(intent.getExtras());
        m.put("action",intent.getAction());
        this.eventDispatcher.fireEvent(m);
    }
    @Override
    public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
        Map2 m=new Map2();
        m.put("action","onLeScanEvent");
        m.put("bluetoothDevice",bluetoothDevice);
        m.put("rssi",rssi);
    }
}
