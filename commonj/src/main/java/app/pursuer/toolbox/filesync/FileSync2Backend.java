package app.pursuer.toolbox.filesync;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.net.ConnectionSlot;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.net.NetConnection;
import xplatj.javaplat.pursuer.net.ICommonNet;
import xplatj.javaplat.pursuer.net.NetAddress;
import xplatj.javaplat.pursuer.net.NetMessage;
import xplatj.javaplat.pursuer.net.NetStringMessage;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.EventHandlerImpl;
import xplatj.javaplat.pursuer.util.EventListener2;

class FileSync2Backend {
	public static final String slotId = FileSync2Backend.class.getName();

	public static final int statusWaitForServer = 1;
	public static final int statusWaitForClient = 2;
	public static final int statusTranslating = 3;
	public static final int statusFinishSend = 4;
	public static final int statusFinishRecv = 5;
	public static final int statusError = 6;
	public static final int statusTimeout = 7;
	public static final int statusInterrupted=8;

	public File rootDir;
	public static final String cfgFileName = "filesync2.cfg.json";

	public int fsStatus;
	protected Container<EventHandler<FileSync2Backend, Integer>> onStatusChange;

	public Container<EventHandler<FileSync2Backend, Integer>> getOnStatusChange() {
		return onStatusChange;
	}

	public FileSync2Backend() {
		onStatusChange = new Container<EventHandler<FileSync2Backend, Integer>>();
	}

	public void init(File rootDir) {
		this.rootDir = rootDir;
	}

	private NetConnection conn;

	public void startServeProg() {
		stopRunningProg();
		Env.i(ConnectionSlot.class).onConnected(slotId, new ServConnected());
		fireStatusChange(statusWaitForClient);
	}

	public void stopRunningProg() {
		if (conn != null) {
			try {
				conn.close();
			} catch (IOException e) {
			}
			conn = null;
		}
		Env.i(ConnectionSlot.class).onConnected(slotId, null);
		synchronized (hangThread) {
			if(!hangThread.isEmpty()) {
				for(Thread t:hangThread) {
					t.interrupt();
				}
			}
		}
	}

	private class ServConnected extends EventHandlerImpl<ConnectionSlot, NetConnection> {
		@Override
		public void run() {
			stopRunningProg();
			getSource().onConnected(slotId, null);
			fireStatusChange(statusTranslating);
			conn = getData();
			DirScan scan1 = new DirScan();
			scan1.scan(rootDir);
			try {
				new FileSender(rootDir.getAbsolutePath(), scan1.updatingRelPath, DirScan.cfgFile, conn.read(),
						conn.write(), null);
			} catch (IOException e) {
				fireStatusChange(statusError);
			}
			if (fsStatus == statusTranslating) {
				fireStatusChange(statusFinishSend);
			}
		}
	}

	protected Set<Thread> hangThread=new HashSet<Thread>();
	public void startClientProg() {
		PlatCoreConfig.get().executor.execute(new Runnable() {
			@Override
			public void run() {
				stopRunningProg();
				fireStatusChange(statusWaitForServer);
				Collection<NetAddress> found;
				try {
					synchronized (hangThread) {
						hangThread.add(Thread.currentThread());
					}
					found = Env.i(ConnectionSlot.class).queryrCertainAmountServ(slotId, 1, 3, TimeUnit.SECONDS);
					synchronized (hangThread) {
						hangThread.remove(Thread.currentThread());
						if(Thread.interrupted()) {
							throw new InterruptedException();
						}
					}
					if (found.size() == 0) {
						fireStatusChange(statusTimeout);
					} else {
						fireStatusChange(statusTranslating);
						try {
							conn = Env.i(ConnectionSlot.class)
									.connect(found.iterator().next(),
											slotId);
							try {
								new FileReceiver(rootDir.getAbsolutePath(), conn.read(), conn.write(), null);
								conn.close();
							} catch (IOException e) {
								fireStatusChange(statusError);
							}
							if (fsStatus == statusTranslating) {
								fireStatusChange(statusFinishRecv);
							}
						} catch (IOException e1) {
							fireStatusChange(statusError);
						}
					}
				} catch (InterruptedException e2) {
					fireStatusChange(statusInterrupted);
				} finally {
					stopRunningProg();
				}
			}
		});

	}



	public void fireStatusChange(int status) {
		this.fsStatus = status;
		EventHandler<FileSync2Backend, Integer> handler = getOnStatusChange().get();
		if (handler != null) {
			handler.handle(this, fsStatus);
		}
	}
}
