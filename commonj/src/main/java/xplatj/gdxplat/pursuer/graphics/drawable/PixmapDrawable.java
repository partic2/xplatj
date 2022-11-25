package xplatj.gdxplat.pursuer.graphics.drawable;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class PixmapDrawable extends PixmapCache implements Disposable {

	private Pixmap pmp;

	public PixmapDrawable() {
	}

	public PixmapDrawable(Pixmap p) {
		pmp = p;
		setWidth(p.getWidth());
		setHeight(p.getHeight());
		setFormat(p.getFormat());
	}

	@Override
	public void drawOnTexture(Texture cacheTex, int x, int y) {
		cacheTex.draw(pmp, x, y);
	}

	public Pixmap getPixmap(){
		return pmp;
	}
	@Override
	public void dispose() {
		pmp.dispose();
	}
}
