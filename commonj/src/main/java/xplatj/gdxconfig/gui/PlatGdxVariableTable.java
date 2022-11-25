package xplatj.gdxconfig.gui;

import java.io.Closeable;
import java.io.IOException;

import com.badlogic.gdx.utils.Disposable;

import xplatj.javaplat.pursuer.util.VariableTable;

public class PlatGdxVariableTable extends VariableTable implements Closeable{
	public PlatGdxVariableTable(VariableTable parent) {
		super(parent);
	}
	public void close() {
		try {
			super.close();
		} catch (IOException e) {}
		for(Object ee:listAllItem()){
			if(ee instanceof Disposable) {
				((Disposable) ee).dispose();
			}
		}
	}
}
