/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * @author
 *
 */
public class ImgUtility {

	/**
	 * Converts byte array to the image.
	 * @param bytes
	 * @return
	 */
	public static BufferedImage convertByteArrayToImage(byte[] bytes) {
		
		BufferedImage image = null;
		InputStream inputStream = new ByteArrayInputStream(bytes);
		try {
			image = ImageIO.read(inputStream);
		}
		catch (IOException e) {
			return null;
		}
		return image;
	}

	/**
	 * Resize image
	 * @param image
	 * @param width
	 * @param height
	 * @return
	 */
	public static BufferedImage resizeImage(BufferedImage image, int width, int height) {

		int type = image.getType();
		if (type == 0) {
			type = BufferedImage.TYPE_INT_ARGB;
		}
		
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		
		return resizedImage;
	}

	/**
	 * Converts image to the byte array in given format.
	 * @param image
	 * @return
	 */
	public static byte[] convertImageToByteArray(BufferedImage bufferedImage, String format) {
		
		// Obtain image content.
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(bufferedImage, format, outputStream);
			return outputStream.toByteArray();
		}
		catch (IOException e) {
			return null;
		}
	}
}
