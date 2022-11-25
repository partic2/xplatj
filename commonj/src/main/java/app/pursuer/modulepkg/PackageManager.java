package app.pursuer.modulepkg;

import app.pursuer.modulepkg.IRepository.PackageNotFoundException;
import app.pursuer.modulepkg.JavaPackage.*;

import com.badlogic.gdx.utils.*;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.control.*;
import xplatj.gdxconfig.core.*;
import xplatj.gdxplat.pursuer.utils.*;
import xplatj.javaplat.pursuer.filesystem.*;
import xplatj.javaplat.pursuer.io.*;
import xplatj.javaplat.pursuer.util.EventHandler;

public class PackageManager implements Closeable {
	private UtilsService quick;
	private PlatCoreConfig core;
	public boolean versionCheck = true;
	public boolean depencyCheck = true;

	public static class Status {
		public boolean firstRun = true;
		public RepositoryManager repositoryManager;
		public Map<String, ClassLoader> loadedPackage = new HashMap<String, ClassLoader>();
	}

	protected Status status;

	public PackageManager() {
		initDefault();
	}

	public PackageManager(boolean init) {
		if (init) {
			initDefault();
		}
	}

	private void initDefault() {
		core = PlatCoreConfig.get();
		quick = Env.i(UtilsService.class);
		Json json = new Json();
		config = json.fromJson(Config.class, quick.readStringFromClassRelativeFile(this.getClass(), regFilePath));
		if (config == null) {
			config = new Config();
		}
		if (config.packages == null) {
			config.packages = new LinkedList<JavaPackage.Manifest>();
			config.nextReg = null;
		}
		status = Env.s(Status.class);
		if (status == null) {
			status = new Status();
			status.repositoryManager = new RepositoryManager();
			status.repositoryManager.getReposList().add(new LocalRepository());
			Env.ss(Status.class, status);
		}
		if (status.firstRun) {
			for (Manifest itmf : config.packages) {
				if (itmf.listener != null && Arrays.asList(itmf.listener).contains(PackageListenerConstant.onStartup)) {
					try {
						IJavaPackageEntry entry = loadPackage(itmf.id);
						Runnable service = entry.getInstance(Runnable.class, PackageListenerConstant.onStartup);
						service.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			status.firstRun = false;
		}

	}

	public static class Config {
		int splitFileSize = 200 * 1024;
		Collection<Manifest> packages;
		String nextReg;
	}

	private Config config;

	private float progress = 0;

	public float getProgress() {
		return progress;
	}

	public void installFromRepo(DependentPackage pkg) throws PackageNotFoundException, IOException, InstallException {
		tryInstallDependency(pkg);
	}
	

	public void install(JavaPackage pkg) throws IOException, PackageNotFoundException, InstallException {
			Manifest mf = pkg.getManifest();
			progress = 0;
			if (mf.type == JavaPackage.typePackageManifest) {
				checkAndUpdateDependence(mf);
					if (mf.classFile!=null&&!mf.classFile.equals("")) {
						IFile rootDir = pkg.getRootDir();
						IFile srcIFile = rootDir.next(mf.classFile);
						Collection<IFile> tmpClsFiles = null;
						boolean deleteClsFiles = false;
						IDataBlock db = srcIFile.open();
						if (db.size() < config.splitFileSize) {
							tmpClsFiles = new LinkedList<IFile>();
							tmpClsFiles.add(srcIFile);
						} else {
							tmpClsFiles = splitClassFile(srcIFile);
							deleteClsFiles = true;
						}
						db.free();
						File[] dests = createClassFiles(mf.id, tmpClsFiles.size());
						int jarId = 0;
						for (IFile eif : tmpClsFiles) {
							Gdx2.module.compile(eif.getJavaFile(), dests[jarId]);
							jarId++;
						}
						if (deleteClsFiles) {
							for (IFile eif : tmpClsFiles) {
								eif.delete();
							}
						}
					}
					if (mf.resourceFile!=null&&!mf.resourceFile.equals("")) {
						IFile rootDir = pkg.getRootDir();
						quick.unzip(rootDir.next(mf.resourceFile), core.fs.resolve(mf.resourceRoot), null);
					}
					mf.classFile = mf.id;
					mf.resourceFile = "";
					if (mf.listener != null && Arrays.asList(mf.listener).contains(PackageListenerConstant.onInstalled)) {
						try {
							IJavaPackageEntry entry = loadPackage(mf.id);
							entry.getInstance(Runnable.class, PackageListenerConstant.onInstalled).run();
							entry = null;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					saveModified();
			} else if (mf.type == JavaPackage.typeGroupManifest) {
				boolean ret = true;
				Iterator<JavaPackage> itjp = pkg.getPackageGroup().iterator();
				while (itjp.hasNext()) {
					JavaPackage ejp = itjp.next();
					install(ejp);
				}
			}
	}

	private IFile newCacheSplitFile() throws IOException {
		IFileSystem fs = PlatCoreConfig.get().fs;
		IFile cacheDir = Env.i(UtilsService.class).getClassRelativeFile(getClass(), "cache");
		IFile newFile = null;
		for (int i = 0; i < 0x10000; i++) {
			newFile = cacheDir.next("tmp" + i + ".jar");
			if (!newFile.exists()) {
				break;
			}
		}
		if (newFile == null) {
			throw new IOException("exeed cache file limit.");
		}
		newFile.create();
		return newFile;
	}

	private Collection<IFile> splitClassFile(IFile classFile) throws IOException {
		LinkedList<IFile> outputFiles = new LinkedList<IFile>();
		ZipInputStream zipin = new ZipInputStream(Env.i(UtilsService.class).readFromIFile(classFile));
		int byteTrans = 0;
		byte[] buff = new byte[0x200];
		IFile writingFile = newCacheSplitFile();
		outputFiles.add(writingFile);
		ZipOutputStream writingStream = new ZipOutputStream(quick.writeToIFile(writingFile));
		while (true) {
			int readBytes = zipin.read(buff);
			if (readBytes == -1) {
				ZipEntry nextEntry = zipin.getNextEntry();
				if (nextEntry == null) {
					break;
				}
				if (byteTrans > config.splitFileSize) {
					writingStream.close();
					writingFile = newCacheSplitFile();
					outputFiles.add(writingFile);
					writingStream = new ZipOutputStream(quick.writeToIFile(writingFile));
					byteTrans = 0;
				}
				ZipEntry outEntry = new ZipEntry(nextEntry);
				outEntry.setCompressedSize(-1);
				writingStream.putNextEntry(outEntry);
			} else {
				writingStream.write(buff, 0, readBytes);
				byteTrans += readBytes;
			}
		}
		writingStream.close();
		return outputFiles;
	}

	public static final String regFilePath = "pkgreg.json";
	private int installError = 0;
	public static final int installErrorCheckFail = 2;

	public Collection<Manifest> getInstalledPackageList() {
		return config.packages;
	}

	private void tryInstallDependency(JavaPackage.DependentPackage dpkg) throws PackageNotFoundException, IOException, InstallException {
		JavaPackage jpkg = new JavaPackage();
		jpkg = Env.s(Status.class).repositoryManager.getPackage(dpkg.id, dpkg.minimalVersion);
		if(depencyCheck) {
			try {
				install(jpkg);
			}catch(PackageNotFoundException e) {};
		}else {
			install(jpkg);
		}
	}
	
	public static class InstallException extends Exception{
		private static final long serialVersionUID = 1L;
		public InstallException(String string) {
		}
		public InstallException() {
		}
		public String message;
		@Override
		public String getMessage() {
			return message;
		}
	}
	private void checkAndUpdateDependence(Manifest mf) throws PackageNotFoundException, IOException, InstallException {
		Manifest oldmf = findPackageInfo(mf.id);
		if (versionCheck && oldmf != null && (oldmf.version >= mf.version || mf.version > 0)) {
			throw new InstallException("version check is enabled and version is not match.");
		}
		if (depencyCheck && mf.dependency != null) {
			for (DependentPackage edep : mf.dependency) {
				Manifest pkgInfo = findPackageInfo(edep.id);
				if (pkgInfo == null || pkgInfo.version < edep.minimalVersion) {
					tryInstallDependency(edep);
				}
			}
		}
		if (oldmf != null) {
			removePackage(mf.id);
		}
		config.packages.add(mf);
	}

	public int getLastInstallError() {
		return installError;
	}

	private Collection<Manifest> searchDependenceMf(Manifest mf) {
		LinkedList<Manifest> list = new LinkedList<Manifest>();
		list.add(mf);
		if (mf == null) {
			return list;
		}
		for (DependentPackage edep : mf.dependency) {
			list.addAll(searchDependenceMf(findPackageInfo(edep.id)));
		}
		return list;
	}

	public Manifest findPackageInfo(String id) {
		Iterator<Manifest> itmf;
		itmf = config.packages.iterator();
		while (itmf.hasNext()) {
			Manifest emf = itmf.next();
			if (emf.id.equals(id)) {
				return emf;
			}
		}
		return null;
	}

	private IFile getClassFileDir(String pkgid) {
		IFile f = quick.getClassRelativeFile(this.getClass(), "repo");
		IFile destDir = f.next(pkgid);
		return destDir;
	}

	public File[] getClassFiles(String pkgid) {
		IFile destDir = getClassFileDir(pkgid);
		Iterable<String> children = destDir.list();
		if (children == null) {
			return new File[0];
		}
		LinkedList<File> files = new LinkedList<File>();
		for (String es : children) {
			files.add(destDir.next(es).getJavaFile());
		}
		return files.toArray(new File[0]);
	}

	public File[] createClassFiles(String pkgid, int fileCount) {
		IFile destDir = getClassFileDir(pkgid);
		File[] files = new File[fileCount];
		for (int i = 0; i < fileCount; i++) {
			IFile part = destDir.next("p" + i + ".jar");
			files[i] = part.getJavaFile();
		}
		return files;
	}

	public IJavaPackageEntry loadPackage(String id)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		Manifest mf = findPackageInfo(id);

		if (mf == null || mf.classEntry.equals("")) {
			return null;
		}
		Collection<Manifest> pkglist = searchDependenceMf(mf);
		ClassLoader mainLoader = null;
		for (Manifest ep : pkglist) {
			File[] classFiles = getClassFiles(ep.id);
			if (classFiles != null) {
				if (!status.loadedPackage.containsKey(ep.id)) {
					ClassLoader loader = core.loadClasses(classFiles);
					status.loadedPackage.put(ep.id, loader);
					if (mf.id.equals(ep.id)) {
						mainLoader = loader;
					}
				}
			}
		}
		IJavaPackageEntry entry;
		if (mainLoader != null) {
			entry = (IJavaPackageEntry) (mainLoader.loadClass(mf.classEntry).newInstance());
		} else {
			entry = (IJavaPackageEntry) (core.classSpace.loadClass(mf.classEntry).newInstance());
		}

		return entry;
	}

	public void unloadPackage(String id) {
		ClassLoader loader = status.loadedPackage.get(id);
		if (loader != null) {
			core.unloadClasses(loader);
			status.loadedPackage.remove(id);
		}
	}

	public void removePackage(String id) {
		try {
			Manifest mf = findPackageInfo(id);
			if (mf.listener != null && Arrays.asList(mf.listener).contains(PackageListenerConstant.onUninstalling)) {
				try {
					IJavaPackageEntry entry = loadPackage(mf.id);
					entry.getInstance(Runnable.class, PackageListenerConstant.onUninstalling).run();
					entry = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.gc();
			Env.i(FSUtils.class).deleteDirectory(getClassFileDir(id));
		} catch (IOException e) {
		}
		config.packages.remove(findPackageInfo(id));
		saveModified();
	}

	public Collection<Manifest> findPackagesDependOn(String id) {
		AbstractList<Manifest> ret = new LinkedList<Manifest>();
		Iterator<Manifest> itip = config.packages.iterator();
		while (itip.hasNext()) {
			Manifest eip = itip.next();
			for (DependentPackage edep : eip.dependency) {
				if (edep.id.equals(id)) {
					ret.add(eip);
					ret.add(findPackageInfo(id));
				}
			}

		}
		return ret;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void close() {
	}
	
	public void saveModified() {
		Json json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);
		quick.writeStringToClassRelativeFile(this.getClass(), regFilePath, json.toJson(config));
	}
	
}
