package c9u;

import java.io.IOException;

import csf.LittleEndianDataInputStream;
import csf.LittleEndianDataOutputStream;

public class C9UString {

	private long offset;
	private String oldString;
	private String newString;
	
	public C9UString(long pOffset, String pOldString) {
		oldString = pOldString;
		offset = pOffset;
	}
	
	public String getOldString() {
		return oldString;
	}
	
	public String getNewString() {
		return newString;
	}
	
	public void setNewString(String pNewString) {
		newString = pNewString;
	}
	
	public long getOffset() {
		return offset;
	}
	
	public int write(int offDif, int fileLength, LittleEndianDataInputStream c9uReader, LittleEndianDataOutputStream c9uWriter) throws IOException {
		while((fileLength - c9uReader.available()) < offset) {
			c9uWriter.write(c9uReader.read());
		}
		
		int strSize = c9uReader.readInt();
		if (newString != null && newString != "") {
			for(int i=0; i<strSize; i++) {
				c9uReader.readChar();
			}
			
			c9uWriter.writeInt(newString.length() + 1);
			
			char[] newChar= newString.toCharArray();
			
			for(int i=0; i<newString.length(); i++) {
				c9uWriter.writeChar(newChar[i]);
			}
			c9uWriter.writeChar(0x00);
			
			offset += offDif;
			return ((newString.length() + 1) - strSize) * 2; 
			
		} else {
			c9uWriter.writeInt(strSize);
			for(int i=0; i<strSize; i++) {
				c9uWriter.writeChar(c9uReader.readChar());
			}
			offset += offDif;
			return 0;
		}
		
	}
}
