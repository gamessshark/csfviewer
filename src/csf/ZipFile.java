package csf;

import ihm.Fenetre;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.zip.Deflater;

import c9u.C9UDecoder;
import c9u.C9UString;


public class ZipFile {
	
	private int crc32;
	private int sizeCompressed;
	private int sizeUncompressed;
	private int versionExtract;
	private int versionMadeBy;
	private int bitFlag;
	private int compressionMethod;
	private int lastModTime;
	private int lastModDate;
	private int headOffset;
	private String fileName;
	private String fileComment = "";
	private int extraFieldLength;
	private int diskNumber;
	private byte[] extraField;
	
	private int internalAttributes;
	private int externalAttributes;
	
	private byte[] data = null;
	private byte[] head = null;
	
	private C9Type fileType = null;
	
	private byte[] compressedData;
	
	private boolean isModify = false;
	private boolean isSave = false;
	private int id;
	private byte[] fileNameByte;
	
	private ArrayList<C9UString> listString;
	
	public ZipFile(int id, int versionM, int version, int flag, int method, int modTime, int modDate, int crc, int sizeC, int sizeUC, int diskNum, int extraSize, byte[] byteName,String name, String fileComment, byte[] extraData, int iattr, int eattr, int offset) {
		versionMadeBy = versionM;
		versionExtract = version;
		bitFlag = flag;
		compressionMethod = method;
		lastModTime = modTime;
		lastModDate = modDate;
		crc32 = crc;
		sizeCompressed = sizeC;
		sizeUncompressed = sizeUC;
		fileName = name;
		diskNumber = diskNum;
		extraFieldLength = extraSize;
		extraField = extraData;
		internalAttributes = iattr;
		externalAttributes = eattr;
		headOffset = offset;
		fileNameByte = byteName;
		this.id = id;
	}
	
	public void setListString(ArrayList<C9UString> pListString) {
		listString = pListString;
	}
	
	public ArrayList<C9UString> getListString() {
		return listString;
	}
	
	public void rewriteC9UFile() {
		if (this.getType() == C9Type.C9U) {
			BufferedInputStream fileBuffStream = new BufferedInputStream(new ByteArrayInputStream(this.getData()));
			try {
				this.setData(C9UDecoder.rewriteFile(listString, fileBuffStream));
				fileBuffStream.close();
			} catch (IOException e) {
				Fenetre.getInstance().showErreur(e.getMessage());
			}
		}
	}
	
	public int getId() {
		return id;
	}
	
	public void calcCompressedData() {
		if (this.isSave()) {
			byte[] data = new byte[sizeUncompressed * 2];
			//Init the password
			ZipCrypto.InitCipher("66b4427013838ceb5b275d5ba884b0ed9df353e0dc6220955e008d9d");
			//Encode the header
			byte[] headerEncode = ZipCrypto.EncryptMessage(this.getHeader(), this.getHeader().length);
			
			Deflater def = new Deflater(Deflater.DEFLATED, true);
			def.setInput(this.getData());
			def.finish();
			
			int compressedSize = def.deflate(data);
						
			//Encode the data
			byte[] dataEncode = ZipCrypto.EncryptMessage(data, compressedSize);
			
			this.sizeCompressed = compressedSize + 12;
			compressedData = new byte[compressedSize + 12];
			
			for(int i=0; i<(compressedSize+12); i++) {
				if (i < 12) {
					compressedData[i] = headerEncode[i];
				} else {
					compressedData[i] = dataEncode[i - 12];
				}
			}
			
		}
	}
	
	public byte[] getCompressedData() {
		return compressedData;
	}
	
	public boolean isSave() {
		return isSave;
	}
	
	public void setSave(boolean save) {
		isSave = save;
	}
	
	public int getOffset() {
		return headOffset;
	}
	
	public void setOffset(int off) {
		headOffset = off;
	}
	
	public int getUncompressedSize() {
		return sizeUncompressed;
	}
	
	public int getCompressedSize() {
		return sizeCompressed;
	}
	
	public int getHeaderSize() {
		return 30 + fileName.length() + extraFieldLength;
	}
	
	public int getCompressionMethod() {
		return compressionMethod;
	}
	
