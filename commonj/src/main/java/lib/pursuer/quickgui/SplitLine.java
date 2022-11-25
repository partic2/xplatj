package lib.pursuer.quickgui;

import com.badlogic.gdx.scenes.scene2d.ui.*;

public class SplitLine extends Widget {
	private float space = 0;
	private boolean vertical = false;

	public SplitLine() {
	}

	public void setSpace(float space) {
		this.space = space;
	}

	public void setVertical(boolean v) {
		vertical = v;
	}

	@Override
	public float getPrefWidth() {
		float r = 0;
		if (vertical) {
			r = space;
		} else {
			r = getParent().getWidth();
		}
		return r;
	}

	@Override
	public float getPrefHeight() {
		float r = 0;
		if (!vertical) {
			r = space;
		} else {
			r = getParent().getHeight();
		}
		return r;
	}
}
