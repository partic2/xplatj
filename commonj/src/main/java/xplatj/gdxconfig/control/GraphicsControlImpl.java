package xplatj.gdxconfig.control;

import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxconfig.gui.PlatGuiConfig;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;

public class GraphicsControlImpl implements GraphicsControl {
	
	public GraphicsControlImpl() {
	}
	
	private LinkedList<ScheduledFuture<?>> stopRends=new LinkedList<ScheduledFuture<?>>();
	private class StopRender implements Runnable {
		@Override
		public void run() {
			stopRends.remove(this);
			if(stopRends.isEmpty()){
				cancelRender();
			}
		}
	}
	public void renderForTime(int renderMiles) {
		if (renderMiles > 0) {
			StopRender stopFunc = new StopRender();
			stopRends.add(PlatCoreConfig.get().executor.schedule(stopFunc, renderMiles,TimeUnit.MICROSECONDS));
		} else if (renderMiles == 0) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					Gdx.graphics.requestRendering();
				}
			});
			return;
		}
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Gdx.graphics.setContinuousRendering(true);
			}
		});
	}

	public void cancelRender() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				Gdx.graphics.setContinuousRendering(false);
			}
		});
		while (!stopRends.isEmpty()) {
			stopRends.pop().cancel(false);
		}
	}

	public boolean hasGLContext() {
		return Thread.currentThread() == PlatGuiConfig.get().glContextThread;
	}

	public void runInGLThreadAndWait(Runnable run) {
		if (hasGLContext()) {
			run.run();
		} else {
			FutureTask<Integer> task = new FutureTask<Integer>(run, new Integer(0));
			Gdx.app.postRunnable(task);
			try {
				task.get();
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
		}
	}
}
