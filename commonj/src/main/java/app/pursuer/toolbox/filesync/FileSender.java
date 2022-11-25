package app.pursuer.toolbox.filesync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.javaplat.pursuer.io.stream.PackageIOStream;
import xplatj.javaplat.pursuer.io.stream.StreamTransmit;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.ParcelableString;

class FileSender {
	Collection<String> pathlist;
	String root;
	Collection<String> lu;
	String cfgFile;
	PackageIOStream iomsg;
	InputStream in;
	OutputStream out;
	PlatCoreConfig core;

	sendThread tsnd;

	public FileSender() {
	};

	public FileSender(String root, Collection<String> listUpdate, String cfgFile, InputStream response, OutputStream to,
			EventHandler<FileSender, Integer> onFinish) {
		init(root, listUpdate, cfgFile, response, to, onFinish);
	}

	private EventHandler<FileSender, Integer> finish;

	public void init(String root, Collection<String> listUpdate, String cfgFile, InputStream response, OutputStream to,
			EventHandler<FileSender, Integer> onFinish) {
		core = PlatCoreConfig.get();
		lu = listUpdate;
		this.cfgFile = cfgFile;
		in = response;
		out = to;
		this.root = root;
		tsnd = new sendThread();
		finish = onFinish;
		if(finish==null){
			tsnd.run();
		}else{
			core.executor.execute(tsnd);
		}

	}

	private class sendThread implements Runnable {
		private void sendFile(String name, PackageIOStream iomsg) throws IOException {
			FileInfo info;
			File f;
			FileInputStream fis;
			f = new File(root + name);
			info = new FileInfo(name, (int) f.length());

			iomsg.sendPackage(info.saveToBytes());
			fis = new FileInputStream(f);
			new StreamTransmit(null, fis, out, info.getSize(), 0x100, null);
			fis.close();
		}

		@Override
		public void run() {
			try {
				iomsg = new PackageIOStream(in, out);
				ParcelableString msg = new ParcelableString();
				msg.setString("fs.cfgfile");
				iomsg.sendPackage(msg.saveToBytes());
				sendFile(cfgFile, iomsg);
				msg.setString("fs.update");
				iomsg.sendPackage(msg.saveToBytes());
				Iterator<String> it = lu.iterator();
				while (it.hasNext()) {
					sendFile(it.next(), iomsg);
				}
				ParcelableString finmsg = new ParcelableString("fin");
				iomsg.sendPackage(finmsg.saveToBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (finish != null) {
				finish.handle(FileSender.this, 1);
			}
		}
	}
}
