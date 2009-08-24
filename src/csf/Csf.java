package csf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

public class Csf {

	private boolean isValid = true;
	private boolean isModify = false;
	
	private FileInputStream fileStream;
	private LittleEndianDataInputStream csfReader; 
	private int length;
	private long fileDesc;
	private int virtualStartOffset;
	private int realStartOffset;
	private String erreurMessage;
	
	private HashMap<String, ZipFile> fileList;
	
	public Csf(String filePath) {
		try {
			fileList = new HashMap<String, ZipFile>();
			
			fileStream = new FileInputStream(filePath);
			
			csfReader = new LittleEndianDataInputStream(new BufferedInputStream(fileStream));
			
			this.parseFile();
			
		} catch (FileNotFoundException e) {
			isValid = false;
			erreurMessage = "Fichier inexistant";
		} catch (IOException e) {
			isValid = false;
			erreurMessage = "Erreur de lecture";
		}
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public boolean isModify() {
		return isModify;
	}
	
	public String getErreurMessage() {
		return erreurMessage;
	}
	
	public void close() {
		try {
			if (fileStream != null) {
				fileStream.close();
			}
		} catch (IOException e) {
			isValid = true;
			erreurMessage = "Impossible de fermer le fichier\n" + e.getMessage();
		}
	}
	
	private void parseFile() throws IOException {
		//En premier lieu on verifie que le fichiest est un bien un csf
		byte[] endHeader = new byte[22];
		byte testByte;
		boolean foundStruct = false;
		
		int sizeHeader;
		
		
		length = csfReader.available();
		//We mark the stream
		csfReader.mark(length);
		csfReader.skipBytes(length - 22);
		
		sizeHeader = csfReader.read(endHeader);
		
		if (sizeHeader != 22) {
			isValid = false;
			erreurMessage = "This is not a csf file, length = "+sizeHeader;
			return;
		}
		
		LittleEndianDataInputStream headerReader = new LittleEndianDataInputStream(new ByteArrayInputStream(endHeader));
		if (headerReader.readInt() != (int)0x06054b50) {
			isValid = false;
			
			erreurMessage = "This is not a csf file, false header";
			return;
		}
		
		if (headerReader.readInt() != (int)0x54235423) {
			isValid = false;
			erreurMessage = "This is not a csf file, false signature";
			return;
		}
		
		fileDesc = headerReader.readLong();
		
		virtualStartOffset = headerReader.readInt();
		
		if (headerReader.readUnsignedShort() != (short)0x5423) {
			isValid = false;
			erreurMessage = "This is not a csf file, false end signature";
			return;
		}
		
		//Once we are here we search the tree structure first definition
		csfReader.reset();
		
		for(int i=0; i<length; i++) {
			testByte = csfReader.readByte();
			if (testByte == (byte)0x50) {
				testByte = csfReader.readByte();
				i++;
				if (testByte == (byte)0x4b) {
					testByte = csfReader.readByte();
					i++;
					if (testByte == (byte)0x01) {
						testByte = csfReader.readByte();
						i++;
						if (testByte == (byte)0x02) {
							//We are a the first definition of a file
							foundStruct = true;
							break;
						}
					}
				}
			}
		}
				
		if (!foundStruct) {
			isValid = false;
			erreurMessage = "This is not a csf file";
			return;
		}
		
		//On définit le vrai offset
		realStartOffset = length - (csfReader.available() + 4);
		
		//On se replace au debut
		csfReader.reset();
		csfReader.skipBytes(realStartOffset);
		
		//Read the structural header
		while(csfReader.readInt() == (int)0x02014b50) {
			
			csfReader.readShort();
			short version = csfReader.readShort();
			short flag = csfReader.readShort();
			short method = csfReader.readShort();
			short modTime = csfReader.readShort();
			short modDate = csfReader.readShort();
			
			int crc = csfReader.readInt();
			int csize = csfReader.readInt();
			int ucsize = csfReader.readInt();
			
			short nameSize = csfReader.readShort();
			short extraSize = csfReader.readShort();
			short commentSize = csfReader.readShort();
			
			short disk = csfReader.readShort();
			short iattr = csfReader.readShort();
			int eattr = csfReader.readInt();
			int offset = csfReader.readInt();
			
			byte[] byteFileName = new byte[nameSize];
			csfReader.read(byteFileName);
			String fileName = new String(byteFileName, Charset.forName("UTF-8"));
			byte[] byteExtra = new byte[extraSize];
			csfReader.read(byteExtra);
			byte[] byteComment = new byte[commentSize];
			csfReader.read(byteComment);
			String comment = new String(byteComment, Charset.forName("UTF-8"));
			
			ZipFile newZipFile = new ZipFile(version, flag, method, modTime, modDate, crc, csize, ucsize, disk, extraSize, fileName, comment, byteExtra, iattr, eattr, offset);
			
			fileList.put(fileName, newZipFile);
		}
	}
	
	public HashMap<String, ZipFile>getFileList() {
		return fileList;
	}
	
}
