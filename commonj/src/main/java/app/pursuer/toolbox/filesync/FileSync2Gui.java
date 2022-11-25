package app.pursuer.toolbox.filesync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;

import app.pursuer.toolbox.filesync.DirScan.SyncDirConfig;
import lib.pursuer.quickgui.AutoExtendTextArea;
import lib.pursuer.quickgui.FileBrowser;
import lib.pursuer.quickgui.FitWidthButton;
import lib.pursuer.quickgui.ScenesFlowWidget;
import lib.pursuer.quickgui.StyleConfig;
import lib.pursuer.quickgui.StyleConfig.TextStyle;
import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.gui.widget.Scene;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.util.EventListener2;

public class FileSync2Gui extends ScenesFlowWidget {
	public static final String configFileName = "config.json";
	public FileSync2Backend backend;
	public Skin skin;

	public static class StringConstant {
		public String selectedPath = "selected path:";
		public String browser = "browser >";
		public String dataFile = "data file";
		public String resourceFile = "resource file";
		public String listenAsSource = "listen as file source";
		public String reuireSource = "require source";
		public String addToFavo = "add to favorites";
		public String favorites = "favorites >";
		public String cancel = "cancel";
		public String[] statusText = new String[] { "", "waiting for server...",
				"waiting for client...", "translating...", "send finish.",
				"recveive finsh.", "error occured.","timeout.","interrupted." };
		public String swSyncCfgFile="switch sync config file >";
	}

	public static class Config {
		public String selectedPath;
		public ArrayList<String> favos;
	}

	public Config cfg;
	public StringConstant stringConstant;
	PlatCoreConfig core=PlatCoreConfig.get();

	private class FileSyncHomeView extends VerticalGroup {
		AutoExtendTextArea syncPathView;
		Label lblSyncPath;
		FitWidthButton btnBrowser;
		FitWidthButton btnAsSrc;
		FitWidthButton btnReqSrc;
		FitWidthButton btnAddFavo;
		FitWidthButton btnSelFavo;
		Label statusView;
		FitWidthButton btnSwSyncCfg;

