package nl.wouter.routeapphost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

public class Group{
	
	private double lon, lat;
	private String groupName;
	private PrintStream out;
	private BufferedReader in;
	private ArrayList<Integer> messageBuffer = new ArrayList<>();
	private Date lastUpdateTime;
	
	public Group(String groupName) {
		this.groupName = groupName;
		setLastUpdateTime(new Date());
	}
	
	public void setConnection(PrintStream out, BufferedReader in){
		this.in = in;
		this.out = out;
		
		setLastUpdateTime(new Date());
		
		try {
			lon = Integer.parseInt(read(true)) / 10000000.0;
			lat = Integer.parseInt(read(true)) / 10000000.0;
			
			Thread.sleep(750);
			
			this.out.println("" + messageBuffer.size());
			for(int i:messageBuffer){
				out.write(Util.intToBytes(i));
			}
			messageBuffer.clear();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String read(boolean verbose) throws IOException{
		return in.readLine();
	}
	
	public int readInt(boolean verbose){
		byte[] bytes = new byte[4];
		try {
			bytes[0] = (byte) in.read();
			bytes[1] = (byte) in.read();
			bytes[2] = (byte) in.read();
			bytes[3] = (byte) in.read();
			if(verbose)System.out.println(Util.byteArrayToInt(bytes));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Util.byteArrayToInt(bytes);
	}
	
	public void sendMessage(String message){
		char[] chars = message.toCharArray();
		for(char c: chars){
			sendMessage((int) c);
		}
	}
	
	public void sendMessage(int i){
		messageBuffer.add(i);
	}
	
	public double getLon(){
		return lon;
	}
	
	public double getLat(){
		return lat;
	}

	public String getGroupName() {
		return groupName;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	private void setLastUpdateTime(Date lastUpdate) {
		this.lastUpdateTime = lastUpdate;
	}

}
