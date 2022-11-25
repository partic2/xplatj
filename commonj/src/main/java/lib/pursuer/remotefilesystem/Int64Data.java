package lib.pursuer.remotefilesystem;



public class Int64Data {
	public long value;
	public Int64Data() {};
	public Int64Data(byte[] data) {
		parse(data);
	}
	public Int64Data(long value) {
		this.value=value;
	}
	public void parse(byte[] data) {
		value=0;
		for(int i=0;i<data.length;i++) {
			value=value+(((long)data[i]&0xff)<<(i*8));
		}
	}
	public byte[] toByteArray() {
		byte[] result = new byte[8];
		for(int i=0;i<result.length;i++) {
			result[i]=(byte)(value>>(8*i)&0xff);
		}
		return result;
	}
}
