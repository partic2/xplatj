package app.pursuer.modulepkg;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import xplatj.gdxplat.pursuer.file.IFileFileHandle;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.filesystem.IFileSystem;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;

import app.pursuer.modulepkg.IRepository.PackageNotFoundException;
import app.pursuer.modulepkg.PackageManager.InstallException;

public class JavaPackage {
	public static final int typePackageManifest = 0;
	public static final int typeGroupManifest = 1;

	public static class DependentPackage{
		public String id;
		public int minimalVersion;
	}
	public static class Manifest {
		public int type=typePackageManifest;

		// typePackageManifest
		public String id="";
		public String name="";
		public int version=0;
		public String description="";
		public String classFile="";
		public String classEntry="";
		public String resourceFile="";
		public String resourceRoot="";
		public Collection<DependentPackage> dependency;
		public String[] listener;

		// typeGroupManifest
		public Collection<String> manifestsPath;

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!Manifest.class.isInstance(obj)) {
				return false;
			}
			return id.equals(((Manifest) obj).id);
		}
		
		@Override
		public Object clone() {
			Manifest copy=new Manifest();
			Field[] fields = getClass().getFields();
			for(Field it:fields){
				try {
					it.set(copy, it.get(this));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			return copy;
		}
	}

	private Manifest mf;
	private Collection<JavaPackage> pkggroup;
	private IFile rootDir;

	public Manifest getManifest() {
		return mf;
	}

	public void setManifest(Manifest mf) {
		this.mf = mf;
	}

	public IFile getRootDir() {
		return rootDir;
	}

	public void setRootDir(IFile d) {
		rootDir = d;
	}

	public Collection<JavaPackage> getPackageGroup() {
		if (pkggroup == null) {
			pkggroup = new LinkedList<JavaPackage>();
		}
		return pkggroup;
	}

	public void loadFromFile(IFile file) throws IOException {
		Json json = new Json();
		try {
			setRootDir(file.last());
			IFileFileHandle fh = new IFileFileHandle(file);
			Manifest mffile = json.fromJson(Manifest.class, fh);
			setManifest(mffile);
			if (mffile.type == typePackageManifest) {
			} else if (mffile.type == typeGroupManifest) {
				Iterator<String> itmfp = mffile.manifestsPath.iterator();
				while (itmfp.hasNext()) {
					String emfp = itmfp.next();
					JavaPackage jpkg = new JavaPackage();
					jpkg.loadFromFile(rootDir.next(emfp));
					getPackageGroup().add(jpkg);
				}
			}
		} catch (SerializationException e) {
			throw new IOException("Unavailable install manifest.");
		}
	}

	public void install() throws IOException, PackageNotFoundException, InstallException {
		PackageManager pkgmgr = Env.i(PackageManager.class);
		pkgmgr.install(this);
	}

}
