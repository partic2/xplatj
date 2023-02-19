package project.xplat.launcher.pxprpcapi.androidhelper;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.*;
import android.view.Surface;
import project.xplat.launcher.pxprpcapi.ApiServer;
import project.xplat.launcher.pxprpcapi.Utils;
import project.xplat.launcher.pxprpcapi.androidhelper.AndroidCamera2.CameraWrap1;
import pursuer.patchedmsgpack.tools.ArrayBuilder2;
import pursuer.patchedmsgpack.tools.MPValueTable;
import pursuer.patchedmsgpack.tools.MapBuilder2;
import pursuer.pxprpc.AsyncReturn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Misc2 {
	Vibrator vb;
	ClipboardManager cbm;
	AudioManager am;
	LocationManager lm;

	public static class Light2 {
		public int id;
		public String desc;
		public String camId;
		public AndroidCamera2.CameraWrap1 camdev;
	}

	protected ArrayList<Light2> lights;

	public Misc2() {
		vb = (Vibrator) ApiServer.defaultAndroidContext.getSystemService(Service.VIBRATOR_SERVICE);
		cbm = (ClipboardManager) ApiServer.defaultAndroidContext.getSystemService(Service.CLIPBOARD_SERVICE);
		am = (AudioManager) ApiServer.defaultAndroidContext.getSystemService(Service.AUDIO_SERVICE);
		lm = (LocationManager) ApiServer.defaultAndroidContext.getSystemService(Service.LOCATION_SERVICE);
		lights = new ArrayList<Light2>();
		initCameraFlashLight();
	}

	protected void initCameraFlashLight() {
		CameraManager camSrv = ApiServer.androidcamera2.camSrv;
		String[] camList;
		try {
			camList = camSrv.getCameraIdList();
			for (String camId : camList) {
				CameraCharacteristics info = camSrv.getCameraCharacteristics(camId);
				if (info.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
					Light2 tl = new Light2();
					tl.id = lights.size();
					tl.desc = "flash for cameara " + camId;
					tl.camId = camId;
				}
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	public void init() {
	}

	public void deinit() {
	}

	public boolean hasVibrator() {
		return vb.hasVibrator();
	}

	public void vibrate(int ms, int amplitude) {
		// amplitude:-1 default, range 0-255
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			vb.vibrate(VibrationEffect.createOneShot(ms, amplitude));
		} else {
			vb.vibrate(ms);
		}
	}

	public String getClipboardText() {
		ClipData cb = cbm.getPrimaryClip();
		if (cb.getItemCount() > 0) {
			return cb.getItemAt(0).getText().toString();
		}
		return null;
	}

	public void setClipboardText(String text) {
		cbm.setPrimaryClip(ClipData.newPlainText("pxprpc", text));
	}

	public int getDefaultAudioVolume() {
		return am.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	public void setDefaultAudioVolume(int vol) {
		am.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
	}

	public LocationListener lastLocationListener = null;

	public void getGpsLocationInfo(final AsyncReturn<Object> ret, final boolean msgpackMode) {
		if (this.lastLocationListener != null)
			this.cancelGetGpsLocationInfo();
		this.lastLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				if (msgpackMode) {
					ret.result(Utils.packFrom(new MapBuilder2().put("latitude", location.getLatitude())
							.put("longitude", location.getLongitude()).put("speed", location.getSpeed())
							.put("bearing", location.getBearing()).put("altitude", location.getAltitude())
							.put("accuracy", location.getAccuracy()).build()));
				} else {
					ByteBuffer bb = ByteBuffer.allocate(40);
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
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
				Misc2.this.cancelGetGpsLocationInfo();
				ret.result(new IOException("User disable gps provider"));
			}
		};
		lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this.lastLocationListener,
				ApiServer.getHandler().getLooper());
	}

	public void cancelGetGpsLocationInfo() {
		lm.removeUpdates(this.lastLocationListener);
	}

	public byte[] getLightsInfo() {
		MPValueTable mvt = new MPValueTable();
		mvt.header(new String[] { "id", "desc" });
		for (Light2 tl : this.lights) {
			mvt.addRow(new ArrayBuilder2().add(tl.id).add(tl.desc).build());
		}
		return Utils.packFrom(mvt.toValue());
	}

	public void turnOnLight(final AsyncReturn<Object> aret, int id) {
		final Light2 l = this.lights.get(id);
		final CameraManager camSrv = ApiServer.androidcamera2.camSrv;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				camSrv.setTorchMode(l.camId, true);
			} else {
				camSrv.openCamera(l.camId, new CameraDevice.StateCallback() {
					@Override
					public void onOpened(CameraDevice camera) {
						l.camdev = new AndroidCamera2.CameraWrap1(ApiServer.androidcamera2, camera);
						List<Surface> tarSurf = new ArrayList<Surface>();
						try {
							l.camdev.wrapped.createCaptureSession(tarSurf, new CameraCaptureSession.StateCallback() {
								@Override
								public void onConfigured(CameraCaptureSession session) {
									try {
										l.camdev.capSess = session;
										CaptureRequest.Builder capReq = l.camdev.wrapped
												.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
										capReq.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
										session.setRepeatingRequest(capReq.build(), null, ApiServer.handler);
										aret.result(0);
									} catch (Exception e) {
										aret.result(e);
									}
								}

								@Override
								public void onConfigureFailed(CameraCaptureSession session) {
									aret.result(new RuntimeException("createCaptureSession failed."));
								}
							}, ApiServer.getHandler());
						} catch (Exception e) {
							aret.result(new Exception("Android Camera2 Error:" + e.toString()));
						}
					}

					@Override
					public void onDisconnected(CameraDevice camera) {
					}

					@Override
					public void onError(CameraDevice camera, int error) {
						aret.result(new Exception("Android Camera2 Error:" + error));
					}
				}, ApiServer.getHandler());
			}
		} catch (Exception e) {
			aret.result(e);
		}
	}

	public void turnOffLight(int id) throws CameraAccessException {
		final Light2 l = this.lights.get(id);
		CameraManager camSrv = ApiServer.androidcamera2.camSrv;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			camSrv.setTorchMode(l.camId, false);
		} else if(l.camdev!=null){
			ApiServer.closeQuietly(l.camdev);
			l.camdev=null;
		}
	}
}
