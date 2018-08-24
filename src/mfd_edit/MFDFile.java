package mfd_edit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MFDFile {
	private byte headerData[];
	private ArrayList<MFDRecord> recordList = new ArrayList<>();

	public ArrayList<MFDRecord> getRecordList() {
		return recordList;
	}

	public void setRecordList(ArrayList<MFDRecord> recordList) {
		this.recordList = recordList;
	}

	private int readHeader(InputStream s) throws IOException {
		byte header[] = new byte[36];
		s.read(header);
		this.headerData = Arrays.copyOf(header, 34);

		return MFDRecord.bytesToUInt(header[34], header[35]);
	}

	private void readTrailer(InputStream s) throws IOException {
		byte[] trailerHeader = new byte[8];
		s.read(trailerHeader);

		if (!new String(Arrays.copyOfRange(trailerHeader, 0, 4)).equals("FPhd")) {
			throw new IOException("TrailerHeader invalid");
		}
		if ((trailerHeader[4] != 0) || (trailerHeader[5] != 0)) {
			throw new IOException("TrailerHeader invalid");
		}

		int trailer_len = MFDRecord.bytesToUInt(trailerHeader[6], trailerHeader[7]);

		/* Trailer Records */
		while (trailer_len > 0) {
			byte trailerRecord[] = new byte[9];
			s.read(trailerRecord);

			if (!new String(Arrays.copyOfRange(trailerRecord, 0, 4)).equals("FPdt")) {
				throw new IOException("TrailerTrailer invalid");
			}

			int recordLen = MFDRecord.bytesToUInt(trailerRecord[4], trailerRecord[5]);
			int recordOffset = MFDRecord.bytesToUInt(trailerRecord[6], trailerRecord[7]);
			int recordFlags = MFDRecord.byteToUInt(trailerRecord[8]);

			byte recordData[] = new byte[recordLen - 3];
			s.read(recordData);
			String stylePath = new String(recordData, "ISO-8859-1");

			this.recordList.get(recordOffset).updateExtPath(stylePath, recordFlags != 0);

			trailer_len -= 9;
			trailer_len -= recordLen - 3;
		}

		/* TrailerTrailer */
		byte trailerTrailer[] = new byte[4];
		s.read(trailerTrailer);
		if (!new String(Arrays.copyOfRange(trailerTrailer, 0, 4)).equals("FPed")) {
			throw new IOException("TrailerTrailer invalid");
		}
	}

	public void export(OutputStream s) throws IOException {
		s.write(this.headerData);

		s.write(MFDRecord.getUpperByte(recordList.size()));
		s.write(MFDRecord.getLowerByte(recordList.size()));

		int rec_no = 0;
		for (MFDRecord r : recordList) {
			r.export(s, rec_no);
			rec_no++;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		/* Trailer Header */
		s.write("FPhd".getBytes());
		s.write(0);
		s.write(0);

		/*
		 * Trailer length will be outputted, after the records have been
		 * temporarily written, to get length in bytes
		 */

		/* Trailer Records */
		rec_no = 0;
		for (MFDRecord r : recordList) {
			r.export_ext(bos, rec_no);
			rec_no++;
		}

		int trailerLen = bos.toByteArray().length;
		s.write(MFDRecord.getUpperByte(trailerLen));
		s.write(MFDRecord.getLowerByte(trailerLen));
		s.write(bos.toByteArray());

		/* Trailer Trailer */
		s.write("FPed".getBytes());
	}

	public MFDFile() {
		// new String("MDB-100-100-3000TYROS5\0v1.13\0\0\0\0\0\0");
		this.headerData = new byte[] {
				0x4D,
				0x44,
				0x42,
				0x2D,
				0x31,
				0x30,
				0x30,
				0x2D,
				0x31,
				0x30,
				0x30,
				0x2D,
				0x33,
				0x30,
				0x30,
				0x30,
				0x54,
				0x59,
				0x52,
				0x4F,
				0x53,
				0x35,
				0x00,
				0x76,
				0x31,
				0x2E,
				0x31,
				0x33,
				0x00,
				0x00,
				0x00,
				0x00,
				0x00,
				0x00 };
	}

	public MFDFile(InputStream s) throws IOException {
		int no_records = this.readHeader(s);
		for (int i = 0; i < no_records; i++) {
			recordList.add(new MFDRecord(s));
		}

		this.readTrailer(s);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (MFDRecord r : recordList) {
			sb.append("");
			sb.append(r);
			sb.append("\n");
		}
		return sb.toString();
	}

	public void addEmptyRecord() {
		this.recordList.add(new MFDRecord());
	}

	public void removeRecord(int row) {
		this.recordList.remove(row);
	}
}
