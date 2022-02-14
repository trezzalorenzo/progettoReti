package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Set;

import dataStructures.StrutturaDatiServer;
import dataStructures.Tripla;
import dataStructures.Utente;
import dataStructures.Wallet;
import eccezioni.ElementNotPresentException;

public class TaskCalcolatoreGuadagni implements Runnable {
	String address;
	int port;
	StrutturaDatiServer hash;
	int timeSleep;
	double percentualeAutore;

	public TaskCalcolatoreGuadagni(StrutturaDatiServer hash, int timeSleep, String address, int port,
			int percentualeAutore) {
		this.hash = hash;
		this.timeSleep = timeSleep;
		this.address = address;
		this.port = port;
		this.percentualeAutore = percentualeAutore;
	}

	public void run() {
		// Creo una DatagramSocket per l'invio dei pacchetti.
		try (DatagramSocket ms = new DatagramSocket();) {
			// creo il pacchetto da inviare ai client
			InetAddress gruppo = InetAddress.getByName(address);
			String notifica = "Wallet aggiornato";
			byte[] bytedata = notifica.getBytes();
			DatagramPacket dp = new DatagramPacket(bytedata, bytedata.length, gruppo, port);

			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(timeSleep);
				} catch (InterruptedException e) {
					break;
				}
				// ottengo l'autore,il valore e i curatori di ogni post
				ArrayList<Tripla<String, Double, Set<String>>> contabilita = this.hash.valutaPosts();
				// per ogni elemento dell'arraylist
				for (Tripla<String, Double, Set<String>> c : contabilita) {
					try {
						// aggiorno il wallet dell'autore calcolando la sua percentuale
						Utente u = hash.utenteDaNome(c.getFirst());
						Wallet w = u.getWallet();
						w.aggiornaWallet(c.getSecond() * (percentualeAutore / (double) 100));

						// determino il premio destinato ai curatori e aggiorno il loro wallet
						double guadagnoCuratori = (c.getSecond() * ((100 - percentualeAutore) / 100)
								/ c.getThird().size());
						for (String s : c.getThird()) {
							hash.utenteDaNome(s).getWallet().aggiornaWallet(guadagnoCuratori);

						}
					} catch (ElementNotPresentException | IllegalArgumentException e) {
						continue;
					}
				}
				// invio il messaggio di avvenuto calcolo reward
				System.out.println("conteggio effettuato");
				ms.send(dp);

			}

		} catch (Exception e) {
		}
	}

}