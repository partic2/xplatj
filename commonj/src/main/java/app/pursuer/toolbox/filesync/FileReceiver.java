package app.pursuer.toolbox.filesync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;


import java.util.Stack;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.filesystem.impl.FileHelper;
import xplatj.javaplat.pursuer.io.stream.PackageIOStream;
import xplatj.javaplat.pursuer.io.stream.StreamTransmit;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.ParcelableString;

class FileReceiver {
	PackageIOStream iomsg;
	String dir;
	PlatCoreConfig core;
	InputStream in;
	OutputStream out;
	recvThread trcv;

	public FileReceiver() {
	};

	public FileReceiver(String root, InputStream from, OutputStream response,
			EventHandler<FileReceiver, Integer> onFinish) {
		init(root, from, response, onFinish);
	}

	private EventHandler<FileReceiver, Integer> finish;

	public void init(String root, InputStream from, OutputStream response,
			EventHandler<FileReceiver, Integer> onFinish) {
		in = from;
		out = response;
		dir = root;
		core = PlatCoreConfig.get();
		trcv = new recvThread();
		finish = onFinish;
		if(finish==null){
			trcv.run();
		}else{
			core.executor.execute(trcv);
		}
	}

	private class recvThread implements Runnable {
		int step;

		@Override
		public void run() {
			File rootdir = new File(dir);
			try {
				rootdir.mkdirs();
				iomsg = new PackageIOStream(in, out);
				FileInfo info = new FileInfo();
				step = 0;
				byte[] pack;
				ParcelableString msg = new ParcelableString();
				while (true) {
					pack = iomsg.waitPackage();
					if (msg.loadFromBytes(pack)) {
						if (msg.getString().equals("fs.update")) {
							step = 3;
							continue;
						} else if (msg.getString().equals("fs.cfgfile")) {
							step = 1;
							continue;
						} else if (msg.getString().equals("fin")) {
							break;
						}
					}

					if (info.loadFromBytes(pack)) {
						String fullpath = dir + info.getName();

						if (step == 3 || step == 1) {
							File hf = new File(fullpath);
							hf.getParentFile().mkdirs();
							FileOutputStream f = new FileOutputStream(fullpath);
							new StreamTransmit(null, in, f, info.getSize(), 0x100, null);
							if (step == 1) {
								DirScan scanner = new DirScan();
								scanner.scan(rootdir);
								for (String es : scanner.updatingRelPath) {
									File upf = new File(dir + es);
									upf.delete();
								}
							}
						}
					} else {
						continue;
					}
				}
			} catch (IOException e) {
			}
			clearEmptyDir(rootdir);
			if (finish != null) {
				finish.handle(FileReceiver.this, 1);
			}
		}
		private void clearEmptyDir(File rootDir){
			Stack<File> pf=new Stack<File>();
			pf.add(rootDir);
			for(int rp=0;pf.size()>0&&rp<0x2000;rp++){
				File top=pf.pop();
				File[] children = top.listFiles();
				if(children==null){
					continue;
				}
				if(children.length>0){
					for(File child:children){
						if(child.isDirectory()){
							pf.add(child);
						}
					}
				}else{
					File parent = top.getParentFile();
					if(parent!=null&&!parent.equals(rootDir)&&!parent.equals(pf.peek())){
						pf.add(top.getParentFile());
					}
					top.delete();
				}
			}
		}
	}
	
}
