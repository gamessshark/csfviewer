package csf;

import ihm.Fenetre;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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

			csfReader = new LittleEndianDataInputStream(fileStream);
			
			fileStream.getChannel().position(0);
			
			this.parseFile();
			
		} catch (FileNotFoundException e) {
			isValid = false;
			erreurMessage = "Fichier inexistant";
		} catch (IOException e) {
			isValid = false;
			erreurMessage = "Erreur de lecture\n"+ e.getMessage();
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
		//csfReader.mark(length);
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
		//The virtualOffset is often 
		fileStream.getChannel().position((virtualStartOffset - 0x10000 < 0) ? 0 : virtualStartOffset - 0x10000);
		
		
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
		fileStream.getChannel().position(0);
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
			int ucsize = csfReader.readInt();
			int csize = csfReader.readInt();
			
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
			
			fileList.put(fileName.toLowerCase(), newZipFile);
			
		}
	}
	
	

	
	public byte[] getData(ZipFile zipToOpen) throws IOException, DataFormatException {
		byte[] data = new byte[zipToOpen.getUncompressedSize()];
		byte[] dataC = new byte[zipToOpen.getCompressedSize() - 12];
		byte[] dataZip = new byte[zipToOpen.getCompressedSize() - 12];
		byte[] encHead = new byte[12];
		
		int offsetHeader = zipToOpen.getOffset() + zipToOpen.getHeaderSize();
		
		fileStream.getChannel().position(offsetHeader);
		
		/** Step 1 Init the keys **/
		String password = "66b4427013838ceb5b275d5ba884b0ed9df353e0dc6220955e008d9d";
		
		csfReader.read(encHead);
		csfReader.read(dataC);
		
		ZipCrypto.InitCipher(password);
		
		encHead = ZipCrypto.DecryptMessage(encHead, 12);
		
		//Verification of the password
		//First with the high order byte of the crc
		//But Almost the time, it's with the high order byte of the lastModTime
		
		//System.out.println(encHead[11]);
		
		dataZip = ZipCrypto.DecryptMessage(dataC, zipToOpen.getCompressedSize() - 12);
		
		//Now we decompress the data
		Inflater inf = new Inflater(true);
		inf.setInput(dataZip);
		
		inf.inflate(data);
		inf.end();
		
		return data;
	}
	
	
	
	public HashMap<String, ZipFile>getFileList() {
		return fileList;
	}
	
}
