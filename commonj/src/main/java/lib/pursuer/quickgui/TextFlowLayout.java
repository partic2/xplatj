package lib.pursuer.quickgui;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;

import lib.pursuer.quickgui.*;

import java.util.*;

import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout;
import xplatj.gdxplat.pursuer.gui.widget.ExtendLayout.LayoutType;

public class TextFlowLayout extends VerticalGroup {
	public ArrayList<HorizontalGroup> lines;
	public ArrayList<Layout> elements;
	public boolean topAlign=false;
	
	public TextFlowLayout() {
		lines = new ArrayList<HorizontalGroup>();
		elements = new ArrayList<Layout>();
		columnLeft();
	}
	
	private boolean elemChanged=false;
	
	public void addWidget(Widget widget) {
		elements.add(widget);
		elemChanged=true;
	}
	
	public void addWidgetGroup(WidgetGroup group) {
		elements.add(group);
		elemChanged=true;
	}

	private BitmapFont textFont;
	private float textSize=1;
	private Color textColor;
	
	public void setTextStyle(BitmapFont textFont,Color clr,float size){
		if(textFont!=null){
			this.textFont=textFont;
		}
		if(clr!=null){
			this.textColor=clr;
		}
		if(size>0){
			this.textSize=size;
		}
	}
	
	public void appendText(String text){
		Label label=new Label(text, new Label.LabelStyle(textFont, textColor));
		label.setFontScale(textSize);
		addWidget(label);
	}
	
	public void appendTextLiterally(String s){
		for(int i=0;i<s.length();i++){
			appendText(s.substring(i,i+1));
		}
	}
	
	private void addLine(HorizontalGroup line) {
		
		line.rowBottom();
		ExtendLayout wrapline=new ExtendLayout();
		wrapline.setChild(line);
		wrapline.setWidthLayout(LayoutType.FillParent);
		wrapline.setHeightLayout(LayoutType.WrapContent);
		
		addActor(wrapline);
		lines.add(line);
	}
	public void clearWidget(){
		elements.clear();
	}
	@Override
	protected void positionChanged() {
		super.positionChanged();
	}
	@Override
	protected void sizeChanged() {
		super.sizeChanged();
	}
	@Override
	public void layout() {
		if(elemChanged){
			lines.clear();
			clearChildren();
			float maxWidth=getWidth();
			HorizontalGroup line=new HorizontalGroup();
			addLine(line);
			if(topAlign) {
				line.rowTop();
			}
			for (Layout elem:elements) {
				if (elem instanceof FlowControlWidget) {
					int controlCode=((FlowControlWidget)elem).type;
					if(controlCode==FlowControlWidget.NewLine){
						line = new HorizontalGroup();
						addLine(line);
						if(topAlign) {
							line.rowTop();
						}
					}else if(controlCode==FlowControlWidget.CenterAlign){
						line.center();
					}else if(controlCode==FlowControlWidget.RightAlign){
						line.right();
					}else if(controlCode==FlowControlWidget.LeftAlign){
						line.left();
					}
				} else {
					if (line.getPrefWidth() + elem.getPrefWidth() <= maxWidth) {
						line.addActor((Actor)elem);
					} else {
						line = new HorizontalGroup();
						addLine(line);
						line.addActor((Actor)elem);
					}
				}
				line.layout();
			}
			elemChanged=false;
		}
		super.layout();
	} 
}
