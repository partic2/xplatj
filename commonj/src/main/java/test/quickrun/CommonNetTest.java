package test.quickrun;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

import xplatj.gdxconfig.core.PlatCoreConfig;
import xplatj.javaplat.pursuer.net.CommonNetDefaultImpl;
import xplatj.javaplat.pursuer.net.DefaultCommonNetAddress;
import xplatj.javaplat.pursuer.net.ICommonNet;
import xplatj.javaplat.pursuer.net.NetConnection;
import xplatj.javaplat.pursuer.net.NetMessage;
import xplatj.javaplat.pursuer.util.EventListener2;

public class CommonNetTest extends BaseSimpleTestEntry{

	@Override
	public void run() {
		CommonNetDefaultImpl.Config config1 = new CommonNetDefaultImpl.Config();
		config1.msgPort=2056;
		final CommonNetDefaultImpl net1 = new CommonNetDefaultImpl(PlatCoreConfig.get().executor, config1);
		
		CommonNetDefaultImpl.Config config2 = new CommonNetDefaultImpl.Config();
		config2.msgPort=2060;
		CommonNetDefaultImpl net2 = new CommonNetDefaultImpl(PlatCoreConfig.get().executor, config2);
		
		new EventListener2<ICommonNet, NetMessage>(net1.getOnMsgRecv()) {
			@Override
			public void run() {
				super.run();
				byte[] data2 = getData().getData();
				net1.sendMessage(getData(), getData().getSource());
				out.println("net1:"+Arrays.toString(data2));
				out.flush();
			}
		};
		new EventListener2<ICommonNet, NetMessage>(net2.getOnMsgRecv()) {
			@Override
			public void run() {
				super.run();
				out.println("net2:"+Arrays.toString(getData().getData()));
				out.flush();
			}
		};
		
		NetMessage msg = new NetMessage();
		msg.setData(new byte[] {1,2,3,4});
		net2.sendMessage(msg, new DefaultCommonNetAddress(
				new InetSocketAddress("192.168.1.106", 2056)));
		
		new EventListener2<ICommonNet, NetConnection>(net1.getOnConnect()) {
			@Override
			public void run() {
				DefaultCommonNetAddress remote = (DefaultCommonNetAddress)getData().getConnectedAddress();
				byte[] buf=new byte[4];
				try {
					getData().read().read(buf);
					getData().close();
					NetConnection conn2 = net1.connectTo(remote);
					conn2.write().write(buf);
					conn2.close();
				} catch (IOException e) {
				}
				out.println("net1:"+remote.getSocketAddress().toString());
				out.println("net1:"+Arrays.toString(buf));
				out.flush();
				super.run();
			}
		};
		
		new EventListener2<ICommonNet, NetConnection>(net2.getOnConnect()) {
			@Override
			public void run() {
				DefaultCommonNetAddress remote = (DefaultCommonNetAddress)getData().getConnectedAddress();
				byte[] buf=new byte[4];
				try {
					getData().read().read(buf);
					getData().close();
				} catch (IOException e) {
				}
				out.println("net2:"+remote.getSocketAddress().toString());
				out.println("net2:"+Arrays.toString(buf));
				out.flush();
				super.run();
			}
		};
		
		try {
			NetConnection conn = net2.connectTo(new DefaultCommonNetAddress(
					new InetSocketAddress("localhost", 2056)));
			conn.write().write(new byte[] {5,6,7,8});
		} catch (IOException e2) {
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		try {
			net1.close();
		}catch(IOException e) {}
		try {
			net2.close();
		}catch(IOException e) {}
		
	}

}
