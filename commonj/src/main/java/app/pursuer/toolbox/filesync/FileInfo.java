package app.pursuer.toolbox.filesync;

import java.io.UnsupportedEncodingException;

import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.util.Parcelable;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;

class FileInfo implements Parcelable {
	private static class pFileInfo {
		String path;
		int size;
	}

	pFileInfo info;

	public FileInfo(String path, int size) {
		info = new pFileInfo();
		info.path = path;
		info.size = size;
	}

	public FileInfo() {
	};

	public String getName() {
		return info.path;
	}

	public int getSize() {
		return info.size;
	}

	@Override
	public byte[] saveToBytes() {
		try {
			Json tjs = Env.t(Json.class);
			String str = tjs.toJson(info, pFileInfo.class);
			return str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	@Override
	public boolean loadFromBytes(byte[] buff) {
		try {
			String jstr = new String(buff, "UTF-8");
			Json json = Env.t(Json.class);
			json.setIgnoreUnknownFields(true);
			try {
				info = json.fromJson(pFileInfo.class, jstr);
			} catch (SerializationException e) {
				return false;
			}
			if (info == null) {
				return false;
			}
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}

}
