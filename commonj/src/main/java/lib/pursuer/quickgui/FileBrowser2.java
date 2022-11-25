package lib.pursuer.quickgui;

import java.io.File;
import java.io.IOException;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import xplatj.gdxconfig.Gdx2;
import xplatj.javaplat.pursuer.filesystem.IFile;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.HandlerGroup;

public class FileBrowser2 extends WidgetGroup {
	ScrollPane mainPane;
	List<FileItems> files;
	TextButton btnOk;
	TextButton btnCancel;
	private float space;
	private Container<EventHandler<FileBrowser2, IFile>> onResult;

	public Container<EventHandler<FileBrowser2, IFile>> getOnResult() {
		return onResult;
	}

	public FileBrowser2(Skin s) {
		onResult = new Container<EventHandler<FileBrowser2, IFile>>(new HandlerGroup<FileBrowser2, IFile>());
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
				if (files.getSelected().f.list()!=null) {
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
				onResult.get().handle(FileBrowser2.this, getFile());
				super.clicked(event, x, y);
			}
		});
		btnCancel.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				onResult.get().handle(FileBrowser2.this, null);
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

	IFile currentDIR;

	public IFile getFile() {
		if (files.getSelected().f.list()!=null) {
			return currentDIR;
		} else {
			return files.getSelected().f;
		}
	}

	public IFile getDirectory() {
		return currentDIR;
	}

	public void navigate(IFile f) throws IOException {
		if (f.list()==null) {
			throw new IOException("directory required.");
		}
		currentDIR = f;
		Array<FileItems> items = new Array<FileItems>();

		FileItems t = new FileItems();
		t.display = "..";
		t.f = f.last();
		if (t.f != null) {
			items.add(t);
		}
		for(String e: f.list()) {
			t = new FileItems();
			if (f.next(e).list()!=null) {
				t.display = "[DIR]";
			} else {
				t.display = "";
			}
			t.display += e;
			t.f = f.next(e);
			items.add(t);
		}
		files.setItems(items);
		files.pack();
	}

	private class FileItems {
		String display;
		IFile f;

		public FileItems() {
		}

		@Override
		public String toString() {
			return display;
		}
	}
}