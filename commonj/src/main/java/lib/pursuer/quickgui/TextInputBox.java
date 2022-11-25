package lib.pursuer.quickgui;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import xplatj.gdxconfig.core.*;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;
import xplatj.javaplat.pursuer.util.HandlerGroup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

import lib.pursuer.quickgui.StyleConfig.CommonStyle;
import xplatj.gdxconfig.control.*;

import com.badlogic.gdx.*;

public class TextInputBox extends WidgetGroup {
	private EditField text;
	private HorizontalButonGroup btns;
	private Skin skin;
	private Label tips;
	private PlatCoreConfig core;
	private float space;
	private Container<EventHandler<TextInputBox, String>> onResult;

	public static class TextInputWriter extends Writer{
		TextInputBox input;
		StringBuffer cache;
		Lock cacheWriteLock;
		public TextInputWriter(TextInputBox input) {
			this.input=input;
			cache=new StringBuffer();
			cacheWriteLock=new ReentrantLock(false);
		}
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			try {
				cacheWriteLock.lockInterruptibly();
			} catch (InterruptedException e) {
				throw new InterruptedIOException();
			}
			cache.append(cbuf,off,len);
			cacheWriteLock.unlock();
		}

		@Override
		public void flush() throws IOException {
			if(cache.length()!=0){
				Gdx.app.postRunnable(new Runnable(){
					@Override
					public void run(){
						boolean getLock=cacheWriteLock.tryLock();
						StringBuffer cacheBackup = cache;
						cache=new StringBuffer();
						if(getLock){
							cacheWriteLock.unlock();
						}
						input.setText(input.getText()+cacheBackup.toString());
					}
				});
			}
		}

		@Override
		public void close() throws IOException {
		}
	}
	
	
	public Container<EventHandler<TextInputBox, String>> getOnResult() {
		return onResult;
	}

	private class EditField extends TextArea {
		boolean textChanged;
		public EditField(Skin s, String name) {
			super("", s, name);
			setFocusTraversal(false);
			textChanged=true;
			addListener(new InputListener(){
				@Override
				public boolean keyTyped(InputEvent event, char character) {
					boolean r = super.keyTyped(event, character);
					textChanged=true;
					return r;
				}
			});
		}
		@Override
		public void setText(String str) {
			super.setText(str);
			textChanged=true;
		}
		@Override
		public void draw(Batch batch, float parentAlpha) {
			super.draw(batch, parentAlpha);
			if(textChanged){
				Actor p=getParent();
				if(p instanceof Layout){
					((Layout)p).invalidate();
				}
			}
			textChanged=false;
		}
		@Override
		public float getPrefHeight() {
			setPrefRows(getLines()+15);
			return super.getPrefHeight();
		}
	}

	private class UndoRecord implements Runnable {
		public LinkedList<String> undoStack;

		public UndoRecord() {
			undoStack = new LinkedList<String>();
		}

		@Override
		public void run() {
			if (undoStack.size() >= 10) {
				undoStack.removeLast();
			}
			String newText = text.getText();
			if (!newText.equals(undoStack.peekFirst())) {
				undoStack.offerFirst(newText);
			}
		}

		public void undo() {
			if (undoStack.size() > 0) {
				String newText = text.getText();
				if (!newText.equals(undoStack.peekFirst())) {
					text.setText(undoStack.pollFirst());
				} else {
					undoStack.removeFirst();
					text.setText(undoStack.pollFirst());
				}
			}
		}
	}

	private UndoRecord undoInst;
	private ScrollPane inputPane;
	public TextInputBox(Skin skin) {
		core = PlatCoreConfig.get();
		this.skin = skin;
		onResult = new Container<EventHandler<TextInputBox, String>>();
		onResult.set(new HandlerGroup<TextInputBox, String>());
		tips = new Label("tips", skin, StyleConfig.DefaultStyle);
		addActor(tips);

		btns = new HorizontalButonGroup();
		btns.initButtonGroup(new String[] { "undo", "save", "back" }, skin,
				StyleConfig.ButtonStyle);
		addActor(btns);
		text = new EditField(skin, StyleConfig.DefaultStyle);
		inputPane=new ScrollPane(text);
		inputPane.setScale(2/3f);
		addActor(inputPane);
		AbstractList<TextButton> blist = btns.getButtons();
		undoInst = new UndoRecord();
		core.executor.scheduleAtFixedRate(undoInst, 3, 3, TimeUnit.SECONDS);
		blist.get(0).addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				undoInst.undo();
				super.clicked(event, x, y);
			}
		});
		blist.get(1).addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				onResult.get().handle(TextInputBox.this, text.getText());
				super.clicked(event, x, y);
			}
		});
		blist.get(2).addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				onResult.get().handle(TextInputBox.this, null);
				super.clicked(event, x, y);
			}
		});
		space = skin.get(StyleConfig.CommonStyle.class).thinSpace;
	};

	public void setTitle(String t) {
		tips.setText(t);
	}

	public void setText(String str) {
		text.setText(str);
	}

	private Writer writer;
	public Writer getWriter(){
		if(writer==null){
			writer=new TextInputWriter(this);
		}
		return writer;
	}
	public String getText(){
		return text.getText();
	}
	public TextArea getTextArea(){
		return text;
	}
	@Override
	public void layout() {
		btns.setPosition(0, 0);
		btns.pack();
		tips.setPosition(0, getHeight() - tips.getPrefHeight());
		tips.pack();
		inputPane.setPosition(0, btns.getTop() + space);
		inputPane.setSize(getWidth()/inputPane.getScaleX(), (tips.getY() - inputPane.getY() - space)/inputPane.getScaleX());
		super.layout();
	}

	private float prefWidth;

	@Override
	public float getPrefWidth() {
		prefWidth = getParent().getWidth();
		return prefWidth;
	}

	@Override
	public float getPrefHeight() {
		return 400;
	}
}
