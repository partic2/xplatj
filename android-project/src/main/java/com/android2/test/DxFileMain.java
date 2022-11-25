package com.android2.test;

import java.io.File;
import java.io.IOException;

import com.android2.dx.command.dexer.DxContext;
import com.android2.dx.command.dexer.Main;
import com.android2.dx.command.dexer.Main.Arguments;

public class DxFileMain {

	public static void main(String[] args) {
		Arguments arg = new Main.Arguments();
		arg.fileNames=new String[] {"C:\\Users\\user1\\Repository\\xplatj-dist\\res\\classes\\app.pursuer.modulepkg.LocalRepository\\clsorg.jetbrains.kotlin.kotlin-stdlib.jar"};
		arg.jarOutput=true;
		arg.debug=false;
		arg.outName="./output.jar";
		try {
			DxContext ctx=new DxContext(System.out, System.err);
			Main m=new Main(ctx);
			m.runDx(arg);
			Main.clearInternTables();
			System.gc();
			System.out.println("done");
			System.in.read();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
