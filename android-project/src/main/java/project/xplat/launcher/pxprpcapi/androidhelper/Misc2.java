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
import android.os.*;
import org.slf4j.helpers.Util;
import project.xplat.launcher.pxprpcapi.ApiServer;
import project.xplat.launcher.pxprpcapi.Utils;
import pursuer.patchedmsgpack.tools.MapBuilder2;
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
    }
    public void init(){
        vb = (Vibrator) ApiServer.defaultAndroidContext.getSystemService(Service.VIBRATOR_SERVICE);
        cbm = (ClipboardManager) ApiServer.defaultAndroidContext.getSystemService(Service.CLIPBOARD_SERVICE);
        am = (AudioManager) ApiServer.defaultAndroidContext.getSystemService(Service.AUDIO_SERVICE);
        lm=(LocationManager) ApiServer.defaultAndroidContext.getSystemService(Service.LOCATION_SERVICE);
    }
    public void deinit(){
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
    public void getGpsLocationInfo(final AsyncReturn<Object> ret, final boolean msgpackMode){
        if(this.lastLocationListener!=null)this.cancelGetGpsLocationInfo();
        this.lastLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(msgpackMode){
                	ret.result(Utils.packFrom(
                    new MapBuilder2().put("latitude", location.getLatitude())
                    .put("longitude", location.getLongitude())
                    .put("speed",location.getSpeed())
                    .put("bearing", location.getBearing())
                    .put("altitude", location.getAltitude())
                    .put("accuracy",location.getAccuracy())
                    .build()));
                }else{
                    ByteBuffer bb=ByteBuffer.allocate(40);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    bb.putDouble(location.getLatitude());
                    bb.putDouble(location.getLongitude());
                    bb.putDouble(location.getSpeed());
                    bb.putDouble(location.getBearing());
                    bb.putDouble(location.getAltitude());
                    bb.putDouble(location.getAccuracy());
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
