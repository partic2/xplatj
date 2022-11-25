package lib.pursuer.remotefilesystem;

import java.io.IOException;

public class RemoteIoException extends IOException {
	private static final long serialVersionUID = 1L;
	public RemoteIoException() {
	}
	public RemoteIoException(String msg) {
		super(msg);
	}
}
