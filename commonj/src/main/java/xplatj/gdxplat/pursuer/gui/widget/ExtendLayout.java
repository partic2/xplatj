package xplatj.gdxplat.pursuer.gui.widget;


import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.*;

public class ExtendLayout extends WidgetGroup {
	
	public static class LayoutType {
		public static final int FillParent=1;
		public static final int WrapContent=2;
		public static final int Custom=3;
	}

	private int wType;
	private int hType;
	private float wFactor;
	private float hFactor;
	private boolean scalableLayout=false;

	private Layout childLayout;
	private Actor childActor;

	public ExtendLayout() {
		wFactor = 1;
		hFactor = 1;
	}
	public ExtendLayout(Actor child,int widthLayout,int heightLayout){
		this();
		setChild(child);
		setWidthLayout(widthLayout);
		setHeightLayout(heightLayout);
	}

	public void setChild(Actor child) {
		if (childActor != null) {
			removeActor(childActor);
		}
		childActor = child;
		if(childActor!=null){
			addActor(childActor);
			if(childActor instanceof Layout){
				childLayout = (Layout) child;
				childLayout.invalidate();
			}else{
				childLayout=null;
			}
		}else{
			childLayout=null;
		}
	}

	public Actor getChild() {
		return childActor;
	}

	public void setWidthLayout(int l) {
		wType = l;
	}

	public void setWidthFactor(float f) {
		wFactor = f;
	}

	public void setHeightLayout(int l) {
		hType = l;
	}

	public void setHeightFactor(float f) {
		hFactor = f;
	}
	
	public void setScalableLayout(boolean scalable){
		this.scalableLayout=scalable;
	}
	
	public boolean isScalableLayout(){
		return scalableLayout;
	}

	@Override
	public float getPrefWidth() {
		
		float w = 0;
		if (wType == LayoutType.Custom) {
			w = getWidth();
		} else if (wType == LayoutType.FillParent) {
			Group p=getParent();
			if(p!=null){
				w = p.getWidth();
			}
		} else if (wType == LayoutType.WrapContent) {
			if (childLayout == null) {
				return 0;
			}
			w = childLayout.getPrefWidth();
			if(isScalableLayout()) {
				w=w*getScaleX();
			}
		}
		w = w * wFactor;
		return w;
	}
	@Override
	public float getPrefHeight() {
		float h = 0;
		if (hType == LayoutType.Custom) {
			h = getHeight();
		} else if (hType == LayoutType.FillParent) {
			Group p=getParent();
			if(p!=null){
				h = p.getHeight();
			}
		} else if (hType == LayoutType.WrapContent) {
			if (childLayout == null) {
				return 0;
			}
			h = childLayout.getPrefHeight();
			if(isScalableLayout()) {
				h=h*getScaleY();
			}
		}
		h = h * hFactor;
		return h;
	}

	@Override
	public void layout() {
		if (childActor != null) {
			childActor.setPosition(0, 0);
			if(scalableLayout){
				childActor.setSize(getWidth()/getScaleX(), getHeight()/getScaleY());
			}else{
				childActor.setSize(getWidth(), getHeight());
			}
		}
		super.layout();
	}
}
