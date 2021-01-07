/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package com.maclan;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * @author
 *
 */
public class ColorObj extends Color implements Cloneable {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Enable alpha channel flag.
	 */
	private static final boolean isAlphaChannel = false;

	/**
	 * Constructor.
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public ColorObj(int red, int green, int blue, int alpha) {
		
		super(red, green, blue, alpha);
	}

	/**
	 * Constructor.
	 * @param color
	 */
	public ColorObj(Color color) {
		
		super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	/**
	 * Get color text.
	 * @return
	 */
	public String getText() {
		
		if (isAlphaChannel) {
			return String.format("#%02X%02X%02X%02X", getAlpha(), getRed(), getGreen(), getBlue());
		}
		else {
			return String.format("#%02X%02X%02X", getRed(), getGreen(), getBlue());
		}
	}
	
	/**
	 * Get color text. (RGB decimal.)
	 * @return
	 */
	public String getTextRgbDecimal() {
		
		if (isAlphaChannel) {
			return String.format("%d,%d,%d,%d", getAlpha(), getRed(), getGreen(), getBlue());
		}
		else {
			return String.format("%d,%d,%d", getRed(), getGreen(), getBlue());
		}
	}

	/**
	 * Get long value.
	 * @return
	 */
	public long getLong() {
		
		long alphaLong = isAlphaChannel ? getAlpha() : 0xFF;
		long redLong = getRed();
		long greenLong = getGreen();
		long blueLong = getBlue();
		
		long colorLong = (alphaLong << 24) | (redLong << 16) | (greenLong << 8) | blueLong;
		return colorLong;
	}

	/**
	 * Get double value.
	 * @return
	 */
	public double getDouble() {
		
		return ((Long) getLong()).doubleValue();
	}

	/**
	 * Parse text.
	 * @param text
	 * @return
	 */
	public static ColorObj parse(String text) {
		
		if (text == null) {
			return new ColorObj(BLACK);
		}
		try {
			// Get color by hex or octal value.
			text = "0x" + text.substring(3);
			Color color = decode(text);
			return new ColorObj(color);
		}
		catch (Exception nfe) {
			// If we can't decode lets try to get it by name.
			try {
				// Try to get a color by name using reflection.
				final Field field = Color.class.getField(text);

				return new ColorObj((Color) field.get(null));
			}
			catch (Exception ce) {
				// If we can't get any color return black.
				return new ColorObj(BLACK);
			}
		}
	}

	/**
	 * Convert long value to color.
	 * @param value
	 * @return
	 */
	public static ColorObj convertLong(long value) {
		
		int blue = (int) (value & 0xFF);
		int green = (int) ((value >> 8) & 0xFF);
		int red = (int) ((value >> 16) & 0xFF);
		int alpha = (int) ((value >> 24) & 0xFF);
		
		return new ColorObj(red, green, blue, alpha);
	}

	/**
	 * Convert long value to color.
	 * @param value
	 * @return
	 */
	public static ColorObj convertLongNoAlpha(long value) {
		
		int blue = (int) (value & 0xFF);
		int green = (int) ((value >> 8) & 0xFF);
		int red = (int) ((value >> 16) & 0xFF);
		
		return new ColorObj(red, green, blue, 255);
	}

	/**
	 * Convert double value to color.
	 * @param value
	 * @return
	 */
	public static ColorObj convertDouble(double value) {
		
		long longValue = (long) Math.floor(value);
		
		return convertLong(longValue);
	}

	/**
	 * Convert hexa text to a color.
	 * @param hexaText
	 * @return
	 */
	public static ColorObj convertStringHexa(String hexaText) {

		ColorObj color = null;
		
		// Scan clipboard text for hexadecimal color number.
		Scanner hexaScanner = new Scanner(hexaText);
		
		// Try 4 bytes first and than 3 bytes.
		String foundText = hexaScanner.findInLine(Pattern.compile("\\p{XDigit}{8}", Pattern.CASE_INSENSITIVE));
		if (foundText != null) {
			
			// Convert found text to a long number and convert the number to a color.
			try {
				long colorLong = Long.parseLong(foundText, 16);
				color = ColorObj.convertLong(colorLong);
			}
			catch (Exception e) {
			}
		}

		if (color == null) {
			
			foundText = hexaScanner.findInLine(Pattern.compile("\\p{XDigit}{6}", Pattern.CASE_INSENSITIVE));
			if (foundText != null) {
				
				// Convert found text to a long number and convert the number to a color.
				try {
					long colorLong = Long.parseLong(foundText, 16);
					color = ColorObj.convertLongNoAlpha(colorLong);
				}
				catch (Exception e) {
				}
			}
		}
		hexaScanner.close();
		
		return color;
	}

	/**
	 * Convert RGB text to a color.
	 * @param rgbText
	 * @return
	 */
	public static ColorObj convertStringRgb(String rgbText) {
		
		ColorObj color = null;
		
		// Try to get RGB.
		Scanner rgbScanner = new Scanner(rgbText);
		
		// Try to find RGBA.
		String foundText = rgbScanner.findInLine(Pattern.compile("(\\p{Digit}{1,3}\\s*\\,\\s*){3}\\p{Digit}{1,3}"));
		if (foundText != null) {
			
			String [] parts = foundText.split("\\,");
			if (parts.length == 4) {
			
				int red = Integer.parseInt(parts[0].trim());
				if (red > 255) red = 255;
				
				int green = Integer.parseInt(parts[1].trim());
				if (green > 255) green = 255;
				
				int blue = Integer.parseInt(parts[2].trim());
				if (blue > 255) blue = 255;
				
				int alpha = Integer.parseInt(parts[3].trim());
				if (alpha > 255) alpha = 255;
				
				color = new ColorObj(red, green, blue, alpha);
			}
		}
		else {
			// Try to find RGB.
			foundText = rgbScanner.findInLine(Pattern.compile("(\\p{Digit}{1,3}\\s*\\,\\s*){2}\\p{Digit}{1,3}"));
			if (foundText != null) {
			
				String [] parts = foundText.split("\\,");
				if (parts.length == 3) {
				
					int red = Integer.parseInt(parts[0].trim());
					if (red > 255) red = 255;
					
					int green = Integer.parseInt(parts[1].trim());
					if (green > 255) green = 255;
					
					int blue = Integer.parseInt(parts[2].trim());
					if (blue > 255) blue = 255;
					
					color = new ColorObj(red, green, blue, 255);
				}
			}
		}
		
		rgbScanner.close();
		
		return color;
	}

	/**
	 * Convert string to color.
	 * @param text
	 * @return
	 */
	public static ColorObj convertString(String text) {

		ColorObj color = convertStringHexa(text);

		if (color == null) {
			color = convertStringRgb(text);
		}
		
		return color;
	}
	
	/**
	 * Blend two colors.
	 * @param c0
	 * @param c1
	 * @return
	 */
	public static Color blend(Color c0, Color c1) {
		
		double totalAlpha = c0.getAlpha() + c1.getAlpha();
		double weight0 = c0.getAlpha() / totalAlpha;
		double weight1 = c1.getAlpha() / totalAlpha;

		double r = weight0 * c0.getRed() + weight1 * c1.getRed();
		double g = weight0 * c0.getGreen() + weight1 * c1.getGreen();
		double b = weight0 * c0.getBlue() + weight1 * c1.getBlue();
		double a = Math.max(c0.getAlpha(), c1.getAlpha());

		return new Color((int) r, (int) g, (int) b, (int) a);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getText();
	}

	/**
	 * Get black color.
	 * @return
	 */
	public static ColorObj getBlack() {
		
		return new ColorObj(Color.BLACK);
	}

	/**
	 * Get boolean value.
	 * @return
	 */
	public boolean getBoolean() {
		
		return getRed() == 0 && getGreen() == 0 && getBlue() == 0;
	}

	/**
	 * Convert boolean to color.
	 * @param value
	 * @return
	 */
	public static ColorObj convertBoolean(boolean value) {
		
		return value ? new ColorObj(Color.WHITE) : new ColorObj(Color.BLACK);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		
		return super.clone();
	}
}
