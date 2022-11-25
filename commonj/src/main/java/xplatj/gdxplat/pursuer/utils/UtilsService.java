package xplatj.gdxplat.pursuer.utils;

import com.badlogic.gdx.utils.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import xplatj.gdxconfig.core.*;
import xplatj.gdxplat.pursuer.decoder.audio.*;
import xplatj.gdxplat.pursuer.media.*;
import xplatj.javaplat.pursuer.filesystem.*;
import xplatj.javaplat.pursuer.io.*;
import xplatj.javaplat.pursuer.io.stream.*;
import xplatj.javaplat.pursuer.util.*;

public class UtilsService implements Disposable {
	@Override
	public void dispose() {
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	private PlatCoreConfig core;

	public UtilsService() {
		core = PlatCoreConfig.get();
	}

	public void trace(String[] bs) {
		trace("[");
		for (int i = 0; i < bs.length; i++) {
			trace(bs[i]);
			trace(",");
		}
		trace("]");
	}

	public void trace(byte[] bs) {
		trace("[");
		for (int i = 0; i < bs.length; i++) {
			trace(bs[i]);
			trace(",");
		}
		trace("]");
	}

	public void trace(int[] bs) {
		trace("[");
		for (int i = 0; i < bs.length; i++) {
			trace(bs[i]);
			trace(",");
		}
		trace("]");
	}

	public void trace(float[] bs) {
		trace("[");
		for (int i = 0; i < bs.length; i++) {
			trace(bs[i]);
			trace(",");
		}
		trace("]");
	}

	public void trace(boolean[] bs) {
		trace("[");
		for (int i = 0; i < bs.length; i++) {
			trace(bs[i]);
			trace(",");
		}
		trace("]");
	}

	public void trace(int i) {
		trace(Integer.toString(i));
	}

	public void trace(byte b) {
		trace(Byte.toString(b));
	}

	public void trace(double b) {
		trace(Double.toString(b));
	}

	public void trace(boolean b) {
		trace(Boolean.toString(b));
	}

	public void trace(String s) {
		core.stdwriter.write(s);
		core.stdwriter.flush();
	}


	public DataBlockInputStream readFromIFile(IFile file) throws IOException {
		return Env.t(FSUtils.class).readFromIFile(file);
	}

	public String readStringFromIFile(IFile file) throws IOException {
		InputStream in;
		in = readFromIFile(file);
		InputStreamReader reader = new InputStreamReader(in, "utf-8");
		String ret = readString(reader);
		in.close();
		return ret;
	}

	public void writeStringToIFile(IFile file, String data) throws IOException {
		OutputStream out;
		out = writeToIFile(file);
		OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8");
		writer.write(data);
		writer.flush();
		out.close();
	}

	public DataBlockOutputStream writeToIFile(IFile file) throws IOException {
		return Env.t(FSUtils.class).writeToIFile(file);
	}

	public String getClassRelativePath(Class<?> cls, String filename) {
		String dir = cls.getName();
		int dollar=dir.indexOf('$');
		if(dollar!=-1){
			dir=dir.substring(0, dollar);
		}
		String path="/6/classes/".concat(dir);
		if(filename==null||filename.length()==0){
			return path;
		}else{
			return path.concat("/").concat(filename);
		}
	}

	public IFile javaFileInStdPrefixFS(File f) {
		return core.fs.resolve("/1/" + f.getAbsolutePath().replace('\\', '/'));
	}

	public IFile getClassRelativeFile(Class<?> cls, String filename) {
		PlatCoreConfig core = PlatCoreConfig.get();
		return core.fs.resolve(getClassRelativePath(cls, filename));
	}

	public String readStringFromClassRelativeFile(Class<?> cls, String filename) {
		try {
			String ret = readStringFromIFile(core.fs.resolve(getClassRelativePath(cls, filename)));
			return ret;
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}
		return new String();
	}

	public void writeStringToClassRelativeFile(Class<?> cls, String filename, String data) {
		try {
			writeStringToIFile(core.fs.resolve(getClassRelativePath(cls, filename)), data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Properties readClassRelativeConfig(Class<?> cls) throws IOException {
		Properties ini = new Properties();
		IFile cfgFile = core.fs.resolve(getClassRelativePath(cls, "config.ini"));
		if (!cfgFile.exists()) {
			cfgFile.create();
		}
		InputStream in = readFromIFile(cfgFile);
		ini.load(in);
		in.close();
		return ini;
	}

	public void writeClassRelativeConfig(Class<?> cls, Properties ini) throws IOException {
		OutputStream out = writeToIFile(core.fs.resolve(getClassRelativePath(cls, "config.ini")));
		ini.store(out, null);
		out.close();
	}

	public <T> T readClassJsonConfig(Class<T> jsonClass,String name) {
		String jsonStr=readStringFromClassRelativeFile(jsonClass, name);
		Json json = Env.t(Json.class);
		json.setIgnoreUnknownFields(true);
		return json.fromJson(jsonClass, jsonStr);
	}
	
	public void writeClassJsonConfig(Object configObj,String name) {
		String jsonStr=Env.t(Json.class).toJson(configObj);
		Env.t(UtilsService.class).writeStringToClassRelativeFile(configObj.getClass(), name, jsonStr);
	}
	
	
	public String readString(Reader reader) throws IOException {
		char[] chs = new char[0x200];
		int count;
		String ret = new String();
		count = reader.read(chs);
		while (count > 0) {
			ret = ret.concat(new String(chs, 0, count));
			count = reader.read(chs);
		}
		return ret;
	}

	public static class EmptyFilter<T> implements IFilter<T> {
		@Override
		public boolean check(T checkData) {
			return true;
		}
	}

	public JsonValue parseJson(String json) {
		JsonReader reader = new JsonReader();
		JsonValue jv = reader.parse(json);
		return jv;
	}

	private void zipInternal(ZipOutputStream out, IFile cur, int homeLen, IFilter<String> ft) throws IOException {
		Iterable<String> sub = cur.list();
		if (sub != null) {
			for (String its : sub) {
				IFile subfile = cur.next(its);
				String zname = subfile.getPath().substring(homeLen);
				if (ft.check(zname)) {
					if(subfile.canOpen()){
						InputStream dbin = readFromIFile(subfile);
						out.putNextEntry(new ZipEntry(zname));
						new StreamTransmit(null, dbin, out, 0x1000000, 0x400, null);
						out.closeEntry();
						dbin.close();
					}
				}
				zipInternal(out, subfile, homeLen, ft);
			}
		}
	}

	
	public void unzip(IFile from, IFile home, IFilter<String> ft) throws IOException {
		if (ft == null) {
			ft = new EmptyFilter<String>();
		}
		InputStream in = readFromIFile(from);
		ZipInputStream zipreader = new ZipInputStream(in);
		for (ZipEntry ze = zipreader.getNextEntry(); ze != null; ze = zipreader.getNextEntry()) {
			if (ft.check(ze.getName())) {
				IFile zipf = home.next(ze.getName());
				try{zipf.create();}catch(IOException e){};
				OutputStream out = writeToIFile(zipf);
				new StreamTransmit(null, zipreader, out, 0x1000000, 0x400, null);
				zipreader.closeEntry();
				out.close();
			}
		}
		in.close();
	}

	public void zip(IFile to, IFile home, IFilter<String> ft) throws IOException {
		if (ft == null) {
			ft = new EmptyFilter<String>();
		}
		try {
			to.create();
			OutputStream out = writeToIFile(to);
			try {
				ZipOutputStream zipout = new ZipOutputStream(out);
				zipInternal(zipout, home, home.getPath().length() + 1, ft);
				zipout.finish();
				zipout.close();
			}catch(IOException e) {
				out.close();
				throw e;
			}
		}catch(IOException e) {
			throw e;
		}
		
	}
	

	public void zipFiles(IFile to,Iterator<IFile> sourceZip,OneArgFunc<String, IFile> zipFileNameMapper) throws IOException {
		CloseableGroup sg=new CloseableGroup();
		try {
			ZipOutputStream zipOut = sg.add(new ZipOutputStream(this.writeToIFile(to)));
			while(sourceZip.hasNext()&&!Thread.interrupted()) {
				IFile f = sourceZip.next();
				String name = zipFileNameMapper.call(f);
				zipOut.putNextEntry(new ZipEntry(name));
				DataBlockInputStream srcStream = this.readFromIFile(f);
				try {
					StreamUtils.copyStream(srcStream, zipOut);
				}finally {
					srcStream.close();
				}
			}
		}finally {
			sg.closeQuietly();
		}
		
	}
	public static class FileAudioPlayer extends AudioPlayer implements Disposable{
		private IFile file;
		private IDataBlock db;
		public void loadIFile(IFile file) throws IOException{
			IAudioInfo info;
			if(file.getPath().endsWith(".mp3")){
				info=new Mp3AudioInfo();
			}else if(file.getPath().endsWith(".ogg")){
				info=new OggAudioInfo();
			}else if(file.getPath().endsWith(".wav")){
				info=new WavAudioInfo();
			}else{
				throw new IOException("Not support format");
			}
			this.file=file;
			db=file.open();
			info.loadRaw(db);
			init(info);
		}
		@Override
		public void dispose() {
			if(db!=null){
				db.free();
				db=null;
				file=null;
			}
		}
		@Override
		protected void finalize() throws Throwable {
			dispose();
		}
	}
	public FileAudioPlayer playSound(IFile file) throws IOException{
		FileAudioPlayer player=new FileAudioPlayer();
		player.loadIFile(file);
		return player;
	}
	public FileAudioPlayer playSoundOnceImmediately(IFile file) throws IOException{
		FileAudioPlayer player = playSound(file);
		new EventListener2<AudioPlayer, Integer>(player.getOnComplete()) {
			@Override
			public void run() {
				FileAudioPlayer player2 = ((FileAudioPlayer)getSource());
				player2.dispose();
				super.run();
			}
		};
		player.play();
		return player;
	}
}
