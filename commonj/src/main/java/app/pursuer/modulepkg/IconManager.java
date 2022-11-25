package app.pursuer.modulepkg;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import xplatj.gdxplat.pursuer.file.IFileFileHandle;
import xplatj.gdxplat.pursuer.graphics.drawable.PixmapDrawable;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.IFile;

public class IconManager {
	public Drawable getPackageIcon(String id){
		UtilsService quick = Env.i(UtilsService.class);
		IFile iconFile=quick.getClassRelativeFile(getClass(), id+".png");
		if(iconFile.exists()){
			PixmapDrawable pd=new PixmapDrawable(new Pixmap(new IFileFileHandle(iconFile)));
			
			return pd;
		}else{
			return new BaseDrawable();
		}
		
		
	}
}
