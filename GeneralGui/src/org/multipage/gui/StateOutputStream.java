/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 29-12-2021
 *
 */
package org.multipage.gui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream.PutField;

import com.thoughtworks.xstream.XStream;

/**
 * @author vacla
 *
 */
public interface StateOutputStream {
	
	/**
	 * Returns the file name used.
	 * @return
	 */
	public String getFileName();
	
	/**
	 * Get the output stream.
	 * @return
	 */
	public FileOutputStream getRawOutputStream();
	
	/**
	 * Set alias used in XML marks for some class.
	 * @param alias
	 * @param classObject
	 */
	public void setXmlObjectAlias(String alias, Class<?> classObject);
	
	/**
	 * Get the main XStream library object.
	 * @return
	 */
	public XStream getXStream();
	
	/**
	 * Close the output stream.
	 */
	public void close() throws IOException;
	
	/**
	 * Write BOM mark into the output stream.
	 * @throws IOException 
	 */
	public void writeBom() throws IOException;
	
	/**
	 * Delegate the method call.
	 * @param version 
	 */
	public void useProtocolVersion(int version) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeUnshared(Object obj) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void defaultWriteObject() throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public PutField putFields() throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeFields() throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void reset() throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void write(int val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void write(byte[] buf) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void write(byte[] buf, int off, int len) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void flush() throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeBoolean(boolean val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeByte(int val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeShort(int val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeChar(int val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeInt(int val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeLong(long val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeFloat(float val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeDouble(double val) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeBytes(String str) throws IOException;
	
	/**
	 * Delegate the method call.
	 */
	public void writeChars(String str) throws IOException;
	
	/**
	 * Delegate the method call. 
	 */
	public void writeUTF(String str) throws IOException;

	/**
	 * Delegate the method call.
	 */
	public void writeObject(Object object) throws IOException;
}