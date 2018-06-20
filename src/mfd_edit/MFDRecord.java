package mfd_edit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class MFDRecord {
	public enum IntroNextId {
		OFF(0x0), INTRO1(0x1), INTRO2(0x2), INTRO3(0x3), MAIN_A(0x4), MAIN_B(0x5), MAIN_C(0x6), MAIN_D(0x7), ENDING_A(
				0x8), ENDING_B(0x9), ENDING_C(0xA);

		private final int flag;

		private IntroNextId(int flag) {
			this.flag = flag;
		}

		public int getValue() {
			return this.flag;
		}
	}

	public int record_no;
	public int style_no;
	public int time_upper;
	public int time_lower;
	public int tempo;
	public boolean fav;
	public boolean s1;
	public boolean s2;
	public String title;
	public String genre;
	public String keywords;
	public IntroNextId intro;
	public IntroNextId next;
	public String extStylePath;
	public boolean isMusic; /* false: style; true: music */

	public static int lowerNibble(byte b) {
		return b & 0x0f;
	}

	public static int upperNibble(byte b) {
		return (b & 0xf0) >> 4;
	}

	public static int byteToUInt(byte b) {
		return b & 0xff;
	}

	public static int bytesToUInt(byte msb, byte lsb) {
		return ((msb & 0xff) << 8) | (lsb & 0xff);
	}

	public static byte getUpperByte(int i) {
		return (byte) ((i & 0xff00) >> 8);
	}

	public static byte getLowerByte(int i) {
		return (byte) (i & 0x00ff);
	}

	public MFDRecord() {
		this.record_no = 0;
		this.style_no = 0;
		this.time_upper = 4;
		this.time_lower = 4;
		this.tempo = 120;
		this.fav = false;
		this.s1 = false;
		this.s2 = false;
		this.title = "";
		this.genre = "";
		this.keywords = "";
		this.intro = IntroNextId.OFF;
		this.next = IntroNextId.OFF;
		this.extStylePath = "";
		this.isMusic = false;
	}

	public MFDRecord(InputStream s) throws IOException {
		byte record[] = new byte[92];
		s.read(record);

		if ((record[0] != 0) || (record[1] != 0)) {
			throw new IOException("MFDRecord invalid");
		}

		// details see: http://www.jososoft.dk/yamaha/articles/mff.htm

		this.record_no = bytesToUInt(record[2], record[3]);
		this.style_no = bytesToUInt(record[4], record[5]);
		this.time_upper = byteToUInt(record[6]);
		this.time_lower = byteToUInt(record[7]);
		this.tempo = bytesToUInt(record[8], record[9]);
		this.fav = (record[10] & 0b001) != 0;
		this.s1 = (record[10] & 0b010) != 0;
		this.s2 = (record[10] & 0b100) != 0;
		this.title = new String(Arrays.copyOfRange(record, 11, 11 + 32), "ISO-8859-1");
		this.genre = new String(Arrays.copyOfRange(record, 43, 43 + 16), "ISO-8859-1");
		this.keywords = new String(Arrays.copyOfRange(record, 59, 59 + 32), "ISO-8859-1");
		this.intro = IntroNextId.values()[upperNibble(record[91])];
		this.next = IntroNextId.values()[lowerNibble(record[91])];

		this.isMusic = false;
	}

	public void export(OutputStream s, int out_rec_no) throws IOException {
		s.write(0);
		s.write(0);

		s.write(getUpperByte(out_rec_no));
		s.write(getLowerByte(out_rec_no));

		s.write(getUpperByte(this.style_no));
		s.write(getLowerByte(this.style_no));
		s.write(this.time_upper);
		s.write(this.time_lower);
		s.write(getUpperByte(this.tempo));
		s.write(getLowerByte(this.tempo));

		int flags = 0;
		flags |= this.fav ? 0b001 : 0;
		flags |= this.s1 ? 0b010 : 0;
		flags |= this.s2 ? 0b100 : 0;
		s.write(flags);

		for (byte b : Arrays.copyOf(this.title.getBytes("ISO-8859-1"), 32)) {
			s.write(b);
		}

		for (byte b : Arrays.copyOf(this.genre.getBytes("ISO-8859-1"), 16)) {
			s.write(b);
		}

		for (byte b : Arrays.copyOf(this.keywords.getBytes("ISO-8859-1"), 32)) {
			s.write(b);
		}

		s.write(this.intro.getValue() * 16 + this.next.getValue());
	}

	public void export_ext(OutputStream s, int rec_no) throws IOException {
		if (this.style_no == 65533) {
			s.write("FPdt".getBytes());

			int recordLen = this.extStylePath.getBytes("ISO-8859-1").length + 3;
			s.write(getUpperByte(recordLen));
			s.write(getLowerByte(recordLen));

			s.write(getUpperByte(rec_no));
			s.write(getLowerByte(rec_no));

			if (this.isMusic) {
				s.write(1);
			} else {
				s.write(0);
			}

			s.write(this.extStylePath.getBytes("ISO-8859-1"));
		}
	}

	public void updateExtPath(String path, boolean isMusic) throws IOException {
		if (this.style_no != 65533) {
			throw new IOException("Ext. style added after internal style found");
		}

		this.extStylePath = path;
		this.isMusic = isMusic;
	}

	@Override
	public String toString() {
		return this.title;
	}

	public void parseTimeString(String time) {
		/*
		 * TODO: This method silently ignores errors! This is not good, but
		 * currently the only option I see that does not contain huge
		 * refactoring
		 */

		String[] parts = time.split("/");

		if (parts.length != 2) {
			/* Silently ignore */
			return;
		}

		try {
			int upper = Integer.parseInt(parts[0]);
			int lower = Integer.parseInt(parts[1]);

			if ((upper < 0) || (lower < 0)) {
				/* Silently ignore */
				return;
			}

			this.time_upper = upper;
			this.time_lower = lower;
		} catch (NumberFormatException e) {
			/* Silently ignore */
		}
	}
}
