package xplatj.gdxplat.pursuer.graphics.drawable;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class WrapDrawable extends BaseDrawable {
	public Drawable d;

	public WrapDrawable(Drawable d) {
		this.d = d;
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		d.draw(batch, x, y, width, height);
	}
}
