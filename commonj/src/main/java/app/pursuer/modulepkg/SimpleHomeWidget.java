package app.pursuer.modulepkg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;

import app.pursuer.modulepkg.IRepository.PackageNotFoundException;
import app.pursuer.modulepkg.JavaPackage.Manifest;
import app.pursuer.modulepkg.PackageManager.InstallException;
import app.pursuer.toolbox.debugloader.PackageDebugLoaderGui;
import app.pursuer.toolbox.filesync.FileSync2Gui;
import app.pursuer.toolbox.packagemaker.PackageMakerGui;
import lib.pursuer.quickgui.FileBrowser;
import lib.pursuer.quickgui.FitWidthButton;
import lib.pursuer.quickgui.FitWidthLabel;
import lib.pursuer.quickgui.FormScene;
import lib.pursuer.quickgui.HorizontalButonGroup;
import lib.pursuer.quickgui.StyleConfig;
import lib.pursuer.remotedebugbridge.DebugRemoteFileSystem;
import test.quickrun.SimpleTestWidget;
import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.gui.widget.Scene;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.util.EventHandlerImpl;
import xplatj.javaplat.pursuer.util.EventListener2;

public class SimpleHomeWidget extends LifecycleWidget {
	public ArrayList<LifecycleWidget> applist;
	public ArrayList<String> pkglist;
	

	public static class TextConst {
		public String tUninstall = "uninstall";
		public String tInstall = "install";
		public String tRun = "run";
		public String tIgnoreDependency = "ignore dependency check";
		public String tIgnoreVersion = "ignore version check";
		public String tHideInternalWidget = "hide internal widget";
		public String tSetting = "setting";
		public String tConfirm = "confirm";
		public String tDetail="detail";
		public String tInstallFail="install failed.";
		public String tInstallSuccess="install successed.";
	}
	public TextConst textConst;

	PackageManager pm;
	PlatCoreConfig core;
	PlatGuiConfig gui;
	FormScene settingScene;

	public SimpleHomeWidget() {
		textConst=new TextConst();
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}

	public static final String settingFile = "setting.json";

	@Override
	protected void start() {
		core = PlatCoreConfig.get();
		gui = PlatGuiConfig.get();
		UtilsService quick = Env.i(UtilsService.class);
		setWidthLayout(LayoutType.Custom);
		setHeightLayout(LayoutType.Custom);
		pm = Env.i(PackageManager.class);
		registerScene(new ListScene());
		registerScene(new InstallScene());
		registerScene(new InstallStatusScene());
		settingScene = (FormScene) registerScene(new FormScene());
		settingScene.setSkin(Env.i(StyleConfig.class).getDefaultSkin());
		registerScene(new PackageDetailScene());

		gotoScene(getSceneByClass(ListScene.class));
		Json json = new Json();
		try {
			setting = json.fromJson(SimpleHomeWidgetSetting.class,
					quick.readStringFromClassRelativeFile(this.getClass(), settingFile));
		} catch (SerializationException e) {
		}
		if (setting == null) {
			setting = new SimpleHomeWidgetSetting();
		}
		updateSetting();
		settingScene.enquire(textConst.tIgnoreDependency, setting.ignoreDependency, null);
		settingScene.enquire(textConst.tIgnoreVersion, setting.ignoreVersion, null);
		settingScene.enquire(textConst.tHideInternalWidget, setting.hideInternalWidget, null);
		settingScene.apply(textConst.tConfirm, null);
		settingScene.getOnSubmit().set(new EventHandlerImpl<FormScene, Integer>() {
			@Override
			public void run() {
				if (this.getData() == FormScene.INT_Confirm) {
					setting.ignoreDependency = settingScene.readResultFor(setting.ignoreDependency);
					setting.ignoreVersion = settingScene.readResultFor(setting.ignoreVersion);
					setting.hideInternalWidget = settingScene.readResultFor(setting.hideInternalWidget);
					settingScene.resetResult();
					Json json = new Json();
					Env.i(UtilsService.class).writeStringToClassRelativeFile(this.getClass(), settingFile, json.toJson(setting));
					updateSetting();
				}
				gotoScene(getSceneByClass(ListScene.class));
			}
		});
		super.start();
	}

	public void updateSetting() {
		pm.depencyCheck = !setting.ignoreDependency;
		pm.versionCheck = !setting.ignoreVersion;
	}

