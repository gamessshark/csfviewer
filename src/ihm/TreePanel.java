package ihm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import csf.ZipFile;



public class TreePanel extends JScrollPane {

	/**
	 * 
	 */
	private JTree zipTree;
	private HashMap<String, ZipFile> zipList;
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode("CSF");
	
	private static final long serialVersionUID = 1L;
	public TreePanel() {
		super();
		zipTree = new JTree(root);
		zipTree.setRootVisible(true);
		
		this.setViewportView(zipTree);
		
	}
	

	public void show(HashMap<String, ZipFile> zipList) {
		this.zipList = zipList;
		//Create the root
		this.createTree();
	}
	
	private void createTree() {
		HashMap<String, DefaultMutableTreeNode> repList = new HashMap<String, DefaultMutableTreeNode>();
		
		Set<String> listFile = zipList.keySet();
		
		Iterator<String> i = listFile.iterator();
		
		while(i.hasNext()) {
			String file = i.next();
			file = file.toLowerCase();
			String[] listRep = file.split("/");
			
			DefaultMutableTreeNode tempRep = root;
			if (listRep.length > 1) {
				String fileName = listRep[listRep.length - 1];
				String repName = "";
				for(int j = 0; j<listRep.length - 1; j++) {
					repName += listRep[j] + "/";
					if (repList.containsKey(repName)) {
						tempRep = repList.get(repName);
						
					} else {
						DefaultMutableTreeNode newRep = new DefaultMutableTreeNode(listRep[j]);
						repList.put(repName, newRep);
						tempRep.add(newRep);
						tempRep = newRep;
					}
				}
				tempRep.add(new DefaultMutableTreeNode(fileName));
			} else {
				root.add(new DefaultMutableTreeNode(file));
			}
		}
	}
}
