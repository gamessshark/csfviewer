package main;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ihm.Fenetre;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			   //On force à utiliser le look and feel du system
			   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			   //Ici on force tous les composants de notre fenêtre (this) à se redessiner avec le look and feel du système
			} catch (InstantiationException e) {
			} catch (ClassNotFoundException e) {
			} catch (UnsupportedLookAndFeelException e) {
			} catch (IllegalAccessException e) {}

		
		Fenetre.getInstance();
	}

}
