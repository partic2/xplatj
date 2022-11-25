package app.pursuer.toolbox.packagemaker;

import java.io.File;

import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import app.pursuer.modulepkg.IJavaPackageEntry;
import app.pursuer.modulepkg.PackageListenerConstant;
import app.pursuer.toolbox.filesync.FileSync2Gui;

public class Entry implements IJavaPackageEntry {

	@Override
	public <T> T getInstance(Class<T> cls, String id) {
		if(cls.isAssignableFrom(PackageMakerGui.class)&&PackageListenerConstant.onGui.equals(id)){
			return (T) new PackageMakerGui();
		}
		return null;
	}

}
