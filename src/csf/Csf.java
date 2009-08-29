package csf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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
	private String filePath;
	
	private HashMap<String, ZipFile> fileList;
	
	public Csf(String pFilePath) {
		try {
			filePath = pFilePath;
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
	
	public String getFilePath() {
		return filePath;
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public boolean isModify() {
		return isModify;
	}
	
	public void setModify(boolean mod) {
		isModify = mod;
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
		
		int id = 0;
		
		//Read the structural header
		while(csfReader.readInt() == (int)0x02014b50) {
			
			int versionMade = csfReader.readUnsignedShort();
			int version = csfReader.readUnsignedShort();
			int flag = csfReader.readUnsignedShort();
			int method = csfReader.readUnsignedShort();
			int modTime = csfReader.readChar();
			int modDate = csfReader.readChar();
			
			int crc = csfReader.readInt();
			int csize = csfReader.readInt();
			int ucsize = csfReader.readInt();
			
			int nameSize = csfReader.readUnsignedShort();
			int extraSize = csfReader.readUnsignedShort();
			int commentSize = csfReader.readUnsignedShort();
			
			int disk = csfReader.readUnsignedShort();
			int iattr = csfReader.readUnsignedShort();
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
			
			ZipFile newZipFile = new ZipFile(id, versionMade, version, flag, method, modTime, modDate, crc, csize, ucsize, disk, extraSize, fileName, comment, byteExtra, iattr, eattr, offset);
			id++;
			fileList.put(fileName.toLowerCase(), newZipFile);
			
		}
	}
	
	

	
	public void setData(ZipFile zipToOpen) throws IOException, DataFormatException {
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
		
		//TODO
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
		
		zipToOpen.setHeader(encHead);
		zipToOpen.setData(data);
	}
	
	public Iterator<ZipFile> sortFileListByOffset() {
		ArrayList<ZipFile> list = new ArrayList<ZipFile>(fileList.values());
		ZipFile temp;
		for (int i = 0; i < list.size(); i++) {
			int min = i;
			for( int j = i ; j < list.size(); j++) {
				if( list.get(min).getId() > list.get(j).getId()) {
					min = j;
				}
				temp = list.get(i);
				list.set(i, list.get(min));
				list.set(min, temp);
			}
		}
		
		return list.iterator();
	}
	
	public void save(File newFile) {
			//Sinon on recréer totalement l'archive
			File tempFile = new File(newFile.getPath()+"_temp");
			
			try {
				
				tempFile.createNewFile();
				FileOutputStream outputStream = new FileOutputStream(tempFile);
				LittleEndianDataOutputStream csfWriter = new LittleEndianDataOutputStream(outputStream);
				
				Iterator<ZipFile> listZipFile = sortFileListByOffset();
				
				//For each file
				while(listZipFile.hasNext()) {
					ZipFile fileToWrite = listZipFile.next();
					
					fileToWrite.calcCompressedData();
					
					long fileOffset = outputStream.getChannel().position();
					
					csfWriter.write(fileToWrite.getLocalHeader());
					
					if (fileToWrite.isSave()) {
						csfWriter.write(fileToWrite.getCompressedData(), 0, fileToWrite.getCompressedSize());
					} else {
						//Searching the data
						fileStream.getChannel().position(fileToWrite.getOffset() + fileToWrite.getHeaderSize());
						byte[] data = new byte[fileToWrite.getCompressedSize()];
						csfReader.read(data);
						csfWriter.write(data);
					}
					csfWriter.write(fileToWrite.getDataDescriptor());
					
					fileToWrite.setOffset((int)fileOffset);
				}
				
				long newRealOffset = outputStream.getChannel().position();
				
				listZipFile = sortFileListByOffset();
				
				while(listZipFile.hasNext()) {
					csfWriter.write(listZipFile.next().getFileHeader());
				}
				
				csfWriter.writeByte(0x50);
				csfWriter.writeByte(0x4b);
				csfWriter.writeByte(0x05);
				csfWriter.writeByte(0x06);
				
				csfWriter.writeInt(0x54235423);
				csfWriter.writeLong(fileDesc);
				
				long newVirtualOffset = (virtualStartOffset - realStartOffset) + newRealOffset;
				
				csfWriter.writeInt((int)newVirtualOffset);
				
				csfWriter.writeShort((int)0x5423);
				
				csfWriter.close();
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
	}
	
	
	
	public HashMap<String, ZipFile>getFileList() {
		return fileList;
	}
	
}
