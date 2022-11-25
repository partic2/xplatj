package app.pursuer.toolbox.packagemaker;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

import app.pursuer.modulepkg.IconManager;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.FSUtils;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.util.AsyncFunc;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.IFilter;
import xplatj.javaplat.pursuer.util.OneArgFunc;
import xplatj.javaplat.pursuer.util.Promise;

public class PackageMaker {
	protected PlatCoreConfig coreCfg=PlatCoreConfig.get();
	protected UtilsService utils=Env.i(UtilsService.class);
	
	
	public Json json = Env.i(Json.class);
	
	public PrintWriter consoleOutput;
	protected void writeText(String msg) {
		if(consoleOutput!=null) {
			consoleOutput.println(msg);
			consoleOutput.flush();
		}
	}
	
	
	public Promise<Object> package1(final PackConfig pcfg){
		return new Promise<Object>(new AsyncFunc<Object>() {
			boolean hasClassFile=false;
			boolean hasResourceFile=false;
			FSUtils fsu=Env.i(FSUtils.class);
			PlatCoreConfig core=PlatCoreConfig.get();
			UtilsService utils=Env.i(UtilsService.class);
			@Override
			public void run() {
				Env.i(Json.class).setOutputType(OutputType.json);
				coreCfg.executor.execute(new Runnable() {
					IFile outdir;
					IFile fmf;
					IFile fcls;
					IFile fres;
					@Override
					public void run() {
						writeText("init...");
						initEnv();
						writeText("ok");
						writeText("generating class file...");
						packClass();
						writeText("ok");
						writeText("generating resource file...");
						packResource();
						writeText("ok");
						writeText("generating manifest file...");
						makeManifest();
						writeText("finished.");
						resolve(null);
					}

					private void initEnv() {
						outdir = coreCfg.fs.resolve(pcfg.outputDir);
						fmf = outdir.next(pcfg.pkgfilename);
						fcls = outdir.next(pcfg.pkginfo.classFile);
						fres = outdir.next(pcfg.pkginfo.resourceFile);
					}

					private void makeManifest() {
						pcfg.pkginfo.resourceRoot="/6/classes";
						try {
							utils.writeStringToIFile(fmf, json.toJson(pcfg.pkginfo));
						} catch (IOException e) {
						}
					}

					private void packClass() {
						String slashPkg = pcfg.packagepath.replace('.', '/');
						try {
							final IFile classRoot = coreCfg.fs.resolve(pcfg.classpath);
							IFile classFileRoot = classRoot.next(slashPkg);
							if(classFileRoot.list()!=null) {
								Iterator<IFile> classFile = fsu.searchInDir(classFileRoot, new IFilter<String>() {
									public boolean check(String checkData) {return true;};
								}, true);
								if(classFile.hasNext()) {
									utils.zipFiles(fcls, classFile, new OneArgFunc<String, IFile>() {
										@Override
										public String call(IFile a) {
											return a.getPath().substring(classRoot.getPath().length()+1);
										}
									});
								}else {
									hasClassFile=false;
									pcfg.pkginfo.classFile = "";
								}
							}else {
								hasClassFile=false;
								pcfg.pkginfo.classFile = "";
							}
						} catch (IOException e) {
							writeText("\n");
							writeText(e.getMessage());
							writeText(Arrays.toString(e.getStackTrace()));
							pcfg.pkginfo.classFile = "";
						}
					}

					private void packResource() {
						try {
							LinkedList<IFile> rf = new LinkedList<IFile>();
							IFile resRoot = core.fs.resolve(pcfg.resourcepath).next(pcfg.packagepath);
							if(resRoot.list()!=null) {
								Iterator<IFile> resourceFile = fsu.searchInDir(resRoot, new IFilter<String>() {
									public boolean check(String checkData) {return true;};
								}, true);
								while(resourceFile.hasNext()&&!Thread.interrupted()) {
									rf.add(resourceFile.next());
								}
							}
							IFile iconFile = core.fs.resolve(pcfg.resourcepath).next(IconManager.class.getName()).next(pcfg.pkginfo.id+".png");
							if(iconFile.exists()) {
								rf.add(iconFile);
							}
							utils.zipFiles(fres, rf.iterator(), new OneArgFunc<String, IFile>() {
								@Override
								public String call(IFile a) {
									return a.getPath().substring(pcfg.resourcepath.length()+1);
								}
							});
						} catch (IOException e) {
							writeText(Arrays.toString(e.getStackTrace()));
							pcfg.pkginfo.resourceFile = "";
						}
					}
				});
			}
		});
	}
}
