package mfd_edit;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBoxMenuItem;
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

public class UI {

	private JFrame frame;
	private JTable table;
	private JPopupMenu popupMenu;
	JCheckBoxMenuItem menuItemEditEnable;
	private MFDFile mfdFile;
	private MFDTableModel tm;

	public class OpenActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();

			// TODO: set real path (maybe last)
			c.setCurrentDirectory(
					new File("/media/data2/Dokumente/Programming/workspaces/workspace_java/MFDEdit/test"));

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
	}

	public class SaveActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();

			// TODO: set real path (maybe last)
			c.setCurrentDirectory(
					new File("/media/data2/Dokumente/Programming/workspaces/workspace_java/MFDEdit/test"));

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
	}

	public class CloseActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO: save stuff?!
			System.exit(0);
		}
	}

	public class CopyActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Clipboard system =
			// Toolkit.getDefaultToolkit().getSystemClipboard();
			//
			// system.setContents(contents, owner);
		}
	}

	public class PasteActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

		}
	}

	public class EditActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tm.setEditable(menuItemEditEnable.getState());
		}
	}

	public class AddActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			tm.addEmptyRow();
			tm.fireTableDataChanged();
		}
	}

	public class RemoveActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int[] rows = table.getSelectedRows();
			for (int row : rows) {
				tm.removeRow(table.convertRowIndexToModel(row));
			}
		}
	}

	public class TableMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				int rowAtPoint = table.rowAtPoint(new Point(e.getX(), e.getY()));
				if (rowAtPoint > -1) {
					table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
				}

				if (table.isEditing()) {
					table.getCellEditor().cancelCellEditing();
				}

				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void buildMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("Datei");
		menuBar.add(menuFile);

		JMenuItem menuItemOpen = new JMenuItem("Laden...");
		menuItemOpen.addActionListener(new OpenActionListener());
		menuFile.add(menuItemOpen);
		JMenuItem menuItemSave = new JMenuItem("Speichern...");
		menuItemSave.addActionListener(new SaveActionListener());
		menuFile.add(menuItemSave);
		menuFile.addSeparator();
		JMenuItem menuItemClose = new JMenuItem("Schließen");
		menuItemClose.addActionListener(new CloseActionListener());
		menuFile.add(menuItemClose);

		JMenu menuEdit = new JMenu("Bearbeiten");
		menuBar.add(menuEdit);

		// JMenuItem menuItemCopy = new JMenuItem("Kopieren");
		// menuItemCopy.addActionListener(new CopyActionListener());
		// menuEdit.add(menuItemCopy);
		// JMenuItem menuItemPaste = new JMenuItem("Einfügen");
		// menuItemPaste.addActionListener(new PasteActionListener());
		// menuEdit.add(menuItemPaste);
		// menuEdit.addSeparator();
		menuItemEditEnable = new JCheckBoxMenuItem("Editieren");
		menuItemEditEnable.addActionListener(new EditActionListener());
		menuEdit.add(menuItemEditEnable);

		frame.setJMenuBar(menuBar);
	}

	public void buildTable() {
		tm = new MFDTableModel();
		table = new JTable(tm);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.getRowSorter().toggleSortOrder(0);

		JScrollPane scrollPane = new JScrollPane(table);
		frame.add(scrollPane);
	}

	public void buildTableEditors() {
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
	}

	public void buildPopupMenu() {
		popupMenu = new JPopupMenu();

		JMenuItem menuItemAdd = new JMenuItem("Add");
		popupMenu.add(menuItemAdd);
		menuItemAdd.addActionListener(new AddActionListener());
		JMenuItem menuItemRemove = new JMenuItem("Remove");
		popupMenu.add(menuItemRemove);
		menuItemRemove.addActionListener(new RemoveActionListener());

		table.addMouseListener(new TableMouseListener());
	}

	public void buildShortcuts() {
		// KeyStroke keyStrokeCopy = KeyStroke.getKeyStroke(KeyEvent.VK_C,
		// ActionEvent.CTRL_MASK, false);
		// KeyStroke keyStrokePaste = KeyStroke.getKeyStroke(KeyEvent.VK_V,
		// ActionEvent.CTRL_MASK, false);
		//
		// table.registerKeyboardAction(new CopyActionListener(), "Kopieren",
		// keyStrokeCopy, JComponent.WHEN_FOCUSED);
		// table.registerKeyboardAction(new PasteActionListener(), "Einfügen",
		// keyStrokePaste, JComponent.WHEN_FOCUSED);
	}

	public UI() {
		frame = new JFrame("MFDEdit");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1200, 800));

		this.buildMenu();
		this.buildTable();
		this.buildTableEditors();
		this.buildPopupMenu();
		this.buildShortcuts();

		frame.pack();
		frame.setVisible(true);
	}

	public void run() {

	}
}
