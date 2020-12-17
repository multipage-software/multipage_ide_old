/**
 * 
 */
package org.multipage.gui;

import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Pocitac
 *
 */
public class RestartApplication {
	
	/** 
	 * Sun property pointing the main class and its arguments. 
	 * Might not be defined on non Hotspot VM implementations.
	 */
	public static final String SUN_JAVA_COMMAND = "sun.java.command";
	private static Timer watchDog;
	private static long watchDogPeriod = 5000;
	
	/**
	 * Open AWT restart dialog.
	 */
	public static void openAWTRestartDialog() {
		
		new Thread(() -> {
			
			Frame w = new Frame("Restart");
			w.add("Center", new Button ("Restart"));
			w.setSize(200, 200);
			w.setVisible(true);
		}).start();
	}

	/**
	 * Restart the current Java application
	 * @param runBeforeRestart some custom code to be run before restarting
	 * @throws IOException
	 */
	public static void restart(Runnable runBeforeRestart) throws IOException {
	try {
	// java binary
	String java = System.getProperty("java.home") + "/bin/java";
	// vm arguments
	List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
	StringBuffer vmArgsOneLine = new StringBuffer();
	for (String arg : vmArguments) {
	// if it's the agent argument : we ignore it otherwise the
	// address of the old application and the new one will be in conflict
	if (!arg.contains("-agentlib")) {
	vmArgsOneLine.append(arg);
	vmArgsOneLine.append(" ");
	}
	}
	// init the command to execute, add the vm args
	final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);

	// program main and program arguments
	String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
	// program main is a jar
	if (mainCommand[0].endsWith(".jar")) {
	// if it's a jar, add -jar mainJar
	cmd.append("-jar " + new File(mainCommand[0]).getPath());
	} else {
	// else it's a .class, add the classpath and mainClass
	cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
	}
	// finally add program arguments
	for (int i = 1; i < mainCommand.length; i++) {
	cmd.append(" ");
	cmd.append(mainCommand[i]);
	}
	// execute the command in a shutdown hook, to be sure that all the
	// resources have been disposed before restarting the application
	Runtime.getRuntime().addShutdownHook(new Thread() {
	@Override
	public void run() {
	try {
	Runtime.getRuntime().exec(cmd.toString());
	} catch (IOException e) {
	e.printStackTrace();
	}
	}
	});
	// execute some custom code before restarting
	if (runBeforeRestart!= null) {
	runBeforeRestart.run();
	}
	// exit
	System.exit(0);
	} catch (Exception e) {
	// something went wrong
	throw new IOException("Error while trying to restart the application", e);
	}
	}

	/**
	 * Start running a watch dog that checks every watchDogPeriod milliseconds
	 * if an event queue exist. If it does not then the application is restarted.
	 */
	public static void runRestartWathdog() {
		
		watchDog = new Timer("IDE-Restart-WatchDog", true);
		
		watchDog.schedule(new TimerTask() {
			@Override
			public void run() {
				
				EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
				if (eventQueue.peekEvent() == null) {
					
					try {
						restart(null);
					}
					catch (Exception e) {
					}
				}
			}
		}, watchDogPeriod, watchDogPeriod);
	}
}
