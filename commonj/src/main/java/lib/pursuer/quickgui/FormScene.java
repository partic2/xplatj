package lib.pursuer.quickgui;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import xplatj.gdxplat.pursuer.gui.widget.*;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.EventListener2;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

public class FormScene implements Scene {
	public static class FormSceneStyle {
		public boolean popupCombobox = true;
	}

	private Skin skin;
	private LinkedList<Actor> inputls;
	protected LifecycleWidget widget;
	private FormView formMain;
	private ScrollPane pane;
	private Container<EventHandler<FormScene, Integer>> onSubmit;

	public FormScene() {
		clearQuest();
	}

	public FormScene(Skin skin) {
		clearQuest();
		setSkin(skin);
	}

	public void clearQuest() {
		onSubmit = new Container<EventHandler<FormScene, Integer>>();
		formMain = new FormView();
		inputls = new LinkedList<Actor>();
		ita = null;
	}

	public void setSkin(Skin skin) {
		this.skin = skin;
	}

	public Skin getSkin() {
		return skin;
	}

	@Override
	public String getName() {
		return FormScene.class.getSimpleName();
	}

	@Override
	public void enter(LifecycleWidget widget, Scene from) {
		this.widget = widget;
		pane = new ScrollPane(formMain);
		widget.setChild(pane);
	}

	@Override
	public void leave(LifecycleWidget widget, Scene will) {
		widget.setChild(null);
	}

	private class BindSceneClickListener extends ClickListener {
		Scene boundScene;

		public BindSceneClickListener(Scene boundScene) {
			this.boundScene = boundScene;
		}

