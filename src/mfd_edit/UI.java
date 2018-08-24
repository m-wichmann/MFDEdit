package mfd_edit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import mfd_edit.MFDRecord.IntroNextId;

public class UI {

	private Settings settings;
	private JFrame frame;
	private JTable table;
	private TableRowSorter<MFDTableModel> sorter;
	private JPopupMenu popupMenu;
	JCheckBoxMenuItem menuItemEditEnable;
	private MFDFile mfdFile;
	private MFDTableModel tm;
	private JFrame infoFrame;
	private JTextField filterInput;

	public class OpenActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();

			c.setCurrentDirectory(new File(settings.getLastSaveDir()));

			c.setFileFilter(new FileNameExtensionFilter("MFD File", "mfd"));
			int rVal = c.showOpenDialog(frame);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				settings.setLastSaveDir(c.getSelectedFile().toString());
				settings.save();

				try {
					mfdFile = new MFDFile(new FileInputStream(c.getSelectedFile()));
					tm.setMfdFile(mfdFile);
					tm.fireTableDataChanged();
				} catch (IOException e1) {
					// ignore error
					System.out.println(e1);
				}
			}
		}
	}

	public class SaveActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser c = new JFileChooser();

			c.setCurrentDirectory(new File(settings.getLastSaveDir()));

			c.setFileFilter(new FileNameExtensionFilter("MFD File", "mfd"));
			int rVal = c.showSaveDialog(frame);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				settings.setLastSaveDir(c.getSelectedFile().toString());
				settings.save();

				if (mfdFile != null) {
					try {
						mfdFile.export(new FileOutputStream(c.getSelectedFile()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public class InfoActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			infoFrame.setVisible(true);
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
			int[] rows = table.getSelectedRows();

			MFDRecordList copyList = new MFDRecordList();
			for (int i : rows) {
				copyList.add(mfdFile.getRecordList().get(table.convertRowIndexToModel(i)));
			}

			Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
			system.setContents(copyList, null);
		}
	}

	public class PasteActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			MFDRecordList recordList = null;

			try {
				Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
				recordList = (MFDRecordList) system.getData(MFDRecordList.mfdRecordListFlavor);
			} catch (UnsupportedFlavorException | IOException e1) {
				e1.printStackTrace();
			}

			if ((mfdFile != null) && (recordList != null)) {
				mfdFile.getRecordList().addAll(recordList);
				tm.fireTableDataChanged();
			}
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
			ArrayList<Integer> rowList = new ArrayList<>();
			for (int row : rows) {
				rowList.add(row);
			}
			rowList.sort(Comparator.comparing(Integer::intValue).reversed());

			for (Integer row : rowList) {
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
					/*
					 * Select row, if user clicked outside of already selected
					 * rows
					 */
					int[] selectedRows = table.getSelectedRows();
					if (Arrays.binarySearch(selectedRows, rowAtPoint) < 0) {
						table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
					}
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
		JMenuItem menuItemInfo = new JMenuItem("Info");
		menuItemInfo.addActionListener(new InfoActionListener());
		menuFile.add(menuItemInfo);
		menuFile.addSeparator();
		JMenuItem menuItemClose = new JMenuItem("Schließen");
		menuItemClose.addActionListener(new CloseActionListener());
		menuFile.add(menuItemClose);

		JMenu menuEdit = new JMenu("Bearbeiten");
		menuBar.add(menuEdit);

		JMenuItem menuItemCopy = new JMenuItem("Kopieren");
		menuItemCopy.addActionListener(new CopyActionListener());
		menuEdit.add(menuItemCopy);
		JMenuItem menuItemPaste = new JMenuItem("Einfügen");
		menuItemPaste.addActionListener(new PasteActionListener());
		menuEdit.add(menuItemPaste);
		menuEdit.addSeparator();
		menuItemEditEnable = new JCheckBoxMenuItem("Editieren");
		menuItemEditEnable.addActionListener(new EditActionListener());
		menuEdit.add(menuItemEditEnable);

		frame.setJMenuBar(menuBar);
	}

	public JComponent buildTable() {
		tm = new MFDTableModel();

		table = new JTable(tm);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		sorter = new TableRowSorter<MFDTableModel>(tm);
		sorter.toggleSortOrder(0);
		table.setRowSorter(sorter);

		return new JScrollPane(table);
	}

	private void updateFilter(String s) {
		try {
			filterInput.setBackground(UIManager.getColor("TextField.background"));

			RowFilter<MFDTableModel, Object> filter = RowFilter.regexFilter("(?i)" + s);
			sorter.setRowFilter(filter);
		} catch (PatternSyntaxException e) {
			/* Reuse old filter */
			filterInput.setBackground(Color.RED);
		}
	}

	public JComponent buildFilter() {
		JPanel filterPane = new JPanel();
		filterPane.setLayout(new BoxLayout(filterPane, BoxLayout.LINE_AXIS));

		JLabel filterLabel = new JLabel("Filter:");

		filterInput = new JTextField();
		filterInput.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFilter(filterInput.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFilter(filterInput.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFilter(filterInput.getText());
			}
		});

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterInput.setText("");
			}
		});

		filterLabel.setMinimumSize(new Dimension(50, 25));
		filterLabel.setPreferredSize(new Dimension(50, 25));
		filterLabel.setMaximumSize(new Dimension(50, 25));
		filterInput.setMinimumSize(new Dimension(150, 25));
		filterInput.setPreferredSize(new Dimension(200, 25));
		filterInput.setMaximumSize(new Dimension(500, 25));
		clearButton.setMinimumSize(new Dimension(150, 25));
		clearButton.setPreferredSize(new Dimension(150, 25));
		clearButton.setMaximumSize(new Dimension(150, 25));

		filterPane.add(Box.createRigidArea(new Dimension(10, 0)));
		filterPane.add(filterLabel);
		filterPane.add(Box.createRigidArea(new Dimension(10, 0)));
		filterPane.add(filterInput);
		filterPane.add(Box.createRigidArea(new Dimension(10, 0)));
		filterPane.add(clearButton);
		filterPane.add(Box.createHorizontalGlue());

		return filterPane;
	}

	public void buildFrame(JComponent tableComp, JComponent filterComp) {
		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));

		topPane.add(filterComp);
		topPane.add(Box.createRigidArea(new Dimension(0, 4)));
		topPane.add(tableComp);

		frame.add(topPane);
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

		JMenuItem menuItemCopy = new JMenuItem("Copy");
		popupMenu.add(menuItemCopy);
		menuItemCopy.addActionListener(new CopyActionListener());
		JMenuItem menuItemPaste = new JMenuItem("Paste");
		popupMenu.add(menuItemPaste);
		menuItemPaste.addActionListener(new PasteActionListener());

		table.addMouseListener(new TableMouseListener());
	}

	public void buildShortcuts() {
		KeyStroke keyStrokeCopy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
		KeyStroke keyStrokePaste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);

		table.registerKeyboardAction(new CopyActionListener(), "Kopieren", keyStrokeCopy, JComponent.WHEN_FOCUSED);
		table.registerKeyboardAction(new PasteActionListener(), "Einfügen", keyStrokePaste, JComponent.WHEN_FOCUSED);
	}

	public void buildInfoFrame() {
		infoFrame = new JFrame();
		infoFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		infoFrame.setPreferredSize(new Dimension(400, 300));

		JPanel infoPane = new JPanel();
		infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.PAGE_AXIS));

		infoPane.add(Box.createRigidArea(new Dimension(30, 30)));

		JLabel versionLabel = new JLabel("MFDEdit " + Main.version);
		versionLabel.setFont(new Font(versionLabel.getFont().getName(), Font.PLAIN, 20));
		infoPane.add(versionLabel);

		infoPane.add(Box.createRigidArea(new Dimension(0, 30)));

		JLabel linkLabel = new JLabel("see github.com/m-wichmann/MFDEdit for details");
		infoPane.add(linkLabel);

		infoPane.add(Box.createRigidArea(new Dimension(30, 30)));

		infoFrame.add(infoPane);
		infoFrame.pack();
	}

	public UI() {
		this.settings = new Settings();

		frame = new JFrame("MFDEdit");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1200, 800));

		this.buildMenu();
		JComponent table = this.buildTable();
		JComponent filter = this.buildFilter();
		this.buildFrame(table, filter);
		this.buildTableEditors();
		this.buildPopupMenu();
		this.buildShortcuts();
		this.buildInfoFrame();

		this.mfdFile = new MFDFile();
		tm.setMfdFile(mfdFile);
		tm.fireTableDataChanged();

		frame.pack();
		frame.setVisible(true);
	}

	public void run() {

	}
}
