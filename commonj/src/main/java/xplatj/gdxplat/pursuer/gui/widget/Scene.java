package xplatj.gdxplat.pursuer.gui.widget;

public interface Scene {
	public String getName();

	public void enter(LifecycleWidget widget, Scene from);

	public void leave(LifecycleWidget widget, Scene will);
}