		@Override
		public void clicked(InputEvent event, float x, float y) {
			widget.gotoScene(boundScene);
			super.clicked(event, x, y);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void enquire(String hint, T initVal, Object opt) {
		if (inputls.size() > 0 && spaceWidth > 0) {
			splitLine(spaceWidth);
		}
		if (Boolean.class.isInstance(initVal)) {
			Boolean val = (Boolean) initVal;
			CheckBox cb = new CheckBox(hint, skin, StyleConfig.DefaultStyle);
			cb.getImage().setScaling(Scaling.fit);
			cb.getImageCell().size(cb.getLabel().getPrefHeight());
			cb.align(Align.left);
			cb.setChecked(val);
			formMain.addActor(cb);
			inputls.add(cb);
		} else if (Integer.class.isInstance(initVal) || String.class.isInstance(initVal)
				|| Float.class.isInstance(initVal) || Double.class.isInstance(initVal)) {
			AutoExtendTextArea tf = new AutoExtendTextArea(initVal.toString(), skin, StyleConfig.DefaultStyle);
			inputls.add(tf);
			tf.setMessageText(hint);
			formMain.addActor(tf);
		} else if (Short.class.isInstance(initVal)) {
			FormSceneStyle style;
			if (skin.has(StyleConfig.DefaultStyle, FormSceneStyle.class)) {
				style = skin.get(StyleConfig.DefaultStyle, FormSceneStyle.class);
			} else {
				style = new FormSceneStyle();
			}
			if (style.popupCombobox) {
				ComboBoxEntry cbe = new ComboBoxEntry();
				cbe.setList(hint, (Collection<String>) opt, new Integer((Short) initVal));
				inputls.add(cbe);
				formMain.addActor(cbe);
			} else {
				FixedComboBox fcb = new FixedComboBox();
				fcb.setList(hint, (Collection<String>) opt, new Integer((Short) initVal));
				inputls.add(fcb);
				formMain.addActor(fcb);
			}

		} else if (IFile.class.isInstance(initVal)) {
			info(hint);
			FileBrowserEntry fbe = new FileBrowserEntry((IFile) initVal);
			inputls.add(fbe);
			formMain.addActor(fbe);
		} else if (ClickListener.class.isInstance(initVal)) {
			TextButton tb = new TextButton(hint, skin, StyleConfig.DefaultStyle);
			tb.addListener((ClickListener) initVal);
			inputls.add(tb);
			formMain.addActor(tb);
		} else if (FormScene.class.isInstance(initVal)) {
			TextButton tb = new TextButton(hint, skin, StyleConfig.ButtonStyle);
			FormScene fsv = (FormScene) initVal;
			new EventListener2<FormScene, Integer>(fsv.getOnSubmit()) {
				@Override
				public void run() {
					widget.gotoScene(FormScene.this);
					super.run();
				}
			};
			tb.setUserObject(fsv);
			tb.addListener(new BindSceneClickListener(fsv));
			inputls.add(tb);
			formMain.addActor(tb);
		}
	}

	public void info(String str) {
		Label infoLabel = new Label(str, skin);
		infoLabel.setFontScale(skin.get(StyleConfig.DefaultStyle, StyleConfig.TextStyle.class).textSize);
		formMain.addActor(infoLabel);
	}

	public void splitLine(float width) {
		SplitLine line = new SplitLine();
		line.setSpace(width);
		formMain.addActor(line);
	}

	private float spaceWidth = 8;

	public void setSpaceWidth(float w) {
		spaceWidth = w;
	}

	public Container<EventHandler<FormScene, Integer>> getOnSubmit() {
		return onSubmit;
	}

	public static final int INT_Confirm = 1;
	public static final int INT_Cancel = 2;

	public void apply(String confirmMessage, String cancelMessage) {
		if (confirmMessage == null) {
			return;
		}
		SplitLine sl = new SplitLine();
		sl.setSpace(skin.get(StyleConfig.DefaultStyle, StyleConfig.CommonStyle.class).thickSpace);
		formMain.addActor(sl);
		HorizontalButonGroup tbs = new HorizontalButonGroup();
		if (cancelMessage == null) {
			tbs.initButtonGroup(new String[] { confirmMessage }, skin, StyleConfig.ButtonStyle);
		} else {
			tbs.initButtonGroup(new String[] { confirmMessage, cancelMessage }, skin, StyleConfig.ButtonStyle);
		}
		formMain.addActor(tbs);
		tbs.getButtons().get(0).addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (onSubmit.get() != null) {
					onSubmit.get().handle(FormScene.this, INT_Confirm);
				}
				super.clicked(event, x, y);
			}
		});
		if (cancelMessage != null) {
			tbs.getButtons().get(1).addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (onSubmit.get() != null) {
						onSubmit.get().handle(FormScene.this, INT_Cancel);
					}
					super.clicked(event, x, y);
				}
			});
		}

	}

	private Iterator<Actor> ita;

	public void resetResult() {
		ita = null;
	}

	@SuppressWarnings("unchecked")
	public <T> T readResultFor(T obj) {
		if (ita == null) {
			ita = inputls.iterator();
		}
		if (obj == null) {
			ita.next();
		}
		if (!ita.hasNext()) {
			ita = null;
			return null;
		}
		if (Boolean.class.isInstance(obj)) {
			CheckBox cb = (CheckBox) ita.next();
			return (T) Boolean.valueOf(cb.isChecked());
		} else if (Integer.class.isInstance(obj)) {
			TextField tf = (TextField) ita.next();
			return (T) Integer.valueOf(tf.getText());
		} else if (Float.class.isInstance(obj)) {
			TextField tf = (TextField) ita.next();
			return (T) Float.valueOf(tf.getText());
		} else if (Double.class.isInstance(obj)) {
			TextField tf = (TextField) ita.next();
			return (T) Double.valueOf(tf.getText());
		} else if (String.class.isInstance(obj)) {
			TextField tf = (TextField) ita.next();
			return (T) tf.getText();
		} else if (Short.class.isInstance(obj)) {
			Actor input = ita.next();
			if (input instanceof ComboBoxEntry) {
				ComboBoxEntry cbe = (ComboBoxEntry) input;
				return (T) new Short(cbe.getSelectedIndex());
			} else if (input instanceof FixedComboBox) {
				FixedComboBox fcb = (FixedComboBox) input;
				return (T) new Short(fcb.getSelectedIndex());
			}
		} else if (IFile.class.isInstance(obj)) {
			FileBrowserEntry fbe = (FileBrowserEntry) ita.next();
			return (T) fbe.file;
		} else if (FormScene.class.isInstance(obj)) {
			TextButton tbfs = (TextButton) ita.next();
			return (T) tbfs.getUserObject();
		}
		throw new ClassCastException("Not support class:" + obj.getClass());
	}

	private ComboBoxEntry onModifyCbB;

	private class ComboBoxListView extends List<String> {
		public ComboBoxListView(Skin skin) {
			super(skin, StyleConfig.DefaultStyle);
		}
	}

	private List<String> comboBoxView;
	private Actor orgActor;

	private void modifyComboBox(ComboBoxEntry cbe) {
		onModifyCbB = cbe;
		comboBoxView = new ComboBoxListView(skin);
		comboBoxView.setItems(cbe.items);
		comboBoxView.setSelectedIndex(cbe.getSelectedIndex());
		orgActor = widget.getChild();
		widget.setChild(comboBoxView);
		comboBoxView.addListener(new ClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				onModifyCbB.setSelectedIndex(((List<String>) event.getTarget()).getSelectedIndex());
				widget.setChild(orgActor);
			}
		});
	}

	private class ComboBoxEntry extends TextButton {
		public Array<String> items;
		public short selected;

		public ComboBoxEntry() {
			super("", skin, StyleConfig.DefaultStyle);

		}

		public void setList(String hint, Collection<String> items, int selected) {
			this.items = new Array<String>();
			String[] ss = items.toArray(new String[0]);
			this.items.addAll(ss, 0, ss.length);
			setSelectedIndex(selected);
			initListener();
		}

		public void setSelectedIndex(int index) {
			selected = (short) index;
			setText(items.get(selected));

		}

		public short getSelectedIndex() {
			return selected;
		}

		private void initListener() {
			addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					modifyComboBox(ComboBoxEntry.this);
					super.clicked(event, x, y);
				}
			});
		}
	}

	private class FixedComboBox extends ExtendLayout {
		public List<String> list;

		public FixedComboBox() {
			setWidthLayout(ExtendLayout.LayoutType.FillParent);
			setHeightLayout(ExtendLayout.LayoutType.WrapContent);
		}

		public void setList(String hint, Collection<String> items, int selected) {
			list = new List<String>(skin, StyleConfig.DefaultStyle);
			setChild(list);
			list.setItems(new Array<String>(items.toArray(new String[0])));
			list.setSelectedIndex(selected);
		}

		public short getSelectedIndex() {
			return (short) list.getSelectedIndex();
		}
	}

	private FileBrowserEntry onModifyFBE;

	private void selectFileView(FileBrowserEntry fbe) {
		onModifyFBE = fbe;
		FileBrowser2 fb = new FileBrowser2(skin);
		widget.setChild(fb);
		try {
			if (fbe.file.list() != null) {
				fb.navigate(fbe.file);
			} else {
				fb.navigate(fbe.file.last());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		new EventListener2<FileBrowser2, IFile>(fb.getOnResult()) {
			@Override
			public void run() {
				if (getData() != null) {
					onModifyFBE.file = getData();
					onModifyFBE.setText(getAbbrPath(onModifyFBE.file.getPath()));
				}
				widget.setChild(formMain);
			}
		};

	}

	private String getAbbrPath(String fullPath) {
		if (fullPath.length() > 10) {
			return "..." + fullPath.substring(fullPath.length() - 8);
		}
		return fullPath;
	}

	private class FileBrowserEntry extends TextButton {
		public IFile file;

		public FileBrowserEntry(IFile initFile) {
			super("", skin, StyleConfig.DefaultStyle);
			setText(getAbbrPath(initFile.getPath()));
			file = initFile;
			addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					selectFileView(FileBrowserEntry.this);
					super.clicked(event, x, y);
				}
			});
		}
	}

	private class FormView extends VerticalGroup {
		public FormView() {
		}

		@Override
		public void layout() {
			super.layout();
			Iterator<Actor> ita = getChildren().iterator();
			while (ita.hasNext()) {
				Actor ea = ita.next();
				ea.setWidth(getWidth());
				ea.setX(0);
			}
		}
	}
}
