package lib.pursuer.quickgui;

import java.util.EmptyStackException;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.gui.widget.Scene;



public class ScenesFlowWidget extends LifecycleWidget {
	public class Config{
		public boolean autoExitOnNoScene=true;
	}
	public Config config;
	private boolean hookBackEvent=false;
	protected Stack<Scene> scenesFlow;
	protected boolean gotoForward;
	private InputListener hookBackListener=new InputListener(){

		@Override
		public boolean keyDown(InputEvent event, int keycode) {
			if(keycode==Input.Keys.BACK||keycode==Input.Keys.ESCAPE){
				goBackScene();
			}
			return super.keyDown(event, keycode);
		}
	};
	public ScenesFlowWidget() {
		scenesFlow=new Stack<Scene>();
		config=new Config();
	}
	
	@Override
	protected void start() {
		setHookBackEvent(true);
		super.start();
	}
	
	@Override
	public void dispose() {
		setHookBackEvent(false);
		super.dispose();
	}

	public void gotoSceneWithFlow(Scene target){
		gotoForward=true;
		scenesFlow.push(target);
		gotoScene(target);
	}
	public void goBackScene(){
		gotoForward=false;
		try {
			scenesFlow.pop();
		}catch(EmptyStackException e) {
		}
		if(scenesFlow.size()==0){
			if(config.autoExitOnNoScene){
				PlatGuiConfig gui=PlatGuiConfig.get();
				if(gui.homeViewClass!=null){
					if(gui.homeViewClass.isInstance(this)){
						this.dispose();
					}else{
						try {
							gui.mainView=gui.homeViewClass.newInstance();
							Gdx.app.postRunnable(gui.applyConfig);
							this.dispose();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}else{
			gotoScene(scenesFlow.peek());
		}
	}
	public Stack<Scene> getScenesFlow(){
		return scenesFlow;
	}
	public boolean isForward(){
		return gotoForward;
	}
	
	private boolean backStatus;
	public void setHookBackEvent(boolean s){
		if(!hookBackEvent&&s){
			backStatus=Gdx.input.isCatchBackKey();
			addCaptureListener(hookBackListener);
			Gdx.input.setCatchBackKey(true);
		}
		if(hookBackEvent&&!s){
			Gdx.input.setCatchBackKey(backStatus);
			removeCaptureListener(hookBackListener);
		}
		hookBackEvent=s;
	}
}
