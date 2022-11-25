package lib.pursuer.remotedebugbridge;

import lib.pursuer.remotefilesystem.Server;
import xplatj.gdxconfig.core.PlatCoreConfig;

public class DebugRemoteFileSystem extends Server{
	public DebugRemoteFileSystem() {
		selectedFileSystem=PlatCoreConfig.get().fs;
	}
	public boolean isRunning() {
		return !stopped;
	}
}
