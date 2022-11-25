package project.xplat.launcher.pxprpcapi.androidhelper;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import project.xplat.launcher.pxprpcapi.ApiServer;
import pursuer.pxprpc.AsyncReturn;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Misc2 {
    Vibrator vb;
    ClipboardManager cbm;
    AudioManager am;
    LocationManager lm;
    public Misc2(){
        vb = (Vibrator) ApiServer.defaultAndroidContext.getSystemService(Service.VIBRATOR_SERVICE);
        cbm = (ClipboardManager) ApiServer.defaultAndroidContext.getSystemService(Service.CLIPBOARD_SERVICE);
        am = (AudioManager) ApiServer.defaultAndroidContext.getSystemService(Service.AUDIO_SERVICE);
        lm=(LocationManager) ApiServer.defaultAndroidContext.getSystemService(Service.LOCATION_SERVICE);
    }
    public boolean hasVibrator(){
        return vb.hasVibrator();
    }
    public void vibrate(int ms,int amplitude){
        //amplitude:-1 default, range 0-255
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vb.vibrate(VibrationEffect.createOneShot(ms,amplitude));
        }else{
            vb.vibrate(ms);
        }
    }
    public String getClipboardText(){
        ClipData cb = cbm.getPrimaryClip();
        if(cb.getItemCount()>0){
            return cb.getItemAt(0).getText().toString();
        }
        return null;
    }
    public void setClipboardText(String text){
        cbm.setPrimaryClip(ClipData.newPlainText("pxprpc",text));
    }
    public int getDefaultAudioVolume(){
        return am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    public void setDefaultAudioVolume(int vol){
        am.setStreamVolume(AudioManager.STREAM_MUSIC,vol,0);
    }
    public LocationListener lastLocationListener=null;
    public void getGpsLocationInfo(final AsyncReturn<Object> ret, final boolean stringMode){
        if(this.lastLocationListener!=null)this.cancelGetGpsLocationInfo();
        this.lastLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(stringMode){
                    StringBuilder sb=new StringBuilder();
                    sb.append("latitude:").append(location.getLatitude()).append("\n");
                    sb.append("longitude:").append(location.getLongitude()).append("\n");
                    sb.append("speed:").append(location.getSpeed()).append("\n");
                    sb.append("bearing:").append(location.getBearing()).append("\n");
                    sb.append("altitude:").append(location.getAltitude()).append("\n");
                    sb.append("accuracy:").append(location.getAccuracy()).append("\n");
                    ret.result(sb.toString());
                }else{
                    ByteBuffer bb=ByteBuffer.allocate(40);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    bb.putDouble(location.getLatitude());
                    bb.putDouble(location.getLongitude());
                    bb.putFloat(location.getSpeed());
                    bb.putFloat(location.getBearing());
                    bb.putDouble(location.getAltitude());
                    bb.putFloat(location.getAccuracy());
                    ret.result(bb.array());
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {
                Misc2.this.cancelGetGpsLocationInfo();
                ret.result(new IOException("User disable gps provider"));
            }
        };
        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this.lastLocationListener,ApiServer.getHandler().getLooper());
    }
    public void cancelGetGpsLocationInfo(){
        lm.removeUpdates(this.lastLocationListener);
    }
}
