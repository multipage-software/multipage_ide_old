/**
 * 
 */
package program.localizer;

import javax.swing.*;

import org.multipage.gui.*;

/**
 * @author
 *
 */
public class LocalizerMain {

	/**
	 * Default language and country.
	 */
	private static final String defaultLanguage = "en";
	private static final String defaultCountry = "US";
	
	/**
	 * Serialized data location.
	 */
	private static final String serilizedDataLocation = "localizer.settings";
	
	/**
	 * Application state serializer.
	 */
	private static StateSerializer serializer;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	    // Set System L&F.
        try {
			UIManager.setLookAndFeel(
			    UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
        

		// Get local identifiers.
		String language;
		String country;
		
		if (args.length == 0) {
			
			language = defaultLanguage;
			country = defaultCountry;
		}
		else if (args.length == 2) {
			
			language = args[0];
			country = args[1];
		}
		else {
			// If are wrong parameters.
			JOptionPane.showMessageDialog(
					null,
					"Error application arguments.\nUse:\n\tjava ProgrammerMain language COUNTRY",
					"Swing GUI Builder",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}

		// Create serializer.
		serializer = new StateSerializer(serilizedDataLocation);
		
		// Initialize modules.
		GeneralGui.initialize(language, country, serializer);
		Localizer.initialize(language, country, serializer);

		// Load states.
		serializer.startLoadingSerializedStates();
		
		// Start GUI.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				
				// Create frame and make it visible.
				LocalizerFrame frame = new LocalizerFrame(serializer);
				frame.setVisible(true);
			}
		});
	}
}
