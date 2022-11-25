package app.pursuer.modulepkg.simplehome;

import java.util.Arrays;

import app.pursuer.modulepkg.IJavaPackageEntry;
import app.pursuer.modulepkg.IconManager;
import app.pursuer.modulepkg.PackageListenerConstant;
import app.pursuer.modulepkg.PackageManager;
import app.pursuer.modulepkg.SimpleHomeWidget;
import app.pursuer.modulepkg.JavaPackage.Manifest;
import lib.pursuer.quickgui.TextFlowLayout;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import xplatj.gdxconfig.Gdx2;
import xplatj.gdxconfig.gui.PlatGuiConfig;
import xplatj.gdxplat.pursuer.gui.widget.LifecycleWidget;
import xplatj.gdxplat.pursuer.utils.Env;

public class SimpleHomeWidget2 extends LifecycleWidget{
	public SimpleHomeWidget2() {
	}
	private ScrollPane wrapAppList;
	private TextFlowLayout appList;
	
	
	private class OnAppClick extends ClickListener{
		private AppIcon appIcon;
		public OnAppClick(AppIcon appIcon) {
			this.appIcon=appIcon;
		}
		private void runWidget(LifecycleWidget w) {
			PlatGuiConfig gui = PlatGuiConfig.get();
			gui.mainView=w;
			Gdx2.graphics.runInGLThreadAndWait(gui.applyConfig);
		}
		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(appIcon.id==null){
				runWidget(new SimpleHomeWidget());
			}else{
				try {
					PackageManager pm = Env.i(PackageManager.class);
					IJavaPackageEntry entry = pm.loadPackage(appIcon.id);
					if (entry != null) {
						LifecycleWidget view = entry.getInstance(LifecycleWidget.class, PackageListenerConstant.onGui);
						if (view != null) {
							runWidget(view);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			super.clicked(event, x, y);
		}
	}
	@Override
	protected void start() {
		appList=new TextFlowLayout();
		appList.topAlign=true;
		PackageManager pm=Env.i(PackageManager.class);
		IconManager iconManager = Env.i(IconManager.class);
		AppIcon appIcon = new AppIcon(new BaseDrawable(),"package list",null);
		appIcon.addListener(new OnAppClick(appIcon));
		appList.addWidgetGroup(appIcon);
		for(Manifest mf:pm.getInstalledPackageList()){
			if(mf.listener!=null&&Arrays.asList(mf.listener).contains(PackageListenerConstant.onGui)){
				appIcon = new AppIcon(iconManager.getPackageIcon(mf.id), mf.name,mf.id);
				appList.addWidgetGroup(appIcon);
				appIcon.addListener(new OnAppClick(appIcon));
			}
		}
		wrapAppList=new ScrollPane(appList);
		setChild(wrapAppList);
		super.start();
	}
	@Override
	public void layout() {
		appList.setWidth(getWidth());
		appList.setHeight(getHeight());
		appList.layout();
		super.layout();
	}
}
