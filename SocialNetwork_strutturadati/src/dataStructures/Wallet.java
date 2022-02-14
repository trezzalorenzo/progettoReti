package dataStructures;

import java.util.ArrayList;

public class Wallet {
	ArrayList<String> listaMovimenti;
	double saldo;

	Wallet() {
		this.listaMovimenti = new ArrayList<String>();
		this.saldo = (double) 0;
	}

	public synchronized void aggiornaWallet(double valore) throws IllegalArgumentException {
		this.saldo = this.saldo + valore;
		if (valore != 0) {
			this.listaMovimenti.add(Double.toString(valore));
		}
	}

	public synchronized double getSaldo() {
		return this.saldo;
	}

	public synchronized ArrayList<String> getListaMovimenti() {
		return this.listaMovimenti;
	}

	public synchronized String formattaWallet() {
		String s = "";
		if (listaMovimenti == null) {
			s = s + "portafoglio vuoto";
		} else {
			String transazioni = "";
			for (String i : listaMovimenti) {
				transazioni = transazioni + " " + i + ",";
			}
			s = s + "Saldo: " + this.saldo + "\nTransazioni:" + transazioni;
		}
		return s;
	}
}