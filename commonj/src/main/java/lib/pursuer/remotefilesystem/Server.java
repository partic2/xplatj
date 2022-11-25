package lib.pursuer.remotefilesystem;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import xplatj.gdxplat.pursuer.net.ConnectionSlot;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.filesystem.IFileSystem;
import xplatj.javaplat.pursuer.io.IDataBlock;
import xplatj.javaplat.pursuer.io.stream.PackageIOStream;
import xplatj.javaplat.pursuer.net.NetConnection;
import xplatj.javaplat.pursuer.net.SocketConnection;
import xplatj.javaplat.pursuer.util.EventHandler;

//Remote File System Default Server Implementation
public class Server implements Closeable{

	protected Charset defaultCharset = Charset.forName("utf-8");
	protected IFileSystem selectedFileSystem;

	public Server() {
	};

	public Server(IFileSystem selected) {
		selectedFileSystem = selected;
	}

	public Charset getCharset() {
		return defaultCharset;
	}

	protected String getServName() {
		return Server.class.getName();
	}
	protected Collection<NetConnection> workingConnections = new ArrayList<NetConnection>();

	public void start() {
		ConnectionSlot conn = Env.i(ConnectionSlot.class);
		conn.onConnected(getServName(), new RemoteFileHandler());
		stopped=false;
	}

	protected boolean stopped=true;
	public void stop() {
		stopped=true;
		Env.i(ConnectionSlot.class).onConnected(getClass().getName(), null);
		for (NetConnection elem : workingConnections) {
			try {
				elem.close();
			} catch (IOException e) {
			}
		}
	}

	protected class RemoteFileHandler implements EventHandler<ConnectionSlot, NetConnection> {
		protected PackageIOStream io1;
		protected InputStream in;
		protected OutputStream out;
		protected IFile selectedFile;
		protected IDataBlock selectedData;

		protected String nextString() throws IOException {
			return new String(io1.waitPackage(), defaultCharset);
		}

		protected long nextInt64() throws IOException {
			return new Int64Data(io1.waitPackage()).value;
		}

		protected void sendInt64(long i64) throws IOException {
			io1.sendPackage(new Int64Data(i64).toByteArray());
		}

		protected void sendString(String s) throws IOException {
			io1.sendPackage(s.getBytes(defaultCharset));
		}

		protected String status = "";

		protected void server() throws IOException {
			io1 = new PackageIOStream(in, out);
			boolean running = true;
			try {
				while (running) {
					String op = nextString();
					try {
						if (op.equals(Constant.RemoteOperation.FileSystemResolve)) {
							String path = nextString();
							selectedFile = selectedFileSystem.resolve(path);
						} else if (op.equals(Constant.RemoteOperation.FileOpen)) {
							selectedData = selectedFile.open();
						} else if (op.equals(Constant.RemoteOperation.FileClose)) {
							selectedData.free();
							selectedData = null;
						} else if (op.equals(Constant.RemoteOperation.DataSize)) {
							long size=-1;
							try {
								size=selectedData.size();
							}finally {
								sendInt64(size);
							}
						} else if (op.equals(Constant.RemoteOperation.DataSetSize)) {
							selectedData.resize(nextInt64());
						} else if (op.equals(Constant.RemoteOperation.FileDelete)) {
							selectedFile.delete();
						} else if (op.equals(Constant.RemoteOperation.FileList)) {
							ArrayList<String> list = new ArrayList<String>();
							boolean canList=true;
							try {
								Iterable<String> children = selectedFile.list();
								if (children == null) {
									canList=false;
								} else {
									canList=true;
									for (String elem : children) {
										list.add(elem);
									}
								}
							}finally {
								if(canList) {
									sendInt64(list.size());
									for (String elem : list) {
										sendString(elem);
									}
								}else {
									sendInt64(-1);
								}
							}
						} else if (op.equals(Constant.RemoteOperation.DataSeekAndRead)) {
							long pos = nextInt64();
							int len = (int) nextInt64();
							int readLen=0;
							byte[] buff = new byte[len];
							try {
								selectedData.lock().lock();
								try {
									selectedData.seek(pos);
									readLen = selectedData.read(buff, 0, len);
								}finally {
									selectedData.lock().unlock();
								}
							}finally {
								io1.sendPackage(Arrays.copyOf(buff, readLen));
							}
						} else if (op.equals(Constant.RemoteOperation.DataSeekAndWrite)) {
							long pos = nextInt64();
							byte[] buff = io1.waitPackage();
							int writtenLen=0;
							try {
								selectedData.lock().lock();
								try {
									selectedData.seek(pos);
									writtenLen = selectedData.write(buff, 0, buff.length);
								}finally {
									selectedData.lock().unlock();
								}
							}finally {
								sendInt64(writtenLen);
							}
						} else if (op.equals(Constant.RemoteOperation.FileCanOpen)) {
							boolean canOpen=false;
							try {
								canOpen=selectedFile.canOpen();
							}finally {
								sendString(Boolean.toString(canOpen));
							}
						} else if (op.equals(Constant.RemoteOperation.StatusQuery)) {
							sendString(status);
						} else if (op.equals(Constant.RemoteOperation.StatusReset)) {
							status = "OK";
						} else if (op.equals(Constant.RemoteOperation.FileExists)) {
							boolean exists=false;
							try {
								exists=selectedFile.exists();
							}finally {
								sendString(Boolean.toString(exists));
							}
						} else if (op.equals(Constant.RemoteOperation.FileRename)) {
							selectedFile.rename(nextString());
						} else if (op.equals(Constant.RemoteOperation.FileCreate)) {
							selectedFile.create();
						}
					} catch (IOException e) {
						status = "ERR:" + e.getMessage();
					}
				}
			} finally {
				if (selectedData != null) {
					selectedData.free();
					selectedData=null;
				}
			}

		}

		@Override
		public void handle(ConnectionSlot from, NetConnection data) {
			try {
				workingConnections.add(data);
				in = data.read();
				out = data.write();
				io1 = new PackageIOStream(in, out);
				server();
				workingConnections.remove(data);
				if(data instanceof SocketConnection) {
					((SocketConnection) data).getSocket().setSoLinger(true, 0);
				}
				data.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void close() throws IOException {
		if(!stopped) {
			stop();
		}
	}
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}
