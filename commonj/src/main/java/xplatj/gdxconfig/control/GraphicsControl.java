package xplatj.gdxconfig.control;

public interface GraphicsControl {
	

	public void renderForTime(int renderMiles);

	public void cancelRender();

	public boolean hasGLContext();

	public void runInGLThreadAndWait(Runnable run);
}
