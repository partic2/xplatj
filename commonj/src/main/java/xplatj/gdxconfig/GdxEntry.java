package xplatj.gdxconfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import xplatj.gdxconfig.control.GraphicsControlImpl;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxconfig.gui.GdxDefaultApp;
import xplatj.gdxconfig.gui.PlatGdxVariableTable;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.io.IDataBlock;
import xplatj.javaplat.pursuer.io.stream.DataBlockInputStream;

import com.badlogic.gdx.ApplicationListener;

public class GdxEntry implements ApplicationListener {
	private ApplicationListener app;

	public void switchApp(ApplicationListener al) {
		app.dispose();
		app = al;
		app.create();
	}

	private PlatCoreConfig core;

	public void initPlat() {
		if(PlatCoreConfig.get()==null) {
			PlatCoreConfig.singleton.set(new PlatCoreConfig());
		}
		Gdx2.graphics=new GraphicsControlImpl();
		core = PlatCoreConfig.get();
		core.context=new PlatGdxVariableTable(core.context);
		IFile f = core.fs.resolve("/6/cfg.ini");
		Properties ini = new Properties();
		try {
			IDataBlock db = f.open();
			InputStream in = new DataBlockInputStream(db);
			ini.load(in);
			in.close();
			db.free();
		} catch (IOException e) {
			app = new GdxDefaultApp();
			return;
		}
		String cp = ini.getProperty("gdx.classpath");
		String[] cps = cp.split(":");
		for (String s : cps) {
			if (!s.equals("")) {
				core.loadClasses(new File[] {core.fs.resolve(s).getJavaFile()});
			}
		}
		try {
			app = (ApplicationListener) core.classSpace.loadClass(ini.getProperty("gdx.entry")).newInstance();
		} catch (Exception e) {
			app = new GdxDefaultApp();
		}
		
		String debugRfsOpt = ini.getProperty("debug.rdb");
		if("enable".equals(debugRfsOpt)) {
			lib.pursuer.remotedebugbridge.Server rdb = new lib.pursuer.remotedebugbridge.Server();
			rdb.start();
			Env.ss(lib.pursuer.remotedebugbridge.Server.class, rdb);
		}
	}

	@Override
	public void create() {
		Gdx2.entry = this;
		initPlat();
		app.create();
	}

	@Override
	public void resize(int width, int height) {
		app.resize(width, height);
	}

	@Override
	public void render() {
		app.render();
	}

	@Override
	public void pause() {
		app.pause();
	}

	@Override
	public void resume() {
		app.resume();
	}

	@Override
	public void dispose() {
		app.dispose();
		PlatGuiConfig.releaseCurrent();
		PlatCoreConfig.releaseCurrent();
	}

}
