package lib.pursuer.quickgui;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.*;

import lib.pursuer.quickgui.StyleConfig.CommonStyle;
import lib.pursuer.quickgui.StyleConfig.TextStyle;

import com.badlogic.gdx.scenes.scene2d.utils.*;

public class HorizontalButonGroup extends WidgetGroup {
	private ArrayList<TextButton> btns;
	private float halfspace;
	private int align;
	private float space;

	public HorizontalButonGroup() {
	}

	public void initButtonGroup(String[] names, Skin skin, String style) {
		space = skin.get("default", StyleConfig.CommonStyle.class).thinSpace;
		TextStyle tsty = skin.get(style, StyleConfig.TextStyle.class);
		btns = new ArrayList<TextButton>();
		for (String name : names) {
			TextButton el = new TextButton(name, skin, style);
			el.getLabel().setFontScale(tsty.textSize);
			btns.add(el);
			addActor(el);
		}
		this.halfspace = space / 2;
	}

	public AbstractList<TextButton> getButtons() {
		return btns;
	}

	@Override
	public float getPrefWidth() {
		return this.getParent().getWidth();
	}

	@Override
	public float getPrefHeight() {
		if (btns != null && btns.size() > 0) {
			return btns.get(0).getPrefHeight();
		}
		return 0;
	}

	@Override
	public void layout() {
		float avgw = getWidth() / btns.size();
		Iterator<TextButton> ittb = btns.iterator();
		float left;
		left = 0;
		while (ittb.hasNext()) {
			TextButton etb = ittb.next();
			etb.setX(left + halfspace);
			etb.setWidth(avgw - halfspace * 2);
			etb.setY(0);
			etb.setHeight(etb.getPrefHeight());
			left += avgw;
		}
		super.layout();
	}
}
