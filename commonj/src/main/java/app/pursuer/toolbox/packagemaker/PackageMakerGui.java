package app.pursuer.toolbox.packagemaker;

import app.pursuer.modulepkg.IconManager;
import app.pursuer.modulepkg.JavaPackage;
import app.pursuer.modulepkg.JavaPackage.DependentPackage;
import lib.pursuer.quickgui.*;
import app.pursuer.modulepkg.PackageListenerConstant;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;

import java.io.*;
import java.util.*;
import java.util.Collections;

import xplatj.gdxconfig.core.*;
import xplatj.gdxconfig.gui.*;
import xplatj.gdxplat.pursuer.gui.widget.*;
import xplatj.gdxplat.pursuer.utils.*;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.util.*;

public class PackageMakerGui extends ScenesFlowWidget {
	private PlatCoreConfig core;
	private PlatGuiConfig gui;
	private UtilsService quick;
	public static final String pathCachedCfgList = "pccf.json";
	private Skin skin;
	private PackageMaker backend;

	public PackageMakerGui() {
		backend = new PackageMaker();
	}

	@Override
	protected void start() {
		super.start();
		core = PlatCoreConfig.get();
		gui = PlatGuiConfig.get();
		quick = Env.i(UtilsService.class);
		skin = Env.i(StyleConfig.class).getDefaultSkin();
		registerScene(new ConfigListScene());
		registerScene(new BaseInfo());
		registerScene(new ResourcePackScene());
		registerScene(new OutputOption());
		registerScene(new PackScene());
		gotoSceneWithFlow(getSceneByClass(ConfigListScene.class));
	}

	private PackConfig curCfg;
	private Boolean newcfg;
	private ConfigList cfgls;
	private Boolean updatecfg;

	private static class ConfigList {
		List<PackConfig> ls;
	}

	protected class ConfigListScene extends FormScene {
		private Short selectedCfg;

		public ConfigListScene() {
			setSkin(skin);
			readConfigList();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			if (updatecfg) {
				writeConfigFile();
				updatecfg = false;
			}
			if (cfgls == null) {
				cfgls = new ConfigList();
				cfgls.ls = new ArrayList<PackConfig>();
			}
			info("Select Config");
			Collection<String> cfgfmt = formatHint();
			if (cfgfmt.size() == 0) {
				newcfg = true;
				curCfg = new PackConfig();
				curCfg.defaultInit("MyPackage");
				gotoScene(getSceneByClass(BaseInfo.class));
				return;
			}
			enquire("Config File List", (short) 0, cfgfmt);
			newcfg = false;
			enquire("Create New Config", newcfg, null);
			enquire("quick pack jar /6/temp", new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					try {
						new PackJarTool().packJarCollection(core.fs.resolve("/6/temp"));
					} catch (IOException e) {
					}
					super.clicked(event, x, y);
				}
			}, null);

