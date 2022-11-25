package xplatj.gdxplat.pursuer.media;


import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.javaplat.pursuer.util.Container;
import xplatj.javaplat.pursuer.util.EventHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AudioPlayer {
	public static int bufferedTimeInMS = 1000;
	private IAudioInfo info;
	private AudioDevice ad;
	protected Container<EventHandler<AudioPlayer, Integer>> onComplete;

	private PlatCoreConfig core;
	private int len;
	private boolean autoloop;

	private class PCMBuffer {
		public short[] buffer;
		public int len;

		public PCMBuffer(int initLen) {
			buffer = new short[initLen];
		}
	}

	private PCMBuffer[] buffs;

	public AudioPlayer() {
		onComplete=new Container<EventHandler<AudioPlayer,Integer>>();
	}

	public void init(IAudioInfo audioInfo) {
		core = PlatCoreConfig.get();
		if (ad != null) {
			ad.dispose();
		}
		info = audioInfo;
		ad = Gdx.audio.newAudioDevice(info.getSampleRate(), info.getChannel() == 1);
		ad.setVolume(0.6f);
		len = bufferedTimeInMS * info.getSampleRate() / 1000 * info.getChannel();

		buffs = new PCMBuffer[2];
		buffs[0] = new PCMBuffer(len);
		buffs[1] = new PCMBuffer(len);
		autoloop = false;

		
	}
	
	public void setVolumn(float vol) {
		ad.setVolume(vol);
	}

	public void setAutoLoop(boolean loop) {
		autoloop = loop;
	}

	private PlayThread play_t;

	public void play() {
		if (play_t!=null) {
			return;
		}
		play_t = new PlayThread();
		PCMBuffer b = buffs[0];
		b.len = fillBuffer(b.buffer);
		core.executor.execute(play_t);
	}

	private class PlayThread implements Runnable {
		private int buffid;
		public boolean playing;

		public PlayThread() {
			this.playing=true;
			buffid = 0;
			fill_t = new FillThread();
		}

		public PCMBuffer getIdleBuff() {
			if (buffid == 0) {
				return buffs[1];
			} else {
				return buffs[0];
			}
		}

		public PCMBuffer getInUseBuff() {
			return buffs[buffid];
		}

		public void switchBuff() {
			if (buffid == 0) {
				buffid = 1;
			} else {
				buffid = 0;
			}
		}

		FillThread fill_t;

		private FutureTask<Object> callFillThread() {
			PCMBuffer b = getIdleBuff();
			fill_t.buffer = b;
			FutureTask<Object> task = new FutureTask<Object>(fill_t);
			core.executor.execute(task);
			return task;
		}

		@Override
		public void run() {
			while (this.playing) {
				PCMBuffer b = getInUseBuff();
				if (b.len == 0) {
					if (onComplete.get() != null) {
						onComplete.get().handle(AudioPlayer.this, 1);
					}
					if (autoloop) {
						info.rewind();
						b.len=fillBuffer(b.buffer);
					} else {
						pause();
					}
				} else {
					FutureTask<Object> fillTask = callFillThread();
					if (iash != null) {
						iash.hookData(b.buffer, b.len);
					}
					ad.writeSamples(b.buffer, 0, b.len);
					try {
						fillTask.get();
					} catch (Exception e) {
					}
					switchBuff();
				}
			}
		}
	}

	private class FillThread implements Callable<Object> {
		public PCMBuffer buffer;

		@Override
		public Object call() {
			try {
				buffer.len = fillBuffer(buffer.buffer);
			} catch (GdxRuntimeException e) {
				buffer.len = 0;
			}
			return null;
		}
	}

	public Container<EventHandler<AudioPlayer, Integer>> getOnComplete() {
		return onComplete;
	}

	public void pause() {
		if(play_t!=null) {
			play_t.playing=false;
		}
		play_t=null;
	}

	public void stop() {
		if(play_t!=null) {
			play_t.playing=false;
		}
		play_t=null;
		if(info!=null) {
			info.rewind();
		}
	}

	public void seek(float sec) {
		if(play_t!=null) {
			pause();
			info.seek(sec);
			play();
		}else {
			info.seek(sec);
		}
		
	}

	public float pos() {
		return info.pos();
	}

	public IAudioInfo getInfo() {
		return info;
	}

	private IAudioSampleHook iash;

	public IAudioSampleHook useSampleHook(IAudioSampleHook hook) {
		IAudioSampleHook last;
		last = iash;
		iash = hook;
		return last;
	}

	private int fillBuffer(short[] buff) {
		int ret = info.read(buff,0,buff.length);
		return ret;
	}
}