		public FileSyncHomeView() {

			lblSyncPath = new Label(stringConstant.selectedPath, skin,
					StyleConfig.DefaultStyle);
			addActor(new ExtendLayout(lblSyncPath,
					ExtendLayout.LayoutType.FillParent,
					ExtendLayout.LayoutType.WrapContent));

			syncPathView = new AutoExtendTextArea("",skin, StyleConfig.EditableStyle);
			float textSize = skin.get(StyleConfig.EditableStyle, TextStyle.class).textSize;
			syncPathView.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent arg0, Actor arg1) {
					cfg.selectedPath = syncPathView.getText();
				}
			});
			ExtendLayout pathViewWrap=new ExtendLayout(syncPathView, LayoutType.FillParent,
					LayoutType.WrapContent);
			pathViewWrap.setScalableLayout(true);
			addActor(pathViewWrap);
			pathViewWrap.setScale(textSize);

			btnBrowser = new FitWidthButton();
			btnBrowser.initButton(stringConstant.browser, skin,
					StyleConfig.ButtonStyle);
			btnBrowser.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					FileSync2Gui.this.gotoSceneWithFlow(getSceneByClass(FileBrowserScene.class));
					super.clicked(event, x, y);
				}
			});
			addActor(btnBrowser);

			btnAsSrc = new FitWidthButton();
			btnAsSrc.initButton(stringConstant.listenAsSource, skin,
					StyleConfig.ButtonStyle);
			btnAsSrc.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					backend.init(new File(cfg.selectedPath));
					backend.startServeProg();
					super.clicked(event, x, y);
				}
			});
			addActor(btnAsSrc);

			btnReqSrc = new FitWidthButton();
			btnReqSrc.initButton(stringConstant.reuireSource, skin,
					StyleConfig.ButtonStyle);
			btnReqSrc.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					backend.init(new File(cfg.selectedPath));
					backend.startClientProg();
					super.clicked(event, x, y);
				}
			});
			addActor(btnReqSrc);

			btnAddFavo = new FitWidthButton();
			btnAddFavo.initButton(stringConstant.addToFavo, skin,
					StyleConfig.ButtonStyle);
			btnAddFavo.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					cfg.favos.remove(cfg.favos.size() - 1);
					cfg.favos.add(0, cfg.selectedPath);
					reload();
					super.clicked(event, x, y);
				}
			});
			addActor(btnAddFavo);

			btnSelFavo = new FitWidthButton();
			btnSelFavo.initButton(stringConstant.favorites, skin,
					StyleConfig.ButtonStyle);
			btnSelFavo.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					gotoSceneWithFlow(getSceneByClass(FavoSelectScene.class));
					super.clicked(event, x, y);
				}
			});
			addActor(new ExtendLayout(btnSelFavo, ExtendLayout.LayoutType.FillParent,
					ExtendLayout.LayoutType.WrapContent));

			statusView = new Label("...",skin, StyleConfig.DefaultStyle);
			statusView.setFontScale(2f/3);
			addActor(new ExtendLayout(statusView, LayoutType.FillParent,
					LayoutType.WrapContent));
			new EventListener2<FileSync2Backend, Integer>(backend.getOnStatusChange()) {
				@Override
				public void run() {
					Gdx.app.postRunnable(new Runnable(){
						@Override
						public void run() {
							statusView.setText(
									stringConstant.statusText[getData()]);
						}
					});
					Gdx2.graphics.renderForTime(0);
					super.run();
				}
			};
			
			btnSwSyncCfg=new FitWidthButton();
			btnSwSyncCfg.initButton(stringConstant.swSyncCfgFile,skin,StyleConfig.ButtonStyle);
			btnSwSyncCfg.getButton().addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					gotoSceneWithFlow(getSceneByClass(SwitchSyncConfigScne.class));
					super.clicked(event, x, y);
				}
			});
			addActor(btnSwSyncCfg);
			
			reload();

			space(skin.get(StyleConfig.DefaultStyle, StyleConfig.CommonStyle.class).thinSpace);
		}

		public void reload() {
			syncPathView.setText(cfg.selectedPath);
		}

	}

	private class FileSyncHomeScene implements Scene {
		FileSyncHomeView view;

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			if (from == null) {
				Json json = Env.t(Json.class);
				UtilsService quick = Env.i(UtilsService.class);
				json.setIgnoreUnknownFields(true);
				String cfgInJson = quick.readStringFromClassRelativeFile(
						this.getClass(), configFileName);
				cfg = json.fromJson(Config.class, cfgInJson);
			}
			if (cfg == null) {
				cfg = new Config();
				cfg.favos = new ArrayList<String>();
				for (int i = 0; i < 3; i++) {
					cfg.favos.add("");
				}
				cfg.selectedPath = "";
			}
			if (view == null) {
				view = new FileSyncHomeView();
			}
			setChild(view);
			view.reload();
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			if (will == null) {
				Json json = Env.t(Json.class);
				UtilsService quick = Env.i(UtilsService.class);
				quick.writeStringToClassRelativeFile(getClass(), configFileName,
						json.toJson(cfg));
			}
			setChild(null);
		}

	}

	private class FileBrowserScene implements Scene {
		FileBrowser view;

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			view = new FileBrowser(Env.i(StyleConfig.class).getDefaultSkin());
			try {
				view.navigate(new File(cfg.selectedPath));
			} catch (IOException e) {
				goBackScene();
			}
			new EventListener2<FileBrowser, File>() {
				@Override
				public void run() {
					File newfile = getData();
					if (newfile != null) {
						cfg.selectedPath = newfile.getAbsolutePath();
					}
					goBackScene();
				}
			}.listen(view.getOnResult());
			setChild(view);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			setChild(null);
			view = null;
		}
	}

	private class OnFavoClicked extends ClickListener {
		int id;

		public OnFavoClicked(int id) {
			this.id = id;
		}

		@Override
		public void clicked(InputEvent event, float x, float y) {
			cfg.selectedPath = cfg.favos.get(id);
			goBackScene();
			super.clicked(event, x, y);
		}
	}

	private class FavoSelectScene implements Scene {
		VerticalGroup view;
		public ArrayList<FitWidthButton> btnFavos;

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			view = new VerticalGroup();
			btnFavos = new ArrayList<FitWidthButton>(cfg.favos.size());
			int cnt = cfg.favos.size();
			for (int i = 0; i < cnt; i++) {
				FitWidthButton btnFavo1 = new FitWidthButton();
				btnFavo1.initButton(getPathAbbr(cfg.favos.get(i)), skin,
						StyleConfig.ButtonStyle);
				view.addActor(btnFavo1);
				btnFavo1.getButton().addListener(new OnFavoClicked(i));
				btnFavos.add(btnFavo1);
			}
			FitWidthButton btnDataFile;
			FitWidthButton btnResFile;

			btnDataFile = new FitWidthButton();
			btnDataFile.initButton(stringConstant.dataFile, skin,
					StyleConfig.ButtonStyle);
			btnDataFile.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					cfg.selectedPath = PlatCoreConfig.get().fs.resolve("/7/")
							.getJavaFile().getAbsolutePath();
					goBackScene();
					super.clicked(event, x, y);
				}
			});
			view.addActor(btnDataFile);

			btnResFile = new FitWidthButton();
			btnResFile.initButton(stringConstant.resourceFile, skin,
					StyleConfig.ButtonStyle);
			btnResFile.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					cfg.selectedPath = PlatCoreConfig.get().fs.resolve("/6/")
							.getJavaFile().getAbsolutePath();
					goBackScene();
					super.clicked(event, x, y);
				}
			});
			view.addActor(btnResFile);

			FitWidthButton btnCancel = new FitWidthButton();
			btnCancel.initButton(stringConstant.cancel, skin,
					StyleConfig.ButtonStyle);
			btnCancel.getButton().addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					goBackScene();
					super.clicked(event, x, y);
				}
			});
			view.addActor(btnCancel);
			view.space(8);
			setChild(view);
		}

		private String getPathAbbr(String path) {
			int maxLen = 20;
			if (path.length() > maxLen) {
				return "..." + path.substring(path.length() - maxLen + 3);
			} else {
				return path;
			}

		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			widget.setChild(null);
			view = null;
		}

	}

	static class SyncCfgOpt{
		public DirScan.SyncDirConfig opts[];
	}
	
	
	private class SwitchSyncConfigScne implements Scene{
		VerticalGroup view;
		SyncCfgOpt cfgs;
		
		private class SwitchSyncConfigClick extends ClickListener{
			int id;
			public SwitchSyncConfigClick(int id) {
				this.id=id;
			}
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Json json=Env.t(Json.class);
				json.toJson(cfgs.opts[id], new FileHandle(new File(cfg.selectedPath+DirScan.cfgFile)));
				goBackScene();
				super.clicked(event, x, y);
			}
		}
		
		@Override
		public String getName() {
			return getClass().getSimpleName();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			view=new VerticalGroup();
			File optCfg = new File(cfg.selectedPath+DirScan.cfgOptFile);
			if(!optCfg.isFile()){
				goBackScene();
				return;
			}
			Json json = Env.t(Json.class);
			json.setIgnoreUnknownFields(true);
			cfgs = json.fromJson(SyncCfgOpt.class, new FileHandle(optCfg));
			for(int i=0;i<cfgs.opts.length;i++){
				SyncDirConfig ecfg = cfgs.opts[i];
				FitWidthButton btn = new FitWidthButton();
				btn.initButton(ecfg.name, skin, StyleConfig.ButtonStyle);
				btn.getButton().addListener(new SwitchSyncConfigClick(i));
				view.addActor(btn);
			}
			FitWidthButton btn = new FitWidthButton();
			btn.initButton(stringConstant.cancel, skin, StyleConfig.ButtonStyle);
			btn.getButton().addListener(new ClickListener(){
				public void clicked(InputEvent event, float x, float y) {
					goBackScene();
				}
			});
			view.addActor(btn);
			widget.setChild(view);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			widget.setChild(null);
			view=null;
		}
		
	}
	public FileSync2Gui() {
		stringConstant = new StringConstant();
	}

	@Override
	protected void start() {
		if (backend == null) {
			backend = new FileSync2Backend();
		}
		if (skin == null) {
			skin = Env.i(StyleConfig.class).getDefaultSkin();
		}
		registerScene(new FileSyncHomeScene());
		registerScene(new FileBrowserScene());
		registerScene(new FavoSelectScene());
		registerScene(new SwitchSyncConfigScne());
		gotoSceneWithFlow(getSceneByClass(FileSyncHomeScene.class));
		super.start();
	}
	
	@Override
	public void dispose() {
		this.backend.stopRunningProg();
		super.dispose();
	}
}
