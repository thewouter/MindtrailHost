package nl.wouter.routeapphost;

import java.nio.ByteBuffer;

public class Util {
	
	public static double LON = 0;
	public static double LAT = 0;

	public static String decimalToDMS(double coord, boolean lattitude) {
		boolean flag = sign(coord) == 1;
		String prefix = "";
		if(flag && lattitude){
			prefix = "N ";
		}else if(flag && !lattitude){
			prefix = "E ";
		}else if(!flag && !lattitude){
			prefix = "W ";
		}else if(!flag && lattitude){
			prefix = "S ";
		}
		if(!flag){
			coord = coord * -1;
		}
	    String output, degrees, minutes, seconds;
	    double mod = coord % 1;
	    int intPart = (int)coord;
	    degrees = String.valueOf(intPart);
	    coord = mod * 60;
	    mod = coord % 1;
	    intPart = (int)coord;
	    minutes = String.valueOf(intPart);
	    coord = mod * 60;
	    intPart = (int)coord;
	    seconds = String.valueOf(intPart);
	    output = degrees + "°" + minutes + "\'" + seconds + "\"";
	    return prefix + output;
	}
	
	public static int sign(double f) {
	    if (f != f) throw new IllegalArgumentException("NaN");
	    if (f == 0) return 0;
	    if (f > 0) return +1;
	    else return -1;
	}
	
	public static byte[] intToBytes( final int i ) {
	    ByteBuffer bb = ByteBuffer.allocate(4); 
	    bb.putInt(i); 
	    return bb.array();
	}
	
	public static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

}
