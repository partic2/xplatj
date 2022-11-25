package xplatj.gdxplat.pursuer.utils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;

public class GLUtils {
	public static int getGLFormat(Pixmap.Format fmt) {
		switch (fmt) {
		case RGB565:
			return GL20.GL_RGB;
		case RGB888:
			return GL20.GL_RGB;
		case RGBA4444:
			return GL20.GL_RGBA;
		case RGBA8888:
			return GL20.GL_RGBA;
		default:
			return 0;
		}
	}

	public static int getGLType(Pixmap.Format fmt) {
		switch (fmt) {
		case RGB565:
			return GL20.GL_UNSIGNED_SHORT_5_6_5;
		case RGB888:
			return GL20.GL_UNSIGNED_BYTE;
		case RGBA4444:
			return GL20.GL_UNSIGNED_SHORT_4_4_4_4;
		case RGBA8888:
			return GL20.GL_UNSIGNED_BYTE;
		default:
			return 0;
		}
	}
}
