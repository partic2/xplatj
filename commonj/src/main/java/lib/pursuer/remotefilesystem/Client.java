package lib.pursuer.remotefilesystem;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.net.ConnectionSlot;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.filesystem.IFileSystem;
import xplatj.javaplat.pursuer.net.NetAddress;
import xplatj.javaplat.pursuer.net.NetConnection;

//Remote File System Default Client Implementation
public class Client implements IFileSystem,Closeable {

	protected Charset defaultCharset = Charset.forName("utf-8");
	protected NetConnection cachedConn;
	public Charset getCharset() {
		return defaultCharset;
	}
	
	protected Lock lock=new ReentrantLock();
	protected String getServName() {
		return Server.class.getName();
	}
	public NetConnection lockCachedConn() throws IOException {
		lock.lock();
		if(connCleanner!=null) {
			connCleanner.cancel(false);
			connCleanner=null;
		}
		if(cachedConn==null||!cachedConn.isOpen()) {
			cachedConn=Env.i(ConnectionSlot.class).connect(getTarget(), getServName());
		}
		return cachedConn;
	}
	private class IdleConnCleanner implements Runnable{
		public void run() {
			try {
				cachedConn.close();
			} catch (IOException e) {
			}
			cachedConn=null;
			connCleanner=null;
		};
	}
	protected ScheduledFuture<?> connCleanner;
	public void releaseCachedConn() {
		if(cachedConn.isOpen()) {
			connCleanner=PlatCoreConfig.get().executor.schedule(new IdleConnCleanner(), 30, TimeUnit.SECONDS);
		}else {
			cachedConn=null;
		}
		lock.unlock();
	}
	@Override
	public IFile resolve(String path) {
		return new RemoteFile(this,path);
	}
	
	protected NetAddress targetAddr;
	public void setTarget(NetAddress addr) throws IOException {
		this.targetAddr=addr;
	}
	public NetAddress getTarget() {
		return targetAddr;
	}
	@Override
	public void close() throws IOException {
		if(connCleanner!=null) {
			connCleanner.cancel(false);
			connCleanner=null;
		}
		if(cachedConn!=null) {
			if(cachedConn.isOpen()) {
				cachedConn.close();
			}
			cachedConn=null;
		}
	}
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
