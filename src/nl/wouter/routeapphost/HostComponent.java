package nl.wouter.routeapphost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HostComponent extends Thread{
	private static int PORT_ROUTE_RECEIVE = 1995;
	public static String WEBROOT = "";
	ArrayList<Group> groups;
	ArrayList<String> removed = new ArrayList<>();
	NetworkHandler networkHandler;
	SiteHandler siteHandler;
	RouteHandler routeHandler = new RouteHandler(PORT_ROUTE_RECEIVE);
	public static String logFile = "log/" + new Date().getTime();
	private File log;
	
	private static int
	
	UPDATE_TIME = 5000;
	
	public HostComponent() {
		groups = new ArrayList<>();
		networkHandler = new NetworkHandler(this);
		networkHandler.start();
		final HostComponent comp = this;
		new Thread(){
			public void run(){
				try {
					siteHandler = new SiteHandler(comp);
					siteHandler.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
		try {
			log=new File(WEBROOT + logFile);
			log.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		routeHandler.start();
	}
	
	public void run(){
		while(true){
			System.out.println("");
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			System.out.println(dateFormat.format(cal.getTime()));
			Date currentTime = new Date();
			if(groups.isEmpty()){
				System.out.println("no groups detected");
			}
			for(Group g: groups){
				System.out.println("<" + g.getGroupName().replace("$", " ") + "> " + Util.decimalToDMS(g.getLat(), true) + "  " + Util.decimalToDMS(g.getLon(),false) + " Last Update: " + ((currentTime.getTime() - g.getLastUpdateTime().getTime()) / 1000) + "s");
				siteHandler.sendCoordinates(g);
			}
			checkMessages();
			writeCoords(dateFormat.format(cal.getTime()));
			try {
				sleep(UPDATE_TIME);
			} catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	
	private void checkMessages(){
		File messageFile = new File(WEBROOT + "message.txt");
		BufferedReader reader = null;
		try {
			InputStream fis = new FileInputStream(messageFile);
			InputStreamReader ir = new InputStreamReader(fis);
			reader =  new BufferedReader(ir);
			String line = "";
			while ((line = reader.readLine()) != null){
				System.out.println(line);
				messageFromSiteReceived(line);
			}
			
		} catch (FileNotFoundException e) {
			log(e);
		} catch (IOException e) {
			log(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				log(e);
			}
		}
		PrintWriter writer;
		try {
			writer = new PrintWriter(messageFile);
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			log(e);
		} 
	}
	
	private boolean writeCoords(String time){
		//check if file exists
		File coordsFile = new File(WEBROOT + "groepjes.txt");
		File logFile =  new File(WEBROOT + "groepjes_log.txt");
		try {
			coordsFile.createNewFile();
			logFile.createNewFile();
			Files.write(Paths.get(WEBROOT + "groepjes_log.txt"), ("\n" + time + "\n").getBytes(), StandardOpenOption.APPEND);
			PrintWriter writer = new PrintWriter(coordsFile);
			writer.print("");
			writer.close();
			Date currentTime = new Date();
			for(Group g: groups){
				int timeDifference = (int) ((currentTime.getTime() - g.getLastUpdateTime().getTime()) / 1000);
				Files.write(Paths.get(WEBROOT + "groepjes.txt"), (g.getGroupName() + " " + String.valueOf((int)(g.getLat()*1000000)) + " " + String.valueOf((int)(g.getLon()*1000000)) + " " + timeDifference + "\n").getBytes(), StandardOpenOption.APPEND);
				Files.write(Paths.get(WEBROOT + "groepjes_log.txt"), (g.getGroupName() + " " + String.valueOf((g.getLat())) + " " + String.valueOf((g.getLon())) + "\n").getBytes(), StandardOpenOption.APPEND);
			}
			for(String s: removed){
				Files.write(Paths.get(WEBROOT + "groepjes.txt"), (s + " removed\n").getBytes(), StandardOpenOption.APPEND);
			}
		}catch (IOException e) {
			log(e);
			return false;
		}
		return true;
	}
	
	public static void main(String[] args){
		String webRoot = "";
		if(args.length > 0){
			webRoot = args[0];
			System.out.println("Running in " + webRoot);
			webRoot = webRoot + "/";
		}
		HostComponent.WEBROOT = webRoot;
		new HostComponent().start();
	}
	
	public void messageFromSiteReceived(String message){
		String[] name = message.split(" ", 2);
		if(name[1].equals("remove")){
			Group removeGroup = null;
			for(Group g:groups){
				if(g.getGroupName().equals(name[0])){
					removeGroup = g;
				}
			}
			if (removeGroup != null){
				groups.remove(removeGroup);
				removed.add(name[0]);
				return;
			}
		}
		for(Group g:groups){
			if(g.getGroupName().equals(name[0])){
				g.sendMessage(name[1]);
			}
		}
	}
	
	public void connectionReceived(Socket s) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintStream out = new PrintStream(s.getOutputStream());
			String groupName = in.readLine(); // Read groupname
			boolean flag = true;
			Group oldGroup = null;
			for(Group g: groups){
				if(g.getGroupName().equals(groupName)){ // group already registered
					flag = false;
					oldGroup = g;
					break;
				}
			}
			if(flag){ // new group
				removed.remove(groupName);
				Group g = new Group(groupName);
				g.setConnection(out, in);
				groups.add(g);
				
			}else{ // group already registered
				oldGroup.setConnection(out, in);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void log(Exception e){
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		try {
			Files.write(Paths.get(WEBROOT + logFile), (errors.toString()).getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
