package lib.pursuer.quickgui;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.graphics.drawable.WrapDrawable;
import xplatj.gdxplat.pursuer.gui.drawable.BorderRectDrawable;
import xplatj.gdxplat.pursuer.gui.drawable.SolidRectDrawable;
import xplatj.gdxplat.pursuer.utils.Env;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;

public class StyleConfig implements Disposable {

	private Skin dskin;

	public Color textColor;

	public Drawable borderDrawable;
	public Drawable highlight1Drawable;
	public Drawable highlight2Drawable;
	public Drawable highlight3Drawable;
	public Drawable floatDrawable;
	public Drawable transparentDrawable;

	public float normalSize;
	public float largeSize;
	public float smallSize;

	public float thinSpace;
	public float thickSpace;
	public float longPressDelay;
	public float tapCountInterval;
	
	public static String DefaultStyle="default";
	public static String FloatStyle="float";
	public static String ButtonStyle="button";
	public static String EditableStyle="editable";
	public static String DisableStyle="disable";

	public static class CommonStyle {
		public float thinSpace;
		public float thickSpace;
	}

	public static class TextStyle {
		public float textSize;
		public Color textColor;
	}

	public StyleConfig() {
		
		textColor=Color.BLACK;
		
		highlight3Drawable=new SolidRectDrawable(Color.CORAL);
		highlight2Drawable = new SolidRectDrawable(Color.SKY);
		highlight1Drawable = new SolidRectDrawable(Color.LIGHT_GRAY);
		borderDrawable = new BorderRectDrawable(null,Color.BLACK,3);
		floatDrawable = new BorderRectDrawable(null,Color.BLACK,3);

		normalSize = 1;
		largeSize = 1.5f;
		smallSize = 2f / 3;

		thinSpace = 5;
		thickSpace = 15;
		longPressDelay = 1;
		tapCountInterval = 0.5f;
	}

	public Skin getDefaultSkin() {
		PlatGuiConfig gui = PlatGuiConfig.get();
		PlatCoreConfig core = PlatCoreConfig.get();
		if (dskin == null) {
			dskin = new Skin();
			List.ListStyle slist = new List.ListStyle();
			slist.font = gui.font;
			slist.background=borderDrawable;
			slist.fontColorSelected = textColor;
			slist.fontColorUnselected = textColor;
			slist.selection = highlight1Drawable;
			dskin.add(DefaultStyle, slist);
			slist = new List.ListStyle();
			slist.font = gui.font;
			slist.background=floatDrawable;
			slist.fontColorSelected = textColor;
			slist.fontColorUnselected = textColor;
			slist.selection = highlight2Drawable;
			dskin.add(FloatStyle, slist);
			
			Label.LabelStyle slabel = new Label.LabelStyle();
			slabel.font = gui.font;
			slabel.fontColor = textColor;
			slabel.background = null;
			dskin.add(DefaultStyle, slabel);
			slabel = new Label.LabelStyle();
			slabel.font = gui.font;
			slabel.fontColor = textColor;
			slabel.background = highlight1Drawable;
			dskin.add(ButtonStyle, slabel);
			slabel = new Label.LabelStyle();
			slabel.font = gui.font;
			slabel.fontColor = textColor;
			slabel.background = floatDrawable;
			dskin.add(FloatStyle, slabel);

			TextField.TextFieldStyle stf = new TextField.TextFieldStyle();
			stf.font = gui.font;
			stf.fontColor = textColor;
			stf.background = borderDrawable;
			stf.cursor = new WrapDrawable(highlight2Drawable);
			stf.cursor.setMinWidth(5);
			stf.selection=highlight1Drawable;
			dskin.add(DefaultStyle, stf);
			dskin.add(EditableStyle, stf);
			stf = new TextField.TextFieldStyle();
			stf.font = gui.font;
			stf.fontColor = textColor;
			stf.background = floatDrawable;
			stf.cursor = new WrapDrawable(highlight2Drawable);
			stf.cursor.setMinWidth(5);
			stf.focusedBackground=highlight1Drawable;
			dskin.add(FloatStyle, stf);

			TextStyle stxt = new TextStyle();
			stxt.textSize = smallSize;
			stxt.textColor = textColor;
			dskin.add(DefaultStyle, stxt);
			stxt = new TextStyle();
			stxt.textSize = normalSize;
			stxt.textColor = textColor;
			dskin.add(ButtonStyle, stxt);
			stxt = new TextStyle();
			stxt.textSize = smallSize;
			stxt.textColor = textColor;
			dskin.add(FloatStyle, stxt);
			stxt = new TextStyle();
			stxt.textSize = smallSize;
			stxt.textColor = textColor;
			dskin.add(EditableStyle, stxt);

			TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
			tbs.up = highlight1Drawable;
			tbs.down = highlight2Drawable;
			tbs.font = gui.font;
			tbs.fontColor = textColor;
			dskin.add(DefaultStyle, tbs);
			dskin.add(ButtonStyle, tbs);
			dskin.add(FloatStyle, tbs);


			Window.WindowStyle ws = new Window.WindowStyle();
			ws.background = borderDrawable;
			ws.stageBackground = transparentDrawable;
			ws.titleFont = gui.font;
			ws.titleFontColor = textColor;
			dskin.add(DefaultStyle, ws);
			dskin.add(FloatStyle, ws);

			CheckBox.CheckBoxStyle cbs = new CheckBox.CheckBoxStyle();
			PredefinedDrawable iconlib = Env.t(PredefinedDrawable.class);
			cbs.checkboxOn = iconlib.getCheckBoxOnIcon();
			cbs.checkboxOff = iconlib.getBoxIcon();
			cbs.font = gui.font;
			cbs.fontColor = textColor;
			dskin.add(DefaultStyle, cbs);

			CommonStyle cs = new CommonStyle();
			cs.thickSpace = thickSpace;
			cs.thinSpace = thinSpace;
			dskin.add(DefaultStyle, cs);
			
			FormScene.FormSceneStyle dstyle=new FormScene.FormSceneStyle();
			dstyle.popupCombobox=false;
			dskin.add(DefaultStyle, dstyle);
		}
		return dskin;
	}

	@Override
	public void dispose() {
	}
}
