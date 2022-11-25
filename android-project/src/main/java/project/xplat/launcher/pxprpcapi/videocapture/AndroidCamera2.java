package project.xplat.launcher.pxprpcapi.videocapture;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Size;
import android.view.Surface;
import project.xplat.launcher.pxprpcapi.ApiServer;
import project.xplat.launcher.pxprpcapi.Utils;
import pursuer.pxprpc.AsyncReturn;
import pursuer.pxprpc.EventDispatcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidCamera2 {
    public CameraManager camSvr;
    public String uid="";
    public void  accuireCameraService(String uid) {
        this.uid=uid;
        camSvr=(CameraManager)ApiServer.defaultAndroidContext.getSystemService(Context.CAMERA_SERVICE);
    }
    public void releaseCameraService(){
        this.uid="";
    }

    public String getUid(){
        return uid;
    }
    public String getCameraIdList() throws CameraAccessException {
        return Utils.joinStringList(Arrays.asList(camSvr.getCameraIdList()),"\n");
    }

    public String getBaseCameraInfo(String id) throws CameraAccessException {
        CameraCharacteristics info = camSvr.getCameraCharacteristics(id);
        StringBuilder sb=new StringBuilder();
        sb.append("face:");
        if(info.get(CameraCharacteristics.LENS_FACING)==0){
            sb.append("front");
        }else{
            sb.append("back");
        }
        sb.append("\n");
        sb.append("flashAvailable:");
        sb.append(info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)?1:0);
        sb.append("\n");
        StreamConfigurationMap sscm = info.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        sb.append("size:");
        Size[] sizes = sscm.getOutputSizes(ImageFormat.YUV_420_888);
        for(Size e : sizes){
            sb.append(e.getWidth()+","+e.getHeight());
            sb.append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }

    public static class CameraWrap1 extends EventDispatcher implements Closeable{
        public CameraDevice wrapped;
        AndroidCamera2 ctx;
        CameraCaptureSession capSess;
        ImageReader imgRead;
        public CameraWrap1(AndroidCamera2 ctx, CameraDevice wrapped){
            this.ctx=ctx;
            this.wrapped=wrapped;
        }
        public boolean closed=false;
        @Override
        public void close() throws IOException {
            if(wrapped!=null&&!closed){
                if(capSess!=null){
                    capSess.close();
                }
                if(imgRead!=null){
                    imgRead.close();
                }
                wrapped.close();
                closed=true;
            }
        }
    }
    public void openCamera(final AsyncReturn<Object> aret, String id) {
        try {
            camSvr.openCamera(id, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    CameraWrap1 c = new CameraWrap1(AndroidCamera2.this, camera);
                    aret.result(c);
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    aret.result(new Exception("Android Camera2 Error:" + error));
                }

            }, ApiServer.getHandler());
        }catch(Exception e){
            aret.result(e);
        }
    }
    public void closeCamera(CameraWrap1 cam){
        cam.wrapped.close();
    }

    public void requestContinuousCapture(final AsyncReturn<Object> aret, final CameraWrap1 camWrap, int width, int height) throws CameraAccessException {
        CameraDevice camDev = camWrap.wrapped;
        ImageReader imgReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2);
        if(camWrap.imgRead!=null)camWrap.imgRead.close();
        camWrap.imgRead=imgReader;
        
        try {
            List<Surface> tarSurf = new ArrayList<Surface>();
            tarSurf.add(imgReader.getSurface());
            camDev.createCaptureSession(tarSurf, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try{
                        if(camWrap.capSess!=null)camWrap.capSess.close();
                        CaptureRequest.Builder capReq = camWrap.wrapped.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        capReq.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        capReq.addTarget(camWrap.imgRead.getSurface());
                        session.setRepeatingRequest(capReq.build(),null,ApiServer.handler);
                        aret.result(0);
                    }catch (Exception e) {
                        aret.result(e);
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    aret.result(new RuntimeException("createCaptureSession failed."));
                }
            },ApiServer.getHandler());
        } catch (Exception e) {
            aret.result(e);
        }
    }

    public void stopContinuousCapture(CameraWrap1 camWrap) throws CameraAccessException {
        camWrap.capSess.stopRepeating();
    }

    public void requestOnceCapture(final AsyncReturn<Object> aret, final CameraWrap1 camWrap, int width, int height) throws CameraAccessException {
        final CameraDevice camDev = camWrap.wrapped;
        ImageReader imgReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2);
        if(camWrap.imgRead!=null)camWrap.imgRead.close();
        camWrap.imgRead=imgReader;

        try {
            List<Surface> tarSurf = new ArrayList<Surface>();
            tarSurf.add(imgReader.getSurface());
            camDev.createCaptureSession(tarSurf, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try{
                        if(camWrap.capSess!=null)camWrap.capSess.close();
                        CaptureRequest.Builder capReq = camWrap.wrapped.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        capReq.addTarget(camWrap.imgRead.getSurface());
                        session.capture(capReq.build(), null, ApiServer.handler);
                        aret.result(0);
                    }catch (Exception e) {
                        aret.result(e);
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    aret.result(new RuntimeException("createCaptureSession failed."));
                }
            },ApiServer.getHandler());
        } catch (Exception e) {
            aret.result(e);
        }
    }

    public List<Image.Plane> accuireLastestImageData(CameraWrap1 camDev){
        Image.Plane[] plane1 = camDev.imgRead.acquireLatestImage().getPlanes();
        return Arrays.asList(plane1);
    }

    public String getPlaneInfo(Image.Plane plane1){
        StringBuilder sb=new StringBuilder();
        sb.append("pixelStride:"+plane1.getPixelStride()+"\n")
                .append("rowStride:"+plane1.getRowStride()+"\n");
        return sb.toString();
    }

    public byte[] getPlaneData(Image.Plane plane1){
        ByteBuffer buf1 = plane1.getBuffer();
        //avoid method signature error on low version android.
        byte[] buf2 = new byte[((ByteBuffer)buf1).remaining()];
        buf1.get(buf2);
        return buf2;
    }

}
