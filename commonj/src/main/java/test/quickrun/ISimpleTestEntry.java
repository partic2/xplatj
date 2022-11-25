package test.quickrun;

import java.io.PrintWriter;
import java.io.Reader;

import com.badlogic.gdx.scenes.scene2d.Actor;

import xplatj.javaplat.pursuer.util.Container;

public interface ISimpleTestEntry extends Runnable {
	public void setEnv(Reader in,PrintWriter out,Container<Actor> testActor);
	public void requestStop();
}
