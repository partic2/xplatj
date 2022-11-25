package lib.pursuer.quickgui;


import xplatj.gdxplat.pursuer.graphics.drawable.*;
import xplatj.gdxplat.pursuer.gui.drawable.BorderRectDrawable;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.*;

public class PredefinedDrawable {

	public Color tickColor;
	public Color borderColor;
	public Color fillColor;

	public PredefinedDrawable() {
		tickColor = Color.GREEN;
		borderColor = Color.BLACK;
		fillColor = Color.WHITE;
	}

	private class TickIcon extends PixmapCache{
		public TickIcon() {
			super(16,16,Pixmap.Format.RGBA4444);
		}
		@Override
		public void draw(Pixmap pmp) {
			pmp.setColor(Color.CORAL);
			pmp.drawLine(0, 7, 7, 15);
			pmp.drawLine(7, 15, 15, 0);
		}
	}

	private TickIcon tick;

	public Drawable getTickIcon() {
		if (tick == null) {
			tick = new TickIcon();
		}
		return tick;
	}
	
	private class BoxIcon extends BorderRectDrawable{
		public BoxIcon() {
			super(null,Color.BLACK,3);
			setMinWidth(16f);
			setMinHeight(16f);
		}
		@Override
		public void draw(Batch batch, float x, float y, float width, float height) {
			super.draw(batch, x, y, width, height);
		}
	}
	private BoxIcon box;

	public Drawable getBoxIcon() {
		if (box == null) {
			box = new BoxIcon();
		}
		return box;
	}

	private class CheckBoxOnIcon extends BaseDrawable {
		public CheckBoxOnIcon() {
			setMinHeight(8);
			setMinWidth(8);
		}

		@Override
		public void draw(Batch batch, float x, float y, float width, float height) {
			Drawable d1 = getBoxIcon();
			Drawable d2 = getTickIcon();
			d1.draw(batch, x, y, width, height);
			d2.draw(batch, x, y, width, height);
			super.draw(batch, x, y, width, height);
		}
	}

	CheckBoxOnIcon checkOnBox;

	public Drawable getCheckBoxOnIcon() {
		if (checkOnBox == null) {
			checkOnBox = new CheckBoxOnIcon();
		}
		return checkOnBox;
	}

}
