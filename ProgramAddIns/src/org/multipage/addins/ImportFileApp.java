/*
 * Copyright 2010-2022 (C) vakol
 * 
 * Created on : 01-09-2022
 */
package org.multipage.addins;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

/**
 * @author vakol
 *
 */
public class ImportFileApp {
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		// Check number of input arguments.
		if (args.length < 4) {
			return;
		}
		
		// Get file to import into the JAR file.
		String importedFilePath = args[0];
		// Get JAR file path.
		String jarFilePath = args[1];
		// Get target folder.
		String targetFolder = args[2];
		// Application to restart.
		String restartedAppPath = args[3];
		
		// TODO: <---DEBUG MESSAGES
		JOptionPane.showConfirmDialog(null, "START IMPORT");
		
		// Import file to JAR archive.
	    Boolean success = importToJarArchive(importedFilePath, jarFilePath, targetFolder);
	    JOptionPane.showConfirmDialog(null, "Import successful " + success);
	    
	    // Restart main application.
	    File appFile = new File(restartedAppPath);
	    if (!appFile.isFile()) {
	    	JOptionPane.showConfirmDialog(null, "Missing file " + appFile);
	    	return;
	    }
	    String workingDirectory = appFile.getParent();
	    
		// Run main application.
		try {
			String result = runExecutableJar(workingDirectory, restartedAppPath, null);
			JOptionPane.showConfirmDialog(null, "Restart result " + result);
		}
		catch (Exception e) {
			JOptionPane.showConfirmDialog(null, "Restart exception " + e.getLocalizedMessage());
		}
	}
	
	/**
	 * Import file to JAR archive.
	 */
	private static boolean importToJarArchive(String importedFilePath, String jarFilePath, String targetFolder) {
		
		// Check imported file.
		File importedFile = new File(importedFilePath);
		if (!importedFile.isFile()) {
			return false;
		}
		
		FileSystem jarFileSystem = null;
		boolean success = false;
		
		try {
			// Check JAR file.
			final String jarUriString = "jar:file:/" + jarFilePath.replace('\\', '/');
			final URI jarUri = URI.create(jarUriString);
			final Map<String, String> jarEnvironment = Map.of("create", "true");
			jarFileSystem = FileSystems.newFileSystem(jarUri, jarEnvironment);
			
			// Convert to target path.
			Path targetPath = jarFileSystem.getPath(targetFolder);
			
			// Copy directory to jar folder.
			copyFromDirToJar(importedFile.toPath(), targetPath, jarFileSystem);
			success = true;
		}
		catch (Exception e) {
			JOptionPane.showConfirmDialog(null, "Import exception " + e.getLocalizedMessage());
		}
		finally {
			if (jarFileSystem != null) {
				try {
					jarFileSystem.close();
				}
				catch (Exception e) {
				}
			}
		}
		
		return success;
	}

	/**
	 * Copy source directory to a target JAR file system.
	 * @param sourcePath
	 * @param destinationPath
	 * @param destinationFileSystemo
	 */
	private static void copyFromDirToJar(Path sourcePath, Path destinationPath, FileSystem destinationFileSystem)
			throws Exception {
		
		// Create destination directory if it doesn't exist.
	    if (!Files.exists(destinationPath)) {
	    	Files.createDirectories(destinationPath);
	    }
	    
	    // If the source and destination paths designate files, copy the source
	    // file directly to the destination file.
	    if (Files.isRegularFile(sourcePath) && Files.isRegularFile(destinationPath)) {
	    	Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
	    }
	    
	    // List child source paths.
	    Exception [] exception = new Exception [] { null };
	    Files.list(sourcePath).forEachOrdered(sourceSubPath -> {
	    	try {
	    		Path fileOrFolder = sourceSubPath.getFileName();
	    		Path destinationSubPath = destinationFileSystem.getPath(destinationPath.toString(), fileOrFolder.toString());
	    		
	    		// Copy the directory or the file.
	    	    if (Files.isDirectory(sourceSubPath)) {
	    	        copyFromDirToJar(sourceSubPath, destinationSubPath, destinationFileSystem);
	    	    }
	    	    else {
	    			Files.copy(sourceSubPath, destinationSubPath, StandardCopyOption.REPLACE_EXISTING);
	    	    }
	    	}
	    	catch (Exception e) {
	    		exception[0] = e;
	    	}
	    });
	    
	    // Throw exception.
	    if (exception[0] != null) {
	    	throw exception[0];
	    }
	}
	
	/**
	 * Run executable file using given command that is placed in directory
	 * designated by "path" argument.
	 * @param workingDirectoryPath
	 * @param command
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws Exception 
	 */
	public static String runExecutable(String workingDirectoryPath, String command, Integer timeout, TimeUnit unit)
			throws Exception {
		
		StringBuilder text = new StringBuilder("");
		Exception exception = null;
		
		// Check working directory.
		File workingDirectory = new File(workingDirectoryPath);
		if (!workingDirectory.isDirectory()) {
			throw new Exception(String.format(
					"Unknown working directory \"%s\".",
					workingDirectoryPath,
					command));
		}
		
		InputStream standardOutput = null;
		BufferedReader reader = null;
		try {
			
			// Run the command as a process and wait for it.
			Process process = Runtime.getRuntime().exec(command, null, workingDirectory);
			
			// Wait given time span for process termination.
			if (timeout != null && unit != null) {
				process.waitFor(timeout, unit);
	        
		        // Get its stdout and read the output text.
		        standardOutput = process.getInputStream();
				reader = new BufferedReader(new InputStreamReader(standardOutput));
				
				while (true) {
					
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					
					text.append(line);
					text.append("\n");
				}
			}
			else {
				standardOutput = process.getInputStream();
			}
		}
		catch (Exception e) {
			exception = e;
		}
		finally {
			
			// Close stdout.
			if (standardOutput != null) {
				try {
					standardOutput.close();
				}
				catch (Exception e) {
				}
			}
			
			// Close reader.
			if (reader != null) {
				try {
					reader.close();
				}
				catch (Exception e) {
				}
			}
		}
		
		// If there is an exception, throw it.
		if (exception != null) {
			throw exception;
		}
		
		return text.toString();
	}
	
	/**
	 * Run executable JAR using java.exe.
	 * @param workingDirectory
	 * @param executableJarPath
	 * @param parameters
	 */
	public static String runExecutableJar(String workingDirectory, String executableJarPath, String [] parameters)
			throws Exception {
		
		// Get Java home directory.
		String javaExePath = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java.exe";
		
		// Compile java execution command.
		StringBuilder javaCommand = new StringBuilder();
		javaCommand.append('\"').append(javaExePath).append("\" -jar \"").append(executableJarPath).append('\"');
		
		// Add parameters.
		if (parameters != null) {
			for (String parameter : parameters) {
				javaCommand.append(" \"").append(parameter).append('\"');
			}
		}
		
		// Run java command.
		String result = runExecutable(workingDirectory, javaCommand.toString(), null, null);
		return result;
	}
}