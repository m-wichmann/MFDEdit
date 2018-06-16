package mfd_edit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;

import mfd_edit.MFDRecord.IntroNextId;

public class Main {

	private MFDFile mfdFile;
	private MFDTableModel tm;

	private void runUI() {
		JFrame frame = new JFrame("MFDEdit");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Datei");
		menuBar.add(menu);

		JMenuItem menuItemOpen = new JMenuItem("Laden...");
		menuItemOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();

				// TODO: set real path (maybe last)
				c.setCurrentDirectory(
						new File("/media/data2/Dokumente/Programming/workspaces/workspace_java/mfd_edit/"));

				c.setFileFilter(new FileNameExtensionFilter("MFD File", "mfd"));
				int rVal = c.showOpenDialog(frame);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					try {
						mfdFile = new MFDFile(new FileInputStream(c.getSelectedFile()));
						tm.setMfdFile(mfdFile);
						tm.fireTableDataChanged();
					} catch (IOException e1) {
						// ignore error
					}
				}
			}
		});
		menu.add(menuItemOpen);

		JMenuItem menuItemSave = new JMenuItem("Speichern...");
		menuItemSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
				// c.setCurrentDirectory(dir);
				c.setFileFilter(new FileNameExtensionFilter("MFD File", "mfd"));
				int rVal = c.showSaveDialog(frame);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					if (mfdFile != null) {
						try {
							mfdFile.export(new FileOutputStream(c.getSelectedFile()));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});
		menu.add(menuItemSave);

		menu.addSeparator();

		JMenuItem menuItemClose = new JMenuItem("Schließen");
		menuItemClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: save stuff?!
				System.exit(0);
			}
		});
		menu.add(menuItemClose);

		frame.setJMenuBar(menuBar);

		tm = new MFDTableModel();
		JTable table = new JTable(tm);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		
		JComboBox<String> styleComboBox = new JComboBox<String>();
		for (String style : Tyros5Styles.getStyleList()) {
			styleComboBox.addItem(style);	
		}
		TableColumn styleColumn = table.getColumnModel().getColumn(1);
		styleColumn.setCellEditor(new DefaultCellEditor(styleComboBox));

		
		JComboBox<String> introComboBox = new JComboBox<String>();
		JComboBox<String> nextComboBox = new JComboBox<String>();
		for (IntroNextId introNextId : MFDRecord.IntroNextId.values()) {
			introComboBox.addItem(introNextId.name());
			nextComboBox.addItem(introNextId.name());
		}
		TableColumn introColumn = table.getColumnModel().getColumn(9);
		introColumn.setCellEditor(new DefaultCellEditor(introComboBox));
		TableColumn nextColumn = table.getColumnModel().getColumn(10);
		nextColumn.setCellEditor(new DefaultCellEditor(nextComboBox));
		
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItemAdd = new JMenuItem("Add");
		popupMenu.add(menuItemAdd);
		JMenuItem menuItemRemove = new JMenuItem("Remove");
		popupMenu.add(menuItemRemove);
		frame.add(popupMenu);
		
		frame.add(scrollPane);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		new Main().runUI();
	}
}
