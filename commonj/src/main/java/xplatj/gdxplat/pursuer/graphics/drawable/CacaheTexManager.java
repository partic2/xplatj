package xplatj.gdxplat.pursuer.graphics.drawable;

import java.util.HashSet;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

public class CacaheTexManager implements Disposable {
	private HashSet<PixmapCacheTex> VGCTs;

	public CacaheTexManager() {
		VGCTs = new HashSet<PixmapCacheTex>();
	}

	public void newCacheTex(PixmapCacheTex vgct) {
		VGCTs.add(vgct);
	}

	public PixmapCacheTex obtainCacheTex(int size, Pixmap.Format fmt) {
		if (fmt == null) {
			fmt = Pixmap.Format.RGBA4444;
		}
		Iterator<PixmapCacheTex> it = VGCTs.iterator();
		PixmapCacheTex vgct;
		while (it.hasNext()) {
			vgct = it.next();
			if (vgct.getSize() > size && vgct.getFormat() == fmt) {
				return vgct;
			}
		}
		if (size < 64) {
			size = 256;
		}
		vgct = new PixmapCacheTex(size, fmt);
		newCacheTex(vgct);
		return vgct;
	}

	public void clearAllCacheTex() {
		Iterator<PixmapCacheTex> it = VGCTs.iterator();
		while (it.hasNext()) {
			PixmapCacheTex v = it.next();
			v.dispose();
		}
		VGCTs.clear();
	}

	@Override
	public void dispose() {
		clearAllCacheTex();
	}
}
