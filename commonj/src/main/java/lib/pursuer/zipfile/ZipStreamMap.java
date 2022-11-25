package lib.pursuer.zipfile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.badlogic.gdx.utils.StreamUtils;

import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.util.defaultimpl.DefaultImplMap;


public class ZipStreamMap extends DefaultImplMap<String, InputStream> {
	protected IFile zip;
	public ZipStreamMap(IFile zipfile){
		zip=zipfile;
	}
	public ZipStreamMap(){
	}
	@Override
	public Set<String> keySet() {
		HashSet<String> fset=new HashSet<String>();
		InputStream in = null;
		try {
			in=Env.i(UtilsService.class).readFromIFile(zip);
			ZipInputStream zin=new ZipInputStream(in);
			int rp=0;
			for(ZipEntry entry = zin.getNextEntry();entry!=null;entry=zin.getNextEntry()){
				if(rp<0x1000){
					rp++;
				}else{
					zin.close();
					return fset;
				}
				fset.add(entry.getName());
			}
			zin.close();
			return fset;
		} catch (IOException e) {
			StreamUtils.closeQuietly(in);
			return null;
		}
	}
	@Override
	public boolean containsKey(Object key) {
		InputStream in=get(key);
		if(in==null){
			return false;
		}else{
			try {
				in.close();
			} catch (IOException e) {
			}
			return true;
		}
	}
	@Override
	public InputStream get(Object key) {
		InputStream in = null;
		try {
			in=Env.i(UtilsService.class).readFromIFile(zip);
			ZipInputStream zin=new ZipInputStream(in);
			int rp=0;
			for(ZipEntry entry = zin.getNextEntry();entry!=null;entry=zin.getNextEntry()){
				if(rp<0x1000){
					rp++;
				}else{
					zin.close();
					return null;
				}
				if(key.equals(entry.getName())){
					return zin;
				}
			}
			zin.close();
			return null;
		} catch (IOException e) {
			StreamUtils.closeQuietly(in);
			return null;
		}
	}
}
