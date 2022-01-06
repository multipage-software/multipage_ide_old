/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 01-01-2022
 *
 */
package org.multipage.gui;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectInputValidation;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.input.BOMInputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Input stream of objects which describe application settings.
 * @author vakol
 *
 */
public class StateInputStreamImpl implements StateInputStream {
	
	/**
	 * Main object of the XStream library.
	 */
	private XStream xStream = null;
	
	/**
	 * Name of file used by this input stream.
	 */
	private String fileName = null;
	
	/**
	 * A reference to a input file stream.
	 */
	private FileInputStream fileInputStream = null;
	
	/**
	 * A reference to BOM (byte order mark for UTF-8 encoding) input stream that extends the above file stream.
	 */
	private BOMInputStream bufferedBomInputStream = null;

	/**
	 * Input stream created with the XStream library which extends the BOM input stream and that can provide
	 * objects with application settings.
	 */
	private ObjectInputStream objectInputStream = null;
	
	/**
	 * Create a new input stream.
	 * @param rootNodeName
	 */
	public static StateInputStreamImpl newXStreamInstance(String fileName) 
			throws Exception {
		
		// Create stream and initialize XML driver.
		StateInputStreamImpl thisObject = new StateInputStreamImpl();
		thisObject.xStream = new XStream(new DomDriver());
		
		thisObject.fileName = fileName;
		thisObject.fileInputStream = new FileInputStream(fileName);
		thisObject.bufferedBomInputStream = new BOMInputStream(new BufferedInputStream(thisObject.fileInputStream));
		thisObject.objectInputStream = thisObject.xStream.createObjectInputStream(thisObject.bufferedBomInputStream);
		
		return thisObject;
	}
	
	/**
	 * Return the XStream object.
	 * @return
	 */
	public XStream getXStream() {
		
		return xStream;
	}
	
	/**
	 * Get raw input stream, but not the extended object stream.
	 * @return
	 */
	public BOMInputStream getInputStream() {
		
		return bufferedBomInputStream;
	}
	
	/**
	 * Return current file name.
	 * @return
	 */
	public String getFileName() {
		
		return fileName;
	}
	
	/**
	 * Load character set of the input stream. It is placed as a BOM mark at the beginning
	 * of the input file.
	 * @return
	 */
	public Charset loadCharset() {
		
		Charset charset = null;
		
		// Check the input stream reference.
		if (bufferedBomInputStream != null) {
			
			// Try to load character set from the input stream.
			try {
				
				// Herein the BOM can be with maximum of 8 characters. If BOM is not included
				// in the input file, you can than reset the stream to following mark at zero
				// position of the input stream.
				bufferedBomInputStream.mark(8);
				
				boolean hasBom = bufferedBomInputStream.hasBOM();
				if (hasBom) {
					
					// Convert the BOM mark to appropriate character set name.
					String charsetName = bufferedBomInputStream.getBOMCharsetName();
					if (charsetName != null) {
						charset = Charset.forName(charsetName);
					}
				}
				else {
					// If the input file has no BOM mark, go back to file beginning.
					bufferedBomInputStream.reset();
				}
			}
			catch (Exception e) {
			}
		}
		
		// If the character set name was not loaded, use a default character set of UTF-8.
		if (charset == null) {
			charset = StandardCharsets.UTF_8;
		}
		
		return charset;
	}

	/**
	 * Close the input stream.
	 */
	public void close() throws IOException {
		
		if (bufferedBomInputStream != null) {
			try {
				bufferedBomInputStream.close();
			}
			catch (Exception e) {
			}
		}
	}
	
	/**
	 * Delegate overridden interface method call to the XStream call.
	 * @throws ClassNotFoundException 
	 */
	public Object readUnshared() throws IOException, ClassNotFoundException {
		
		return this.objectInputStream.readUnshared();
	}
	
	/**
	 * Delegate overridden interface method call to the XStream call.
	 */
	public int read() throws IOException {
		
		return this.objectInputStream.read();
	}
	
	/**
	 * Delegate overridden interface method call to the XStream call.
	 */
	public int read(byte[] buf, int off, int len) throws IOException {
		
		return this.objectInputStream.read(buf, off, len);
	}
	
	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public boolean readBoolean() throws IOException {
		
		return this.objectInputStream.readBoolean();
	}
	
	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public byte readByte() throws IOException {

		return this.objectInputStream.readByte();
	}
	
	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public int readUnsignedByte() throws IOException {

		return this.objectInputStream.readUnsignedByte();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public char readChar() throws IOException {

		return this.objectInputStream.readChar();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public short readShort() throws IOException {

		return this.objectInputStream.readShort();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public int readUnsignedShort() throws IOException {

		return this.objectInputStream.readUnsignedShort();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public int readInt() throws IOException {

		return this.objectInputStream.readInt();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public long readLong() throws IOException {

		return this.objectInputStream.readLong();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public float readFloat() throws IOException {

		return this.objectInputStream.readFloat();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public double readDouble() throws IOException {

		return this.objectInputStream.readDouble();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public void readFully(byte[] buf) throws IOException {

		this.objectInputStream.readFully(buf);
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public void readFully(byte[] buf, int off, int len) throws IOException {

		this.objectInputStream.readFully(buf, off, len);
	}
	
	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	@SuppressWarnings("deprecation")
	public String readLine() throws IOException {

		return this.objectInputStream.readLine();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	public String readUTF() throws IOException {

		return this.objectInputStream.readUTF();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 * @throws ClassNotFoundException 
	 */
	public Object readObject() throws IOException, ClassNotFoundException {
		
		return this.objectInputStream.readObject();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	@Override
	public void defaultReadObject() throws IOException, ClassNotFoundException {
		
		this.objectInputStream.defaultReadObject();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	@Override
	public GetField readFields() throws IOException, ClassNotFoundException {
		
		return this.objectInputStream.readFields();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	@Override
	public void registerValidation(ObjectInputValidation obj, int prio)
			throws NotActiveException, InvalidObjectException {
		
		this.objectInputStream.registerValidation(obj, prio);
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	@Override
	public int available() throws IOException {
		
		return this.objectInputStream.available();
	}

	/**
	 * Delegate overridden interface method call to the XStream call.+
	 */
	@Override
	public int skipBytes(int len) throws IOException {
		
		return this.objectInputStream.skipBytes(len);
	}
}
