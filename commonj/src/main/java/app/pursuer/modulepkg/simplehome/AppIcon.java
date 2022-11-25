package app.pursuer.modulepkg.simplehome;

import xplatj.gdxplat.pursuer.utils.Env;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import lib.pursuer.quickgui.StyleConfig;

public class AppIcon extends Table {
	private Image iconView;
	private Label nameLabel;
	
	private Drawable iconDrawable;
	public static final float iconSize=160;
	public String id;
	public AppIcon(Drawable iconDrawable,final String name,String id) {
		this.iconDrawable=iconDrawable;
		iconView=new Image(iconDrawable);
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				nameLabel=new Label(name,Env.i(StyleConfig.class).getDefaultSkin(),StyleConfig.DefaultStyle);
				nameLabel.setFontScale(1/2f);
				nameLabel.setWrap(true);
				addActor(nameLabel);
			}
		});
		addActor(iconView);
		this.id=id;
	}
	@Override
	protected void finalize() throws Throwable {
		if(iconDrawable instanceof Disposable){
			((Disposable) iconDrawable).dispose();
		}
		super.finalize();
	}
	@Override
	public float getPrefWidth() {
		return iconSize+20;
	}
	@Override
	public float getPrefHeight() {
		if(nameLabel==null){
			return iconSize+20;
		}
		return nameLabel.getPrefHeight()+iconSize+20;
	}
	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
	@Override
	public void layout() {
		if(nameLabel==null||iconView==null){
			return;
		}
		float width=getWidth();
		nameLabel.setX(0);
		nameLabel.setY(0);
		nameLabel.setWidth(getWidth());
		nameLabel.setHeight(nameLabel.getPrefHeight());
		nameLabel.setAlignment(Align.center);
		iconView.setX((width-iconSize)/2);
		iconView.setY(nameLabel.getTop());
		iconView.setWidth(iconSize);
		iconView.setHeight(iconSize);
		
	}
}
