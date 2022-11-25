package xplatj.gdxplat.pursuer.graphics.drawable;

import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.gui.PlatGuiConfig;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

public class PixmapCache extends BaseDrawable {
	TextureRegion trgn;
	private int width, height;
	private Pixmap.Format format;
	public PixmapCacheTex cacheTex;
	public boolean cached;

	public PixmapCache() {
		setFormat(PlatGuiConfig.get().defaultFormat);
	}

	public PixmapCache(int width, int height, Pixmap.Format fmt) {
		setFormat(fmt);
		setWidth(width);
		setHeight(height);
	}

	public Pixmap.Format getFormat() {
		return format;
	}

	public void setFormat(Pixmap.Format format) {
		this.format = format;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
		setMinWidth(width);
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
		setMinHeight(height);
	}

	public void updateCache() {
		if(cacheTex!=null) {
			cacheTex.updateCache(PixmapCache.this);
		}
	}

	public void draw(Pixmap pmp) {
	};

	public void drawOnTexture(Texture cacheTex, int x, int y) {
		Pixmap pmp = new Pixmap(getWidth(), getHeight(), getFormat());
		pmp.setBlending(Blending.None);
		draw(pmp);
		cacheTex.draw(pmp, x, y);
		pmp.dispose();
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		batch.draw(getTextureRegion(), x, y, width, height);
	}

	public TextureRegion getTextureRegion() {
		if (cached) {
			return trgn;
		} else {
			if (cacheTex != null) {
				return cacheTex.addCache(this);
			} else {
				return PlatGuiConfig.get().cacaheTexManager.obtainCacheTex(
						Math.max(this.getWidth(), this.getHeight()), getFormat()).addCache(
						this);
			}
		}
	}
}
