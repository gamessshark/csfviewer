package c9u;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import csf.LittleEndianDataInputStream;
import csf.LittleEndianDataOutputStream;

public class C9UDecoder {
	
	
	public static ArrayList<C9UString> parse(BufferedInputStream inStream) throws IOException {
		LittleEndianDataInputStream c9uReader = new LittleEndianDataInputStream(inStream);
		ArrayList<C9UString> listString = new ArrayList<C9UString>();
		boolean foundFont = false;
		int stringLength;
		char[] textByte;
		
		int fileLength = inStream.available();
		
		while (inStream.available() > 0) {
			inStream.mark(inStream.available());
			//On detecte les font
			if (detectFont("BaseFont", c9uReader)) {
				foundFont = true;
			} else {
				inStream.reset();
				if (detectFont("BaseFontS12", c9uReader)) {
					foundFont = true;
				} else {
					inStream.reset();
					if (detectFont("BaseFontS14", c9uReader)) {
						foundFont = true;
					} else {
						inStream.reset();
						if (detectFont("BaseFont_npc", c9uReader)) {
							foundFont = true;
						} else {
							inStream.reset();
							if (detectFont("BaseFont_out", c9uReader)) {
								foundFont = true;
							} else {
								inStream.reset();
								if (detectFont("BaseFont_Value11", c9uReader)) {
									foundFont = true;
								} else {
									inStream.reset();
									if (detectFont("BaseFontS16", c9uReader)) {
										foundFont = true;
									} else {
										inStream.reset();
										if (detectFont("BaseFontS20", c9uReader)) {
											foundFont = true;
										} else {
											inStream.reset();
											if (detectFont("BaseFontS32", c9uReader)) {
												foundFont = true;
											} else {
												inStream.reset();
												if (detectFont("BaseFont_S8", c9uReader)) {
													foundFont = true;
												} else {
													inStream.reset();
													if (detectFont("NameFont", c9uReader)) {
														foundFont = true;
													} else {
														inStream.reset();
														if (detectFont("NameFont_Value", c9uReader)) {
															foundFont = true;
														} else {
															inStream.reset();
															if (detectFont("NameFont_S14", c9uReader)) {
																foundFont = true;
															} else {
																inStream.reset();
																if (detectFont("ChatBallonFont", c9uReader)) {
																	foundFont = true;
																} else {
																	inStream.reset();
																	if (detectFont("BaseFontS09", c9uReader)) {
																		foundFont = true;
																	} else {
																		inStream.reset();
																		inStream.read();
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			if (foundFont) {
				int currOffset = fileLength - inStream.available();
				stringLength = c9uReader.readInt();
				if (stringLength > 0 && stringLength < 1024) {
					textByte = new char[stringLength - 1];
					for (int i=0; i<stringLength - 1; i++) {
						textByte[i] = c9uReader.readChar();
					}
					C9UString str = new C9UString(currOffset, new String(textByte));
					listString.add(str);
					c9uReader.readShort();
				}
			}
			foundFont = false;
		}
		
		c9uReader.close();
		return listString;
		
	}
	
	public static byte[] rewriteFile(ArrayList<C9UString> listString, BufferedInputStream inStream) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		LittleEndianDataInputStream c9uReader = new LittleEndianDataInputStream(inStream);
		LittleEndianDataOutputStream c9uWriter = new LittleEndianDataOutputStream(byteStream);
		int fileLength = inStream.available();
		
		Iterator<C9UString> iterStr = listString.iterator();
		
		int diff = 0;
		while (iterStr.hasNext()) {
			diff += iterStr.next().write(diff, fileLength, c9uReader, c9uWriter);
		}
		
		//On oublie pas d'ecrire la fin du fichier
		while(c9uReader.available() > 0) {
			c9uWriter.write(c9uReader.read());
		}
		
		return byteStream.toByteArray();
	}
	
	public static boolean detectFont(String fontName, LittleEndianDataInputStream c9uReader) throws IOException {
		int sizeName = fontName.length() + 1;
		
		int byteRead = c9uReader.read();
		
		if (byteRead == sizeName) {
			int byte1 = c9uReader.read();
			int byte2 = c9uReader.read();
			int byte3 = c9uReader.read();
			if (byte1 == 0x00 && byte2 ==  0x00 && byte3 == 0x00) {
				byte[] textByte = new byte[sizeName - 1];
				c9uReader.read(textByte);
				if (new String(textByte).equals(fontName)) {
					c9uReader.read();
					return true;
				}
			}
		}
		
		return false;
	}
}
