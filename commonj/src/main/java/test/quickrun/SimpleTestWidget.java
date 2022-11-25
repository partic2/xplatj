package test.quickrun;

import java.io.PrintWriter;
import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.gui.widget.Scene;
import xplatj.gdxplat.pursuer.utils.Env;
import xplatj.gdxplat.pursuer.utils.UtilsService;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.EventListener2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import lib.pursuer.quickgui.ScenesFlowWidget;
import lib.pursuer.quickgui.StyleConfig;
import lib.pursuer.quickgui.TextInputBox;

public class SimpleTestWidget extends ScenesFlowWidget {

	
	public ISimpleTestEntry testEntry;
	private class TestActorContainer extends Container<Actor>{
		@Override
		public void set(Actor c) {
			testWidget=c;
			super.set(c);
		}
	}
	
	private Container<EventHandler<SimpleTestWidget, Integer>> onRequireTestEntry=new Container<EventHandler<SimpleTestWidget,Integer>>();
	
	public Container<EventHandler<SimpleTestWidget, Integer>> getOnRequireTestEntry(){
		return onRequireTestEntry;
	}
	private class ConsoleScene implements Scene {

		TextInputBox text;
		PrintWriter orgWriter;

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			if (text == null) {
				final PlatCoreConfig core = PlatCoreConfig.get();
				text = new TextInputBox(getDefaultSkin());
				orgWriter = core.stdwriter;
				core.stdwriter = new PrintWriter(text.getWriter());
				if(getOnRequireTestEntry().get()!=null) {
					core.executor.execute(new Runnable() {
						@Override
						public void run() {
							getOnRequireTestEntry().get().handle(SimpleTestWidget.this, 1);
							if(testEntry!=null) {
								testEntry.setEnv(core.stdreader, core.stdwriter, new TestActorContainer());
								testEntry.run();
							}
						}
					});
				}
				new EventListener2<TextInputBox, String>(text.getOnResult()) {
					@Override
					public void run() {
						if(getData()==null){
							gotoSceneWithFlow(getSceneByClass(WidgetTestScene.class));
						}else{
							if(testEntry!=null){
								testEntry.requestStop();
							}
						}
					}
				};
			}
			widget.setChild(text);
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			widget.setChild(null);
			if (will == null) {
				PlatCoreConfig core = PlatCoreConfig.get();
				core.stdwriter = orgWriter;
			}
		}
	}

	public Actor testWidget;

	private class WidgetTestView extends WidgetGroup {
		public TextButton jumpScene;

		public WidgetTestView() {
			jumpScene = new TextButton("back", getDefaultSkin(),
					StyleConfig.ButtonStyle);

			jumpScene.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					goBackScene();
					super.clicked(event, x, y);
				}
			});
			if(testWidget!=null){
				addActor(testWidget);
			}
			addActor(jumpScene);
		}

		@Override
		public void layout() {
			jumpScene.pack();
			jumpScene.setPosition(0, 0);
			if(testWidget!=null){
				testWidget.setSize(getWidth(), getHeight()-200);
				testWidget.setPosition(0, 200);
			}
			super.layout();
		}
	}

	private class WidgetTestScene implements Scene {

		@Override
		public String getName() {
			return this.getClass().getSimpleName();
		}

		@Override
		public void enter(LifecycleWidget widget, Scene from) {
			setChild(new WidgetTestView());
			Env.i(UtilsService.class).trace("widget test:");
		}

		@Override
		public void leave(LifecycleWidget widget, Scene will) {
			setChild(null);
		}

	}
	
	@Override
	protected void start() {
		super.start();
		float wh=(float)Gdx.graphics.getWidth()/Gdx.graphics.getHeight();
		setWidth(getHeight()*wh);
		registerScene(new ConsoleScene());
		registerScene(new WidgetTestScene());
		gotoSceneWithFlow(getSceneByClass(ConsoleScene.class));
		
	}

	public Skin getDefaultSkin() {
		return Env.i(StyleConfig.class).getDefaultSkin();
	}
}