			new EventListener2<FormScene, Integer>((Container<EventHandler<FormScene, Integer>>) getOnSubmit()) {
				@Override
				public void run() {
					int id = getData();
					Short t1 = 0;
					t1 = readResultFor(t1);
					newcfg = readResultFor(newcfg);
					curCfg = cfgls.ls.get(t1);
					if (newcfg) {
						curCfg = (PackConfig) curCfg.clone();
					}
					if (id == FormScene.INT_Cancel) {
						gotoScene(getSceneByClass(BaseInfo.class));
					} else if (id == FormScene.INT_Confirm) {
						gotoScene(getSceneByClass(PackScene.class));
					}
				}
			};
			apply("pack", "modify");
			super.enter(widget, from);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			super.leave(widget, will);
			clearQuest();
		}

		private Collection<String> formatHint() {
			ArrayList<String> scb = new ArrayList<String>(cfgls.ls.size());
			Iterator<PackConfig> itcf = cfgls.ls.iterator();
			while (itcf.hasNext()) {
				PackConfig ecf = itcf.next();
				scb.add(ecf.pkginfo.id);
			}
			return scb;
		}

		private void readConfigList() {
			Json json = new Json();
			cfgls = json.fromJson(ConfigList.class,
					quick.readStringFromClassRelativeFile(getClass(), pathCachedCfgList));
			updatecfg = false;
		}

		private void writeConfigFile() {
			Json json = new Json();
			quick.writeStringToClassRelativeFile(getClass(), pathCachedCfgList, json.toJson(cfgls));
		}
	}

	protected class PackScene extends TextInputBox implements Scene {

		public PackScene() {
			super(skin);
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			setText("");
			setTitle("Install Status");
			widget.setChild(this);
			if (backend.consoleOutput == null) {
				backend.consoleOutput = new PrintWriter(getWriter());
			}
			getOnResult().set(new EventHandler<TextInputBox, String>() {
				@Override
				public void handle(TextInputBox from, String data) {
					gotoScene(getSceneByClass(ConfigListScene.class));
				}
			});
			backend.package1(curCfg);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
		}

	}

	protected class BaseInfo extends FormScene {
		public BaseInfo() {
			setSkin(skin);
		}

		private void saveResult() {
			curCfg.pkginfo.id = readResultFor(curCfg.pkginfo.id);
			curCfg.defaultFilaName(curCfg.pkginfo.id);
			curCfg.pkginfo.name = readResultFor(curCfg.pkginfo.name);
			curCfg.pkginfo.version = readResultFor(curCfg.pkginfo.version);
			String raw = new String();
			raw = readResultFor(raw);
			curCfg.pkginfo.dependency.clear();
			if (!raw.equals("")) {
				LinkedList<DependentPackage> cs = new LinkedList<JavaPackage.DependentPackage>();
				for (String edeps : raw.split(",")) {
					JavaPackage.DependentPackage dpkg = new DependentPackage();
					if (edeps.length() > 0) {
						int vline = edeps.lastIndexOf('-');
						if (vline > 0) {
							dpkg.id = edeps.substring(0, vline);
							dpkg.minimalVersion = Integer.parseInt(edeps.substring(vline + 1, edeps.length()));
						} else {
							dpkg.id = edeps;
						}
					}
					cs.add(dpkg);
				}
				curCfg.pkginfo.dependency.addAll(cs);
			}
			ArrayList<String> listener = new ArrayList<String>();
			Boolean booleanResult = false;
			if (readResultFor(booleanResult)) {
				listener.add(PackageListenerConstant.onStartup);
			}
			if (readResultFor(booleanResult)) {
				listener.add(PackageListenerConstant.onGui);
			}
			if (readResultFor(booleanResult)) {
				listener.add(PackageListenerConstant.onInstalled);
			}
			if (readResultFor(booleanResult)) {
				listener.add(PackageListenerConstant.onUninstalling);
			}
			curCfg.pkginfo.listener = listener.toArray(new String[0]);
		}

		private String formatDependecy(PackConfig cfg) {
			StringBuffer s = new StringBuffer();
			for (DependentPackage edep : cfg.pkginfo.dependency) {
				s.append(edep.id + "-" + edep.minimalVersion).append(',');
			}
			if (s.length() > 0) {
				s.setLength(s.length() - 1);
			}
			return s.toString();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			info("Basic package info\n");
			info("package id.");
			enquire("e.g com.xxx.myclass, libxxx,{4Rg9s==}", curCfg.pkginfo.id, null);
			info("package name.");
			enquire("e.g myxxx, libxxx", curCfg.pkginfo.name, null);
			info("version,Integer only.");
			enquire(" e.g 12", curCfg.pkginfo.version, null);
			info("dependented package");
			enquire("e.g lib1,lib2", formatDependecy(curCfg), null);
			List<String> listener = Collections.emptyList();
			if (curCfg.pkginfo.listener != null) {
				listener = Arrays.asList(curCfg.pkginfo.listener);
			}
			enquire("onStartup listener", listener.contains(PackageListenerConstant.onStartup), null);
			enquire("onGui listener", listener.contains(PackageListenerConstant.onGui), null);
			enquire("onInstalled listener", listener.contains(PackageListenerConstant.onInstalled), null);
			enquire("onUninstalling listener", listener.contains(PackageListenerConstant.onUninstalling), null);
			apply("next", "last");
			new EventListener2<FormScene, Integer>((Container<EventHandler<FormScene, Integer>>) getOnSubmit()) {
				@Override
				public void run() {
					int id = getData();
					if (id == INT_Confirm) {
						saveResult();
						gotoScene(getSceneByClass(ResourcePackScene.class));
					} else if (id == INT_Cancel) {
						gotoScene(getSceneByClass(ConfigListScene.class));
					}
				}
			};
			super.enter(widget, from);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			clearQuest();
			super.leave(widget, will);
		}
	}

	protected class ResourcePackScene extends FormScene {
		IFile fres;
		IFile fcls;

		public ResourcePackScene() {
			setSkin(skin);
		}

		private void saveResult() {
			fcls = readResultFor(fcls);
			fres = readResultFor(fres);
			curCfg.classpath = fcls.getPath();
			curCfg.resourcepath = fres.getPath();
			curCfg.packagepath = readResultFor(curCfg.packagepath);
			String entry = new String();
			entry = readResultFor(entry);
			curCfg.pkginfo.classEntry = curCfg.packagepath + "." + entry;
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			IFile f1 = core.fs.resolve(curCfg.resourcepath);
			if (f1.list() == null) {
				f1 = core.fs.resolve("/6");
			}
			fres = f1;
			f1 = core.fs.resolve(curCfg.classpath);
			if (f1.list() == null) {
				f1 = core.fs.resolve("/6");
			}

			fcls = f1;
			enquire("class file root.", fcls, null);
			enquire("resource file root.", fres, null);
			info("package name to pack ");
			enquire("e.g com.mypackage", curCfg.packagepath, null);
			info("entry class,relative.");
			enquire("e.g Main means com.mypackage.Main", "", null);
			new EventListener2<FormScene, Integer>(getOnSubmit()) {
				@Override
				public void run() {
					int id = getData();
					if (id == INT_Confirm) {
						saveResult();
						gotoScene(getSceneByClass(OutputOption.class));
					} else if (id == INT_Cancel) {
						gotoScene(getSceneByClass(BaseInfo.class));
					}
				}
			};
			apply("next", "last");
			super.enter(widget, from);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			clearQuest();
			super.leave(widget, will);
		}
	}

	protected class OutputOption extends FormScene {
		IFile fout;

		public OutputOption() {
			setSkin(skin);
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			fout = core.fs.resolve(curCfg.outputDir);
			if (!fout.exists()||fout.list()==null) {
				fout = core.fs.resolve("/6/temp");
			}
			enquire("output file root.", fout, null);
			new EventListener2<FormScene, Integer>((Container<EventHandler<FormScene, Integer>>) getOnSubmit()) {
				@Override
				public void run() {
					int id = getData();
					if (id == INT_Confirm) {
						saveResult();
						gotoScene(getSceneByClass(ConfigListScene.class));
					} else if (id == INT_Cancel) {
						gotoScene(getSceneByClass(ResourcePackScene.class));
					}
				}
			};
			apply("finish", "last");
			super.enter(widget, from);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			clearQuest();
			super.leave(widget, will);
		}

		private void saveResult() {
			fout = readResultFor(fout);
			curCfg.outputDir = fout.getPath();
			if (newcfg) {
				cfgls.ls.add(curCfg);
			}
			updatecfg = true;
		}
	}
}
