package dataStructures;

import java.util.ArrayList;
import java.util.Date;

public class Post {
	private int idpost;
	private String autore;
	private String titolo;
	private String contenuto;
	private ArrayList<String> upVote;
	private ArrayList<String> downVote;
	private ArrayList<Commento> commenti;
	private String opAutore; // per la gestione dei rewin
	private int originalId; // per la gestione dei rewin
	private Date data;

	private int iterazioni;
	private int likeDaUltimaIterazione;
	private int dislikeDaUltimaIterazione;
	private int commentiDaUltimaIterazione;

	public Post(int idpost, String autore, String titolo, String contenuto, String opAutore, int originalId) {
		this.idpost = idpost;
		this.autore = autore;
		this.titolo = titolo;
		this.contenuto = contenuto;
		this.upVote = new ArrayList<String>();
		this.downVote = new ArrayList<String>();
		this.commenti = new ArrayList<Commento>();
		this.opAutore = opAutore;
		this.originalId = originalId;
		this.data = new Date();

		this.iterazioni = 1;
		this.likeDaUltimaIterazione = 0;
		this.dislikeDaUltimaIterazione = 0;
		this.commentiDaUltimaIterazione = 0;
	}

	public synchronized int getIdpost() {
		return idpost;
	}

	public synchronized String getAutore() {
		return autore;
	}

	public synchronized String getTitolo() {
		return titolo;
	}

	public synchronized String getContenuto() {
		return contenuto;
	}

	public synchronized ArrayList<String> getUpVote() {
		return upVote;
	}

	public synchronized ArrayList<String> getDownVote() {
		return downVote;
	}

	public synchronized ArrayList<Commento> getCommenti() {
		return commenti;
	}

	public synchronized String getOpAutore() {
		return opAutore;
	}

	public synchronized int getOriginalId() {
		return originalId;
	}

	public synchronized Date getData() {
		return data;
	}

	public synchronized void aggiungiLike(String nome) throws IllegalArgumentException {
		if (nome == null) {
			throw new IllegalArgumentException();
		}
		this.upVote.add(nome);
		this.aggiornaLike(1);
	}

	public synchronized void aggiungiDislike(String nome) throws IllegalArgumentException {
		if (nome == null) {
			throw new IllegalArgumentException();
		}
		this.downVote.add(nome);
		this.aggiornaDislike(1);
	}

	public synchronized void aggiungiCommento(String commento, String nome) throws IllegalArgumentException {
		if (nome == null || commento == null) {
			throw new IllegalArgumentException();
		}
		Commento commentoDaAggiungere = new Commento(nome, commento);
		commenti.add(commentoDaAggiungere);
		this.aggiornaCommenti(1);
	}

	private synchronized void aggiornaLike(int n) {
		if (n == 0) {
			this.likeDaUltimaIterazione = 0;
		}
		this.likeDaUltimaIterazione = this.likeDaUltimaIterazione + n;
	}

	private synchronized void aggiornaDislike(int n) {
		if (n == 0) {
			this.dislikeDaUltimaIterazione = 0;
		}
		this.dislikeDaUltimaIterazione = this.dislikeDaUltimaIterazione + n;
	}

	private synchronized void aggiornaCommenti(int n) {
		if (n == 0) {
			this.commentiDaUltimaIterazione = 0;
		}
		this.commentiDaUltimaIterazione = this.commentiDaUltimaIterazione + n;
	}

	public synchronized void iterazioneEffettuata() {
		this.iterazioni++;
		this.aggiornaCommenti(0);
		this.aggiornaLike(0);
		this.aggiornaDislike(0);
	}

	private synchronized int numeroCommenti(String nome) {
		int returner = 0;
		int commentiVecchi = this.commenti.size() - this.commentiDaUltimaIterazione;
		for (Commento c : commenti) {
			if (commentiVecchi > 0) {
				commentiVecchi--;
			} else {
				if (c.getNome().equals(nome)) {
					returner++;
				}

			}
		}
		return returner;
	}

	public synchronized double valutaPost() {
		double returner = 0;
		double valoreLike = (double) this.likeDaUltimaIterazione - this.dislikeDaUltimaIterazione;
		if (valoreLike < 0) {
			valoreLike = 0;
		}
		valoreLike++;
		double guadagnoDaLike = Math.log(valoreLike);
		double valoreCommenti = 0;
		int commentiVecchi = this.commenti.size() - this.commentiDaUltimaIterazione;
		for (Commento c : commenti) {
			if (commentiVecchi > 0) {
				commentiVecchi--;
			} else {
				valoreCommenti = valoreCommenti + (2 / (1 + Math.exp(-((double) this.numeroCommenti(c.getNome())))));
			}
		}
		valoreCommenti++;
		double guadagniDaCommenti = Math.log(valoreCommenti);
		returner = (guadagnoDaLike + guadagniDaCommenti) / ((double) this.iterazioni);
		this.iterazioneEffettuata();
		return returner;

	}

	public synchronized int getIterazioni() {
		return this.iterazioni;
	}

	public synchronized ArrayList<String> commentatori() {
		ArrayList<String> autoriCommenti = new ArrayList<String>();
		for (Commento c : this.commenti) {
			autoriCommenti.add(c.getNome());
		}
		return autoriCommenti;
	}

	public synchronized String formattaPost() {
		String formatCommenti = "";
		for (Commento c : commenti) {
			formatCommenti = formatCommenti + "\n" + c.getNome() + ": " + c.getTesto();
		}
		return "Titolo: " + this.titolo + "\nContenuto: " + this.contenuto + "\nVoti: " + this.upVote.size()
				+ "positivi, " + this.downVote.size() + "negativi" + "\nCommenti:\n\t" + formatCommenti;
	}

	public synchronized String formattaInfoPost() {
		return this.idpost + "\t| " + this.autore + "\t| '" + this.titolo + "'\n";
	}

}
