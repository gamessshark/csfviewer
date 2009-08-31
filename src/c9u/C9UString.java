package c9u;

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
}
