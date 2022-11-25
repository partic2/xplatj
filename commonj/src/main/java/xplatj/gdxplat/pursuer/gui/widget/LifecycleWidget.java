package xplatj.gdxplat.pursuer.gui.widget;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;

public abstract class LifecycleWidget extends ExtendLayout implements LifecycleListener {
	public float defaultWidth=720;
	public float defaultHeight=1280;
	
	protected List<Scene> scenes;
	protected Scene currentScene;

	public LifecycleWidget() {
		setScenes(new LinkedList<Scene>());
		started = false;
		setWidth(defaultWidth);
		setHeight(defaultHeight);
		setWidthLayout(LayoutType.Custom);
		setHeightLayout(LayoutType.Custom);
	}

	private boolean started;

	@Override
	public void resume() {
		if (!started) {
			start();
			started = true;
		}
		if (syncLifeCycle != null) {
			Iterator<LifecycleListener> it = syncLifeCycle.iterator();
			while (it.hasNext()) {
				it.next().resume();
			}
		}
	}

	@Override
	public void pause() {
		if (syncLifeCycle != null) {
			Iterator<LifecycleListener> it = syncLifeCycle.iterator();
			while (it.hasNext()) {
				it.next().pause();
			}
		}
	}

	@Override
	public void dispose() {
		if (currentScene != null) {
			currentScene.leave(this, null);
		}
		started=false;
		if (syncLifeCycle != null) {
			Iterator<LifecycleListener> it = syncLifeCycle.iterator();
			while (it.hasNext()) {
				it.next().dispose();
			}
		}
	}

	protected void start() {
		setWidth(defaultWidth);
		setHeight(defaultHeight);
		getStage().setKeyboardFocus(LifecycleWidget.this);
	};

	LinkedList<LifecycleListener> syncLifeCycle;

	public Collection<LifecycleListener> getSyncLifeCycle() {
		if (syncLifeCycle == null) {
			syncLifeCycle = new LinkedList<LifecycleListener>();
		}
		return syncLifeCycle;
	}

	public void syncLifeCycleListener(LifecycleListener listener) {
		getSyncLifeCycle().add(listener);

	}

	public Scene getSceneNamed(String name) {
		Iterator<Scene> it = scenes.iterator();
		Scene target = null;
		while (it.hasNext()) {
			target = it.next();
			if (target.getName().equals(name)) {
				break;
			}
		}
		return target;
	}

	public Scene getSceneByClass(Class<?> cls) {
		Iterator<Scene> it = scenes.iterator();
		Scene target = null;
		while (it.hasNext()) {
			target = it.next();
			if (cls.isInstance(target)) {
				break;
			}
		}
		return target;
	}
	protected void onEnterScene(Scene will){
		
	}
	protected void onLeaveScene(Scene from){
		
	}
	private class GotoSceneFunc implements Runnable {
		Scene target;

		public GotoSceneFunc(Scene s) {
			target = s;
		}

		@Override
		public void run() {
			if (target != null) {
				if (currentScene != null) {
					currentScene.leave(LifecycleWidget.this, target);
					onLeaveScene(target);
				}
				Scene prev = currentScene;
				currentScene = target;
				currentScene.enter(LifecycleWidget.this, prev);
				onEnterScene(target);
			}
		}
	}

	public void gotoScene(Scene target) {
		Gdx.app.postRunnable(new GotoSceneFunc(target));
	}

	public void setScenes(List<Scene> s) {
		scenes = s;
	}

	public Collection<Scene> getScenes() {
		return scenes;
	}

	public Scene registerScene(Scene s) {
		scenes.add(s);
		return s;
	}

	public void unregisterScene(Scene s) {
		Iterator<Scene> its = scenes.iterator();
		while (its.hasNext()) {
			Scene es = its.next();
			if (es.equals(s)) {
				its.remove();
				break;
			}
		}
	}
	
}
