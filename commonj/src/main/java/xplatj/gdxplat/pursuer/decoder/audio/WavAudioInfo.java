package xplatj.gdxplat.pursuer.decoder.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import xplatj.gdxplat.pursuer.decoder.internal.WavDecoder;
import xplatj.gdxplat.pursuer.media.RewindSeekAudio;
import xplatj.javaplat.pursuer.io.IDataBlock;
import xplatj.javaplat.pursuer.io.stream.DataBlockInputStream;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class WavAudioInfo extends RewindSeekAudio {

	

	private IDataBlock db;
	private WavDecoder.Music wav;
	private DataBlockInputStream dbin;

	@Override
	public boolean loadRaw(IDataBlock raw) {
		db = raw;
		dbin = new DataBlockInputStream(db);
		try {
			wav = new WavDecoder.Music(dbin);
		} catch (GdxRuntimeException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean rewind() {
		dbin.rewind();
		return super.rewind();
	}

	@Override
	public int getSampleRate() {
		return wav.sample;
	}

	@Override
	public int getChannel() {
		if (wav.mono) {
			return 1;
		} else {
			return 2;
		}
	}

	@Override
	public int getDuration() {
		return -1;
	}

	private int slen = 0;
	private ByteBuffer bb;

	@Override
	public int read(short[] buff,int offset,int len) {
		if (slen != len) {
			slen = len;
			bb = ByteBuffer.allocate(slen * 2);
		}
		bb.order(ByteOrder.LITTLE_ENDIAN);
		int ret = wav.read(bb);
		bb.flip();
		ret /= 2;
		ShortBuffer sb=bb.asShortBuffer();
		sb.get(buff, offset,sb.remaining());
		readSec(ret);
		return ret;
	}

	@Override
	public int write(short[] buffer,int offset,int len) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush(){
		
	}
}
