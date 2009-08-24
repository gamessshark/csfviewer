package ihm;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Menu extends JMenuBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JMenu fileMenu = new JMenu("Fichier");
	
	private JMenuItem openItem = new JMenuItem("Ouvrir");
	private JMenuItem saveItem = new JMenuItem("Sauvegarder");
	private JMenuItem saveAsItem = new JMenuItem("Sauvegarder a ...");
	private JMenuItem closeItem = new JMenuItem("Quitter");
	
	public Menu () {
		closeItem.addActionListener(new CloseListener());
		openItem.addActionListener(new OpenListener());
		
		fileMenu.setMnemonic('F');
		
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(closeItem);
		
		this.add(fileMenu);
	}
	
	public class CloseListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			Fenetre.getInstance().closeProgram();
		}

	}
	
	public class OpenListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			FileDialog chooseFile = new FileDialog(Fenetre.getInstance());
			
			chooseFile.setVisible(true);
			
			Fenetre.getInstance().openFile(chooseFile.getDirectory() + chooseFile.getFile());
		}
	
	}

}