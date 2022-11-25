package xplatj.gdxplat.pursuer.decoder.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import xplatj.gdxplat.pursuer.decoder.internal.OggDecoder;
import xplatj.gdxplat.pursuer.media.RewindSeekAudio;
import xplatj.javaplat.pursuer.io.IDataBlock;
import xplatj.javaplat.pursuer.io.stream.DataBlockInputStream;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class OggAudioInfo extends RewindSeekAudio {

	@Override
	public void flush()
	{
	}

	private IDataBlock db;
	private OggDecoder.Music ogg;
	private DataBlockInputStream dbin;

	@Override
	public boolean loadRaw(IDataBlock raw) {
		db = raw;
		dbin = new DataBlockInputStream(db);
		try {
			ogg = new OggDecoder.Music(dbin);
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
		return ogg.sample;
	}

	@Override
	public int getChannel() {
		if (ogg.mono) {
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
		int ret = ogg.read(bb);
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

}
