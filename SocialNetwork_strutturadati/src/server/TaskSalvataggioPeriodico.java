package server;

import java.io.IOException;

import dataStructures.StrutturaDatiServer;
import utilities.GestioneGson;

public class TaskSalvataggioPeriodico implements Runnable {
	private int timeSleep;
	private StrutturaDatiServer hash;
	private String path;

	TaskSalvataggioPeriodico(String path, StrutturaDatiServer hash, int timeSleep) {
		this.timeSleep = timeSleep;
		this.path = path;
		this.hash = hash;
	}

	// finchè non arriva un interruzione il thread aspetta "timeSleep" millisecondi
	// ed esegue il salvataggio su file
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {

			try {
				Thread.sleep(timeSleep);
				GestioneGson.salvaSuFile(hash, path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
