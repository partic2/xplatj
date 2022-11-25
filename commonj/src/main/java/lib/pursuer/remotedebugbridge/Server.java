package lib.pursuer.remotedebugbridge;

import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.core.*;
import xplatj.gdxplat.pursuer.net.*;
import xplatj.gdxplat.pursuer.utils.*;
import xplatj.javaplat.pursuer.filesystem.*;
import xplatj.javaplat.pursuer.io.stream.*;
import xplatj.javaplat.pursuer.net.*;
import xplatj.javaplat.pursuer.util.*;

import java.lang.StringBuilder;

public class Server {

	public final static char endOfFrame='\n';
	public final static String endOfSession="\\<EXIT>";
	
	public static String encode(String s){
		try {
			return URLEncoder.encode(s,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	public static String decode(String s){
		try {
			return URLDecoder.decode(s,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	public static String encodeErr(Exception e){
		StringWriter s = new StringWriter();
		e.printStackTrace(new PrintWriter(s));
		return encode(s.toString());
	}
	public static class RdbCommand implements CommandHandler {
		public HashMap<String,ClassLoader> loaded=new HashMap<String,ClassLoader>();
		PlatCoreConfig core = PlatCoreConfig.get();
		@Override
		public void process(InputStream in, PrintStream out, String[] args) {
			if("run".equals(args[1])) {
				try {
					String mainClass=decode(args[2]);
					Class<?> mainC = core.classSpace.loadClass(mainClass);
					Method entry = mainC.getMethod("main", String[].class);
					entry.invoke(null, new Object[] {new String[0]});
				} catch (Exception e) {
						out.print(encodeErr(e));
				} 
			}else if("compile".equals(args[1])) {
				try {
					IFileSystem fs = core.fs;
					File from = fs.resolve(decode(args[2])).getJavaFile();
					File to=fs.resolve(decode(args[3])).getJavaFile();
					Gdx2.module.compile(from, to);
				} catch (Exception e) {
					out.print(encodeErr(e));
				}
			}else if("gc".equals(args[1])) {
				System.gc();
			}else if("load".equals(args[1])) {
				try {
					String cpstr=decode(args[2]);
					if(!loaded.containsKey(cpstr)) {
						String[] cp = cpstr.split(":");
						File[] cpf=new File[cp.length];
						for(int i=0;i<cp.length;i++) {
							cpf[i]=core.fs.resolve(cp[i]).getJavaFile();
						}
						ClassLoader loader = core.loadClasses(cpf);
						loaded.put(cpstr, loader);
					}
				}catch(Exception e) {
					out.print(encodeErr(e));
				}
			}else if("unload".equals(args[1])) {
				try {
					String cpstr=decode(args[2]);
					if(loaded.containsKey(cpstr)) {
						core.unloadClasses(loaded.get(cpstr));
						loaded.remove(cpstr);
					}
				}catch(Exception e) {
					out.print(encodeErr(e));
				}
			}
		}

	}
	public Map<String, CommandHandler> commandHandlers;

	public Server() {
		this(true);
	}

	public Server(boolean initDefault) {
		if (initDefault) {
			commandHandlers = new HashMap<String, CommandHandler>();
			commandHandlers.put("rdb", new RdbCommand());
			commandHandlers.put("rfs",new RemoteFileSystemHandler());
		}
	}

	public boolean running;

	private Collection<Closeable> resources = new LinkedList<Closeable>();

	protected void log(String s) {
		//log code... 
	}
	
	public static final int maxLineLenth=0x400;
	private class OnConnected implements
			EventHandler<ConnectionSlot, NetConnection> {
		@Override
		public void handle(ConnectionSlot from, NetConnection data) {
			NetConnection conn = data;
			try {
				InputStream in = conn.read();
				OutputStream out = conn.write();
				BufferedInputStream buffIn=new BufferedInputStream(in,maxLineLenth);
				while (running) {
					byte[] lastByte=new byte[1];
					int bytesOfLine=0;
					conn.setTimeout(0);
					buffIn.mark(maxLineLenth);
					while(!Thread.currentThread().isInterrupted()) {
						int read = buffIn.read(lastByte);
						bytesOfLine+=read;
						if(bytesOfLine>=maxLineLenth||lastByte[0]==endOfFrame) {
							break;
						}
					}
					if(bytesOfLine>=maxLineLenth) {
						throw new IOException("too long line.");
					}else if(bytesOfLine==0) {
						throw new IOException("broken connection");
					}
					byte[] lineData=new byte[bytesOfLine];
					buffIn.reset();
					buffIn.read(lineData);
					String line=new String(lineData, "utf-8");
					log("request");
					log(line);
					String[] args = line.split("\\s+");
					if (endOfSession.equals(args[0])) {
						break;
					}
					CommandHandler handler = commandHandlers.get(args[0].trim());
					
					PrintStream out2 = new PrintStream(out);
					out2.print(endOfFrame);
					out2.flush();
					
					if(handler!=null) {
						handler.process(buffIn, out2, args);
					}else {
						out2.println("ERR:No Handler "+args[0]);
					}
					
					
					log("response done");
				}
				conn.close();
			} catch (IOException e) {
				StreamUtils.closeQuietly(conn);
			}
		}
	}

	private OnConnected slotListener;

	
	public void start() {
		running = true;
		slotListener = new OnConnected();
		Env.i(ConnectionSlot.class).onConnected(Server.class.getName(), slotListener);
	}

	public void stop() {
		running = false;
		Env.i(ConnectionSlot.class).onConnected(Server.class.getName(), null);
		for (Closeable er : resources) {
			try {
				er.close();
			} catch (IOException e) {
			}
		}
	}
}
