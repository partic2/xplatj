package app.pursuer.toolbox.debugloader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import app.pursuer.modulepkg.IJavaPackageEntry;
import app.pursuer.modulepkg.PackageListenerConstant;
import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.FSUtils;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.filesystem.IFileSystem;
import xplatj.javaplat.pursuer.util.IFilter;

public class PackageDebugLoader {
	private List<String> classPaths;
	public String entry;
	private List<String> compiledPaths;
	private List<ClassLoader> loadedLoader = new ArrayList<ClassLoader>();
	public boolean debugging;

	private void ensureListSize(List<?> ls, int size) {
		for (int rp = 0; ls.size() < size && rp < 0x100000; rp++) {
			ls.add(null);
		}
	}

	private void setLoadedLoaderAt(int i, ClassLoader l) {
		ensureListSize(loadedLoader, i + 1);
		loadedLoader.set(i, l);
	}

	public List<ClassLoader> getLoadedLoader() {
		return this.loadedLoader;
	}

	public void compile(int index) throws IOException {
		UtilsService utils = Env.i(UtilsService.class);
		IFileSystem cfs = PlatCoreConfig.get().fs;
		String cp = classPaths.get(index);
		File fcp = new File(cp);
		boolean tempFcp = false;
		if (fcp.isDirectory()) {
			IFile f = utils.javaFileInStdPrefixFS(fcp);
			IFile t = cfs.resolve(Env.t(FSUtils.class).tempFileRoot);
			t = t.next("PackageDebugLoader" + hashCode() + ".jar");
			utils.zip(t, f, null);
			fcp = t.getJavaFile();
			tempFcp = true;
		}
		File outFile = new File(compiledPaths.get(index));
		if (!outFile.delete()) {
			outFile = genCompiledOut().getJavaFile();
			compiledPaths.set(index, outFile.getAbsolutePath());
		}
		Gdx2.module.compile(fcp, outFile);
		if (tempFcp) {
			fcp.delete();
		}
		saveConfigFile();
	}

	private IFile getCompiledDir() {
		return Env.i(UtilsService.class).getClassRelativeFile(PackageDebugLoader.class, "");
	}

	private IFile genCompiledOut() throws FileNotFoundException {
		IFile outFile = null;
		for (int i = 0; i < 8; i++) {
			outFile = getCompiledDir().next("cp-" + Math.round(Math.random() * 0x1000000) + ".jar");
			if (!outFile.exists()) {
				break;
			}
		}
		if (outFile != null) {
			return outFile;
		} else {
			throw new FileNotFoundException("Can't Generate Temp File Name.");
		}
	}

	Class<? extends LifecycleWidget> oldHomeClass;

	public void runEntry() {
		PlatCoreConfig core = PlatCoreConfig.get();

		try {
			Object result = core.classSpace.loadClass(entry).newInstance();
			LifecycleWidget widget = null;
			if (result instanceof IJavaPackageEntry) {
				IJavaPackageEntry pent = ((IJavaPackageEntry) core.classSpace.loadClass(entry).newInstance());
				widget = pent.getInstance(LifecycleWidget.class, PackageListenerConstant.onGui);
			} else if (result instanceof LifecycleWidget) {
				widget = (LifecycleWidget) result;
			}
			if (widget != null) {
				debugging = true;
				PlatGuiConfig gui = PlatGuiConfig.get();
				Env.ss(PackageDebugLoader.class, this);
				oldHomeClass = gui.homeViewClass;
				gui.homeViewClass = PackageDebugLoaderGui.class;
				gui.mainView = widget;
				Gdx.app.postRunnable(gui.applyConfig);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(int index) throws IOException {
		unload(index);
		String cp = compiledPaths.get(index);
		File fcp = new File(cp);
		if (!fcp.exists()) {
			throw new IOException("File Not Exist:" + cp);
		}
		ClassLoader loaded = PlatCoreConfig.get().loadClasses(new File[] {fcp});
		setLoadedLoaderAt(index, loaded);
	}

	public void unload(int index) {
		PlatCoreConfig core = PlatCoreConfig.get();
		try {
			ClassLoader oldLoaded = loadedLoader.get(index);
			if (oldLoaded != null) {
				core.unloadClasses(oldLoaded);
				loadedLoader.set(index, null);
			}
		} catch (IndexOutOfBoundsException e) {
		}
	}

	public void addClasspath(String cp) throws FileNotFoundException {
		classPaths.add(cp);
		compiledPaths.add(genCompiledOut().getJavaFile().getAbsolutePath());
		saveConfigFile();
	}

	public void removeClasspath(int index) {
		try {
			unload(index);
			loadedLoader.remove(index);
		} catch (IndexOutOfBoundsException e) {
		}
		classPaths.remove(index);
		compiledPaths.remove(index);
	}

	public List<String> getClasspath() {
		return classPaths;
	}

	private static class ConfigFile {
		static final String name = "config.json";
		List<String> classPaths;
		String entry;
		List<String> compiledPaths;
	}

	public void clearCompiled() {
		PlatCoreConfig.get().executor.execute(new Runnable() {
			@Override
			public void run() {
				UtilsService utils = Env.i(UtilsService.class);
				IFile dir = getCompiledDir();
				for (String child : dir.list()) {
					if (child.startsWith("cp-") && child.endsWith(".jar")) {
						try {
							dir.next(child).delete();
						} catch (IOException e) {
						}
					}
				}
			}
		});
	}

	public void saveConfigFile() {
		ConfigFile configFile = new ConfigFile();
		configFile.classPaths = classPaths;
		configFile.compiledPaths = compiledPaths;
		configFile.entry = entry;
		Env.i(UtilsService.class).writeClassJsonConfig(configFile, ConfigFile.name);
	}

	public void loadConfigFile() {
		ConfigFile configFile = Env.i(UtilsService.class).readClassJsonConfig(ConfigFile.class, ConfigFile.name);
		if (configFile != null) {
			classPaths = configFile.classPaths;
			compiledPaths = configFile.compiledPaths;
			entry = configFile.entry;
		}
		if (classPaths == null) {
			classPaths = new Stack<String>();
		}
		if (compiledPaths == null) {
			compiledPaths = new Stack<String>();
		}
	}
}
