package mfd_edit;

import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

public class MFDTableModel extends AbstractTableModel {

	private static final class TableData {
		private String header;
		private Function<MFDRecord, Object> valueProvider;
		private BiConsumer<MFDRecord, Object> valueSetter;
		private Class<?> _class;

		public TableData(String header, Function<MFDRecord, Object> valueProvider,
				BiConsumer<MFDRecord, Object> valueSetter, Class<?> _class) {
			this.header = header;
			this.valueProvider = valueProvider;
			this.valueSetter = valueSetter;
			this._class = _class;
		}
	}

	private static final long serialVersionUID = -4830214666051586240L;

	private MFDFile mfdFile;
	private boolean isEditable = false;

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	private static final TableData[] tableData;
	static {
		tableData = new TableData[] {
				new TableData("Name", m -> m.title, (m, n) -> m.title = (String) n, String.class),
				new TableData("Style", m -> Tyros5Styles.fromId(m.style_no),
						(m, n) -> m.style_no = Tyros5Styles.fromName((String) n), String.class),
				new TableData("Takt", m -> m.time_upper + "/" + m.time_lower, (m, n) -> m.parseTimeString((String) n),
						String.class),
				new TableData("Tempo", m -> m.tempo, (m, n) -> m.tempo = (int) n, Integer.class),
				new TableData("Fav.", m -> m.fav, (m, n) -> m.fav = (Boolean) n, Boolean.class),
				new TableData("S1", m -> m.s1, (m, n) -> m.s1 = (Boolean) n, Boolean.class),
				new TableData("S2", m -> m.s2, (m, n) -> m.s2 = (Boolean) n, Boolean.class),
				new TableData("Genre", m -> m.genre, (m, n) -> m.genre = (String) n, String.class),
				new TableData("Keywords", m -> m.keywords, (m, n) -> m.keywords = (String) n, String.class),
				new TableData("Intro", m -> m.intro, (m, n) -> m.intro = MFDRecord.IntroNextId.valueOf((String) n),
						MFDRecord.IntroNextId.class),
				new TableData("Next", m -> m.next, (m, n) -> m.next = MFDRecord.IntroNextId.valueOf((String) n),
						MFDRecord.IntroNextId.class),
				new TableData("Style Pfad", m -> m.extStylePath, (m, n) -> m.extStylePath = (String) n, String.class),
				new TableData("isMusic", m -> m.isMusic, (m, n) -> m.isMusic = (Boolean) n, Boolean.class), };
	}

	public void setMfdFile(MFDFile mfdFile) {
		this.mfdFile = mfdFile;
	}

	public void addEmptyRow() {
		mfdFile.addEmptyRecord();
	}

	public void removeRow(int row) {
		mfdFile.removeRecord(row);
		fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		if (mfdFile != null) {
			return mfdFile.getRecordList().size();
		} else {
			return 0;
		}
	}

	@Override
	public int getColumnCount() {
		return tableData.length;
	}

	@Override
	public String getColumnName(int col) {
		return tableData[col].header;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return tableData[columnIndex]._class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return this.isEditable;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MFDRecord mfdRecord = mfdFile.getRecordList().get(rowIndex);
		tableData[columnIndex].valueSetter.accept(mfdRecord, aValue);
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		MFDRecord mfdRecord = mfdFile.getRecordList().get(rowIndex);
		return tableData[columnIndex].valueProvider.apply(mfdRecord);
	}

	public MFDFile getMfdFile() {
		return mfdFile;
	}
}
