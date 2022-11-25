package xplatj.platform.module;

import java.io.File;

public interface Module {

	void compile(File sourceModule, File nativeModule);

	ClassLoader load(File[] cp, ClassLoader parent);

	File cacheDir();
}
