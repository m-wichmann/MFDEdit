package mfd_edit;

public class Main {
	public static String version = "0.2.1";

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new UI();
			}
		});
	}
}
