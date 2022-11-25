package xplatj.gdxconfig.gui;

import xplatj.gdxplat.pursuer.graphics.drawable.CacaheTexManager;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.javaplat.pursuer.util.Container;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

public class PlatGuiConfig implements Disposable {
	public BitmapFont font;
	public Stage mainStage;
	public Batch mainBatch;
	public LifecycleWidget mainView;
	public Color background;
	public Pixmap.Format defaultFormat;
	public CacaheTexManager cacaheTexManager;
	public Thread glContextThread;
	public ScaleMode scaleMode;
	public Runnable relayout;
	public Runnable applyConfig;
	public Class<? extends LifecycleWidget> homeViewClass;

	public enum ScaleMode {
		Fit, Fill
	};
	
	public PlatGuiConfig() {
		this(true);
	}
	
	public PlatGuiConfig(boolean initDefault) {
		if(initDefault) {
			initDefault();
		}
	}

	public boolean initDefault() {
		font = new BitmapFont();
		defaultFormat = Pixmap.Format.RGBA4444;
		scaleMode = ScaleMode.Fit;
		cacaheTexManager = new CacaheTexManager();
		glContextThread = Thread.currentThread();
		background = new Color(1, 1, 1, 1);
		return true;
	}

	public static Container<PlatGuiConfig> singleton=new Container<PlatGuiConfig>();
	public static PlatGuiConfig get() {
		return singleton.get();
	}
	public static void releaseCurrent() {
		synchronized (singleton) {
			if(singleton.get()!=null) {
				singleton.get().dispose();
				singleton.set(null);
			}
		}
	}
	@Override
	public void dispose() {
		font.dispose();
		cacaheTexManager.dispose();
	}
}
