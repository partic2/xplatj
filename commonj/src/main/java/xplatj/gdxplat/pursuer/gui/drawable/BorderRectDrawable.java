package xplatj.gdxplat.pursuer.gui.drawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

public class BorderRectDrawable extends BaseDrawable{
	private SolidRectDrawable borderDrawable;
	private SolidRectDrawable backgroundDrawable;
	private float borderWidth;
	
	
	public BorderRectDrawable(Color backgroundColor,Color borderColor,float borderWidth) {
		if(borderColor!=null) {
			this.borderDrawable = new SolidRectDrawable(borderColor);
		}
		if(backgroundColor!=null) {
			this.backgroundDrawable=new SolidRectDrawable(backgroundColor);
		}
		this.borderWidth=borderWidth;
	}
	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		if(backgroundDrawable!=null) {
			backgroundDrawable.draw(batch, x+borderWidth, y+borderWidth, width-borderWidth*2, height-borderWidth*2);
		}
		if(borderDrawable!=null) {
			borderDrawable.draw(batch, x, y, width, borderWidth);
			borderDrawable.draw(batch,x,y,borderWidth,height);
			borderDrawable.draw(batch,x+width-borderWidth,y,borderWidth,height);
			borderDrawable.draw(batch, x,y+height-borderWidth,width,borderWidth);
		}
	}
	
}
