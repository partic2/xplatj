package app.pursuer.toolbox.filesync;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.util.IFilter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import lib.pursuer.stringutils.WildcardMatcher;

class DirScan {
	public Collection<String> updatingRelPath;
	public static final String cfgFile = "/filesync.cfg.json";
	public static final String cfgOptFile="/filesync.opt.json";
	public Collection<IFilter<String>> filter;

	public static class FilePathFilter implements IFilter<String> {
		WildcardMatcher matcher;

		public FilePathFilter(String wcexpr) {
			matcher = new WildcardMatcher(wcexpr);
		}

		@Override
		public boolean check(String data) {
			return matcher.match(data);
		}
	}

	public DirScan() {
	}

	public static class SyncDirConfig {
		public String name;
		public String[] ignore = new String[0];
	}

	public void scan(File root) {
		filter = new LinkedList<IFilter<String>>();
		updatingRelPath = new LinkedList<String>();
		File cfg1 = new File(root, cfgFile);
		Json json = Env.t(Json.class);
		json.setIgnoreUnknownFields(true);
		SyncDirConfig dircfg;
		if (cfg1.exists()) {
			dircfg = json.fromJson(SyncDirConfig.class, new FileHandle(cfg1));
		} else {
			dircfg = new SyncDirConfig();
			json.toJson(dircfg, new FileHandle(cfg1));
		}

		for (String ewc : dircfg.ignore) {
			filter.add(new FilePathFilter(ewc));
		}
		if (root.isDirectory()) {
			scanRelate(root, root, "/");
		}
		return;
	}

	private void scanRelate(File root, File parent, String parentPath) {

		File[] ls = parent.listFiles();
		for (File child : ls) {
			String filepath = relatePath(child, root);
			if (filepath.equals(cfgFile)) {
				continue;
			}
			boolean accepted = true;
			for (IFilter<String> ft : filter) {
				if (ft.check(filepath)) {
					accepted = false;
					break;
				}
			}
			if (accepted) {
				if (child.isDirectory()) {
					scanRelate(root, child, filepath);
				} else {
					updatingRelPath.add(filepath);
				}
			}
		}
	}

	private String relatePath(File tar, File ref) {
		String nativePath = tar.getPath().substring(ref.getPath().length());
		return nativePath.replaceAll("\\\\", "/");
	}
}
