package test.quickrun;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Scanner;

import com.badlogic.gdx.scenes.scene2d.Actor;

import xplatj.javaplat.pursuer.util.Container;


public abstract class BaseSimpleTestEntry implements ISimpleTestEntry{
	public Reader in;
	public PrintWriter out;
	public Scanner scanner;
	public boolean requestStop=false;
	public Container<Actor> testActor;
	@Override
	public void setEnv(Reader in, PrintWriter out, Container<Actor> testActor) {
		this.in=in;
		this.out=out;
		this.testActor=testActor;
		scanner=new Scanner(in);
		
	}
	@Override
	public void requestStop() {
		requestStop=true;
	}
}
