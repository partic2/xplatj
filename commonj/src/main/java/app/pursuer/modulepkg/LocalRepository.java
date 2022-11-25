package app.pursuer.modulepkg;

import java.io.IOException;

import com.badlogic.gdx.utils.Json;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.file.IFileFileHandle;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.FSUtils;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.util.EventHandler;

public class LocalRepository implements IRepository {
	private IFile repoDir;
	public LocalRepository() {
		UtilsService utils=Env.i(UtilsService.class);
		repoDir=utils.getClassRelativeFile(getClass(), null);
	}
	public LocalRepository(IFile repoDir){
		this.repoDir=repoDir;
	}
	public void setRepoDir(IFile dir){
		repoDir=dir;
	}
	public IFile getRepoDir() {
		return repoDir;
	}
	public void install(JavaPackage pkg) throws IOException {
		if(pkg.getManifest().type!=JavaPackage.typePackageManifest) {
			throw new UnsupportedOperationException("Package Type Not Supported.");
		}
		FSUtils fsutils = Env.i(FSUtils.class);
		String fileName1 = pkg.getManifest().classFile;
		pkg.getManifest().classFile="cls"+pkg.getManifest().id+".jar";
		if(fileName1.length()>0) {
			fsutils.copyFile(pkg.getRootDir().next(fileName1), getRepoDir().next(pkg.getManifest().classFile));
		}
		fileName1 = pkg.getManifest().resourceFile;
		if(fileName1.length()>0) {
			pkg.getManifest().resourceFile="res"+pkg.getManifest().id+".zip";
			fsutils.copyFile(pkg.getRootDir().next(fileName1), getRepoDir().next(pkg.getManifest().resourceFile));
		}
		Env.t(Json.class).toJson(pkg.getManifest(), new IFileFileHandle(getPackageManifest(pkg.getManifest().id)));
	}
	public IFile getPackageManifest(String pkgId) {
		return repoDir.next("pkg"+pkgId+".json");
	}
	public void removePackage(final String pkgId) throws IOException {
		JavaPackage pkg=new JavaPackage();
		pkg.loadFromFile(getPackageManifest(pkgId));
		repoDir.next(pkg.getManifest().classFile).delete();
		repoDir.next(pkg.getManifest().resourceFile).delete();
		getPackageManifest(pkgId).delete();
	}
	@Override
	public JavaPackage getPackage(String pkgId, int minimalVersion) throws PackageNotFoundException, IOException {
			IFile pkgFile=getPackageManifest(pkgId);
			JavaPackage output=new JavaPackage();
			if(!pkgFile.exists()){
				throw new PackageNotFoundException();
			}
			try {
				output.loadFromFile(pkgFile);
			} catch (IOException e) {
				throw e;
			}
			if(output.getManifest().version<minimalVersion){
				throw new PackageNotFoundException();
			}
			return output;
	}
	@Override
	public void deleteCache() {
	}

}
