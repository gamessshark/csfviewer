package ihm;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.Charset;
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

	
	private Fenetre() {
		//Titre
		this.setTitle("Csf Viewer");
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
				byte[] dataFile;
				zipToOpen = csfFile.getFileList().get(fileName);
				dataFile = csfFile.getData(zipToOpen);
				
				myEditPanel.setText(new String(dataFile, Charset.forName("EUC-KR")));
			}
		} catch (IOException e) {
			showErreur(e.getMessage());
		} catch (DataFormatException e) {
			showErreur(e.getMessage());
		}
	}
	

}
