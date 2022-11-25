package project.sdl;
import android.app.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.util.*;
import project.xplat.launcher.*;
import android.widget.*;
import java.nio.channels.*;
import org.libsdl.app.*;
import project.xplat.launcher.pxprpcapi.ApiServer;

public class MainActivity extends SDLActivity
{
	
	private String escapeStr(String s){
		StringBuffer buf=new StringBuffer();
		for(int i=0;i<s.length();i++){
			char ch=s.charAt(i);
			if(ch=='\\'){
				i++;
				ch=s.charAt(i);
				if(ch=='n'){
					buf.append('\n');
				}else if(ch=='r'){
					buf.append('\r');
				}else if(ch=='\t'){
					buf.append('\t');
				}else if(ch=='\\'){
					buf.append('\\');
				}else if(ch=='s'){
					buf.append(' ');
				}
			}else{
				buf.append(ch);
			}
		}
		return buf.toString();
	}
	private void copy(String src,String dst){
		try{
			FileInputStream srcIn=new FileInputStream(src);
			FileOutputStream dstOut=new FileOutputStream(dst);
			FileChannel chIn=srcIn.getChannel();
			chIn.transferTo(0,chIn.size(),dstOut.getChannel());
			srcIn.close();
			dstOut.close();
		}
		catch (IOException e) {}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApiServer.start(this);
	}

	@Override
	protected void onDestroy() {
		ApiServer.stop();
		super.onDestroy();
	}

	@Override
	protected String[] getLibraries() {
		return new String[] {
				"hidapi",
				"SDL2",
				// "SDL2_image",
				// "SDL2_mixer",
				// "SDL2_net",
				// "SDL2_ttf",
		};
	}
	@Override
	protected String getMainSharedObject()
	{
		return "libSDLLoader.so";
	}
	

}
