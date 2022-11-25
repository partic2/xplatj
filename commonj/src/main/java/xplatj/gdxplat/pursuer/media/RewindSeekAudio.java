package xplatj.gdxplat.pursuer.media;

public abstract class RewindSeekAudio implements IAudioInfo {

	public int framePerSecond=50;
	private float rsec;

	public RewindSeekAudio() {
		rsec = 0;
	}

	@Override
	public boolean seek(float sec) {
		if (sec < pos()) {
			rewind();
		}
		short[] tbuff = new short[getSampleRate() / framePerSecond * getChannel()];
		for (int i = 0; i < 0xffff; i++) {
			if (pos() < sec) {
				read(tbuff,0,tbuff.length);
			} else {
				break;
			}
		}
		return true;
	}

	@Override
	public boolean rewind() {
		rsec = 0;
		return true;
	}

	@Override
	public float pos() {
		return rsec;
	}

	protected void readSec(int shortsLength) {
		rsec += (float) shortsLength / (getChannel() * getSampleRate());
	}
}
