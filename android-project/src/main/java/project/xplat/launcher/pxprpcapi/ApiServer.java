package project.xplat.launcher.pxprpcapi;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import project.xplat.launcher.MainActivity;
import project.xplat.launcher.pxprpcapi.androidhelper.*;
import project.xplat.launcher.pxprpcapi.videocapture.AndroidCamera2;
import pursuer.pxprpc_ex.TCPBackend;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;


public class ApiServer {
    public static TCPBackend tcpServ;
    public static Context defaultAndroidContext;
    public static HandlerThread handlerThread;
    public static Handler handler;
    public static int port=2050;
    public static Handler getHandler(){
        return handler;
    }
    public static void serve() throws IOException {
        tcpServ = new TCPBackend();
        handlerThread = new HandlerThread("PxpRpcHandlerThread");
        handlerThread.start();
        while(handlerThread.getLooper()==null){}
        handler=new Handler(handlerThread.getLooper());
        MainActivity.ensureStartOpts();
        if(MainActivity.debugMode){
            tcpServ.bindAddr= new InetSocketAddress(
                    Inet4Address.getByAddress(new byte[]{(byte)0,(byte)0,(byte)0,(byte)0}),port);
        }else{
            tcpServ.bindAddr= new InetSocketAddress(
                    Inet4Address.getByAddress(new byte[]{(byte)127,(byte)0,(byte)0,(byte)1}),port);
        }
        //Put init into handlerThread to avoid Looper error.
        handler.post(new Runnable(){
            @Override
            public void run() {
                putModule("AndroidHelper-SysBase",new SysBase());
                putModule("AndroidHelper-Camera2",new AndroidCamera2());
                putModule("AndroidHelper-Bluetooth",new Bluetooth2());
                putModule("AndroidHelper-Intent",new Intent2());
                putModule("AndroidHelper-Sensor",new Sensor2());
                putModule("AndroidHelper-Wifi",new Wifi2());
                putModule("AndroidHelper-Misc",new Misc2());
            }
        });
        Log.d("PxpRpc", "start: listen");
        tcpServ.listenAndServe();
    }
    public static void putModule(String modName,Object module){
        tcpServ.funcMap.put(modName,module);
    }
    public static Object getModule(String modName){
        return tcpServ.funcMap.get(modName);
    }

    public static void start(Context context) {
        defaultAndroidContext=context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApiServer.serve();
                } catch (IOException e) {
                }
            }
        }).start();
    }
    public static void stop(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tcpServ.close();
                    tcpServ =null;
                } catch (IOException e) {
                }
            }
        }).start();

    }
}
