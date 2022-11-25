package xplatj.gdxplat.pursuer.graphics.drawable;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;

import xplatj.gdxconfig.Gdx2;

public class TextDrawable extends BaseDrawable {
	public TextDrawable() {
	}

	private GlyphLayout layout;
	private Color clr;
	private BitmapFont font;
	private String text;
	public TextDrawable(BitmapFont font, Color color){
		init(font,color);
	}
	public TextDrawable(BitmapFont font, Color color,String text){
		init(font,color);
		setText(text);
	}
	public void init(BitmapFont font, Color color) {
		this.font = font;
		clr = color;
	}

	public void setText(String text) {
		this.text=text;
		Gdx2.graphics.runInGLThreadAndWait(new Runnable() {
			@Override
			public void run() {
				layout = new GlyphLayout(font, TextDrawable.this.text,clr,0,Align.left,false);
			}
		});
	}
	
	public String getText(){
		return text;
	}
	

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		float scaleY=height/(layout.height-font.getDescent());
		float scaleX=width/layout.width;
		Matrix4 backup=new Matrix4(batch.getTransformMatrix());
		batch.setTransformMatrix(new Matrix4(backup).translate(x, y + height, 0).scale(scaleX, scaleY, 1));
		font.draw(batch, layout,0,0);
		batch.setTransformMatrix(backup);
	}

	@Override
	public float getLeftWidth() {
		return 0;
	}

	@Override
	public void setLeftWidth(float leftWidth) {
	}

	@Override
	public float getRightWidth() {
		return 0;
	}

	@Override
	public void setRightWidth(float rightWidth) {
	}

	@Override
	public float getTopHeight() {
		return 0;
	}

	@Override
	public void setTopHeight(float topHeight) {
	}

	@Override
	public float getBottomHeight() {
		return 0;
	}

	@Override
	public void setBottomHeight(float bottomHeight) {
	}

	@Override
	public float getMinWidth() {
		if(layout!=null){
			return layout.width;
		}else{
			return 0;
		}
	}

	@Override
	public void setMinWidth(float minWidth) {

	}

	@Override
	public float getMinHeight() {
		if(layout!=null){
			return layout.height;
		}else{
			return 0;
		}
	}

	@Override
	public void setMinHeight(float minHeight) {
	}

}
