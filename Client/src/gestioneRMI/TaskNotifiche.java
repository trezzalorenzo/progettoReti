package gestioneRMI;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.List;

public class TaskNotifiche implements Runnable {

	private final String multicast_address;
	private final MulticastSocket ms;
	public TaskNotifiche(String multicastAddress,MulticastSocket ms) {
		this.multicast_address = multicastAddress;
		
		this.ms=ms;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		boolean interrupted=false;
		int maxbuffersize = 2024;
		try {
			InetAddress gruppo = InetAddress.getByName(multicast_address);
			ms.joinGroup(gruppo);
			byte[] buffer = new byte[maxbuffersize];
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			while (!(interrupted = Thread.currentThread().isInterrupted())) {
				ms.receive(dp);
				String s = new String(dp.getData());
				System.out.println(s);
				System.out.print(">");
			}

		} catch (IOException e) {
		
		}finally {
			System.out.println("chiusura thread notifiche");
			ms.disconnect();		
			ms.close();
	}}
}
