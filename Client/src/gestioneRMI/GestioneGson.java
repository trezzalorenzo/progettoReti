package gestioneRMI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



public class GestioneGson {
	
	public static void salvaConfig(ConfigReader config, String pathName) throws FileNotFoundException, IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(config);
		// salvo su file la stringa
		byte[] data = json.getBytes();
		int bufSize = data.length;
		String outputFile = pathName;
		// Apro il file di output e il canale associato allo stream.
		FileOutputStream os = new FileOutputStream(outputFile);
		FileChannel oc = os.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(bufSize);
		for (int l = 0; l < data.length; l += bufSize) {
			buf.clear();
			buf.put(data, l, Math.min(bufSize, data.length - 1));
			buf.flip();
			while (buf.hasRemaining())
				oc.write(buf);
		}
		buf.clear();
		buf.put(new String("\n}").getBytes());
		buf.flip();
		oc.write(buf);
		// chiudo file e channel
		oc.close();
		os.close();

	}
	
	public static ConfigReader caricaConfig(String pathName) throws FileNotFoundException, IOException {
		String s = "";
		String path = pathName;
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {

			String line = br.readLine();
			StringBuilder sb = new StringBuilder();

			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}

			String fileAsString = sb.toString();
			s = s + fileAsString;
		}
		Gson gson = new Gson();
		ConfigReader config = gson.fromJson(s, ConfigReader.class);
		return config;
	}
}
