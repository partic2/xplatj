package project.gdx;
import java.io.*;
import java.util.*;

import android.app.Activity;
import xplatj.platform.storage.*;
import android.content.Context;

public class AndroidStorage implements Storage
{
	protected Context ctx;
	public AndroidStorage(Context ctx1){
		this.ctx=ctx1;
	}
	@Override
	public String[] getStoragePathList() {
		ArrayList<String> paths=new ArrayList<String>();
		if(android.os.Build.VERSION.SDK_INT>=19){
			File[] dirs=ctx.getExternalFilesDirs(null);
			for(File ef:dirs){
				if(ef!=null){
					paths.add(ef.getAbsolutePath());
					ctx.getSystemService(Context.STORAGE_SERVICE);
				}
			}
		}else{
			paths.add(ctx.getExternalFilesDir(null).getAbsolutePath());
		}
		paths.add(ctx.getFilesDir().getAbsolutePath());
		return paths.toArray(new String[0]);
	}
	
}
