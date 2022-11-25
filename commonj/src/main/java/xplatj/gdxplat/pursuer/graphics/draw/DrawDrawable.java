package xplatj.gdxplat.pursuer.graphics.draw;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class DrawDrawable extends DrawOnBatch {
	public Drawable drawable;
	public float x;
	public float y;
	public float width;
	public float height;
	public Matrix4 transform;
	public DrawDrawable(){};
	public DrawDrawable set(Drawable drawable, float x, float y, float width, float height,Matrix4 transform){
		this.drawable = drawable;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.transform=transform;
		return this;
	}
	@Override
	public void run() {
		Matrix4 oldmtx=null;
		if(transform!=null){
			Matrix4 mtx=batch.getTransformMatrix();
			oldmtx=new Matrix4(mtx);
			mtx.mul(transform);
			batch.setTransformMatrix(mtx);
		}
		drawable.draw(batch, x, y, width, height);
		if(oldmtx!=null){
			batch.setTransformMatrix(oldmtx);
		}
	}
}
