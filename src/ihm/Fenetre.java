package ihm;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import csf.Csf;
import csf.ZipFile;

public class Fenetre extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Fenetre mainWindowInstance = null;
	private Menu menuBar = new Menu();
	private TreePanel myTreePanel = new TreePanel();
	private EditPanel myEditPanel = new EditPanel();
	private Csf csfFile = null;
	private JSplitPane split;
	private ZipFile zipOpened = null;

	
	private Fenetre() {
		//Titre
		this.setTitle("Csf Editor");
		//Taille
		this.setSize(1024, 768);
		//Position center
        this.setLocationRelativeTo(null);
        //Close event
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        //On ajoute le menu
        this.setJMenuBar(menuBar);

        //Ajout des panel
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, myTreePanel, myEditPanel);

        this.getContentPane().add(split, BorderLayout.CENTER);
        
        this.setVisible(true);

		
	}
	
	public Csf getCsf() {
		return csfFile;
	}
	
	public static Fenetre getInstance() {
		if (mainWindowInstance == null) {
			mainWindowInstance = new Fenetre();
		}
		
		return mainWindowInstance;
	}

	public void showErreur(String errMsg) {
		JOptionPane.showMessageDialog(this, errMsg, "Erreur", JOptionPane.ERROR_MESSAGE);
	}
	
	public void openFile(String filePath) {
		int resultConfirm;
		
		//Creation de la ressource
		if (csfFile == null ) {
			csfFile = new Csf(filePath);
		} else {
			resultConfirm = JOptionPane.showConfirmDialog(this, "Etes vous sur de vouloir fermer l'ancien fichier ?");
			
			if (resultConfirm == JOptionPane.OK_OPTION) {
				csfFile.close();
				
				if (!csfFile.isValid()) {
					showErreur(csfFile.getErreurMessage());
					return;
				}
				
				csfFile = new Csf(filePath);
			} else {
				return;
			}
		}
		
		//On verifie le fichier
		if (!csfFile.isValid()) {
			//Si c'est pas bon on affiche l'erreur
			showErreur(csfFile.getErreurMessage());
		} else {
			//Sinon on affiche le panel d'arbre
			myTreePanel.show(csfFile.getFileList());
			//On ajoute sur le menu

		}
	}
	
	public void saveFile() {
		saveFile(csfFile.getFilePath());
	}
	
	public void saveFile(String filePath) {
		
		try {
			File newFile = new File(filePath);
			if (!newFile.createNewFile()) {
				int returnOption = JOptionPane.showConfirmDialog(this, "Are you sure to overwrite this file ?", "Overwrite", JOptionPane.YES_NO_OPTION);
				if (returnOption != JOptionPane.YES_OPTION) {
					return;
				}
			}
			csfFile.save(newFile);
		} catch (IOException e) {
			showErreur(e.getMessage());
		}
	}
	
	public void closeFile() {
		//On libère les ressources
		csfFile.close();
		
		if (!csfFile.isValid()) {
			showErreur(csfFile.getErreurMessage());
			return;
		}
		
		//On cache les panel
		
		//On enleve sur le menu
		
	}
	
	public void closeProgram() {
		//TODO Verification des buffer en cours
		if (csfFile != null) {
			csfFile.close();
		}
		
		System.exit(0);
	}

	public void openZipFile(String fileName) {
		try {
			ZipFile zipToOpen;
			if (csfFile.getFileList().containsKey(fileName)) {
				if (zipOpened != null && zipOpened.isModify()) {
					JOptionPane.showConfirmDialog(this, "Voulez vous enregistrer le fichier avant de continuer ? Attention toute modification non enregistré sera perdue.");
				}
				zipToOpen = csfFile.getFileList().get(fileName);
				csfFile.setData(zipToOpen);
				myEditPanel.setFile(zipToOpen);
				zipOpened = zipToOpen;
			}
		} catch (IOException e) {
			showErreur(e.getMessage());
		} catch (DataFormatException e) {
			showErreur(e.getMessage());
		}
	}
	
	public void reloadFile() {
		try {
			csfFile.setData(zipOpened);
			myEditPanel.setFile(zipOpened);
		} catch (IOException e) {
			showErreur(e.getMessage());
		} catch (DataFormatException e) {
			showErreur(e.getMessage());
		}
	}
	
	
	

}
