package project.xplat.launcher.pxprpcapi.androidhelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import project.xplat.launcher.pxprpcapi.ApiServer;

import java.lang.reflect.Field;
import java.util.UUID;

public class SysBase {
    public BroadcastReceiver newBroadcastReceiver(){
        return new PxprpcBroadcastReceiverAdapter();
    }
    public Context getDefaultContext(){
        return ApiServer.defaultAndroidContext;
    }
    public void registerBroadcastReceiver(BroadcastReceiver receiver,String filter){
        getDefaultContext().registerReceiver(receiver,new IntentFilter(filter));
    }
    public void unregisterBroadcastReceiver(BroadcastReceiver receiver){
        getDefaultContext().unregisterReceiver(receiver);
    }
    public Map2 bundleToMap2(Bundle b){
        Map2 m=new Map2();
        for(String k:b.keySet()){
            m.put(k,b.get(k));
        }
        return m;
    }
    public String describeBundle(Bundle b){
        return bundleToMap2(b).describe();
    }
    public String describeIntent(Intent intent){
        Map2 m = bundleToMap2(intent.getExtras());
        m.put("action",intent.getAction());
        return m.describe();
    }
    public String typeof(Object obj){
        return obj.getClass().getName();
    }
    public Object getService(String name){
        return getDefaultContext().getSystemService(name);
    }
    public UUID newUUID(long mostSigBits, long leastSigBits){
        return new UUID(mostSigBits,leastSigBits);
    }
    public String describeFields(Object o) {
        StringBuilder sb=new StringBuilder();
        for(Field f : o.getClass().getFields()){
            Object v = null;
            try {
                v = f.get(o);
                if(v==null)continue;
                sb.append(f.getName()).append(":").append(v.toString()).append("\n");
            } catch (IllegalAccessException e) {
            }
        }
        return sb.toString();
    }
}
