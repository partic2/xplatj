package xplatj.gdxplat.pursuer.graphics.draw;

import java.util.LinkedList;

import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.graphics.drawable.PixmapDrawable;
import xplatj.gdxplat.pursuer.utils.GLUtils;
import xplatj.javaplat.pursuer.util.EventHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;

public class Pixmap2 extends Pixmap {
	private PlatGuiConfig gui;

	private void initInternal() {
		gui = PlatGuiConfig.get();
		queuedOp = new LinkedList<DrawOp>();
	}

	public Pixmap2(FileHandle file) {
		super(file);
		initInternal();
	}

	public Pixmap2(Gdx2DPixmap pixmap) {
		super(pixmap);
		initInternal();
	}

	public Pixmap2(int width, int height, Format format) {
		super(width, height, format);
		initInternal();
	}

	public Pixmap2(byte[] encodedData, int offset, int len) {
		super(encodedData, offset, len);
		initInternal();
	}

	private LinkedList<DrawOp> queuedOp;

	public void requestOp(DrawOp op) {
		queuedOp.add(op);
	}

	private class ImgRenderRunnable implements Runnable {
		private EventHandler<Pixmap2, Integer> callback;

		public ImgRenderRunnable(EventHandler<Pixmap2, Integer> cb) {
			callback = cb;
		}

		@Override
		public void run() {
			FrameBuffer fbo = new FrameBuffer(getFormat(), getWidth(), getHeight(), false);
			Batch bch = gui.mainBatch;
			Matrix4 back = bch.getProjectionMatrix();
			Matrix4 fbmtx = new Matrix4();
			fbmtx.setToOrtho2D(0, 0, getWidth(), getHeight());
			fbmtx.translate(0, getHeight(), 0);
			fbmtx.scale(1, -1, 1);
			fbo.begin();
			bch.setProjectionMatrix(fbmtx);
			bch.begin();
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			PixmapDrawable pd = new PixmapDrawable(Pixmap2.this);
			pd.draw(bch, 0, 0, getWidth(), getHeight());
			bch.end();
			while (queuedOp.size() > 0) {
				DrawOp op = queuedOp.pollFirst();
				if(op.canvasRect==null){
					op.canvasRect=new Rectangle(0, 0,getWidth() , getHeight());
				}
				if (op instanceof DrawOnBatch) {
					DrawOnBatch dobop = (DrawOnBatch) op;
					bch.begin();
					dobop.batch=bch;
					dobop.run();
					bch.end();
				} else if (op instanceof DrawOnShapeRenderer) {
					DrawOnShapeRenderer dsop = (DrawOnShapeRenderer) op;
					dsop.renderer = new ShapeRenderer();
					dsop.renderer.setAutoShapeType(true);
					dsop.renderer.begin();
					dsop.renderer.setProjectionMatrix(fbmtx);
					dsop.run();
					dsop.renderer.flush();
					dsop.renderer.end();
					dsop.renderer.dispose();
				}
			}

			Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
			Gdx.gl.glReadPixels(0, 0, fbo.getWidth(), fbo.getHeight(), GLUtils.getGLFormat(getFormat()),
					GLUtils.getGLType(getFormat()), getPixels());
			bch.setProjectionMatrix(back);
			fbo.end();
			fbo.dispose();
			if (callback != null) {
				callback.handle(Pixmap2.this, 1);
			}
		}
	}

	public void applyOp(EventHandler<Pixmap2, Integer> onFinish) {
		Runnable func = new ImgRenderRunnable(onFinish);
		if (onFinish == null) {
			Gdx2.graphics.runInGLThreadAndWait(func);
		} else {
			Gdx.app.postRunnable(func);
		}
	}
}
