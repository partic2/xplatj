package app.pursuer.modulepkg;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import app.pursuer.modulepkg.IRepository.PackageNotFoundException;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;


//Env.s(PackageManager.Status).repositoryManager
public class RepositoryManager implements IRepository {
	private LinkedList<IRepository> repos=new LinkedList<IRepository>();
	public List<IRepository> getReposList(){
		return repos;
	}
	public JavaPackage getPackage(String pkgId, int minimalVersion) throws PackageNotFoundException, IOException {
		JavaPackage output=null;
			for(IRepository erepo:repos){
				try {
					output=erepo.getPackage(pkgId,minimalVersion);
				}catch(PackageNotFoundException e) {}
			}
			if(output!=null) {
				return output;
			}else {
				throw new PackageNotFoundException().setMissingPackage(pkgId);
			}
	}
	@Override
	public void deleteCache() {
	}
}
