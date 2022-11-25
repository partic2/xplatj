package lib.pursuer.quickgui;

import java.io.File;
import java.io.IOException;

import xplatj.gdxconfig.Gdx2;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.HandlerGroup;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragScrollListener;
import com.badlogic.gdx.utils.Array;

public class FileBrowser extends WidgetGroup {
	ScrollPane mainPane;
	List<FileItems> files;
	TextButton btnOk;
	TextButton btnCancel;
	private float space;
	private Container<EventHandler<FileBrowser, File>> onResult;

	public Container<EventHandler<FileBrowser, File>> getOnResult() {
		return onResult;
	}

	public FileBrowser(Skin s) {
		onResult = new Container<EventHandler<FileBrowser, File>>(new HandlerGroup<FileBrowser, File>());
		files = new List<FileItems>(s);
		mainPane = new ScrollPane(files);
		mainPane.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer,
					int button) {
				return true;
			}
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer,
					int button) {
				Gdx2.graphics.renderForTime(1000);
				super.touchUp(event, x, y, pointer, button);
			}
		});
		btnOk = new TextButton("Ok", s, StyleConfig.ButtonStyle);
		btnCancel = new TextButton("Cancel", s, StyleConfig.ButtonStyle);
		addActor(btnOk);
		addActor(btnCancel);
		addActor(mainPane);
		space = s.get(StyleConfig.DefaultStyle, StyleConfig.CommonStyle.class).thinSpace;
		files.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (files.getSelected().f.isDirectory()) {
					try {
						navigate(files.getSelected().f);
					} catch (IOException e) {
					}
				}
				super.clicked(event, x, y);
			}
		});
		btnOk.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				onResult.get().handle(FileBrowser.this, getFile());
				super.clicked(event, x, y);
			}
		});
		btnCancel.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				onResult.get().handle(FileBrowser.this, null);
				super.clicked(event, x, y);
			}
		});
		invalidate();
	}

	@Override
	public void layout() {
		btnCancel.pack();
		btnCancel.setWidth(getWidth());
		btnCancel.setPosition(0, 0);
		btnOk.pack();
		btnOk.setWidth(getWidth());
		btnOk.setPosition(0, btnCancel.getTop() + space);
		mainPane.setSize(getWidth(), getHeight() - space - btnOk.getTop());
		mainPane.setPosition(0, btnOk.getTop() + space);
		mainPane.invalidate();
		btnOk.invalidate();
		super.layout();
	}

	File currentDIR;

	public File getFile() {
		if (files.getSelected().f.isDirectory()) {
			return currentDIR;
		} else {
			return files.getSelected().f;
		}
	}

	public File getDirectory() {
		return currentDIR;
	}

	public void navigate(File f) throws IOException {
		if (!f.isDirectory()) {
			throw new IOException("directory required.");
		}
		currentDIR = f;
		Array<FileItems> items = new Array<FileItems>();

		FileItems t = new FileItems();
		t.display = "..";
		t.f = f.getParentFile();
		if (t.f != null) {
			items.add(t);
		}
		File[] fs = f.listFiles();
		for (int i = 0; i < fs.length; i++) {
			t = new FileItems();
			if (fs[i].isDirectory()) {
				t.display = "[DIR]";
			} else {
				t.display = "";
			}
			t.display += fs[i].getName();
			t.f = fs[i];
			items.add(t);
		}
		files.setItems(items);
		files.pack();
	}

	private class FileItems {
		String display;
		File f;

		public FileItems() {
		}

		@Override
		public String toString() {
			return display;
		}
	}
}
