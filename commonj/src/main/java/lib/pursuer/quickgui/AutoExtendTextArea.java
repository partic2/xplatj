package lib.pursuer.quickgui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;

import xplatj.gdxconfig.Gdx2;

public class AutoExtendTextArea extends TextArea {
	public AutoExtendTextArea(String text, Skin skin, String styleName) {
		super(text, skin, styleName);
	}
	private int prevNLine=0;
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		int newNLine=getLines();
		if(prevNLine!=newNLine) {
			prevNLine=newNLine;
			invalidateHierarchy();
			setPrefRows(getLines());
			int oldCurPos=getCursorPosition();
			moveCursorLine(0);
			setCursorPosition(oldCurPos);
			Gdx2.graphics.renderForTime(0);
		}
	}
	
}
