package lib.pursuer.remotedebugbridge;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import xplatj.gdxplat.pursuer.net.ConnectionSlot;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.net.NetAddress;
import xplatj.javaplat.pursuer.net.NetConnection;

public class Client implements Closeable{
	public boolean connected;
	protected NetConnection conn;
	public void connect(NetAddress addr) throws IOException {
		conn=Env.i(ConnectionSlot.class).connect(addr,Server.class.getName());
		connected=true;
	}
	protected PrintStream prtOut() throws IOException {
		return new PrintStream(conn.write());
	}
	protected void waitEof() throws IOException{
		InputStream r = conn.read();
		char ch;
		do {
			ch = (char) r.read();
		}while(ch!=Server.endOfFrame&&!Thread.currentThread().isInterrupted());
	}
	protected void sendEof() throws IOException {
		conn.write().write(Server.endOfFrame);
	}
	public void startRemoteFileSystem() throws IOException {
		PrintStream out = prtOut();
		out.print("rfs start");
		sendEof();
		waitEof();
	}
	public void stopRemoteFileSystem() throws IOException {
		PrintStream out = prtOut();
		out.print("rfs stop");
		sendEof();
		waitEof();
	}
	public void compile(String source,String output) throws IOException {
		PrintStream out = prtOut();
		out.print("rdb compile "+Server.encode(source)+" "+Server.encode(output));
		sendEof();
		waitEof();
	}
	public void run(String mainClass) throws IOException {
		PrintStream out = prtOut();
		out.print("rdb run "+Server.encode(mainClass));
		sendEof();
		waitEof();
	}
	public void load(String[] classPath) throws IOException {
		PrintStream out = prtOut();
		StringBuilder sb=new StringBuilder(classPath[0]);
		for(int i=1;i<classPath.length;i++) {
			sb.append(":").append(classPath);
		}
		out.print("rdb load "+Server.encode(sb.toString()));
		sendEof();
		waitEof();
	}
	public void unload(String[] classPath) throws IOException {
		PrintStream out = prtOut();
		StringBuilder sb=new StringBuilder(classPath[0]);
		for(int i=1;i<classPath.length;i++) {
			sb.append(":").append(classPath);
		}
		out.print("rdb unload "+Server.encode(sb.toString()));
		sendEof();
		waitEof();
	}
	public void gc() throws IOException {
		PrintStream out = prtOut();
		out.print("rdb gc");
		sendEof();
		waitEof();
	}
	@Override
	public void close() throws IOException {
		conn.close();
		connected=false;
	}
}
