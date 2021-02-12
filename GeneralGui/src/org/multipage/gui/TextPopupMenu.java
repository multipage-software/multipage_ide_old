/*
 * Copyright 2010-2017 (C) vakol
 * 
 * Created on : 26-04-2017
 *
 */

package org.multipage.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.JTextComponent;

import org.multipage.util.Resources;

/**
 * @author
 *
 */
public class TextPopupMenu extends JPopupMenu {

	/**
	 * Version.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Enable CSS editors.
	 */
	public static boolean enableCss = true;

	/**
	 * Text component.
	 */
	private JTextComponent textComponent;
	
	/**
	 * Menu items.
	 */
	private JMenuItem menuCut;
	private JMenuItem menuCopy;
	private JMenuItem menuPaste;
	private JSeparator separator;
	private JMenuItem menuSelectAll;
    private JMenu menuEdit;
    private JMenu menuInsert;
    private JMenuItem menuEditMoveLeft;
    private JMenuItem menuEditMoveRight;
	private JMenuItem menuInsertDateTime;
	private JMenuItem menuInsertAnchor;
	private JMenuItem menuInsertLoremIpsum;
	private JMenuItem menuInsertCssFont;
	private JMenuItem menuInsertCssBorder;
	private JMenuItem menuInsertCssOutlines;
	private JMenuItem menuInsertCssBoxShadow;
	private JMenuItem menuInsertCssBackground;
	private JMenuItem menuInsertCssNumber;
	private JMenuItem menuInsertCssBorderRadius;
	private JMenuItem menuInsertCssTextShadow;
	private JMenuItem menuInsertCssBorderImage;
	private JMenuItem menuInsertCssClip;
	private JMenuItem menuInsertCssFlex;
	private JMenuItem menuInsertCssSpacing;
	private JMenuItem menuInsertCssCounter;
	private JMenuItem menuInsertCssListStyle;
	private JMenuItem menuInsertCssKeyframes;
	private JMenuItem menuInsertCssAnimation;
	private JMenuItem menuInsertCssPerspectiveOrigin;
	private JMenuItem menuInsertCssTransform;
	private JMenuItem menuInsertCssTransformOrigin;
	private JMenuItem menuInsertCssTransition;
	private JMenuItem menuInsertCssCursor;
	private JMenuItem menuInsertCssQuotes;
	private JMenuItem menuInsertCssTextLine;
	private JMenuItem menuInsertCssResource;
	private JMenuItem menuInsertCssResourceUrl;
	private JMenuItem menuInsertCssResourcesUrls;
	
	/**
	 * Constructor.
	 * @param textComponent 
	 */
	public TextPopupMenu(JTextComponent textComponent) {
		this.textComponent = textComponent;
		// Create menu.
		createPopupMenu();
		// Localize components.
		localize();
		// Set icons.
		setIcons();
	}

