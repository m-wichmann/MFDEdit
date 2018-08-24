package mfd_edit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

public class MFDRecordList extends ArrayList<MFDRecord> implements Transferable {

	private static final long serialVersionUID = 1968056732414793398L;

	public static final DataFlavor mfdRecordListFlavor = new DataFlavor(ArrayList.class, "MFDRecordList");

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] ret = { MFDRecordList.mfdRecordListFlavor };
		return ret;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(MFDRecordList.mfdRecordListFlavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(MFDRecordList.mfdRecordListFlavor)) {
			return this;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
