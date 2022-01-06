/*
 * Copyright 2010-2021 (C) vakol
 * 
 * Created on : 28-12-2021
 *
 */
package org.multipage.gui;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectInputValidation;
import java.nio.charset.Charset;

import org.apache.commons.io.input.BOMInputStream;

/**
 * Input stream interface that provides objects with application states.
 * The XStream implementation uses the XML format to read object fields.
 * @author vakol
 *
 */
public interface StateInputStream {

	/**
	 * Get raw input stream with BOM detection support.
	 * @return
	 */
	public BOMInputStream getInputStream();
	
	/**
	 * Return file name of the opened input stream.
	 * @return
	 */
	public String getFileName();
	
	/**
	 * Load name of a character set used by this input stream. It is define with a BOM mark at the beginning
	 * of the input file.
	 * @return
	 */
	public Charset loadCharset();

	/**
	 * Close this input stream.
	 */
	public void close() throws IOException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public Object readUnshared()  throws IOException, ClassNotFoundException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public int read() throws IOException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public int read(byte[] buf, int off, int len) throws IOException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public boolean readBoolean() throws IOException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public byte readByte() throws IOException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public int readUnsignedByte() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public char readChar() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public short readShort() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public int readUnsignedShort() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public int readInt() throws IOException;
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public long readLong() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public float readFloat() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public double readDouble() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public void readFully(byte[] buf) throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public void readFully(byte[] buf, int off, int len) throws IOException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public String readLine() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public String readUTF() throws IOException;
	
	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public Object readObject() throws IOException, ClassNotFoundException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public void defaultReadObject() throws IOException, ClassNotFoundException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public GetField readFields() throws IOException, ClassNotFoundException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public void registerValidation(ObjectInputValidation obj, int prio)
			throws NotActiveException, InvalidObjectException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public int available() throws IOException;

	/**
	 * Method signature taken from ObjectInputStream definition.
	 */
	public int skipBytes(int len) throws IOException;
}
