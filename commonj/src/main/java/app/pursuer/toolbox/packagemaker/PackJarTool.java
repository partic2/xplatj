package app.pursuer.toolbox.packagemaker;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import com.badlogic.gdx.utils.Json;

import app.pursuer.modulepkg.JavaPackage;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.filesystem.IFileSystem;

class PackJarTool {
	public static final String statusCheckDependency="check dependency file.";
	public static final String statusGenerateJson="generate json file.";
	public String status;
	public void packJarCollection(IFile dir) throws IOException{
		status=statusCheckDependency;
		IFileSystem fs=dir.getFileSystem();
		for(String efile:dir.list()){
			if(efile.endsWith(".jar")){
				JavaPackage.Manifest pkginfo=new JavaPackage.Manifest();
				pkginfo.classEntry="";
				pkginfo.classFile=efile;
				pkginfo.id=efile.substring(0, efile.length()-4);
				pkginfo.version=1;
				pkginfo.name=pkginfo.id;
				Json json = Env.t(Json.class);
					Env.i(UtilsService.class).writeStringToIFile(dir.next("pkg"+pkginfo.id+".json"),json.toJson(pkginfo));
			}
		}
		
	}
}
