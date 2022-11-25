package lib.pursuer.quickgui.event;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.javaplat.pursuer.util.IntervalCounter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import lib.pursuer.quickgui.StyleConfig;
import lib.pursuer.quickgui.StyleConfig.CommonStyle;

public class LongPressListener extends InputListener {
	IntervalCounter counter;
	ScheduledFuture<?> conterFuture;
	PlatCoreConfig core;
	public static float longPressDelay=0.5f;

	public LongPressListener(){
	}

	@Override
	public boolean touchDown(InputEvent evt, float x, float y, int pt, int Button) {
		counter = new IntervalCounter();
		counter.setCount(0);
		conterFuture=core.executor.scheduleAtFixedRate(counter, (long) (longPressDelay),(long) (longPressDelay),TimeUnit.SECONDS);
		return true;
	}

	@Override
	public void touchDragged(InputEvent event, float x, float y, int pointer) {
		if(conterFuture!=null) {
			conterFuture.cancel(false);
		}
		counter.setCount(0);
		super.touchDragged(event, x, y, pointer);
	}

	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		if (counter.getCount() >= 1) {
			onLongPress(event);
		}
		super.touchUp(event, x, y, pointer, button);
	}

	protected void onLongPress(InputEvent e) {
	};
}
