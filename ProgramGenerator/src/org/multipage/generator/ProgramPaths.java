/*
 * Copyright 2010-2020 (C) vakol
 * 
 * Created on : 18-02-2020
 *
 */
package org.multipage.generator;

import java.util.function.Supplier;

import org.maclan.MiddleUtility;
import org.multipage.util.Resources;

/**
 * @author user
 *
 */
public class ProgramPaths {
	
	/**
	 * Program path.
	 * @author user
	 *
	 */
	public static class PathSupplier {
		
		/**
		 * Caption.
		 */
		public String caption;
				
		/**
		 * Replacement tag.
		 */
		public String tag;
		
		/**
		 * Path supplier.
		 */
		public Supplier<String> supplier;

		/**
		 * Constructor.
		 */
		public PathSupplier(String captionResource, String tag, Supplier<String> supplier) {
			
			this.caption = Resources.getString(captionResource);
			this.tag = tag;
			this.supplier = supplier;
		}
		
		/**
		 * Default constructor.
		 */
		public PathSupplier() {
			
		}

		/**
		 * Create new path supplier.
		 * @param captionText
		 * @param solvedPath
		 * @return
		 */
		public static PathSupplier newAreaPath(String captionText, String tag, String solvedPath) {
			
			PathSupplier pathSupplier = new PathSupplier();
			
			pathSupplier.caption = captionText;
			pathSupplier.tag = tag;
			pathSupplier.supplier = () -> {
				return solvedPath;
			};
			
			return pathSupplier;
		}
	}
	
	/**
	 * Application path suppliers.
	 */
	public static PathSupplier userDirectorySupplier = new PathSupplier("org.multipage.generator.textUserDirectorySupplier", "[@PATH $user_dir]", () -> { return MiddleUtility.getUserDirectory(); });
	public static PathSupplier databaseDirectorySupplier = new PathSupplier("org.multipage.generator.textDatabaseDirectorySupplier", "[@PATH $database_dir]", () -> { return MiddleUtility.getDatabaseAccess(); });
	public static PathSupplier webInterfaceDirectorySupplier = new PathSupplier("org.multipage.generator.textWebInterfaceDirectorySupplier", "[@PATH $web_dir]", () -> { try { return MiddleUtility.getWebInterfaceDirectory(); } catch (Exception e) { return null; } });
	public static PathSupplier phpDirectorySupplier = new PathSupplier("org.multipage.generator.textPhpDirectorySupplier", "[@PATH $php_dir]", () -> { return MiddleUtility.getPhpDirectory(); });
	public static PathSupplier temporaryDirectory = new PathSupplier("org.multipage.generator.textTemporaryDirectory", "[@PATH $temp_dir]", () -> { return MiddleUtility.getTemporaryDirectory(); });
}
