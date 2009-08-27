package csf;

public class ZipFile {

	private int crc32;
	private int sizeCompressed;
	private int sizeUncompressed;
	private short versionExtract;
	private short bitFlag;
	private short compressionMethod;
	private short lastModTime;
	private short lastModDate;
	private int headOffset;
	private String fileName;
	private String fileComment;
	private int extraFieldLength;
	private short diskNumber;
	private byte[] extraField;
	
	private short internalAttributes;
	private int externalAttributes;
	
	private byte[] compressedData;
	private byte[] uncompressedData;
	
	public ZipFile(short version, short flag, short method, short modTime, short modDate, int crc, int sizeC, int sizeUC, short diskNum, short extraSize, String name, String fileComment, byte[] extraData, short iattr, int eattr, int offset) {
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
	}
	
	public int getOffset() {
		return headOffset;
	}
	
	public int getUncompressedSize() {
		return sizeCompressed;
	}
	
	public int getCompressedSize() {
		return sizeUncompressed;
	}
	
	public int getHeaderSize() {
		return 30 + fileName.length() + extraFieldLength;
	}
	
	public int getCompressionMethod() {
		return compressionMethod;
	}
	
	public void setCompressedData(byte[] data) {
		
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
	
	public void extract() {
		
	}
	
	public void save(byte[] data) {
		
	}
	
	public String getName() {
		return fileName;
	}
	
}
