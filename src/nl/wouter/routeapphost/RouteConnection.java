package nl.wouter.routeapphost;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RouteConnection extends Thread{
	private Socket socket;
	
	public RouteConnection(Socket socket){
		this.socket = socket;
	}
	
	public void run(){
		FileOutputStream fos = null;
		try{
			byte[] buffer = new byte[1024];
			int count;
			DataInputStream in = new DataInputStream(socket.getInputStream());
			int nameLength = in.readInt();
			String fileName = "";
			for(int i = 0; i < nameLength; i++){
				fileName = fileName + in.readChar();
			}
			long size = in.readLong();
			
			File file = new File("/data/" + fileName);
			fos = new FileOutputStream(file);
			while (size > 0 && (count = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)
			{
			    fos.write(buffer, 0, count);
			    size -= count;
			    System.out.println(size);
			}
			
			System.out.println("Out of loop");
			fos.close();
			OutputStream out = socket.getOutputStream();
			System.out.println("writing hash");
			DataOutputStream dataOut = new DataOutputStream(out);
			try {
				dataOut.writeInt(byteArrayToInt(md5(file)));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			System.out.println("written hash");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			System.out.println("Not enough permissions to write file");
		} finally {
			try {
				fos.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static int byteArrayToInt(byte[] b){
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	private byte[] md5(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance("MD5");
		try (InputStream is = new FileInputStream(file)) {
			//DigestInputStream dis = new DigestInputStream(is, md);
		}
		return md.digest();
	}

}
