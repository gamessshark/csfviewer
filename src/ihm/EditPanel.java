package ihm;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import c9u.C9UDecoder;
import c9u.C9UString;

import csf.C9Type;
import csf.ZipFile;

public class EditPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea contentFile;
	private JLabel fileName;
	private ZipFile editFile;
	private String currCharset = "UTF-8";
	private JRadioButton utf8Button;
	private JRadioButton utf16Button;
	
	public EditPanel() {
		super();
		
			
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setSize(500, 768);
		
		contentFile = new JTextArea();
		contentFile.setTabSize(4);
		
		fileName = new JLabel("No file selected");
		fileName.setLabelFor(contentFile);
		
		contentFile.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
				
			}

			public void keyReleased(KeyEvent arg0) {
				
			}

			public void keyTyped(KeyEvent arg0) {
				if (editFile != null && !editFile.isModify()) {
					editFile.setModify(true);
					fileName.setText(fileName.getText() + " *");
				}
			}
			
		});
		
		//Pannel d'entete
		JPanel headPanel = new JPanel();
		headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.LINE_AXIS));
		
		//Radio button
		JLabel labChars = new JLabel("Charset : ");
		utf8Button = new JRadioButton("UTF-8");
		utf8Button.setActionCommand("UTF-8");
		utf8Button.addActionListener(this);
		utf8Button.setSelected(true);
		utf16Button = new JRadioButton("UTF-16");
		utf16Button.setActionCommand("UTF-16");
		utf16Button.addActionListener(this);
		JRadioButton utfKRButton = new JRadioButton("EUC-KR");
		utfKRButton.setActionCommand("EUC-KR");
		utfKRButton.addActionListener(this);
		
		ButtonGroup group = new ButtonGroup();
		group.add(utf8Button);
		group.add(utf16Button);
		group.add(utfKRButton);
		
		headPanel.add(fileName);
		headPanel.add(Box.createRigidArea(new Dimension(30, 0)));
		headPanel.add(labChars);
		headPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		headPanel.add(utf8Button);
		headPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		headPanel.add(utf16Button);
		headPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		headPanel.add(utfKRButton);
		headPanel.add(Box.createHorizontalGlue());
		
		this.add(headPanel);
        this.add(Box.createRigidArea(new Dimension(0,5)));
        JScrollPane scrollPane = new JScrollPane(contentFile);
       	this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        this.add(scrollPane);
        this.add(Box.createRigidArea(new Dimension(0,5)));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton reloadButton = new JButton("Reload");
        JButton exportButton = new JButton("Export");
        
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(saveButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(reloadButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(exportButton);
        
        //Action for the button
        reloadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Fenetre.getInstance().reloadFile();				
			}
        });
        saveButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (editFile.getType() == C9Type.Text) {
					editFile.setData(contentFile.getText().getBytes(Charset.forName(currCharset)));
					editFile.setModify(false);
					editFile.setSave(true);
					fileName.setText(fileName.getText().substring(0, fileName.getText().length() - 2));
					Fenetre.getInstance().getCsf().setModify(true);
        		}
        		if (editFile.getType() == C9Type.C9U) {
        			String[] listNewString = contentFile.getText().split("\n");
        			ArrayList<C9UString> listOldString = editFile.getListString();
        			for(int i = 0; i<listOldString.size(); i++) {
        				if (listNewString[i] != "") {
        					listOldString.get(i).setNewString(listNewString[i]);
        				}
        			}
        			editFile.rewriteC9UFile();
        			editFile.setModify(false);
        			editFile.setSave(true);
        			fileName.setText(fileName.getText().substring(0, fileName.getText().length() - 2));
					Fenetre.getInstance().getCsf().setModify(true);
        		}
			}
        });
        
        this.add(buttonPane);
		
		this.setVisible(true);
	}
	
	public void setFile(ZipFile pFile) {
		editFile = pFile;
		if (editFile.getType() != C9Type.Text) {
			if (editFile.getType() != C9Type.C9U) {
				Fenetre.getInstance().showErreur("Cannot open file of this format");
			} else {
				BufferedInputStream fileBuffStream = new BufferedInputStream(new ByteArrayInputStream(editFile.getData()));
				try {
					ArrayList<C9UString> listString;
					if (editFile.getListString() == null) {
						listString = C9UDecoder.parse(fileBuffStream);
						editFile.setListString(listString);
					}
					listString = editFile.getListString();
					String textContent = "";
					Iterator<C9UString> iString = listString.iterator();
					while (iString.hasNext()) {
						C9UString strTemp = iString.next();
						if (strTemp.getNewString() == null) {
							textContent += strTemp.getOldString() + "\n";
						} else {
							textContent += strTemp.getNewString() + "\n";
						}
					}
					contentFile.setText(textContent);
					fileName.setText("File : " + pFile.getName());
					currCharset = "UTF-16";
					utf16Button.setSelected(true);
					fileBuffStream.close();
				} catch (IOException e) {
					Fenetre.getInstance().showErreur(e.getMessage());
				}
			}
		} else {
			contentFile.setText(pFile.getTextData());
			currCharset = "UTF-8";
			utf8Button.setSelected(true);
			fileName.setText("File : " + pFile.getName());
		}
	}
	
	public void setText(String text) {
		contentFile.setText(text);
	}

	
	public void actionPerformed(ActionEvent e) {
		if (editFile != null && !currCharset.equals(e.getActionCommand()) && editFile.getType() == C9Type.Text) {
			if (editFile.isModify()) {
				byte[] textByte = contentFile.getText().getBytes(Charset.forName(currCharset));
				this.setText(new String(textByte, Charset.forName(e.getActionCommand())));
			} else {
				this.setText(editFile.getTextData(Charset.forName(e.getActionCommand())));
			}
			currCharset = e.getActionCommand();
		}
	}
	
}
