package nl.wouter.routeapphost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkHandler extends Thread{
	ServerSocket serverSocket;

	private HostComponent component;

	public static int PORT = 4444;
	
	public NetworkHandler(HostComponent component) {
		this.component = component;
	}
	
	public void run(){
		try {
			serverSocket = new ServerSocket(PORT);
			while(true){
				System.out.println("listening on port: " + PORT);
				final Socket s = serverSocket.accept();
				System.out.println("connection made with: " + s);
				new Thread(){
					public void run(){
						component.connectionReceived(s);
					}
				}.start();;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("listener is closing....");
	}

}
