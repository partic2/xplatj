package lib.pursuer.quickgui;

import com.badlogic.gdx.scenes.scene2d.ui.*;

public class FlowControlWidget extends Widget {
	public int type;
	public static final int NewLine=10;
	public static final int LeftAlign=11;
	public static final int CenterAlign=12;
	public static final int RightAlign=13;
	public FlowControlWidget(){}
	public FlowControlWidget(int type){this.type=type;}
}
