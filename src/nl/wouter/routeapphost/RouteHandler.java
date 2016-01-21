package nl.wouter.routeapphost;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RouteHandler extends Thread {
	ServerSocket serverSocket;
	private int port;
	
	
	public RouteHandler(int port){
		this.port = port;
	}
	
	public void run(){
		try {
			serverSocket = new ServerSocket(port);
			
			while(true){
				Socket s = serverSocket.accept();
				RouteConnection con = new RouteConnection(s);
				con.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
