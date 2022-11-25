package lib.pursuer.quickgui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SolidProgressBar extends Widget {
	public static class SolidProgressBarStyle {
		public Drawable background;
		public Drawable front;

		public SolidProgressBarStyle() {
		}

		public SolidProgressBarStyle(Drawable background, Drawable front) {
			super();
			this.background = background;
			this.front = front;
		};
	}

	private SolidProgressBarStyle style;
	private float val;

	public SolidProgressBar(Skin skin, String name) {
		if (name == null) {
			name = "default";
		}
		val = 0;
		style = skin.get(name, SolidProgressBarStyle.class);
	}

	// range 0-1
	public void setVal(float val) {
		this.val = val;
	}

	@Override
	public float getPrefWidth() {
		return 300;
	}

	@Override
	public float getPrefHeight() {
		return 20;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
		style.front.draw(batch, getX(), getY(), getWidth() * val, getHeight());
		super.draw(batch, parentAlpha);
	}
}
