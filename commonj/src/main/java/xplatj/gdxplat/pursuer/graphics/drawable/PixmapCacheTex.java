package xplatj.gdxplat.pursuer.graphics.drawable;

import java.util.HashSet;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.utils.Disposable;

public class PixmapCacheTex implements Disposable, Disableable {

	private Texture tex;
	private HashSet<PixmapCache> cachedVG;
	private int size;

	private int xoff;
	private int yoff;
	private int maxHeight;

	public PixmapCacheTex(int size, Pixmap.Format fmt) {
		tex = new Texture(size, size, fmt);
		this.size = size;
		xoff = 0;
		yoff = 0;
		maxHeight = 0;
		cachedVG = new HashSet<PixmapCache>();
		available = true;
	}

	public int getSize() {
		return size;
	}

	public Pixmap.Format getFormat() {
		return tex.getTextureData().getFormat();
	}

	public TextureRegion addCache(PixmapCache vg) {
		int xprev;

		if (Math.max(vg.getWidth(), vg.getHeight()) > size) {
			// too large Pixmap
			return null;
		}
		if (!cachedVG.contains(vg)) {
			// find space
			// check space on x axis
			if (size - xoff < vg.getWidth()) {
				yoff += maxHeight;
				// check space on y axis
				if (size - yoff < vg.getHeight()) {
					// no available space
					clearCache();
				} else {
					xoff = 0;
				}
			}
			maxHeight = Math.max(maxHeight, vg.getHeight());
			vg.drawOnTexture(tex, xoff, yoff);
			xprev = xoff;
			xoff += vg.getWidth();
			vg.trgn = new TextureRegion(tex, xprev, yoff, vg.getWidth(), vg.getHeight());
			vg.cacheTex = this;
			cachedVG.add(vg);
			vg.cached = true;
		}

		return vg.trgn;
	}

	public void cancelCache(PixmapCache vg) {
		vg.cached = false;
		cachedVG.remove(vg);
	}

	public TextureRegion updateCache(PixmapCache vg) {
		if (vg.cached) {
			vg.drawOnTexture(tex, vg.trgn.getRegionX(), vg.trgn.getRegionY());
			return vg.trgn;
		} else {
			return addCache(vg);
		}
	}

	private boolean available;

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	@Override
	public void dispose() {
		Iterator<PixmapCache> it;
		it = cachedVG.iterator();
		while (it.hasNext()) {
			PixmapCache t = it.next();
			t.cached = false;
			t.cacheTex = null;
		}
		cachedVG.clear();
		tex.dispose();
		available = false;
	}

	@Override
	public boolean isDisabled() {
		return !available;
	}

	@Override
	public void setDisabled(boolean isDisabled) {
	}

	private void clearCache() {
		Iterator<PixmapCache> it;
		it = cachedVG.iterator();
		while (it.hasNext()) {
			it.next().cached = false;
		}
		cachedVG.clear();
		xoff = 0;
		yoff = 0;
		maxHeight = 0;
	}
}