	/**
	 * Create popup menu.
	 */
	private void createPopupMenu() {
		
		boolean isEditable = textComponent.isEditable();
		
		addPopup(textComponent, this);
		
		menuCut = new JMenuItem("textCut");
		menuCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCutText();
			}
		});
		add(menuCut);
		
		menuCopy = new JMenuItem("textCopy");
		menuCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCopyText();
			}
		});
		add(menuCopy);
		
		menuPaste = new JMenuItem("textPaste");
		menuPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPasteText();
			}
		});
		add(menuPaste);
		
		separator = new JSeparator();
		add(separator);

		menuEdit = new JMenu("org.multipage.gui.textEdit");
		if (isEditable) {
			add(menuEdit);
		}
		
		menuInsert = new JMenu("org.multipage.gui.textInsert");
		if (isEditable) {
			add(menuInsert);
		}
		
		separator = new JSeparator();
		add(separator);
		
		menuSelectAll = new JMenuItem("org.multipage.gui.textSelectAll");
		menuSelectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSelectAll();
			}
		});
		add(menuSelectAll);
		
		menuEditMoveLeft = new JMenuItem("org.multipage.gui.textMoveLeft");
		menuEdit.add(menuEditMoveLeft);
		menuEditMoveLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onMoveLeft();
			}
		});
		
		menuEditMoveRight = new JMenuItem("org.multipage.gui.textMoveRight");
		menuEdit.add(menuEditMoveRight);
		menuEditMoveRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onMoveRight();
			}
		});
		
		menuInsertDateTime = new JMenuItem("org.multipage.gui.textInsertDataTime");
		menuInsert.add(menuInsertDateTime);
		menuInsertDateTime.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onInsertDateTime();
			}
		});
		
		menuInsertAnchor = new JMenuItem("org.multipage.gui.textInsertAnchor");
		if (enableCss) {
			menuInsert.add(menuInsertAnchor);
			menuInsertAnchor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertAnchor();
				}
			});
		}
	
		menuInsertLoremIpsum = new JMenuItem("org.multipage.gui.textInsertLoremIpsum");
		menuInsert.add(menuInsertLoremIpsum);
		menuInsertLoremIpsum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onInsertLoremIpsum();
			}
		});
		
		menuInsertCssFont = new JMenuItem("org.multipage.gui.textInsertCssFont");
		if (enableCss) {
			menuInsert.add(menuInsertCssFont);
			menuInsertCssFont.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssFont();
				}
			});
		}
		
		menuInsertCssBorder = new JMenuItem("org.multipage.gui.textInsertCssBorder");
		if (enableCss) {
			menuInsert.add(menuInsertCssBorder);
			menuInsertCssBorder.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssBorder();
				}
			});
		}
		
		menuInsertCssOutlines = new JMenuItem("org.multipage.gui.textInsertCssOutlines");
		if (enableCss) {
			menuInsert.add(menuInsertCssOutlines);
			menuInsertCssOutlines.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssOulines();
				}
			});
		}
		
		menuInsertCssBoxShadow = new JMenuItem("org.multipage.gui.textInsertCssBoxShadow");
		if (enableCss) {
			menuInsert.add(menuInsertCssBoxShadow);
			menuInsertCssBoxShadow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssBoxShadow();
				}
			});
		}
		
		menuInsertCssTextShadow = new JMenuItem("org.multipage.gui.textInsertCssTextShadow");
		if (enableCss) {
			menuInsert.add(menuInsertCssTextShadow);
			menuInsertCssTextShadow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssTextShadow();
				}
			});
		}
		
		menuInsertCssBackground = new JMenuItem("org.multipage.gui.textInsertCssBackground");
		if (enableCss) {
			menuInsert.add(menuInsertCssBackground);
			menuInsertCssBackground.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssBackground();
				}
			});
		}
		
		menuInsertCssNumber = new JMenuItem("org.multipage.gui.textInsertCssNumber");
		if (enableCss) {
			menuInsert.add(menuInsertCssNumber);
			menuInsertCssNumber.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssNumber();
				}
			});
		}
		
		menuInsertCssBorderRadius = new JMenuItem("org.multipage.gui.textInsertCssBorderRadius");
		if (enableCss) {
			menuInsert.add(menuInsertCssBorderRadius);
			menuInsertCssBorderRadius.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssBorderRadius();
				}
			});
		}
		
		menuInsertCssBorderImage = new JMenuItem("org.multipage.gui.textInsertCssBorderImage");
		if (enableCss) {
			menuInsert.add(menuInsertCssBorderImage);
			menuInsertCssBorderImage.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssBorderImage();
				}
			});
		}
		
		menuInsertCssClip = new JMenuItem("org.multipage.gui.textInsertCssClip");
		if (enableCss) {
			menuInsert.add(menuInsertCssClip);
			menuInsertCssClip.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssClip();
				}
			});
		}
		
		menuInsertCssFlex = new JMenuItem("org.multipage.gui.textInsertCssFlex");
		if (enableCss) {
			menuInsert.add(menuInsertCssFlex);
			menuInsertCssFlex.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssFlex();
				}
			});
		}
		
		menuInsertCssSpacing = new JMenuItem("org.multipage.gui.textInsertCssSpacing");
		if (enableCss) {
			menuInsert.add(menuInsertCssSpacing);
			menuInsertCssSpacing.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssSpacing();
				}
			});
		}
		
		menuInsertCssCounter = new JMenuItem("org.multipage.gui.textInsertCssCounter");
		if (enableCss) {
			menuInsert.add(menuInsertCssCounter);
			menuInsertCssCounter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssCounter();
				}
			});
		}
		
		menuInsertCssListStyle = new JMenuItem("org.multipage.gui.textInsertCssListStyle");
		if (enableCss) {
			menuInsert.add(menuInsertCssListStyle);
			menuInsertCssListStyle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssListStyle();
				}
			});
		}
		
		menuInsertCssKeyframes = new JMenuItem("org.multipage.gui.textInsertCssKeyframes");
		if (enableCss) {
			menuInsert.add(menuInsertCssKeyframes);
			menuInsertCssKeyframes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssKeyframes();
				}
			});
		}
		
		menuInsertCssAnimation = new JMenuItem("org.multipage.gui.textInsertCssAnimation");
		if (enableCss) {
			menuInsert.add(menuInsertCssAnimation);
			menuInsertCssAnimation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssAnimation();
				}
			});
		}
		
		menuInsertCssPerspectiveOrigin = new JMenuItem("org.multipage.gui.textInsertCssPerspectiveOrigin");
		if (enableCss) {
			menuInsert.add(menuInsertCssPerspectiveOrigin);
			menuInsertCssPerspectiveOrigin.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssPerspectiveOrigin();
				}
			});
		}
		
		menuInsertCssTransform = new JMenuItem("org.multipage.gui.textInsertCssTransform");
		if (enableCss) {
			menuInsert.add(menuInsertCssTransform);
			menuInsertCssTransform.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssTransform();
				}
			});
		}
		
		menuInsertCssTransformOrigin = new JMenuItem("org.multipage.gui.textInsertCssTransformOrigin");
		if (enableCss) {
			menuInsert.add(menuInsertCssTransformOrigin);
			menuInsertCssTransformOrigin.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssTransformOrigin();
				}
			});
		}
		
		menuInsertCssTransition = new JMenuItem("org.multipage.gui.textInsertCssTransition");
		if (enableCss) {
			menuInsert.add(menuInsertCssTransition);
			menuInsertCssTransition.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssTransition();
				}
			});
		}
		
		menuInsertCssCursor = new JMenuItem("org.multipage.gui.textInsertCssCursor");
		if (enableCss) {
			menuInsert.add(menuInsertCssCursor);
			menuInsertCssCursor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssCursor();
				}
			});
		}
		
		menuInsertCssQuotes = new JMenuItem("org.multipage.gui.textInsertCssQuotes");
		if (enableCss) {
			menuInsert.add(menuInsertCssQuotes);
			menuInsertCssQuotes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssQuotes();
				}
			});
		}
		
		menuInsertCssTextLine = new JMenuItem("org.multipage.gui.textInsertCssTextLine");
		if (enableCss) {
			menuInsert.add(menuInsertCssTextLine);
			menuInsertCssTextLine.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssTextLine();
				}
			});
		}
		
		menuInsertCssResource = new JMenuItem("org.multipage.gui.textInsertCssResource");
		if (enableCss) {
			menuInsert.add(menuInsertCssResource);
			menuInsertCssResource.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssResource();
				}
			});
		}
		
		menuInsertCssResourceUrl = new JMenuItem("org.multipage.gui.textInsertCssResourceUrl");
		if (enableCss) {
			menuInsert.add(menuInsertCssResourceUrl);
			menuInsertCssResourceUrl.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssResourceUrl();
				}
			});
		}
		
		menuInsertCssResourcesUrls = new JMenuItem("org.multipage.gui.textInsertCssResourcesUrls");
		if (enableCss) {
			menuInsert.add(menuInsertCssResourcesUrls);
			menuInsertCssResourcesUrls.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onInsertCssResourcesUrls();
				}
			});
		}
	}

	/**
	 * Move left.
	 */
	protected void onMoveLeft() {
		
		if (textComponent.isEditable()) {
			TextEditorPane.moveLines(true, textComponent);
		}
	}

	/**
	 * Move right.
	 */
	protected void onMoveRight() {
		
		if (textComponent.isEditable()) {
			TextEditorPane.moveLines(false, textComponent);
		}
	}

	/**
	 * Localize components.
	 */
	private void localize() {
		
		Utility.localize(menuCut);
		Utility.localize(menuCopy);
		Utility.localize(menuPaste);
		Utility.localize(menuSelectAll);
		Utility.localize(menuEdit);
		Utility.localize(menuEditMoveLeft);
		Utility.localize(menuEditMoveRight);
		Utility.localize(menuInsert);
		Utility.localize(menuInsertDateTime);
		Utility.localize(menuInsertAnchor);
		Utility.localize(menuInsertLoremIpsum);
		Utility.localize(menuInsertCssFont);
		Utility.localize(menuInsertCssBorder);
		Utility.localize(menuInsertCssOutlines);
		Utility.localize(menuInsertCssBoxShadow);
		Utility.localize(menuInsertCssTextShadow);
		Utility.localize(menuInsertCssBackground);
		Utility.localize(menuInsertCssNumber);
		Utility.localize(menuInsertCssBorderRadius);
		Utility.localize(menuInsertCssBorderImage);
		Utility.localize(menuInsertCssClip);
		Utility.localize(menuInsertCssFlex);
		Utility.localize(menuInsertCssSpacing);
		Utility.localize(menuInsertCssCounter);
		Utility.localize(menuInsertCssListStyle);
		Utility.localize(menuInsertCssKeyframes);
		Utility.localize(menuInsertCssAnimation);
		Utility.localize(menuInsertCssPerspectiveOrigin);
		Utility.localize(menuInsertCssTransform);
		Utility.localize(menuInsertCssTransformOrigin);
		Utility.localize(menuInsertCssTransition);
		Utility.localize(menuInsertCssCursor);
		Utility.localize(menuInsertCssQuotes);
		Utility.localize(menuInsertCssTextLine);
		Utility.localize(menuInsertCssResource);
		Utility.localize(menuInsertCssResourceUrl);
		Utility.localize(menuInsertCssResourcesUrls);
	}

	/**
	 * Set icons.
	 */
	private void setIcons() {

		menuCut.setIcon(Images.getIcon("org/multipage/gui/images/cut_icon.png"));
		menuCopy.setIcon(Images.getIcon("org/multipage/gui/images/copy_icon.png"));
		menuPaste.setIcon(Images.getIcon("org/multipage/gui/images/paste_icon.png"));
		menuSelectAll.setIcon(Images.getIcon("org/multipage/gui/images/select_all.png"));
		menuEdit.setIcon(Images.getIcon("org/multipage/gui/images/edit.png"));
		menuEditMoveLeft.setIcon(Images.getIcon("org/multipage/gui/images/left.png"));
		menuEditMoveRight.setIcon(Images.getIcon("org/multipage/gui/images/right.png"));
		menuInsert.setIcon(Images.getIcon("org/multipage/gui/images/insert.png"));
		menuInsertDateTime.setIcon(Images.getIcon("org/multipage/gui/images/watch.png"));
		menuInsertAnchor.setIcon(Images.getIcon("org/multipage/gui/images/anchor.png"));
		menuInsertLoremIpsum.setIcon(Images.getIcon("org/multipage/gui/images/lorem_ipsum.png"));
		menuInsertCssFont.setIcon(Images.getIcon("org/multipage/gui/images/font_icon.png"));
		menuInsertCssBorder.setIcon(Images.getIcon("org/multipage/gui/images/border.png"));
		menuInsertCssOutlines.setIcon(Images.getIcon("org/multipage/gui/images/outlines.png"));
		menuInsertCssBoxShadow.setIcon(Images.getIcon("org/multipage/gui/images/shadow.png"));
		menuInsertCssTextShadow.setIcon(Images.getIcon("org/multipage/gui/images/text_shadow.png"));
		menuInsertCssBackground.setIcon(Images.getIcon("org/multipage/gui/images/background.png"));
		menuInsertCssNumber.setIcon(Images.getIcon("org/multipage/gui/images/number.png"));
		menuInsertCssBorderRadius.setIcon(Images.getIcon("org/multipage/gui/images/radius.png"));
		menuInsertCssBorderImage.setIcon(Images.getIcon("org/multipage/gui/images/border_image.png"));
		menuInsertCssClip.setIcon(Images.getIcon("org/multipage/gui/images/clip.png"));
		menuInsertCssFlex.setIcon(Images.getIcon("org/multipage/gui/images/flex.png"));
		menuInsertCssSpacing.setIcon(Images.getIcon("org/multipage/gui/images/spacing.png"));
		menuInsertCssCounter.setIcon(Images.getIcon("org/multipage/gui/images/increment.png"));
		menuInsertCssListStyle.setIcon(Images.getIcon("org/multipage/gui/images/list.png"));
		menuInsertCssKeyframes.setIcon(Images.getIcon("org/multipage/gui/images/keyframes.png"));
		menuInsertCssAnimation.setIcon(Images.getIcon("org/multipage/gui/images/animation.png"));
		menuInsertCssPerspectiveOrigin.setIcon(Images.getIcon("org/multipage/gui/images/perspective_origin.png"));
		menuInsertCssTransform.setIcon(Images.getIcon("org/multipage/gui/images/transform.png"));
		menuInsertCssTransformOrigin.setIcon(Images.getIcon("org/multipage/gui/images/transform_origin.png"));
		menuInsertCssTransition.setIcon(Images.getIcon("org/multipage/gui/images/transition.png"));
		menuInsertCssCursor.setIcon(Images.getIcon("org/multipage/gui/images/cursor.png"));
		menuInsertCssQuotes.setIcon(Images.getIcon("org/multipage/gui/images/quotes.png"));
		menuInsertCssTextLine.setIcon(Images.getIcon("org/multipage/gui/images/text_line.png"));
		menuInsertCssResource.setIcon(Images.getIcon("org/multipage/gui/images/resource.png"));
		menuInsertCssResourceUrl.setIcon(Images.getIcon("org/multipage/gui/images/url.png"));
		menuInsertCssResourcesUrls.setIcon(Images.getIcon("org/multipage/gui/images/urls.png"));
	}

	/**
	 * Add popup menu.
	 * @param component
	 * @param popup
	 */
	private static void addPopup(Component component, final JPopupMenu popup) {
		
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	/**
	 * On cut text.
	 */
	protected void onCutText() {
		
		if (textComponent.isEditable()) {
			textComponent.grabFocus();
			textComponent.cut();
		}
	}

	/**
	 * On copy text.
	 */
	protected void onCopyText() {

		textComponent.grabFocus();
		textComponent.copy();
	}

	/**
	 * On paste text.
	 */
	protected void onPasteText() {

		if (textComponent.isEditable()) {
			textComponent.grabFocus();
			textComponent.paste();
		}
	}
	
	/**
	 * On select all.
	 */
	protected void onSelectAll() {
		
		textComponent.grabFocus();
		textComponent.selectAll();
	}

	/**
	 * Date / time dialog.
	 */
	protected void onInsertDateTime() {
		
		if (textComponent.isEditable()) {
			
			String dateTimeText = DateTimeDialog.showDialog(this);
			textComponent.replaceSelection(dateTimeText);
		}
	}

	/**
	 * On insert Lorem Ipsum.
	 */
	protected void onInsertLoremIpsum() {
		
		if (textComponent.isEditable()) {
			
			String loremIpsumText = LoremIpsumDialog.showDialog(this);
			
			if (loremIpsumText != null) {
				textComponent.replaceSelection(loremIpsumText);
			}
		}
	}

	/**
	 * On insert CSS font.
	 */
	protected void onInsertCssFont() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			String cssFont = InsertPanelContainerDialog.showDialog(textComponent, new CssFontPanel(selectedText));
			
			if (cssFont != null) {
				textComponent.replaceSelection(cssFont);
			}
		}
	}
	
	/**
	 * On insert CSS border.
	 */
	protected void onInsertCssBorder() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssBorder = InsertPanelContainerDialog.showDialog(textComponent, new CssBorderPanel(selectedText));
			
			if (cssBorder != null) {
				textComponent.replaceSelection(cssBorder);
			}
		}
	}

	/**
	 * Insert CSS outlines.
	 */
	protected void onInsertCssOulines() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssOutlines = InsertPanelContainerDialog.showDialog(textComponent, new CssOutlinesPanel(selectedText));
			
			if (cssOutlines != null) {
				textComponent.replaceSelection(cssOutlines);
			}
		}
	}

	/**
	 * Insert CSS box shadow.
	 */
	protected void onInsertCssBoxShadow() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssShadow = InsertPanelContainerDialog.showDialog(textComponent, new CssBoxShadowPanel(selectedText));
			
			if (cssShadow != null) {
				textComponent.replaceSelection(cssShadow);
			}
		}
	}

	/**
	 * Insert CSS text shadow.
	 */
	protected void onInsertCssTextShadow() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssShadow = InsertPanelContainerDialog.showDialog(textComponent, new CssTextShadowPanel(selectedText));
			
			if (cssShadow != null) {
				textComponent.replaceSelection(cssShadow);
			}
		}
	}

	/**
	 * Insert CSS background.
	 */
	protected void onInsertCssBackground() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssBackground = InsertPanelContainerDialog.showDialog(textComponent, new CssBackgroundImagesPanel(selectedText));
			
			if (cssBackground != null) {
				textComponent.replaceSelection(cssBackground);
			}
		}
	}

	/**
	 * Insert SS number.
	 */
	protected void onInsertCssNumber() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssNumber = InsertPanelContainerDialog.showDialog(textComponent, new CssNumberPanel(selectedText));
			
			if (cssNumber != null) {
				textComponent.replaceSelection(cssNumber);
			}
		}
	}

	/**
	 * Insert border radius.
	 */
	protected void onInsertCssBorderRadius() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssBorderRadius = InsertPanelContainerDialog.showDialog(textComponent, new CssBorderRadiusPanel(selectedText));
			
			if (cssBorderRadius != null) {
				textComponent.replaceSelection(cssBorderRadius);
			}
		}
	}

	/**
	 * Insert clip.
	 */
	protected void onInsertCssClip() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssClip = InsertPanelContainerDialog.showDialog(textComponent, new CssClipPanel(selectedText));
			
			if (cssClip != null) {
				textComponent.replaceSelection(cssClip);
			}
		}
	}

	/**
	 * Insert border image.
	 */
	protected void onInsertCssBorderImage() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssBorderImage = InsertPanelContainerDialog.showDialog(textComponent, new CssBorderImagePanel(selectedText));
			
			if (cssBorderImage != null) {
				textComponent.replaceSelection(cssBorderImage);
			}
		}
	}

	/**
	 * Insert flex.
	 */
	protected void onInsertCssFlex() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssFlex = InsertPanelContainerDialog.showDialog(textComponent, new CssFlexPanel(selectedText));
			
			if (cssFlex != null) {
				textComponent.replaceSelection(cssFlex);
			}
		}
	}

	/**
	 * Insert spacing.
	 */
	protected void onInsertCssSpacing() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssSpacing = InsertPanelContainerDialog.showDialog(textComponent, new CssSpacingPanel(selectedText));
			
			if (cssSpacing != null) {
				textComponent.replaceSelection(cssSpacing);
			}
		}
	}
	
	/**
	 * Insert counter.
	 */
	protected void onInsertCssCounter() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssCounter = InsertPanelContainerDialog.showDialog(textComponent, new CssCountersPanel(selectedText));
			
			if (cssCounter != null) {
				textComponent.replaceSelection(cssCounter);
			}
		}
	}
	
	/**
	 * Insert list style.
	 */
	protected void onInsertCssListStyle() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssListStyle = InsertPanelContainerDialog.showDialog(textComponent, new CssListStylePanel(selectedText));
			
			if (cssListStyle != null) {
				textComponent.replaceSelection(cssListStyle);
			}
		}
	}

	/**
	 * Insert keyframes.
	 */
	protected void onInsertCssKeyframes() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssKeyframes = InsertPanelContainerDialog.showDialog(textComponent, new CssKeyframesPanel(selectedText));
			
			if (cssKeyframes != null) {
				textComponent.replaceSelection(cssKeyframes);
			}
		}
	}

	/**
	 * Insert animation.
	 */
	protected void onInsertCssAnimation() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssAnimation = InsertPanelContainerDialog.showDialog(textComponent, new CssAnimationPanel(selectedText));
			
			if (cssAnimation != null) {
				textComponent.replaceSelection(cssAnimation);
			}
		}
	}

	/**
	 * Insert perspective origin.
	 */
	protected void onInsertCssPerspectiveOrigin() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssPerspectiveOrigin = InsertPanelContainerDialog.showDialog(textComponent, new CssPerspectiveOriginPanel(selectedText));
			
			if (cssPerspectiveOrigin != null) {
				textComponent.replaceSelection(cssPerspectiveOrigin);
			}
		}
	}

	/**
	 * Insert transform.
	 */
	protected void onInsertCssTransform() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssTransform = InsertPanelContainerDialog.showDialog(textComponent, new CssTransformPanel(selectedText));
			
			if (cssTransform != null) {
				textComponent.replaceSelection(cssTransform);
			}
		}
	}
	
	/**
	 * Insert transform origin.
	 */
	protected void onInsertCssTransformOrigin() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssTransformOrigin = InsertPanelContainerDialog.showDialog(textComponent, new CssTransformOriginPanel(selectedText));
			
			if (cssTransformOrigin != null) {
				textComponent.replaceSelection(cssTransformOrigin);
			}
		}
	}
	
	/**
	 * Insert transition.
	 */
	protected void onInsertCssTransition() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssTransition = InsertPanelContainerDialog.showDialog(textComponent, new CssTransitionPanel(selectedText));
			
			if (cssTransition != null) {
				textComponent.replaceSelection(cssTransition);
			}
		}
	}

	/**
	 * Insert cursor.
	 */
	protected void onInsertCssCursor() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssCursor = InsertPanelContainerDialog.showDialog(textComponent, new CssCursorPanel(selectedText));
			
			if (cssCursor != null) {
				textComponent.replaceSelection(cssCursor);
			}
		}
	}

	/**
	 * Insert quotes.
	 */
	protected void onInsertCssQuotes() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssQuotes = InsertPanelContainerDialog.showDialog(textComponent, new CssQuotesPanel(selectedText));
			
			if (cssQuotes != null) {
				textComponent.replaceSelection(cssQuotes);
			}
		}
	}

	/**
	 * Insert text line.
	 */
	protected void onInsertCssTextLine() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssTextLine = InsertPanelContainerDialog.showDialog(textComponent, new CssTextLinePanel(selectedText));
			
			if (cssTextLine != null) {
				textComponent.replaceSelection(cssTextLine);
			}
		}
	}
	
	/**
	 * Insert resource.
	 */
	protected void onInsertCssResource() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssResourceUrl = InsertPanelContainerDialog.showDialog(textComponent, new CssResourcePanel(selectedText, false));
			
			if (cssResourceUrl != null) {
				textComponent.replaceSelection(cssResourceUrl);
			}
		}
	}
	
	/**
	 * Insert resource URL.
	 */
	protected void onInsertCssResourceUrl() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssResourceUrl = InsertPanelContainerDialog.showDialog(textComponent, new CssResourcePanel(selectedText, true));
			
			if (cssResourceUrl != null) {
				textComponent.replaceSelection(cssResourceUrl);
			}
		}
	}
	
	/**
	 * Insert anchor.
	 */
	protected void onInsertAnchor() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			String anchorText = AnchorDialog.showDialog(this, selectedText);
			
			if (anchorText != null) {
				textComponent.replaceSelection(anchorText);
			}
		}
	}

	/**
	 * Insert resources URLs.
	 */
	protected void onInsertCssResourcesUrls() {
		
		if (textComponent.isEditable()) {
			
			String selectedText = textComponent.getSelectedText();
			
			String cssResourcesUrls = InsertPanelContainerDialog.showDialog(textComponent, new CssResourcesUrlsPanel(selectedText));
			
			if (cssResourcesUrls != null) {
				textComponent.replaceSelection(cssResourcesUrls);
			}
		}
	}
	
	/**
	 * Insert menu item.
	 * @param ordinal
	 * @param itemTextResource
	 * @param itemIconResource
	 * @param callback
	 */
	public void insertItem(int ordinal, String itemTextResource, String itemIconResource, Runnable callback) {
		
		// Create menu item.
		JMenuItem menuItem = new JMenuItem();
		
		// Set item text and icon.
		menuItem.setText(Resources.getString(itemTextResource));
		if (itemIconResource != null) {
			menuItem.setIcon(Images.getIcon(itemIconResource));
		}
		
		// Add callback.
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				callback.run();
			}
		});
		
		// Insert new menu item.
		this.insert(menuItem, ordinal);
	}
	
	/**
	 * Insert separator.
	 * @param ordinal
	 */
	public void insertSeparator(int ordinal) {
		
		this.insert(new JSeparator(), ordinal);
	}
}
