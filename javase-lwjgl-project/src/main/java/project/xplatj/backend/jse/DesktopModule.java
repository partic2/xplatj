package project.xplatj.backend.jse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import com.badlogic.gdx.Gdx;

import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.io.stream.StreamTransmit;
import xplatj.platform.module.Module;

public class DesktopModule implements Module {

	@Override
	public void compile(File sourceModule, File nativeModule) {
		if (sourceModule != nativeModule) {
			copyFile(sourceModule, nativeModule);
		}
	}

	private void copyFile(File src, File out) {
		try {
			FileInputStream from = new FileInputStream(src);
			out.getParentFile().mkdirs();
			out.createNewFile();
			FileOutputStream to = new FileOutputStream(out);
			new StreamTransmit(null, from, to, 0x10000000, 0x2000, null);
			from.close();
			to.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ClassLoader load(File[] cp, ClassLoader parent) {
		URL[] us = new URL[cp.length];
		for (int i = 0; i < cp.length; i++) {
			try {
				us[i] = cp[i].toURI().toURL();
			} catch (MalformedURLException e) {
				return null;
			}
		}
		URLClassLoader loader = new URLClassLoader(us, parent);
		return loader;
	}

	@Override
	public File cacheDir() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

}
