/*
 * This class is made available under the Apache License, Version 2.0.
 *
 * See http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Author: Mark Lee
 *
 * (C)2013 Caprica Software (http://www.capricasoftware.co.uk)
 */

package org.multipage.generator;

import java.awt.Canvas;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Implementation of an AWT {@link Canvas} that embeds an SWT {@link Browser} component.
 * <p>
 * With contemporary versions of SWT, the Webkit browser is the default implementation.
 * <p>
 * To embed an SWT component inside of a Swing component there are a number of important
 * considerations (all of which comprise this implementation):
 * <ul>
 *   <li>A background thread must be created to process the SWT event dispatch loop.</li>
 *   <li>The browser component can not be created until after the hosting Swing component (e.g. the
 *       JFrame) has been made visible - usually right after <code>frame.setVisible(true).</code></li>
 *   <li>To cleanly dispose the native browser component, it is necessary to perform that clean
 *       shutdown from inside a {@link WindowListener#windowClosing(WindowEvent)} implementation in
 *       a listener registered on the hosting JFrame.</li>
 *   <li>On Linux, the <code>sun.awt.xembedserver</code> system property must be set.</li>
 * </ul>
 */
public final class SwtBrowserCanvas extends Canvas {

    /**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Indicates that SWT is available for the OS.
	 */
	private static Boolean swtAvailable = null;
	
	/**
	 * Initial URL of browser content.
	 */
	public static String initialUrl = null;
	
	/**
     * Required for Linux, harmless for other OS.
     * <p>
     * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=161911">SWT Component Not Displayed Bug</a>
     */
    static {
        System.setProperty("sun.awt.xembedserver", "true");
    }

    /**
     * SWT browser component reference.
     */
    private final AtomicReference<Browser> browserReference = new AtomicReference<>();

    /**
     * SWT event dispatch thread reference.
     */
    private final AtomicReference<SwtThread> swtThreadReference = new AtomicReference<>();
    
    /**
     * URL of the content
     */
	private String url = "localhost";
    
    /**
     * 
     * @return
     */
	public static SwtBrowserCanvas createInstance() {
		
		if (swtAvailable != null && swtAvailable == false) {
			return null;
		}
		try {
			
			SwtBrowserCanvas browser = new SwtBrowserCanvas();
			new Display();
			
			swtAvailable = true;
			return browser;
		}
		catch (Throwable e) {
		}
		
		swtAvailable = false;
		return null;
	}
	
	/**
	 * Create new instance of SWT browser.
	 * @param callback
	 * @return
	 */
	public static boolean createInstance(Function<SwtBrowserCanvas, String> callback) {
		
		// Create SWT browser and initialize it
		SwtBrowserCanvas browser = new SwtBrowserCanvas();
		boolean initialized = browser.initialise(callback);
		
		// If the SWT browser is not initialized, dispose the thread
		if (!initialized) {
			browser.dispose();
		}
		
		// Return the flag
		return initialized;
	}
	
    /**
     * Get the native browser instance.
     *
     * @return browser, may be <code>null</code>
     */
    public Browser getBrowser() {
        return browserReference.get();
    }

    /**
     * Navigate to a URL.
     *
     * @param url URL
     */
    public void setUrl(final String url) {
    	
        // This action must be executed on the SWT thread
        getBrowser().getDisplay().asyncExec(() -> {
        	
        	this.url = url != null ? url : initialUrl;
        	getBrowser().setUrl(this.url);
        });
    }

    /**
     * Create the browser canvas component.
     * <p>
     * This must be called <strong>after</strong> the parent application Frame is made visible -
     * usually directly after <code>frame.setVisible(true)</code>.
     * <p>
     * This method creates the background thread, which in turn creates the SWT components and
     * handles the SWT event dispatch loop.
     * <p>
     * This method will block (for a very short time) until that thread has successfully created
     * the native browser component (or an error occurs).
     * @param callback 
     *
     * @return <code>true</code> if the browser component was successfully created; <code>false if it was not</code/
     */
    public boolean initialise(Function<SwtBrowserCanvas, String> callback) {
    	
        CountDownLatch browserCreatedLatch = new CountDownLatch(1);
        SwtThread swtThread = new SwtThread(browserCreatedLatch, callback);
        swtThreadReference.set(swtThread);
        swtThread.start();
        boolean result;
        try {
            browserCreatedLatch.await();
            result = browserReference.get() != null;
            if (result) {
            	setUrl(initialUrl);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     * Dispose the browser canvas component.
     * <p>
     * This should be called from a {@link WindowListener#windowClosing(WindowEvent)} implementation.
     */
    public void dispose() {
    	
        browserReference.set(null);
        SwtThread swtThread = swtThreadReference.getAndSet(null);
        
        if (swtThread != null) {
        	swtThread.exitThread = true;
            swtThread.interrupt();
        }
    }

    /**
     * Implementation of a thread that creates the browser component and then implements an event
     * dispatch loop for SWT.
     */
    private class SwtThread extends Thread {

        /**
         * Initialisation latch.
         */
        private final CountDownLatch browserCreatedLatch;
        
        /**
         * Callback lambda.
         */
		private Function<SwtBrowserCanvas, String> callback;
		
		/**
		 * Exit thread flag should be set to leave the thread main loop
		 */
		public boolean exitThread = false;

        /**
         * Create a thread.
         *
         * @param browserCreatedLatch initialization latch.
         * @param callback 
         */
        private SwtThread(CountDownLatch browserCreatedLatch, Function<SwtBrowserCanvas, String> callback) {
        	
        	this.setName("IDE-SWT-Browser");
            this.browserCreatedLatch = browserCreatedLatch;
            this.callback = callback;
        }

        @Override
        public void run() {
        	
            // First prepare the SWT components...
            Display display = null;
            Shell shell;
            try {
            	display = new Display();
            	SwtBrowserCanvas.initialUrl = callback.apply(SwtBrowserCanvas.this);
                shell = SWT_AWT.new_Shell(display, SwtBrowserCanvas.this);
                shell.setLayout(new FillLayout());
                browserReference.set(new Browser(shell, SWT.NONE));
            }
            catch (Throwable e) {
                return;
            }
            finally {
                // Guarantee the count-down so as not to block the caller, even in case of error -
                // there is a theoretical (rare) chance of failure to initialize the SWT components
                browserCreatedLatch.countDown();
            }
            // Execute the SWT event dispatch loop...
            try {
                shell.open();
                while (!isInterrupted() && !shell.isDisposed()) {
                	if (exitThread) {
                		break;
                	}
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
                browserReference.set(null);
                shell.dispose();
                display.dispose();
            }
            catch (Exception e) {
                interrupt();
            }
        }
    }
    
    /**
     * Reload content
     */
	public void reload() {
		
		// Run on SWT thread
		Display.getDefault().asyncExec(() -> {
			
			// Workaround needed because of caching
			getBrowser().addProgressListener(new ProgressAdapter() {
		        public void completed(ProgressEvent event) {
		        	getBrowser().removeProgressListener(this);
		        	
		        	getBrowser().refresh();
		        }
		    });
			
			getBrowser().setUrl(url);
		});
	}
}
