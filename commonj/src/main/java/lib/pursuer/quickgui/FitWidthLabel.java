package lib.pursuer.quickgui;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout.LayoutType;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;

import lib.pursuer.quickgui.StyleConfig.TextStyle;

public class FitWidthLabel extends ExtendLayout {

	private Label wl;

	public FitWidthLabel() {
	}

	public void initLabel(String str, Skin s, String style) {
		wl = new Label(str, s, style);
		wl.setWrap(true);
		setWidthLayout(LayoutType.FillParent);
		setHeightLayout(LayoutType.WrapContent);
		setChild(wl);
		StyleConfig.TextStyle txtStyle = s.get(style, StyleConfig.TextStyle.class);
		wl.setFontScale(txtStyle.textSize);
	}

	public Label getLabel() {
		return wl;
	}

}
