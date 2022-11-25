package xplatj.gdxplat.pursuer.graphics.draw;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public abstract class DrawOnShapeRenderer extends DrawOp implements Runnable {

	public ShapeRenderer renderer;

	public DrawOnShapeRenderer() {
	}
}
