package xplatj.gdxconfig.gui;

import java.io.InputStream;
import java.util.Properties;
import xplatj.gdxconfig.core.*;
import xplatj.gdxplat.com.rpsg.lazyFont.LazyBitmapFont;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout.LayoutType;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.viewport.*;

public class GdxDefaultApp implements ApplicationListener {
	private Batch bch;
	private Stage stg;
	private Viewport vp;
	private LifecycleWidget root;

	PlatCoreConfig core;
	PlatGuiConfig gui;

	FreeTypeFontGenerator ttfg;

	protected float defaultScale = 1;

	protected boolean isSupportGui=false;
	@Override
	public void create() {
		if(Gdx.graphics!=null&&Gdx.graphics.getGL20()!=null) {
			isSupportGui=true;
		}
		if(isSupportGui) {
			Gdx.graphics.setContinuousRendering(false);
			Gdx.app.setLogLevel(Application.LOG_DEBUG);
			core = PlatCoreConfig.get();
			gui = PlatGuiConfig.get();
			if(gui==null) {
				gui=new PlatGuiConfig();
				PlatGuiConfig.singleton.set(gui);
			}
			int w = 0;
			int h = 0;
			LifecycleWidget rootWidget = null;
			try {
				Properties inigui = new Properties();
				UtilsService quick = Env.i(UtilsService.class);
				InputStream in = quick.readFromIFile(core.fs.resolve("/6/config/gui.ini"));
				inigui.load(in);
				in.close();
				w = Integer.parseInt(inigui.getProperty("screen.width"));
				h = Integer.parseInt(inigui.getProperty("screen.height"));
				String fontPath = inigui.getProperty("font.path");
				if(fontPath!=null) {
					ttfg = new FreeTypeFontGenerator(new FileHandle(core.fs.resolve(fontPath).getJavaFile()));
					gui.font.dispose();
					gui.font = new LazyBitmapFont(ttfg, Integer.parseInt(inigui.getProperty("font.size")));
				}
				String rootname = inigui.getProperty("view.root");
				gui.homeViewClass=(Class<? extends LifecycleWidget>) this.getClass().getClassLoader().loadClass(rootname);
				rootWidget = (LifecycleWidget) gui.homeViewClass.newInstance();
				rootWidget.setWidthLayout(LayoutType.Custom);
				rootWidget.setHeightLayout(LayoutType.Custom);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (w > 0 || h > 0) {
				Gdx.graphics.setWindowedMode(w, h);
			}
			bch = new SpriteBatch();
			gui.mainBatch = bch;
			vp = new ScreenViewport();
			stg = new Stage(vp, bch);
			stg.setActionsRequestRendering(false);
			gui.mainStage = stg;
			Gdx.input.setInputProcessor(stg);
			gui.relayout=new Runnable() {
				@Override
				public void run() {
					resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				}
			};
			gui.applyConfig=new Runnable() {
				public void run() {
					applyConfig();
				}
			};
			root = rootWidget;
			stg.addActor(root);
			gui.mainView=root;
			root.resume();
		}
		
	}
	
	public void applyConfig(){
		if(isSupportGui) {
			if(stg!=gui.mainStage){
				stg=gui.mainStage;
			}
			if(bch!=gui.mainBatch){
				bch=gui.mainBatch;
			}
			if(root!=gui.mainView){
				root=gui.mainView;
				stg.clear();
				stg.addActor(root);
				root.resume();
				gui.relayout.run();
			}
		}
	}

	@Override
	public void dispose() {
		if(root!=null) {
			root.dispose();
		}
		if(ttfg!=null) {
			ttfg.dispose();
		}
		if(stg!=null) {
			stg.dispose();
		}
		if(bch!=null) {
			bch.dispose();
		}
	}

	@Override
	public void pause() {
		if(root!=null) {
			root.pause();
		}
	}

	@Override
	public void render() {
		if(isSupportGui) {
			if (gui.background != null) {
				Gdx.gl.glClearColor(gui.background.r, gui.background.g, gui.background.b, gui.background.a);
				Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			}
			stg.act();
			stg.draw();
		}
	}
	@Override
	public void resize(int arg0, int arg1) {
		if(isSupportGui) {
			if (root != null && gui != null && gui.scaleMode != null) {
				vp.update(arg0, arg1, true);
				float pw = root.getPrefWidth();
				float ph = root.getPrefHeight();

				switch (gui.scaleMode) {
				case Fill:
					float fx = arg0 / pw;
					float fy = arg1 / ph;
					root.setScale(fx, fy);
					root.setPosition(0, 0);
					break;
				case Fit:
					fx = arg0 / pw;
					fy = arg1 / ph;
					root.setScale(Math.min(fx, fy));
					root.setPosition(0, 0);
					break;
				}
			}
		}
		
	}

	@Override
	public void resume() {
		if(root!=null) {
			root.resume();
		}
	}
}
