/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 30-07-2022
 *
 */

package org.multipage.generator;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.multipage.gui.Utility;

/**
 * Multipage certificate objects.
 * @author vakol
 *
 */
public class MultipageCertificate {
	
	/**
	 * A flag that designates default certificate placed in an application folder.
	 */
	public boolean isDefault = false;
	
	/**
	 * Path to certificate.
	 */
	public String path = null;
	
	/**
	 * Certificate content.
	 */
	public X509Certificate content = null;
	
	/**
	 * Certificate caption.
	 */
	private String caption = "UNKNOWN";
	
	/**
	 * Constructor.
	 */
	public MultipageCertificate(String certificatePath, boolean isDefault) {
		
		this.path = certificatePath;
		this.isDefault = isDefault;
		
		if (isDefault) {
			this.content = Utility.getApplicationCertificate(certificatePath);
		}
		else {
			this.content = Utility.getFileCertificate(certificatePath);
		}
		
		this.caption  = this.content.getSubjectX500Principal().getName();
	}
	
	/**
	 * Get certificate caption.
	 */
	@Override
	public String toString() {
		
		return caption;
	}
}
