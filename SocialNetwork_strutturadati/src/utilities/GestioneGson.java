package utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataStructures.StrutturaDatiServer;

public class GestioneGson {
	/*
	 * salva StrutturaDatiServer su file
	 * 
	 * @param la struttura dati del server e il nome del file in cui salvarla
	 * 
	 * @return niente
	 * 
	 * @throws FileNotFoundException,IOException
	 * 
	 */
	public static void salvaSuFile(StrutturaDatiServer hash, String pathName)
			throws FileNotFoundException, IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(hash);
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

	/*
	 * salvo ConfigReader su file
	 * 
	 * @param ConfigReader e il pathname
	 * 
	 * @return niente
	 * 
	 * @throws FileNotFoundException,IOException
	 * 
	 */
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

	/*
	 * deserializzo i dati presenti nel file pathname
	 * 
	 * @param il pathname del json
	 * 
	 * @return StutturaDatiServer
	 * 
	 * @throws FileNotFoundException,IOException
	 * 
	 */
	public static StrutturaDatiServer caricaDaFile(String pathName) throws FileNotFoundException, IOException {
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
		StrutturaDatiServer hash = gson.fromJson(s, StrutturaDatiServer.class);
		return hash;
	}

	/*
	 * deserializzo i dati presenti nel file pathname
	 * 
	 * @param il pathname del json
	 * 
	 * @return ConfigReader
	 * 
	 * @throws FileNotFoundException,IOException
	 * 
	 */
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
