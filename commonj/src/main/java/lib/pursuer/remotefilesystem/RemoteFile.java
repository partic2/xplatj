package lib.pursuer.remotefilesystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.net.ConnectionSlot;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.filesystem.IFileSystem;
import xplatj.javaplat.pursuer.io.IDataBlock;
import xplatj.javaplat.pursuer.io.stream.PackageIOStream;
import xplatj.javaplat.pursuer.net.NetConnection;
import xplatj.javaplat.pursuer.net.SocketConnection;

public class RemoteFile implements IFile {
	protected Client client;
	protected String path;

	public RemoteFile(Client client,String path) {
		this.client = client;
		this.path=path;
	}
	protected RemoteFile() {};

	protected NetConnection conn;
	protected PackageIOStream io1;
	protected int connCount = 0;

	protected void connect() throws IOException {
		if (connCount == 0) {
			conn = client.lockCachedConn();
			io1 = new PackageIOStream(conn.read(), conn.write());
		}
		connCount++;
		resetErrStatus();
	}

	protected void disconnect() throws IOException {
		connCount--;
		if (connCount == 0) {
			client.releaseCachedConn();
			conn = null;
			io1 = null;
		}
	}

	protected void forceResetConnection() {
		connCount = 0;
		conn = null;
		io1 = null;
	}

	protected String nextString() throws IOException {
		String data = new String(io1.waitPackage(), client.getCharset());
		return data;
	}

	protected long nextInt64() throws IOException {
		byte[] data = io1.waitPackage();
		long recv = new Int64Data(data).value;
		return recv;
	}

	protected void sendInt64(long i64) throws IOException {
		io1.sendPackage(new Int64Data(i64).toByteArray());
	}

	protected void sendString(String s) throws IOException {
		io1.sendPackage(s.getBytes(client.getCharset()));
	}
	
	protected void resetErrStatus() throws IOException {
		sendString(Constant.RemoteOperation.StatusReset);
	}
	protected void throwIfErr() throws IOException{
		sendString(Constant.RemoteOperation.StatusQuery);
		String status = nextString();
		if(!"OK".equals(status)) {
			throw new RemoteIoException(status);
		}
	}

	protected class DbImpl implements IDataBlock {
		protected long pos;

		@Override
		public void seek(long newPos) throws IOException {
			long size = size();
			pos = size < newPos ? size : newPos;
		}

		@Override
		public long pos() throws IOException {
			return pos;
		}

		@Override
		public long size() throws IOException {
			connect();
			try {
				sendString(Constant.RemoteOperation.FileOpen);
				throwIfErr();
				sendString(Constant.RemoteOperation.DataSize);
				long size = nextInt64();
				sendString(Constant.RemoteOperation.FileClose);
				throwIfErr();
				return size;
			} finally {
				disconnect();
			}
		}

		@Override
		public void resize(long size) throws IOException {
			connect();
			try {
				sendString(Constant.RemoteOperation.FileOpen);
				throwIfErr();
				sendString(Constant.RemoteOperation.DataSetSize);
				sendInt64(size);
				sendString(Constant.RemoteOperation.FileClose);
				throwIfErr();
			} finally {
				disconnect();
			}
		}

		@Override
		public int read(byte[] buffer, int off, int len) throws IOException {
			connect();
			try {
				sendString(Constant.RemoteOperation.FileOpen);
				throwIfErr();
				sendString(Constant.RemoteOperation.DataSeekAndRead);
				sendInt64(pos());
				sendInt64(len);
				byte[] b = io1.waitPackage();
				System.arraycopy(b, 0, buffer, off, b.length);
				sendString(Constant.RemoteOperation.FileClose);
				throwIfErr();
				return b.length;
			} finally {
				disconnect();
			}
		}

		@Override
		public int write(byte[] buffer, int off, int len) throws IOException {
			connect();
			try {
				sendString(Constant.RemoteOperation.FileOpen);
				throwIfErr();
				sendString(Constant.RemoteOperation.DataSeekAndWrite);
				sendInt64(pos());
				io1.sendPackage(Arrays.copyOfRange(buffer, off, len));
				int writeByte=(int) nextInt64();
				sendString(Constant.RemoteOperation.FileClose);
				throwIfErr();
				return writeByte;
			} finally {
				disconnect();
			}
		}
		protected ReentrantLock lock=new ReentrantLock();
		@Override
		public Lock lock() {
			return lock;
		}

		@Override
		public void free() {
		}
	}

	@Override
	public IDataBlock open() throws IOException {
		return new DbImpl();
	}

	@Override
	public File getJavaFile() {
		return null;
	}

	@Override
	public IFileSystem getFileSystem() {
		return client;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public IFile next(String next) {
		String npath = getPath() + "/" + next;
		return getFileSystem().resolve(npath);
	}

	@Override
	public IFile last() {
		String npath = getPath();
		npath = npath.substring(0, npath.lastIndexOf("/"));
		return getFileSystem().resolve(npath);
	}

	@Override
	public Iterable<String> list() {
		try {
			connect();
			try {
				sendString(Constant.RemoteOperation.FileSystemResolve);
				sendString(getPath());
				sendString(Constant.RemoteOperation.FileList);
				int lenChildren = (int)nextInt64();
				Collection<String> children=null;
				if(lenChildren<0) {
					children = null;
				}else {
					children=new LinkedList<String>();
					for(int i=0;i<lenChildren;i++) {
						children.add(nextString());
					}
				}
				throwIfErr();
				return children;
			}finally {
				disconnect();
			}
		} catch (IOException e) {
			return null;
		}
		
	}

	@Override
	public void create() throws IOException {
		connect();
		try {
			sendString(Constant.RemoteOperation.FileSystemResolve);
			sendString(getPath());
			sendString(Constant.RemoteOperation.FileCreate);
			throwIfErr();
		}finally{
			disconnect();
		}
	}

	@Override
	public void delete() throws IOException {
		connect();
		try {
			sendString(Constant.RemoteOperation.FileSystemResolve);
			sendString(getPath());
			sendString(Constant.RemoteOperation.FileOpen);
			throwIfErr();
		}finally{
			disconnect();
		}
	}

	@Override
	public boolean exists() {
		try {
			connect();
			try {
				sendString(Constant.RemoteOperation.FileSystemResolve);
				sendString(getPath());
				sendString(Constant.RemoteOperation.FileExists);
				boolean r="true".equals(nextString());
				throwIfErr();
				return r;
			}finally{
				disconnect();
			}}catch(IOException e) {
				return false;
			}
	}

	@Override
	public boolean canOpen() {
		try {
		connect();
		try {
			sendString(Constant.RemoteOperation.FileSystemResolve);
			sendString(getPath());
			sendString(Constant.RemoteOperation.FileCanOpen);
			String canOpen = nextString();
			throwIfErr();
			return "true".equals(canOpen);
		}finally{
			disconnect();
		}}catch(IOException e) {
			return false;
		}
	}

	@Override
	public void rename(String newName) throws IOException {
		connect();
		try {
			sendString(Constant.RemoteOperation.FileSystemResolve);
			sendString(getPath());
			sendString(Constant.RemoteOperation.FileRename);
			sendString(newName);
			throwIfErr();
		}finally{
			disconnect();
		}
	}

}
