package project.xplatj;

import lib.pursuer.simplewebserver.XplatHTTPDServer;
import xplatj.gdxconfig.GdxEntry;
import xplatj.javaplat.pursuer.util.CloseableGroup;
import xplatj.gdxconfig.Gdx2;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import project.xplatj.backend.jse.DesktopModule;
import project.xplatj.backend.jse.DesktopStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class JMain {
	static Lwjgl3Application japp;
	static GdxEntry gentry;
	static int orientation = 1;
	public static boolean debugMode;
	public static Boolean startOptsParsed=false;
	public static String selectedBackend;
	public static void ensureStartOpts(){
		synchronized (startOptsParsed){
			if(startOptsParsed)return;
			startOptsParsed=true;
			FileInputStream in1 = null;
			try {
				in1 = new FileInputStream("res/flat");
				byte[] content=new byte[1024];
				int len=in1.read(content);
				String[] opts=new String(content,0,len,"utf8").split("\\s+");
				for(String opt:opts){
					if("debug".equals(opt)){
						debugMode=true;
					}
				}
				selectedBackend=opts[0];
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}finally{
				if(in1!=null){
					try {
						in1.close();
					} catch (IOException ex) {
					}
				}
			}
		}
	}
	public static void processStartupConfig() {
		ensureStartOpts();
		if("gdx".equals(selectedBackend)) {
			startGdxBackend();
		}else if("webapp".equals(selectedBackend)) {
			startWebAppBackend();
		}
	}
	public static void startGdxBackend() {
		Lwjgl3ApplicationConfiguration jcfg = new Lwjgl3ApplicationConfiguration();
		jcfg.setIdleFPS(12);
		jcfg.setWindowPosition(40, 40);
		Gdx2.module = new DesktopModule();
		Gdx2.storage=new DesktopStorage();
		japp = new Lwjgl3Application(gentry = new GdxEntry(), jcfg);
	}
	public static XplatHTTPDServer httpd;
	static int httpdPort = 2080;
	public static void startWebAppBackend() {
		File cd = new File("");
		String absPath = cd.getAbsolutePath();
		File rootPath=cd;
		if(absPath.startsWith("/")) {
			//unix like system
			rootPath=new File("/");
		}else if(absPath.substring(1,3).equals(":\\") && System.getProperty("os.name").contains("Windows")){
			//windows
			rootPath=new File(absPath.substring(0,3));
		}
		if(debugMode) {
			httpd=new XplatHTTPDServer("0.0.0.0",httpdPort,rootPath);
		}else {
			httpd=new XplatHTTPDServer("127.0.0.1",httpdPort,rootPath);
		}
		try {
			httpd.start(60*1000);
			String entryUrl = cd.getAbsoluteFile()+"/res/index.html";
			entryUrl=entryUrl.substring(rootPath.getAbsolutePath().length()).replace("\\", "/");
			entryUrl="http://127.0.0.1:"+httpdPort+"/localFile"+(entryUrl.startsWith("/")?"":"/")+entryUrl;
			System.out.println("Open url "+entryUrl+" in borwser.");
			if(System.getProperty("os.name").contains("Windows")) {
				Runtime.getRuntime().exec("explorer "+entryUrl);
			}
			System.out.println("stdin wait for coommand, input \"exit\" to exit.");
			Scanner scanin = new Scanner(System.in);
			while(true) {
				String cmd = scanin.nextLine();
				if("exit".equals(cmd)) {
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String args[]) {
		processStartupConfig();
	}
}
