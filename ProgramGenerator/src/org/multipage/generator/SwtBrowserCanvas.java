/*
 * Copyright 2010-2023 (C) vakol
 * 
 * Created on : 16-12-2023
 *
 */

package org.multipage.generator;

import java.awt.Canvas;
import java.util.HashSet;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.multipage.util.Lock;
import org.multipage.util.Obj;

// TODO: <---COPY to new version
/**
 * Implementation of an AWT that embeds a SWT component.
 * @author vakol
 */
public final class SwtBrowserCanvas extends Canvas {

    /**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;	
    
	/**
	 * Single SWT background thread.
	 */
	private static SwtThread swtThread = null;

	/**
	 * Indicates that SWT is available for the OS.
	 */
	private static Boolean swtAvailable = false;
	
	/**
	 * Set of created shells.
	 */
	private final static HashSet<Shell> swtShells = new HashSet<>();
	
	/**
	 * Associated browser.
	 */
	public Browser browser = null;
	
	/**
	 * Initial URL for the browser.
	 */
	private String initialUrl = "";
	
    /**
     * Implementation of a SWT thread.
     */
    private static class SwtThread extends Thread {
    	
    	/**
    	 * SWT thread termination timeout in milliseconds.
    	 */
        private static final long SWT_THREAD_TERMINATION_TIMEOUT_MS = 3000;
    	
		/**
		 * Single SWT display that is needed when running SWT thread.
		 */
		protected static Display display = null;
    	
		/**
		 * This flag terminates the SWT thread.
		 */
		private boolean exitThread = false;
        
		/**
		 * SWT thread termination lock.
		 */
		private Lock terminationLock = null;
		
        /**
         * Run SWT thread.
         */
		@Override
        public void run() {
			
        	// Create SWT display.
        	display = new Display();
        	
            // Execute the SWT event dispatch loop.
            try {
                while (!isInterrupted() && !exitThread) {
                	
    				// Set SWT available.
    				swtAvailable = true;
                	
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            }
            catch (Exception e) {
                interrupt();
            }
            
            // Set SWT not available.
			swtAvailable = false;
            
            // Notify about termination of the SWT thread.
            if (terminationLock != null) {
            	Lock.notify(terminationLock);
            }
		}
		
		/**
		 * Disposal of thread resources. Must be run at the end of application.
		 */
		void terminate() {
			
			// Signal SWT thread termination.
			terminationLock = new Lock();
			exitThread = true;
			Lock.waitFor(terminationLock, SWT_THREAD_TERMINATION_TIMEOUT_MS);
			
			// Release display.
			display.dispose();
		}
    }
	
	/**
     * Static constructor. Required for Linux, harmless for other OS.
     */
    static {
        System.setProperty("sun.awt.xembedserver", "true");
    }
    
    /**
     * Starts the SWT thread.
     */
    public static boolean startSwtThread() {
    	
    	boolean runThread = (swtThread == null);
    	
    	// Create the background thread if it doesn't already exist.
    	if (runThread) {
			swtThread = new SwtThread();
			swtThread.start();
    	}
        return runThread;
    }
    
    /**
     * Stops SWT thread. Disposes all SWT shells.
     */
    public static void stopSwtThread() {
    	
		SwtThread.display.syncExec(new Runnable() {
			public void run() {

				closeAllShells();
		    	
		        if (swtThread != null) {
		            swtThread.terminate();
		        }
			}
		});
    }
    
    /**
     * Close all SWT shells.
     */
    private static void closeAllShells() {
    	
    	for (Shell swtShell : swtShells) {
    		swtShell.dispose();
    	}
    }
    
    /**
     * Create browser canvas later on SWT thread.
     * @return
     */
	public static SwtBrowserCanvas createLater(Function<SwtBrowserCanvas, String> callback) {
		
		// Check if the SWT thread is available (started).
		if (swtAvailable != null && swtAvailable == false) {
			return null;
		}
		
		try {
			// Create browser object and attach it to SWT shell.
			SwtBrowserCanvas browserCanvas = new SwtBrowserCanvas();
			boolean success = browserCanvas.attachBrowser(callback);
			if (success) {
				return browserCanvas;
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		// On failure.
		return null;
	}
	
    /**
     * Close the browser.
     */
	public void close() {
			
		SwtThread.display.syncExec(new Runnable() {
			public void run() {
				
				// Close the SWT shell on dispatch thread.
				try {
					if (browser != null && !browser.isDisposed()) {
						Shell swtShell = browser.getShell();
						if (swtShell != null) {
							
							closeShell(swtShell);
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				browser = null;
			}
		});
	}
    
    /**
     * Create new browser and attach it to a SWT shell.
     */
    private boolean attachBrowser(Function<SwtBrowserCanvas, String> callback)
    		throws Exception {
    	
    	Obj<Boolean> success = new Obj<Boolean>(false);
    	
    	SwtThread.display.syncExec(new Runnable() {
    	    public void run() {

		        try {
		            // Get initial URL.
		            initialUrl = callback.apply(SwtBrowserCanvas.this);
		        	
		            // Create SWT shell attached to this Canvas.
		    		Shell swtShell = SWT_AWT.new_Shell(SwtThread.display, SwtBrowserCanvas.this);
		            swtShell.setLayout(new FillLayout());
		            
				    // Open the SWT shell and remember it.
		            swtShell.open();
		            swtShells.add(swtShell);
		            
		            // Create new browser in the SWT shell.
		            browser = new Browser(swtShell, SWT.NONE);
		
		            // This action must be executed on the SWT thread
		            browser.getDisplay().asyncExec(() -> {
		            	browser.setUrl(initialUrl);
		            });
		            
		            success.ref = true;
		        }
		        catch (Throwable e) {
		        	e.printStackTrace();
		        }    	    	
    	    }
    	});
    	
    	return success.ref;
    }
    
    /**
     * Reload current URL.
     */
	public void reload() {
		
    	SwtThread.display.syncExec(new Runnable() {
    	    public void run() {
    	    	
				// Check browser object.
				if (browser == null) {
					return;
				}
				
				// Reload browser contents.
				browser.refresh();
    	    }
    	});
	}
    
    /**
     * Close the SWT shell.
     * @param swtShell
     */
    private boolean closeShell(Shell swtShell) {
    	
    	// Check if the SWT shell exists.
    	boolean exists = swtShells.contains(swtShell);
    	if (!exists) {
    		return false;
    	}
    	
    	// Remove it from the set of SWT shells.
    	swtShells.remove(swtShell);
    	
    	// Close the SWT shells.
    	if (!swtShell.isDisposed()) {
    		swtShell.close();
    	}
    	return true;
    }
}
