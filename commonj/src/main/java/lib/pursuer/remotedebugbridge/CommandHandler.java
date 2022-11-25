package lib.pursuer.remotedebugbridge;

import java.io.InputStream;
import java.io.PrintStream;

public interface CommandHandler {
	public void process(InputStream in, PrintStream out, String args[]);
}