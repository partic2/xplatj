package app.pursuer.toolbox.filesync;

import java.io.File;

import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import app.pursuer.modulepkg.IJavaPackageEntry;
import app.pursuer.modulepkg.PackageListenerConstant;

public class Entry implements IJavaPackageEntry {

	@Override
	public <T> T getInstance(Class<T> cls, String id) {
		if(cls.isAssignableFrom(FileSync2Gui.class)&&PackageListenerConstant.onGui.equals(id)){
			return (T) new FileSync2Gui();
		}
		return null;
	}

}
