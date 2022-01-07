/**
 * 
 */
package program.builder.dynamic;

import general.gui.Images;
import general.gui.Utility;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import program.generator.CustomizedControls;

/**
 * @author
 *
 */
public class CustomizedControlsDynamic extends CustomizedControls {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Crossings removal minimum and maximum values.
	 */
	private static final int crossingsMinimum = 0;
	private static final int crossingsMaximum = 30;
	
	private JPanel panelExtension;
	private javax.swing.JSlider crossingsSlider;
	private javax.swing.JButton reloadButton;
	private JLabel labelCrossingsRemoval;

	/**
	 * Constructor.
	 * @param owner
	 */
	public CustomizedControlsDynamic(Window owner) {
		super(owner);
		localizeExtension();
		setIconsExtension();
	}

	/**
	 * Initialize components.
	 */
	@Override
	protected void initializeComponentsExt() {
		
		setBounds(100, 100, 390, 236);

		panelExtension = new JPanel();
		panelExtension.setBounds(10, 93, 364, 91);
		contentPanel.add(panelExtension);
		SpringLayout sl_panelExtension = new SpringLayout();
		panelExtension.setLayout(sl_panelExtension);
		
		labelCrossingsRemoval = new JLabel("textCrossingsRemoval");
		sl_panelExtension.putConstraint(SpringLayout.NORTH, labelCrossingsRemoval, 10, SpringLayout.NORTH, panelExtension);
		panelExtension.add(labelCrossingsRemoval);
		crossingsSlider = new javax.swing.JSlider();
		sl_panelExtension.putConstraint(SpringLayout.NORTH, crossingsSlider, 6, SpringLayout.SOUTH, labelCrossingsRemoval);
		sl_panelExtension.putConstraint(SpringLayout.WEST, crossingsSlider, 0, SpringLayout.WEST, panelExtension);
		sl_panelExtension.putConstraint(SpringLayout.EAST, crossingsSlider, -96, SpringLayout.EAST, panelExtension);
		panelExtension.add(crossingsSlider);
		reloadButton = new JButton();
		sl_panelExtension.putConstraint(SpringLayout.WEST, reloadButton, 6, SpringLayout.EAST, crossingsSlider);
		sl_panelExtension.putConstraint(SpringLayout.SOUTH, reloadButton, 0, SpringLayout.SOUTH, crossingsSlider);
		panelExtension.add(reloadButton);
		

        reloadButton.setText("textReload");
        reloadButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reloadButton.setPreferredSize(new java.awt.Dimension(80, 25));
        
        
        		// Set button listener.
        		reloadButton.addActionListener(new ActionListener() {
        			@Override
        			public void actionPerformed(ActionEvent e) {
        				onReloadButton();
        			}
        		});
		// Set slider listener.
		crossingsSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				onCrossingsRemovalChanged();
			}
		});

		
		// Set crossings removal strength minimum and maximum.
		crossingsSlider.setMinimum(crossingsMinimum);
		crossingsSlider.setMaximum(crossingsMaximum);
		
		// Turn on labels at major tick marks.
		crossingsSlider.setMajorTickSpacing(10);
		crossingsSlider.setMinorTickSpacing(1);
		crossingsSlider.setPaintTicks(true);
		crossingsSlider.setPaintLabels(true);
		reloadButton = new javax.swing.JButton();

	}

	/* (non-Javadoc)
	 * @see program.builder.CustomizedControls#localizeExtension()
	 */
	@Override
	protected void localizeExtension() {
		
		Utility.localize(reloadButton);
		Utility.localize(labelCrossingsRemoval);
	}

	/* (non-Javadoc)
	 * @see program.builder.CustomizedControls#setIconsExtension()
	 */
	@Override
	protected void setIconsExtension() {
		
		// Set button icon.
		reloadButton.setIcon(Images.getIcon("program/generator/images/update_icon.png"));
	}

	protected void onCrossingsRemovalChanged() {

		// Set crossings removal strength.
		int crossingsRemovalStrength = crossingsSlider.getValue();
		StepsDiagram.sweepsCount = crossingsRemovalStrength;
	}

}
