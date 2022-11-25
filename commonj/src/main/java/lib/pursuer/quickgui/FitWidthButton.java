package lib.pursuer.quickgui;

import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout.LayoutType;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import lib.pursuer.quickgui.StyleConfig.TextStyle;

public class FitWidthButton extends ExtendLayout {

	private TextButton wl;

	public FitWidthButton() {
	}

	public void initButton(String str, Skin s, String style) {
		wl = new TextButton(str, s, style);
		setWidthLayout(LayoutType.FillParent);
		setHeightLayout(LayoutType.WrapContent);
		setChild(wl);
		StyleConfig.TextStyle txtStyle = s.get(style, StyleConfig.TextStyle.class);
		wl.getLabel().setFontScale(txtStyle.textSize);
	}

	public TextButton getButton() {
		return wl;
	}

}
