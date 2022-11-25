package lib.pursuer.remotedebugbridge;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.utils.Env;

public class RemoteFileSystemHandler implements CommandHandler{

	@Override
	public void process(InputStream in, PrintStream out, String[] args) {
		if("start".equals(args[1])) {
			start();
		}else if("stop".equals(args[1])) {
			stop();
		}else if("mapToRealPath".equals(args[1])) {
			try {
				out.println(URLEncoder.encode(mapToRealPath(args[2]),"utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	public void start() {
		DebugRemoteFileSystem srv=Env.s(DebugRemoteFileSystem.class);
		if(srv==null||!srv.isRunning()) {
			srv=new DebugRemoteFileSystem();
			srv.start();
			Env.ss(DebugRemoteFileSystem.class, srv);
		}
	}
	public void stop() {
		DebugRemoteFileSystem srv=Env.s(DebugRemoteFileSystem.class);
		if(srv!=null) {
			srv.stop();
			Env.ss(DebugRemoteFileSystem.class, null);
		}
	}
	public String mapToRealPath(String IFilePath) {
		return PlatCoreConfig.get().fs.resolve(IFilePath).getJavaFile().getAbsolutePath();
	}

}
