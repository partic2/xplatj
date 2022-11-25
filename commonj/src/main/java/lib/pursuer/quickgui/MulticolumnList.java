package lib.pursuer.quickgui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import lib.pursuer.quickgui.StyleConfig.CommonStyle;
import xplatj.javaplat.pursuer.util.EventHandler;

public class MulticolumnList extends Table {
	public int nCol;
	
	public MulticolumnList() {
	}
	public MulticolumnList(Skin skin,int nCol) {
		setSkin(skin);
		setTableSize(nCol);
	}
	public void setSkin(Skin skin){
		super.setSkin(skin);
	}
	public void setTableSize(int nCol){
		this.nCol=nCol;
	}
	public Cell<? extends Actor> addItem(Actor item){
		if(getCells().size>0){
			if(getCells().peek().getColumn()+getCells().peek().getColspan()>=nCol){
				row();
			}
		}
		Cell<Actor> cell = add(item);
		return cell;
	}
	private static class ClickListenerBridge extends ClickListener{
		TextButton button;
		EventHandler<TextButton,InputEvent> onClick;
		public ClickListenerBridge(TextButton button,EventHandler<TextButton,InputEvent> onClick) {
			this.button=button;
			this.onClick=onClick;
		}
		@Override
		public void clicked(InputEvent event, float x, float y) {
			super.clicked(event, x, y);
			onClick.handle(button, event);
		}
	}
	private static class CustomHeightTextButton extends TextButton{
		public float heightFactor=1;
		public CustomHeightTextButton(String text, Skin skin,String styleName) {
			super(text, skin,styleName);
		}
		@Override
		public float getPrefHeight() {
			return super.getPrefHeight()*heightFactor;
		}
	}
	public Cell<TextButton> addButton(String text,EventHandler<TextButton, InputEvent> onClick){
		CustomHeightTextButton button=new CustomHeightTextButton(text, getSkin() ,StyleConfig.DefaultStyle);
		button.heightFactor=1.5f;
		button.addListener(new ClickListenerBridge(button, onClick));
		@SuppressWarnings("unchecked")
		Cell<TextButton> cell = (Cell<TextButton>) addItem(button);
		CommonStyle style = getSkin().get(StyleConfig.DefaultStyle, StyleConfig.CommonStyle.class);
		cell.growX().pad(style.thinSpace);
		return cell;
	}
}
