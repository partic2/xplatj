package project.xplatj.backend.jse;

import java.io.File;

import xplatj.platform.storage.Storage;

public class DesktopStorage implements Storage {

	@Override
	public String[] getStoragePathList() {
		File[] roots = File.listRoots();
		String[] list=new String[roots.length];
		for(int i=0;i<roots.length;i++){
			list[i]=roots[i].getAbsolutePath();
		}
		return list;
	}

}
