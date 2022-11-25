package app.pursuer.toolbox.debugloader;

import java.io.File;

import app.pursuer.modulepkg.IJavaPackageEntry;
import app.pursuer.modulepkg.PackageListenerConstant;

public class Entry implements IJavaPackageEntry {


	@Override
	public <T> T getInstance(Class<T> cls, String id) {
		if(cls.isAssignableFrom(PackageDebugLoaderGui.class)&&id.equals(PackageListenerConstant.onGui)) {
			return (T) new PackageDebugLoaderGui();
		}
		return null;
	}

}