	public int getVersion() {
		return versionExtract;
	}
	
	public int getCrc32() {
		return crc32;
	}
	
	public int getBitFlag(int lowBitNum) {
		int c = bitFlag >> lowBitNum;
		c = c & 1;
		return c;
	}
	
	public String getName() {
		return fileName;
	}
	
	public void setHeader(byte[] head) {
		this.head = head;
	}
	
	public byte[] getHeader() {
		return this.head;
	}
	
	public void setData(byte[] data) {
		this.sizeUncompressed = data.length;
		this.data = data;
		java.util.zip.CRC32 newCrc = new java.util.zip.CRC32();
		newCrc.update(data);
		this.crc32 = (int)newCrc.getValue();
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public String getTextData() {
		//Get data by default in UTF-8
		return getTextData(Charset.forName("UTF-8"));
	}
	
	public String getTextData(Charset chars) {
		return new String(data, chars);
	}
	
	public C9Type getType() {
		if (fileType == null && data != null) {
			//We look the header of the file to detect his kind
			if (data.length > 4) {
				if (data[0] == (byte)0x8F && data[1] == (byte)0xC2 && data[2] == (byte)0xF5 && data[3] == (byte)0x3C) {
					fileType = C9Type.C9U;
				}
				if (data[0] == (byte)0x44 && data[1] == (byte)0x44 && data[2] == (byte)0x53 && data[3] == (byte)0x20) {
					fileType = C9Type.DDS;
				}
				if (data[0] == (byte)0xE1 && data[1] == (byte)0x7A && data[2] == (byte)0x94 && data[3] == (byte)0x3F) {
					fileType = C9Type.C9D;
				}
			}
			
			if (fileType == null) {
				fileType = C9Type.Text;
			}
		}
		return fileType;
	}
	
	public byte[] getLocalHeader() {
		/**
		 Local file header:
	        local file header signature     4 bytes  (0x04034b50)
	        version needed to extract       2 bytes
	        general purpose bit flag        2 bytes
	        compression method              2 bytes
	        last mod file time              2 bytes
	        last mod file date              2 bytes
	        crc-32                          4 bytes
	        compressed size                 4 bytes
	        uncompressed size               4 bytes
	        file name length                2 bytes
	        extra field length              2 bytes
	
	        file name (variable size)
	        extra field (variable size)
		 */
		//File name
		byte[] localHeader = new byte[30 + fileNameByte.length + extraFieldLength];
		//Signature
		localHeader[0] = (byte)0x50;
		localHeader[1] = (byte)0x4b;
		localHeader[2] = (byte)0x03;
		localHeader[3] = (byte)0x04;
		           
		//Version
		localHeader[5] = (byte)((versionExtract >>> 8) & 0xFF);
		localHeader[4] = (byte)(versionExtract & 0xFF);
		//Bit Flag
		localHeader[7] = (byte)((bitFlag >>> 8) & 0xFF);
		localHeader[6] = (byte)(bitFlag & 0xFF);
		//Compression Method
		localHeader[9] = (byte)((compressionMethod >>> 8) & 0xFF);
		localHeader[8] = (byte)(compressionMethod & 0xFF);
		//Last mod time
		localHeader[11] = (byte)((lastModTime >>> 8) & 0xFF);
		localHeader[10] = (byte)(lastModTime & 0xFF);
		//Last mod date
		localHeader[13] = (byte)((lastModDate >>> 8) & 0xFF);
		localHeader[12] = (byte)(lastModDate & 0xFF);
		//We see if the bit flag 3 is set to hide the values :
		if (this.getBitFlag(3) != 1) {
			//Crc32
			localHeader[17] = (byte)((crc32 >>> 24) & 0xFF);
			localHeader[16] = (byte)((crc32 >>> 16) & 0xFF);
			localHeader[15] = (byte)((crc32 >>> 8) & 0xFF);
			localHeader[14] = (byte)(crc32 & 0xFF);
			//UncompressedSize
			localHeader[21] = (byte)((sizeCompressed >>> 24) & 0xFF);
			localHeader[20] = (byte)((sizeCompressed >>> 16) & 0xFF);
			localHeader[19] = (byte)((sizeCompressed >>> 8) & 0xFF);
			localHeader[18] = (byte)(sizeCompressed & 0xFF);
			//CompressedSIze
			localHeader[25] = (byte)((sizeUncompressed >>> 24) & 0xFF);
			localHeader[24] = (byte)((sizeUncompressed >>> 16) & 0xFF);
			localHeader[23] = (byte)((sizeUncompressed >>> 8) & 0xFF);
			localHeader[22] = (byte)(sizeUncompressed & 0xFF);
		} else {
			//Crc32
			localHeader[14] = (byte)0x00;
			localHeader[15] = (byte)0x00;
			localHeader[16] = (byte)0x00;
			localHeader[17] = (byte)0x00;
			//UncompressedSize
			localHeader[18] = (byte)0x00;
			localHeader[19] = (byte)0x00;
			localHeader[20] = (byte)0x00;
			localHeader[21] = (byte)0x00;
			//CompressedSIze
			localHeader[22] = (byte)0x00;
			localHeader[23] = (byte)0x00;
			localHeader[24] = (byte)0x00;
			localHeader[25] = (byte)0x00;	
		}
		//File name length
		localHeader[27] = (byte)((fileNameByte.length >>> 8) & 0xFF);
		localHeader[26] = (byte)(fileNameByte.length & 0xFF);
		//Extra field length
		localHeader[29] = (byte)((extraFieldLength >>> 8) & 0xFF);
		localHeader[28] = (byte)(extraFieldLength & 0xFF);
		
		for(int i=0; i<fileNameByte.length; i++) {
			localHeader[30 + i] = fileNameByte[i];
		}
		//Extra field
		for(int j=0; j<extraField.length; j++) {
			localHeader[30 + fileName.length() + j] = extraField[j];
		}
		return localHeader;
	}
	
	public byte[] getDataDescriptor() {
		byte[] dataDescriptor;
		if (this.getBitFlag(3) != 1) {
			dataDescriptor = new byte[0];
		} else {
			dataDescriptor = new byte[16];
			dataDescriptor[0] = (byte)0x50;
			dataDescriptor[1] = (byte)0x4b;
			dataDescriptor[2] = (byte)0x07;
			dataDescriptor[3] = (byte)0x08;
			//Crc32
			dataDescriptor[7] = (byte)((crc32 >>> 24) & 0xFF);
			dataDescriptor[6] = (byte)((crc32 >>> 16) & 0xFF);
			dataDescriptor[5] = (byte)((crc32 >>> 8) & 0xFF);
			dataDescriptor[4] = (byte)(crc32 & 0xFF);
			//CompressedSize
			dataDescriptor[11] = (byte)((sizeCompressed >>> 24) & 0xFF);
			dataDescriptor[10] = (byte)((sizeCompressed >>> 16) & 0xFF);
			dataDescriptor[9] = (byte)((sizeCompressed >>> 8) & 0xFF);
			dataDescriptor[8] = (byte)(sizeCompressed & 0xFF);
			//UncompressedSIze
			dataDescriptor[15] = (byte)((sizeUncompressed >>> 24) & 0xFF);
			dataDescriptor[14] = (byte)((sizeUncompressed >>> 16) & 0xFF);
			dataDescriptor[13] = (byte)((sizeUncompressed >>> 8) & 0xFF);
			dataDescriptor[12] = (byte)(sizeUncompressed & 0xFF);
		}
		
		return dataDescriptor;
	}
	
	public byte[] getFileHeader() {
		byte[] fileHeader = new byte[46 + fileNameByte.length + extraFieldLength + fileComment.length()];
		
		//Signature
		fileHeader[0] = (byte)0x50;
		fileHeader[1] = (byte)0x4b;
		fileHeader[2] = (byte)0x01;
		fileHeader[3] = (byte)0x02;
		//Version made by
		fileHeader[5] = (byte)((versionMadeBy >>> 8) & 0xFF);
		fileHeader[4] = (byte)(versionMadeBy & 0xFF);
		//Version
		fileHeader[7] = (byte)((versionExtract >>> 8) & 0xFF);
		fileHeader[6] = (byte)(versionExtract & 0xFF);
		//Bit Flag
		fileHeader[9] = (byte)((bitFlag >>> 8) & 0xFF);
		fileHeader[8] = (byte)(bitFlag & 0xFF);
		//Compression Method
		fileHeader[11] = (byte)((compressionMethod >>> 8) & 0xFF);
		fileHeader[10] = (byte)(compressionMethod & 0xFF);
		
		//Last mod time
		fileHeader[13] = (byte)((lastModTime >>> 8) & 0xFF);
		fileHeader[12] = (byte)(lastModTime & 0xFF);
		//Last mod date
		fileHeader[15] = (byte)((lastModDate >>> 8) & 0xFF);
		fileHeader[14] = (byte)(lastModDate & 0xFF);
		//Crc32
		fileHeader[19] = (byte)((crc32 >>> 24) & 0xFF);
		fileHeader[18] = (byte)((crc32 >>> 16) & 0xFF);
		fileHeader[17] = (byte)((crc32 >>> 8) & 0xFF);
		fileHeader[16] = (byte)(crc32 & 0xFF);
		//UncompressedSize
		fileHeader[23] = (byte)((sizeCompressed >>> 24) & 0xFF);
		fileHeader[22] = (byte)((sizeCompressed >>> 16) & 0xFF);
		fileHeader[21] = (byte)((sizeCompressed >>> 8) & 0xFF);
		fileHeader[20] = (byte)(sizeCompressed & 0xFF);
		//CompressedSIze
		fileHeader[27] = (byte)((sizeUncompressed >>> 24) & 0xFF);
		fileHeader[26] = (byte)((sizeUncompressed >>> 16) & 0xFF);
		fileHeader[25] = (byte)((sizeUncompressed >>> 8) & 0xFF);
		fileHeader[24] = (byte)(sizeUncompressed & 0xFF);
		//File name length
		fileHeader[29] = (byte)((fileNameByte.length >>> 8) & 0xFF);
		fileHeader[28] = (byte)(fileNameByte.length & 0xFF);
		//Extra field length
		fileHeader[31] = (byte)((extraFieldLength >>> 8) & 0xFF);
		fileHeader[30] = (byte)(extraFieldLength & 0xFF);
		//File comment length
		fileHeader[33] = (byte)((fileComment.length() >>> 8) & 0xFF);
		fileHeader[32] = (byte)(fileComment.length() & 0xFF);
		//Disk number start
		fileHeader[35] = (byte)((diskNumber >>> 8) & 0xFF);
		fileHeader[34] = (byte)(diskNumber & 0xFF);
		//Internal attributes
		fileHeader[37] = (byte)((internalAttributes >>> 8) & 0xFF);
		fileHeader[36] = (byte)(internalAttributes & 0xFF);
		//External attributes
		fileHeader[41] = (byte)((externalAttributes >>> 24) & 0xFF);
		fileHeader[40] = (byte)((externalAttributes >>> 16) & 0xFF);
		fileHeader[39] = (byte)((externalAttributes >>> 8) & 0xFF);
		fileHeader[38] = (byte)(externalAttributes & 0xFF);
		//Offset
		fileHeader[45] = (byte)((headOffset >>> 24) & 0xFF);
		fileHeader[44] = (byte)((headOffset >>> 16) & 0xFF);
		fileHeader[43] = (byte)((headOffset >>> 8) & 0xFF);
		fileHeader[42] = (byte)(headOffset & 0xFF);
		
		//File name
		for(int i=0; i<fileNameByte.length; i++) {
			fileHeader[46 + i] = fileNameByte[i];
		}
		//Extra field
		for(int j=0; j<extraField.length; j++) {
			fileHeader[46 + fileNameByte.length + j] = extraField[j];
		}
		byte[] commentByte = fileComment.getBytes();
		//Comment
		for(int i=0; i<commentByte.length; i++) {
			fileHeader[46 + fileNameByte.length + extraFieldLength + i] = commentByte[i];
		}
		
		return fileHeader;
	}
	
	public boolean isModify() {
		return isModify;
	}
	
	public void setModify(boolean mod) {
		isModify = mod;
	}
	
}
