package mfd_edit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Settings {

	private static final String configDirPath = ".mfd_edit";
	private static final String configFilePath = "config.properties";

	private String lastSaveDir = "";

	public Settings() {
		File configFile = new File(
				System.getProperty("user.home") + File.separator + configDirPath + File.separator + configFilePath);

		try {
			FileReader reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);

			this.lastSaveDir = props.getProperty("last_save_dir");
			if (this.lastSaveDir == null) {
				this.lastSaveDir = "";
			}

			reader.close();
		} catch (IOException ex) {
			lastSaveDir = "";
		}
	}

	public void save() {
		File configFile = new File(
				System.getProperty("user.home") + File.separator + configDirPath + File.separator + configFilePath);

		configFile.getParentFile().mkdirs();

		try {
			Properties props = new Properties();
			props.setProperty("last_save_dir", this.lastSaveDir);

			FileWriter writer = new FileWriter(configFile);
			props.store(writer, "");
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();

			/* save failed */
		}
	}

	public String getLastSaveDir() {
		return lastSaveDir;
	}

	public void setLastSaveDir(String lastSaveDir) {
		this.lastSaveDir = lastSaveDir;
	}
}
