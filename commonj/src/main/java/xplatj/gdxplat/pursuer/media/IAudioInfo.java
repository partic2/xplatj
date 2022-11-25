package xplatj.gdxplat.pursuer.media;

import xplatj.javaplat.pursuer.io.IDataBlock;

public interface IAudioInfo {
	boolean loadRaw(IDataBlock raw);

	int getSampleRate();

	int getChannel();

	int getDuration();

	int read(short[] buff,int offset,int len);
	
	int write(short[] buff,int offset,int len);

	boolean rewind();

	boolean seek(float sec);

	float pos();
	
	void flush();
}
