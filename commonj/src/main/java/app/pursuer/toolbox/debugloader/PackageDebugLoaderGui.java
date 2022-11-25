package app.pursuer.toolbox.debugloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.GuillotineStrategy;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import lib.pursuer.quickgui.FileBrowser;
import lib.pursuer.quickgui.HorizontalButonGroup;
import lib.pursuer.quickgui.ScenesFlowWidget;
import lib.pursuer.quickgui.StyleConfig;
import lib.pursuer.remotedebugbridge.Server;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.gui.widget.Scene;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.util.EventListener2;

public class PackageDebugLoaderGui extends ScenesFlowWidget {
	private File currentSelectedFile;

	PlatCoreConfig core=PlatCoreConfig.get();
	private TextField statusBar;

	public void setStatusText(final String text) {
		if (statusBar != null) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					statusBar.setText(text);
				}
			});
		}
	}

	private class CpLstBrowser implements Scene {
		@Override
		public String getName() {
			return null;
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			FileBrowser fb = new FileBrowser(Env.i(StyleConfig.class).getDefaultSkin());
			try {
				if (currentSelectedFile == null) {
					currentSelectedFile = PlatCoreConfig.get().fs.resolve("/6/").getJavaFile();
				}
				fb.navigate(currentSelectedFile.getAbsoluteFile());
			} catch (IOException e) {
				try {
					fb.navigate(PlatCoreConfig.get().fs.resolve("/6/").getJavaFile().getAbsoluteFile());
				} catch (IOException e2) {
					Gdx.app.error("PackageDebugLoaderGui", e2.getMessage());
				}
				;
			}
			widget.setChild(fb);
			new EventListener2<FileBrowser, File>(fb.getOnResult()) {
				@Override
				public void run() {
					if (getData() != null) {
						try {
							backend.addClasspath(getData().getAbsolutePath());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
					goBackScene();
					super.run();
				}
			};
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			setChild(null);
		}

	}

	private class MainScene implements Scene {
		private VerticalGroup mainWidget;
		private List<String> cpLst;

		@Override
		public String getName() {
			return null;
		}

		private Array<String> toStrList(java.util.List<String> ls) {
			Array<String> cpArr = new Array<String>();
			for (String item : backend.getClasspath()) {
				cpArr.add("..." + item.substring(item.length() - 25));
			}
			return cpArr;
		}

		public String showLoadedIndex() {
			java.util.List<ClassLoader> loaded = backend.getLoadedLoader();
			StringBuilder retStr = new StringBuilder();
			for (int i = 0; i < loaded.size(); i++) {
				if (loaded.get(i) != null) {
					retStr.append(i).append(",");
				}
			}
			return retStr.toString();

		}

		@Override
		public void enter(final LifecycleWidget widget, Scene from) {
			if (mainWidget == null) {
				StyleConfig style = Env.i(StyleConfig.class);
				backend = Env.s(PackageDebugLoader.class);
				if (backend == null) {
					backend = new PackageDebugLoader();
				}
				backend.debugging = false;
				if (backend.oldHomeClass != null) {
					PlatGuiConfig.get().homeViewClass = backend.oldHomeClass;
				}
				backend.loadConfigFile();
				mainWidget = new VerticalGroup();
				mainWidget.columnAlign(Align.left);
				mainWidget.space(8);
				cpLst = new List<String>(style.getDefaultSkin());
				mainWidget
						.addActor(new ExtendLayout(cpLst, ExtendLayout.LayoutType.FillParent, ExtendLayout.LayoutType.WrapContent));
				mainWidget.addActor(new Label("entry", style.getDefaultSkin(), StyleConfig.DefaultStyle));
				TextField entryInput = new TextField(backend.entry, style.getDefaultSkin(), StyleConfig.EditableStyle);
				mainWidget.addActor(
						new ExtendLayout(entryInput, ExtendLayout.LayoutType.FillParent, ExtendLayout.LayoutType.WrapContent));
				entryInput.addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent arg0, Actor arg1) {
						backend.entry = ((TextField) arg0.getTarget()).getText();
					}
				});
				HorizontalButonGroup btnAddDel = new HorizontalButonGroup();
				btnAddDel.initButtonGroup(new String[] { "Add", "Del" }, style.getDefaultSkin(), StyleConfig.ButtonStyle);
				btnAddDel.getButtons().get(0).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						String selPath = null;
						try {
							selPath = backend.getClasspath().get(cpLst.getSelectedIndex());
						} catch (IndexOutOfBoundsException e) {
						}
						if (selPath == null) {
							currentSelectedFile = null;
						} else {
							currentSelectedFile = new File(selPath);
						}
						gotoSceneWithFlow(getSceneByClass(CpLstBrowser.class));
						super.clicked(event, x, y);
					}
				});

				btnAddDel.getButtons().get(1).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						backend.removeClasspath(cpLst.getSelectedIndex());
						cpLst.setItems(toStrList(backend.getClasspath()));
						super.clicked(event, x, y);
					}
				});
				mainWidget.addActor(
						new ExtendLayout(btnAddDel, ExtendLayout.LayoutType.FillParent, ExtendLayout.LayoutType.WrapContent));
				HorizontalButonGroup btnCompLoad = new HorizontalButonGroup();
				btnCompLoad.initButtonGroup(new String[] { "Compile", "Run" }, style.getDefaultSkin(), StyleConfig.ButtonStyle);
				btnCompLoad.getButtons().get(0).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {

						PlatCoreConfig.get().executor.execute(new Runnable() {
							@Override
							public void run() {
								try {
									setStatusText("compiling...");
									backend.compile(cpLst.getSelectedIndex());
									setStatusText("ready");
								} catch (IOException e) {
									setStatusText(e.getMessage());
								}
							}
						});
						super.clicked(event, x, y);
					}
				});
				btnCompLoad.getButtons().get(1).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						PlatCoreConfig.get().executor.execute(new Runnable() {
							@Override
							public void run() {
								backend.runEntry();
							}
						});
						super.clicked(event, x, y);
					}
				});
				mainWidget.addActor(
						new ExtendLayout(btnCompLoad, ExtendLayout.LayoutType.FillParent, ExtendLayout.LayoutType.WrapContent));

				HorizontalButonGroup btnGc = new HorizontalButonGroup();
				btnGc.initButtonGroup(new String[] { "Clear Compiled", "GC" }, style.getDefaultSkin(), StyleConfig.ButtonStyle);
				btnGc.getButtons().get(0).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						backend.clearCompiled();
						super.clicked(event, x, y);
					}
				});
				btnGc.getButtons().get(1).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						System.gc();
						super.clicked(event, x, y);
					}
				});
				mainWidget
						.addActor(new ExtendLayout(btnGc, ExtendLayout.LayoutType.FillParent, ExtendLayout.LayoutType.WrapContent));

				HorizontalButonGroup btnLoadUnload = new HorizontalButonGroup();
				btnLoadUnload.initButtonGroup(new String[] { "Load", "Unload" }, style.getDefaultSkin(),
						StyleConfig.ButtonStyle);
				btnLoadUnload.getButtons().get(0).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						try {
							backend.load(cpLst.getSelectedIndex());
							setStatusText("loaded:"+showLoadedIndex());
						} catch (IOException e) {
							setStatusText(e.getMessage());
						}
						super.clicked(event, x, y);
					}
				});
				btnLoadUnload.getButtons().get(1).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						backend.unload(cpLst.getSelectedIndex());
						setStatusText("loaded:"+showLoadedIndex());
						super.clicked(event, x, y);
					}
				});
				mainWidget.addActor(btnLoadUnload);

				HorizontalButonGroup btnGroupOp1 = new HorizontalButonGroup();
				btnGroupOp1.initButtonGroup(new String[] { "RecompileAndRun" }, style.getDefaultSkin(),
						StyleConfig.ButtonStyle);
				btnGroupOp1.getButtons().get(0).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						PlatCoreConfig core = PlatCoreConfig.get();
						final int selected = cpLst.getSelectedIndex();
						core.executor.execute(new Runnable() {
							@Override
							public void run() {
								setStatusText("unloading");
								backend.unload(selected);
								setStatusText("run GC");
								System.gc();
							}

						});
						core.executor.schedule(new Runnable() {
							@Override
							public void run() {
								setStatusText("compiling");
								try {
									backend.compile(selected);
									setStatusText("loading");
									backend.load(selected);
									backend.runEntry();
									setStatusText("ready");
								} catch (IOException e) {
									setStatusText(e.getMessage());
									return;
								}
							}
						}, 3, TimeUnit.SECONDS);

						super.clicked(event, x, y);
					}
				});
				mainWidget.addActor(btnGroupOp1);
				
				HorizontalButonGroup btnRdb = new HorizontalButonGroup();
				btnRdb.initButtonGroup(new String[] { "Start RDB" ,"Stop RDB"}, style.getDefaultSkin(),
						StyleConfig.ButtonStyle);
				btnRdb.getButtons().get(0).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						core.executor.execute(new Runnable() {
							@Override
							public void run() {
								Server srv = Env.s(lib.pursuer.remotedebugbridge.Server.class);
								if(srv==null||!srv.running) {
									srv=new Server();
									srv.start();
									Env.ss(Server.class, srv);
								}
							}
						});
						super.clicked(event, x, y);
					}
				});
				
				btnRdb.getButtons().get(1).addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						core.executor.execute(new Runnable() {
							@Override
							public void run() {
								Server srv = Env.s(lib.pursuer.remotedebugbridge.Server.class);
								if(srv!=null&&srv.running) {
									srv.stop();
									Env.ss(Server.class, null);
								}
							}
						});
						super.clicked(event, x, y);
					}
				});
				mainWidget.addActor(
						new ExtendLayout(btnRdb, ExtendLayout.LayoutType.FillParent, ExtendLayout.LayoutType.WrapContent));
				
				statusBar = new TextField("ready", style.getDefaultSkin(), StyleConfig.EditableStyle);
				mainWidget.addActor(
						new ExtendLayout(statusBar, ExtendLayout.LayoutType.FillParent, ExtendLayout.LayoutType.WrapContent));
			}
			cpLst.setItems(toStrList(backend.getClasspath()));
			widget.setChild(mainWidget);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
		}
	}

	private PackageDebugLoader backend;

	@Override
	protected void start() {
		System.gc();
		registerScene(new MainScene());
		registerScene(new CpLstBrowser());
		gotoSceneWithFlow(getSceneByClass(MainScene.class));
		super.start();
	}

	@Override
	public void dispose() {
		backend.saveConfigFile();
		super.dispose();
	}
}
