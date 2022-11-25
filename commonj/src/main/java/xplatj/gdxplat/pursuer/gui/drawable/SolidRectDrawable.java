package xplatj.gdxplat.pursuer.gui.drawable;

import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.graphics.drawable.PixmapCache;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;

public class SolidRectDrawable extends PixmapCache {
	private Color c;

	public SolidRectDrawable(Color clr) {
		super(1,1,Pixmap.Format.RGBA4444);
		c = clr;
	}
	@Override
	public void draw(Pixmap pmp) {
		pmp.drawPixel(0, 0, Color.rgba8888(c));
		super.draw(pmp);
	}
}
