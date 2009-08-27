package ihm;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class EditPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea contentFile;
	
	public EditPanel() {
		super();
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setSize(500, 768);
		contentFile = new JTextArea();
		JLabel fileName = new JLabel("No fil selected");
		
		fileName.setLabelFor(contentFile);
		
		this.add(fileName);
        this.add(Box.createRigidArea(new Dimension(0,5)));
        
        this.add(new JScrollPane(contentFile));
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();

		
		this.setVisible(true);
	}
	
	public void setText(String text) {
		contentFile.setText(text);
	}
	
}
