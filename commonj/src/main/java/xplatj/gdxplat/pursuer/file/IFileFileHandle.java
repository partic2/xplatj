package xplatj.gdxplat.pursuer.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.filesystem.FSUtils;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.filesystem.IFileSystem;
import xplatj.javaplat.pursuer.io.*;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class IFileFileHandle extends FileHandle {
	IFile file2;
	IFileSystem fs;
	PlatCoreConfig core;
	UtilsService quick;
	String name;

	public IFileFileHandle(IFile f) {
		core = PlatCoreConfig.get();
		file2 = f;
		file = f.getJavaFile();

		this.fs = f.getFileSystem();
		quick = Env.i(UtilsService.class);
		type = FileType.Internal;
		name = path();
		name = name.substring(name.lastIndexOf('/'));
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public FileHandle[] list() {
		Iterable<String> ls = file2.list();
		Collection<IFileFileHandle> fls = new ArrayList<IFileFileHandle>();
		for (String itls : ls) {
			fls.add(new IFileFileHandle(file2.next(itls)));
		}
		return fls.toArray(new IFileFileHandle[0]);
	}

	public IFileFileHandle() {
	}

	@Override
	public FileHandle parent() {
		return new IFileFileHandle(file2.last());
	}

	@Override
	public FileHandle child(String name) {
		return new IFileFileHandle(file2.next(name));
	}

	@Override
	public InputStream read() {
		try {
			return quick.readFromIFile(file2);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public boolean exists() {
		return file2.exists();
	}

	@Override
	public boolean delete() {
		try {
			file2.delete();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	@Override
	public Writer writer(boolean append, String charset) {
		try{
			if(charset==null){
				charset="utf-8";
			}
			return new OutputStreamWriter(write(append), charset);
		}catch(IOException ex){
			throw new GdxRuntimeException("Error writing file: " + file + " (" + type + ")", ex);
		}
	}
	
	
	@Override
	public boolean deleteDirectory() {
		try {
			new FSUtils().deleteDirectory(file2);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public long length() {
		try
		{
			IDataBlock db=file2.open();
			long size=db.size();
			db.free();
			return size;
		}
		catch (IOException e) {}
		return -1;
	}

	@Override
	public boolean isDirectory() {
		return file2.list() != null;
	}

	@Override
	public OutputStream write(boolean append) {
		try {
			return quick.writeToIFile(file2);
		} catch (IOException e) {
			return null;
		}
	}

	public IFile getIFile() {
		return file2;
	}

	@Override
	public String toString() {
		return file2.getPath();
	}
}
