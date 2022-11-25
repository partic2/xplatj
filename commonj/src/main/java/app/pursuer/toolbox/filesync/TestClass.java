package app.pursuer.toolbox.filesync;

import java.io.*;
import java.util.*;

import xplatj.gdxconfig.core.*;
import xplatj.gdxplat.pursuer.utils.*;

public class TestClass {
	UtilsService quick;
	PlatCoreConfig core;

	public TestClass() {
		core = PlatCoreConfig.get();
		quick = Env.i(UtilsService.class);
	}

	File f;

	public void input(File p1) {
		f = p1;
	}

	public void test() {
		DirScan dscan = new DirScan();
		dscan.scan(f);
		Iterator<String> its = dscan.updatingRelPath.iterator();
		while (its.hasNext()) {
			String es = its.next();
			quick.trace(es + '\n');
		}
	};
}