	private class ListView extends VerticalGroup {
		private List<String> ls;
		private ScrollPane scrollLs;
		private ExtendLayout lsWrap;
		private HorizontalButonGroup btnPkg;
		private HorizontalButonGroup btnRunSetting;

		private float space;

		public ListView(Skin skin) {
			space = skin.get("default", StyleConfig.CommonStyle.class).thinSpace;
			space(space);
			ls = new List<String>(skin);
			scrollLs=new ScrollPane(ls);
			lsWrap=new ExtendLayout(scrollLs,ExtendLayout.LayoutType.FillParent,ExtendLayout.LayoutType.Custom);
			lsWrap.setHeight(1160);
			btnRunSetting=new HorizontalButonGroup();
			btnRunSetting.initButtonGroup(new String[] { textConst.tRun, textConst.tSetting }, skin, StyleConfig.ButtonStyle);
			btnPkg = new HorizontalButonGroup();
			btnPkg.initButtonGroup(new String[] { textConst.tInstall, textConst.tDetail }, getDefaultSkin(), StyleConfig.ButtonStyle);
			Array<String> alist = new Array<String>();
			Iterator<LifecycleWidget> itl = applist.iterator();
			while (itl.hasNext()) {
				LifecycleWidget e = itl.next();
				alist.add(e.getClass().getSimpleName());
			}
			Iterator<String> its = pkglist.iterator();
			while (its.hasNext()) {
				String es = its.next();
				alist.add(es);
			}
			ls.setItems(alist);
			addActor(lsWrap);
			addActor(btnRunSetting);
			addActor(btnPkg);
			btnRunSetting.getButtons().get(0).addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					int index = ls.getSelectedIndex();
					if (index >= applist.size()) {
						index -= applist.size();
						try {
							IJavaPackageEntry entry = pm.loadPackage(pkglist.get(index));
							if (entry != null) {
								LifecycleWidget view = entry.getInstance(LifecycleWidget.class, PackageListenerConstant.onGui);
								if (view != null) {
									runWidget(view);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						runWidget(applist.get(index));
					}
					super.clicked(event, x, y);
				}
			});
			btnPkg.getButtons().get(0).addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					gotoScene(getSceneNamed("installer"));
					super.clicked(event, x, y);
				}
			});
			btnPkg.getButtons().get(1).addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					int index = ls.getSelectedIndex();
					if (index >= applist.size()) {
						index -= applist.size();
						detailManifest=pm.findPackageInfo(pkglist.get(index));
						gotoScene(getSceneByClass(PackageDetailScene.class));
					}
					super.clicked(event, x, y);
				}
			});
			btnRunSetting.getButtons().get(1).addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					gotoScene(getSceneByClass(FormScene.class));
					super.clicked(event, x, y);
				}
			});
		}
	}

	
	protected class ListScene implements Scene {
		ListView lv;

		@Override
		public String getName() {
			return "applist";
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			applist = new ArrayList<LifecycleWidget>();
			if (!setting.hideInternalWidget) {
				applist.add(new SimpleTestWidget());
				applist.add(new FileSync2Gui());
				applist.add(new PackageMakerGui());
				applist.add(new PackageDebugLoaderGui());
			}
			pkglist = new ArrayList<String>();
			Collection<JavaPackage.Manifest> mfs = pm.getInstalledPackageList();
			for (JavaPackage.Manifest mf : mfs) {
				pkglist.add(mf.id);
			}
			lv = new ListView(getDefaultSkin());
			setChild(lv);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			setChild(null);
			lv = null;
		}
	}

	private File installingPkgFile;

	protected class InstallScene implements Scene {
		FileBrowser fb;

		@Override
		public String getName() {
			return "installer";
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			fb = new FileBrowser(getDefaultSkin());
			try {
				fb.navigate(core.fs.resolve("/6/").getJavaFile().getAbsoluteFile());
			} catch (IOException e) {
			}
			setChild(fb);
			new EventListener2<FileBrowser, File>(fb.getOnResult()) {
				@Override
				public void run() {
					File result = getData();
					if (result != null) {
						installingPkgFile = result;
						gotoScene(getSceneNamed("install status"));
					} else {
						gotoScene(getSceneNamed("applist"));
					}
				}
			};
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			setChild(null);
			fb = null;
		}
	}

	private class InstallThread implements Runnable {

		@Override
		public void run() {
			String msg;
			JavaPackage pkg = new JavaPackage();
			try {
				pkg.loadFromFile(Env.i(UtilsService.class).javaFileInStdPrefixFS(installingPkgFile));
			} catch (Exception e) {
				msg = textConst.tInstallFail+e.toString() + "\n";
				((InstallStatusScene) getSceneNamed("install status")).installResult(msg);
				return;
			}
			try {
				pm.install(pkg);
				msg="success";
			} catch(Exception e) {
				msg="failed."+e.getMessage();
			} 
			((InstallStatusScene) getSceneNamed("install status")).installResult(msg);
		}
	}

	protected class InstallStatusScene implements Scene {
		VerticalGroup view;
		FitWidthLabel textStatus;
		FitWidthButton btnOk;

		@Override
		public String getName() {
			return "install status";
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			view = new VerticalGroup();
			setChild(view);
			textStatus = new FitWidthLabel();
			textStatus.initLabel("installing...", getDefaultSkin(), "default");
			view.addActor(textStatus);
			btnOk = null;
			core.executor.execute(new InstallThread());
		}

		public void installResult(final String msg) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					textStatus.getLabel().setText(msg);
					if (btnOk == null) {
						btnOk = new FitWidthButton();
						btnOk.initButton("ok", getDefaultSkin(), "button");
						view.addActor(btnOk);
						btnOk.addListener(new ClickListener() {
							@Override
							public void clicked(InputEvent event, float x, float y) {
								gotoScene(getSceneNamed("applist"));
								super.clicked(event, x, y);
							}
						});
					}
				}
			});

		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			setChild(null);
			btnOk = null;
			textStatus = null;
		}
	}

	public String uninstName;

	public static class SimpleHomeWidgetSetting {
		public Boolean ignoreVersion = false;
		public Boolean ignoreDependency = false;
		public Boolean hideInternalWidget = false;
	}
	
	private SimpleHomeWidgetSetting setting;
	private class UninstallThread implements Runnable{
		String pkgid;
		public UninstallThread(String pkgid) {
			this.pkgid=pkgid;
		}
		@Override
		public void run() {
			pm.removePackage(pkgid);
			gotoScene(getSceneNamed("applist"));
		}
	}
	private Manifest detailManifest;
	private class PackageDetailView extends VerticalGroup{
		private Manifest mf;
		private FitWidthLabel detailTxt;
		private FitWidthButton uninstallBtn;
		public PackageDetailView() {
			this.mf=detailManifest;
			detailTxt=new FitWidthLabel();
			StringBuilder sb=new StringBuilder();
			sb.append(mf.id).append("\n").append("version:").append(mf.version).append("\n").append(mf.name).append("\n").append(mf.description);
			detailTxt.initLabel(sb.toString(), getDefaultSkin(), StyleConfig.DefaultStyle);
			addActor(detailTxt);
			uninstallBtn=new FitWidthButton();
			uninstallBtn.initButton(textConst.tUninstall, getDefaultSkin(), StyleConfig.ButtonStyle);
			uninstallBtn.getButton().addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					core.executor.execute(new UninstallThread(PackageDetailView.this.mf.id));
					super.clicked(event, x, y);
				}
			});
			addActor(uninstallBtn);
			FitWidthButton backBtn = new FitWidthButton();
			backBtn.initButton(textConst.tConfirm, getDefaultSkin(), StyleConfig.ButtonStyle);
			backBtn.getButton().addListener(new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					gotoScene(getSceneNamed("applist"));
					super.clicked(event, x, y);
				}
			});
			addActor(backBtn);
			space(8);
		}
	}
	private class PackageDetailScene implements Scene{
		public PackageDetailScene(){
		}
		@Override
		public String getName() {
			return "PackageDetailScene";
		}
		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			PackageDetailView view = new PackageDetailView();
			widget.setChild(view);
		}
		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			setChild(null);
		}
	}

	private void runWidget(LifecycleWidget w) {
		gui.mainView=w;
		this.dispose();
		Gdx2.graphics.runInGLThreadAndWait(gui.applyConfig);
	}

	private Skin dskin;

	public Skin getDefaultSkin() {
		if (dskin == null) {
			dskin = Env.i(StyleConfig.class).getDefaultSkin();
		}
		return dskin;
	}
}
