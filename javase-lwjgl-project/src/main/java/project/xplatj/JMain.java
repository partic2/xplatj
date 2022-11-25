package project.xplatj;

import lib.pursuer.simplewebserver.XplatHTTPDServer;
import xplatj.gdxconfig.GdxEntry;
import xplatj.gdxconfig.Gdx2;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import project.xplatj.backend.jse.DesktopModule;
import project.xplatj.backend.jse.DesktopStorage;

import java.io.File;
import java.io.IOException;

public class JMain {
	static Lwjgl3Application japp;
	static GdxEntry gentry;
	static int orientation = 1;

	public static void main(String args[]) {
		Lwjgl3ApplicationConfiguration jcfg = new Lwjgl3ApplicationConfiguration();
		jcfg.setIdleFPS(12);
		jcfg.setWindowPosition(40, 40);
		Gdx2.module = new DesktopModule();
		Gdx2.storage=new DesktopStorage();
		japp = new Lwjgl3Application(gentry = new GdxEntry(), jcfg);
	}
}
