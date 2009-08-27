package utils;

public class Hexa {

	public static String toString(int num) {
		return Integer.toHexString(num);
	}
	
	public static String toString(long num) {
		return Long.toHexString(num);
	}
	
	public static String toString(short num) {		
		return Integer.toHexString(toInt(num));
		
	}
	
	public static String toString(byte num) {
		return Integer.toHexString(toInt(num));
	}
	
    
    public static int toInt(byte b) {
    	return (b & 0xFF);
    }
    
    public static int toInt(short b) {
    	return (b & 0xFFFF);
    }
    
    public static int toInt(long b) {
    	return (int)(b & 0x00000000FFFFFFFF);
    }
    
    public static long toLong(int b) {
    	return (b & 0xFFFFFFFF);
    }
}
