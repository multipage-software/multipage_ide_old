/**
 * 
 */
package org.multipage.unit_testing;

import java.awt.Color;

import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.multipage.gui.LogConsoles;
import org.multipage.util.Obj;
import org.multipage.util.j;

/**
 * Log console testing.
 * @author vakol
 *
 */
public class LogConsoleTesting {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		LogConsoles.main(new String [] {});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		final int NUM_TESTS = 3;
		final int MESSAGE_COUNT = 100;
		final int TEST_TIMEOUT_MS = 10000000;
		
		try {
			for (Obj<Integer> index = new Obj<Integer>(0); index.ref < NUM_TESTS; index.ref++) {
				
				SwingUtilities.invokeLater(() -> {
					
					Thread thread1 = new Thread(() -> {
						
						//System.out.format("-----------------------------\nSENDING %d. CHUNK OF MESSAGES\n-----------------------------\n", index.ref);
						
						for (int i = 0; i < MESSAGE_COUNT; i++) {
							//j.log(1, Color.RED, "|Hello world", i);
						}
						
						//System.out.format("\n-----------------------------\nSENDING %d SUCCESFULLY DONE  \n-----------------------------\n", index.ref);
						
					});
					
					Thread thread2 = new Thread(() -> {
						
						//System.out.format("-----------------------------\nSENDING %d. CHUNK OF MESSAGES\n-----------------------------\n", index.ref);
						
						for (int i = 0; i < MESSAGE_COUNT; i++) {
							//j.log(2, Color.GREEN, "|Hello computer world", i);
						}
						
						//System.out.format("\n-----------------------------\nSENDING %d SUCCESFULLY DONE  \n-----------------------------\n", index.ref);
						
					});
					
					Thread thread3 = new Thread(() -> {
						
						//System.out.format("-----------------------------\nSENDING %d. CHUNK OF MESSAGES\n-----------------------------\n", index.ref);
						
						for (int i = 0; i < MESSAGE_COUNT; i++) {
							//j.log(3, Color.YELLOW, "|Hello computer world", i);
						}
						
						//System.out.format("\n-----------------------------\nSENDING %d SUCCESFULLY DONE  \n-----------------------------\n", index.ref);
						
					});
					
					thread1.start();
					thread2.start();
					thread3.start();
				});
				
				// TIMEOUT
				Thread.sleep(TEST_TIMEOUT_MS);

				// Get number of messages.
				int count = LogConsoles.getJUnitProbe1();
				
				//System.err.format("COUNT %d\n", count);
				
				//assertSame(count % (1 * MESSAGE_COUNT), 0);
				
				/*if ((count % MESSAGE_COUNT) != 0) {
					System.err.println("BREAK");
				}*/
				
				LogConsoles.runJUnitProbe2();
				
				//System.out.format("\nTEST NUMBER %d\n", index.ref);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
