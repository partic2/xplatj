package project.gdx;
import android.content.Context;
import android.util.Base64;
import dalvik.system.*;
import java.io.*;
import java.util.*;
import com.android2.dx.command.dexer.*;
import xplatj.javaplat.pursuer.io.stream.*;
import xplatj.platform.module.Module;
import java.security.*;


public class AndroidModule implements Module {

	@Override
	public void compile(File sourceModule, File nativeModule) {

		String spath = sourceModule.getPath();
		nativeModule.getParentFile().mkdirs();
		try {
			nativeModule.createNewFile();
		} catch (IOException e) {}
		Main.Arguments args=new Main.Arguments();
		if (sourceModule.isDirectory()) {
			ArrayList<String> ls=new ArrayList<String>();
			searchClassFile(sourceModule, ls);
			args.debug = false;
			args.fileNames = (String[]) ls.toArray();
			args.optimize = false;
			args.jarOutput = true;
			args.coreLibrary = true;
			args.outName = nativeModule.getPath();
			args.emptyOk=true;
		} else if (spath.endsWith(".jar") || spath.endsWith(".class")) {
			args.debug = false;
			args.fileNames = new String[]{spath};
			args.jarOutput = true;
			args.coreLibrary = true;
			args.outName = nativeModule.getPath();
			args.emptyOk=true;
		}
		if(tryRunDex(args)!=0){
			throw new UnsupportedOperationException();
		}
	}
	private int dxResult;
	private int tryRunDex(final Main.Arguments arg) {

		try {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
				Thread t=new Thread(null, new Runnable(){
						public void run() {
							try {
								dxResult=new Main(new DxContext()).runDx(arg);
							} catch (IOException e) {
								dxResult=1;
							}
						}
					}, "backend.dexer", 10240 * 1024);
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {}
			} else {
				dxResult=new Main(new DxContext()).runDx(arg);
			}
		} catch (Exception e) {
			dxResult=1;
		} finally {
			Main.clearInternTables();
		}
		return dxResult;
	}
	private void searchClassFile(File root, Collection<String> paths) {
		for (File f : root.listFiles()) {
			if (f.isDirectory()) {
				searchClassFile(f, paths);
			} else {
				String fp=f.getPath();
				if (fp.endsWith(".class")) {
					paths.add(fp);
				}
			}
		}
	}

	private void copyFile(File src, File dest) throws IOException {
		FileInputStream srcStream = new FileInputStream(src);
		FileOutputStream destStream = new FileOutputStream(dest, false);
		new StreamTransmit(null, srcStream, destStream, srcStream.available(), 0x3000, null);
		srcStream.close();
		destStream.close();
	}
	@Override
	public ClassLoader load(File[] obj, ClassLoader parent) {
		DexClassLoader dex;
		String cache=cacheDir().getAbsolutePath();
		StringBuilder cp=new StringBuilder();
		HashSet<String> fileNames=new HashSet<String>();
		int cacheFileId=0;
		File newTmpDexDir=null;
		for (File f : obj) {
			String name = f.getName();
			if (fileNames.contains(name)) {
				String dotSuffix=name.substring(name.lastIndexOf("."), name.length());
				if (newTmpDexDir == null) {
					newTmpDexDir = new File(cacheDir(), "tmpDex");
					newTmpDexDir.mkdirs();
				}
				File newDexFile = new File(newTmpDexDir, f.getAbsolutePath().hashCode() + "-" + cacheFileId + dotSuffix);
				for (;newDexFile.exists();cacheFileId++) {}
				cacheFileId++;
				try {
					copyFile(f, newDexFile);
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
				f = newDexFile;
			} else {
				fileNames.add(name);
			}
			cp = cp.append(f.getAbsolutePath()).append(":");
		}
		int cplen=cp.length();
		if (cplen > 0 && cp.charAt(cplen - 1) == ':') {
			cp.deleteCharAt(cplen - 1);
		}
		try {
			String dirprefix=Base64.encodeToString(MessageDigest.getInstance("MD5").digest(cp.toString().getBytes("utf-8")), Base64.DEFAULT).replace('/', '-');
			File odexdir=new File(cache + "/" + dirprefix);
			odexdir.mkdirs();
			dex = new DexClassLoader(cp.toString(), odexdir.getAbsolutePath() , System.getProperty("libraryPath"), parent);
			return dex;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public File cacheDir() {
		return ctx.getCacheDir();
	}
	protected Context ctx;
	public AndroidModule(Context ctx1) {
		ctx = ctx1;
	}
}
