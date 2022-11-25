package app.pursuer.toolbox.packagemaker;

import java.util.ArrayList;

import app.pursuer.modulepkg.JavaPackage;
import app.pursuer.modulepkg.JavaPackage.Manifest;

class PackConfig implements Cloneable {
	public String pkgfilename;
	public JavaPackage.Manifest pkginfo;
	public String classpath;
	public String resourcepath;
	public String packagepath;
	public String outputDir;

	public Object clone() {
		try {
			PackConfig copy = (PackConfig) super.clone();
			copy.pkginfo=(Manifest) pkginfo.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void defaultInit(String pkgid) {
		pkginfo = new JavaPackage.Manifest();
		pkginfo.id = pkgid;
		pkginfo.name=pkgid;
		defaultFilaName(pkgid);
		pkginfo.dependency = new ArrayList<JavaPackage.DependentPackage>(0);
		pkginfo.resourceRoot="/6/classes";
		pkginfo.type = JavaPackage.typePackageManifest;
		pkginfo.version = 0;
		classpath = "/6/classes";
		resourcepath = "/6/classes";
		outputDir = "/6/temp";
		packagepath = "lib.xxx";
	}
	public void defaultFilaName(String pkgid){
		pkginfo.id = pkgid;
		pkgfilename = "pkg" + pkgid + ".json";
		pkginfo.classFile = "cls" + pkgid + ".jar";
		pkginfo.resourceFile = "res" + pkgid + ".zip";
	}
}
