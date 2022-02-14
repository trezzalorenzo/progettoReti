package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import dataStructures.Post;
import dataStructures.StrutturaDatiServer;
import eccezioni.ElementNotPresentException;
import gestioneRMI.RMIcallback;
import utilities.Connection;

public class TaskServer implements Runnable {
	private StrutturaDatiServer hash;
	private String daFare;
	private SelectionKey key;
	public List<String> loggeduser;

	public TaskServer(String daFare, StrutturaDatiServer hash, SelectionKey key, List<String> loggeduser) {
		this.daFare = daFare;
		this.hash = hash;
		this.key = key;
		this.loggeduser = loggeduser;
	}

	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(2048);
		String[] tokens = daFare.split(" ");

		// se l'utente non è gia loggato sul server e la password inserita è corretta
		// allora aggiungo il nome
		// alla lista di utenti loggati
		if (tokens[0].equals("login") && tokens.length == 3) {
			String nome = tokens[1];
			if (ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente già connesso", key, buffer);
			} else {
				String password = tokens[2];
				try {
					String passwordUtente = hash.utenteDaNome(nome).getPassword();
					if (passwordUtente.equals(password)) {
						ServerMain.loggeduser.add(nome);
						Connection.rispondi("successo:login", key, buffer);
					} else {
						Connection.rispondi("fallimento:nome utente e password non coincidono", key, buffer);
					}
				} catch (ElementNotPresentException e) {
					Connection.rispondi("fallimento:utente non registrato", key, buffer);
				}
			}
		}
		

		// se l'utente è loggato chiudo la connessione con il client
		else if (tokens[0].equals("logout") && tokens.length == 2) {
			String nome = tokens[1];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				SocketChannel client = (SocketChannel) key.channel();
				Connection.rispondi("successo:chiusura", key, buffer);
				try {
					loggeduser.remove(nome);
					client.close();
				} catch (IOException e) {
					System.err.println("fallimento:errore nella chiusura della socket");
				}
			}
		}

		// se l'utente è loggato(e quindi già registrato) invio gli utenti con almeno un
		// tag in comune
		else if (tokens[0].equals("list") && tokens[1].equals("users") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				try {
					String risposta = "Utente\t|\tTag\n";
					ArrayList<String> list = hash.utentiIntesessiSimili(nome);
					for (String l : list) {
						risposta = risposta + (hash.utenteDaNome(l).formattaNomeTag());
					}
					Connection.rispondi("successo:\n" + risposta, key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:argomenti errati", key, buffer);
				} catch (ElementNotPresentException e) {
					Connection.rispondi("fallimento:utente non presente", key, buffer);
				}
			}
		}
		// controllo i parametri e rispondo con la lista dei following
		else if (tokens[0].equals("list") && tokens[1].equals("following") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				try {
					ArrayList<String> list = hash.listaFollowingDiUtente(nome);
					String risposta = "Utente\t|\tTag\n";
					for (String l : list) {
						risposta = risposta + (hash.utenteDaNome(l).formattaNomeTag());
					}

					Connection.rispondi("successo:\n" + risposta, key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:argomenti errati", key, buffer);
				} catch (ElementNotPresentException e) {
					Connection.rispondi("fallimento:utente non presente", key, buffer);
				}
			}
		}
		// controllo i parametri e se gli utenti sono registrati,avviso con rmi del
		// nuovo follower
		else if (tokens[0].equals("follow") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				String nomeDaSeguire = tokens[1];
				if (hash.utenteRegistrato(nome) && hash.utenteRegistrato(nomeDaSeguire)) {
					try {
						hash.seguiUtente(nome, nomeDaSeguire);
						Connection.rispondi("successo:utente seguito", key, buffer);
						RMIcallback.notificaFollow(nomeDaSeguire, "+" + nome);
					} catch (IllegalArgumentException e) {
						Connection.rispondi("fallimento:argomenti errati", key, buffer);
					} catch (ElementNotPresentException e) {
						Connection.rispondi("fallimento:utente non presente", key, buffer);
					} catch (RemoteException e) {
					}
				} else {
					Connection.rispondi("fallimento:utente non registrato", key, buffer);
				}
			}
		}
		// controllo i parametri e se gli utenti sono registrati,avviso con rmi della
		// perdita del follow
		else if (tokens[0].equals("unfollow") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				String nomeDaSeguire = tokens[1];
				if (hash.utenteRegistrato(nome) && hash.utenteRegistrato(nomeDaSeguire)) {
					try {
						hash.smettiDiSeguireUtente(nome, nomeDaSeguire);
						Connection.rispondi("successo:non segui piu l'utente", key, buffer);
						RMIcallback.notificaFollow(nomeDaSeguire, "-" + nome);
					} catch (IllegalArgumentException e) {
						Connection.rispondi("fallimento:argomenti errati", key, buffer);
					} catch (ElementNotPresentException e) {
						Connection.rispondi("fallimento:utente non presente", key, buffer);
					} catch (RemoteException e) {

					}
				} else {
					Connection.rispondi("fallimento:utente non registrato", key, buffer);
				}
			}
		}
		// controllato che l'utente è loggato allora invio i post creati dall'utente
		else if (tokens[0].equals("blog") && tokens.length == 2) {
			String nome = tokens[1];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				if (hash.utenteRegistrato(nome)) {
					try {
						String risposta = "ID\t| Autore\t| Titolo\n";
						ArrayList<Post> list = hash.getBlog(nome);
						for (Post p : list) {
							risposta = risposta + p.formattaInfoPost();
						}
						Connection.rispondi("successo:\n" + risposta, key, buffer);
					} catch (IllegalArgumentException e) {
						Connection.rispondi("fallimento:argomenti errati", key, buffer);
					} catch (ElementNotPresentException e) {
						Connection.rispondi("fallimento:utente non presente", key, buffer);
					}
				}
			}
		} else if (tokens[0].equals("post") /*&& tokens.length == 4*/) {
			String[] t = daFare.split("\"");
			if (t.length != 5) {
				Connection.rispondi("non inserire virgolette all'interno del testo", key, buffer);
			}
			String titolo = t[1];
			String contenuto = t[3];
			String nome = t[4].substring(1);

			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else if (titolo.length() > 20 || contenuto.length() > 500) {
				Connection.rispondi("fallimento:parametri errati", key, buffer);
			} else {
				try {
					hash.creaPost(nome, titolo, contenuto);
					Connection.rispondi("successo:post creato", key, buffer);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					Connection.rispondi("fallimento:parametri errati", key, buffer);
				}
			}
		}
		// controllo i parametri rispondo con la lista dei post dei propri following
		else if (tokens[0].equals("show") && tokens[1].equals("feed") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {

				try {
					ArrayList<Post> list = hash.getFeed(nome);
					String risposta = "ID\t| Autore\t| Titolo\n";
					for (Post p : list) {
						risposta = risposta + p.formattaInfoPost();
					}
					Connection.rispondi("successo:" + risposta, key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:argomenti errati", key, buffer);
				} catch (ElementNotPresentException e) {
					Connection.rispondi("fallimento:utente non presente", key, buffer);
				}
			}
		}
		// invia le informazioni del post
		else if (tokens[0].equals("show") && tokens[1].equals("post") && tokens.length == 4) {
			String nome = tokens[3];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				String idd = tokens[2];
				int id = Integer.parseInt(idd);
				try {
					Post p = hash.getPostDaId(id);
					String risposta = p.formattaPost();
					Connection.rispondi("successo:" + risposta, key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:argomenti errati", key, buffer);
				}
			}
		}

		// elimina il post e tutti i suoi rewin
		else if (tokens[0].equals("delete") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				int idPost = Integer.parseInt(tokens[1]);
				try {
					hash.cancellaPost(idPost, nome);
					Connection.rispondi("successo:post cancellato", key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:non sei l'autore del post", key, buffer);
				}
			}
		}
		// effettua rewin
		else if (tokens[0].equals("rewin") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				int id = Integer.parseInt(tokens[1]);
				try {
					hash.pubblicaRewin(nome, id);
					Connection.rispondi("successo:Rewin effettuato", key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:errore nei parametri", key, buffer);
				}
			}
		} else if (tokens[0].equals("rate") && tokens.length == 4) {
			String nome = tokens[3];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				int id = Integer.parseInt(tokens[1]);
				String voto = tokens[2];
				try {
					hash.votaPost(id, voto, nome);
					Connection.rispondi("successo:voto aggiunto", key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:parametri errati o voto già aggiunto", key, buffer);
				}
			}
			
			
		} else if (tokens[0].equals("comment") /*&& tokens.length == 4*/) {
			String[] t = daFare.split("\"");
			if (t.length != 3) {
				Connection.rispondi("fallimento:formattazione errata", key, buffer);
			} else {
				String contenuto = t[1];
				String[] aus = t[0].split(" ");
				String id = aus[1];
				String nome = t[2].substring(1);
				if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer); }
				 
				try {
					int idnum = Integer.parseInt(id);
					hash.aggiungiCommento(idnum, contenuto, nome);
					Connection.rispondi("successo:commento aggiunto", key, buffer);
				} catch (IllegalArgumentException e) {
					Connection.rispondi("fallimento:parametri errati", key, buffer);
				} catch (ElementNotPresentException e) {
					Connection.rispondi("fallimento:post non presente", key, buffer);
				}

			}
			
			
			
		} else if (tokens[0].equals("wallet") && tokens.length == 2) {
			String nome = tokens[1];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				try {
					String risposta = hash.utenteDaNome(nome).getWallet().formattaWallet();
					Connection.rispondi(risposta, key, buffer);
				} catch (ElementNotPresentException e) {
					Connection.rispondi("fallimento:utente non presente", key, buffer);
				}
			}
		} else if (tokens[0].equals("wallet") && tokens[1].equals("btc") && tokens.length == 3) {
			String nome = tokens[2];
			if (!ServerMain.loggeduser.contains(nome)) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				try {
					double saldo = hash.utenteDaNome(nome).getWallet().getSaldo();
					URL url = new URL(
							"https://www.random.org/decimal-fractions/?num=1&dec=20&col=1&format=plain&rnd=new");
					URLConnection url_conn = url.openConnection();
					BufferedReader buf_read = new BufferedReader(new InputStreamReader(url_conn.getInputStream()));
					String body = buf_read.readLine();
					buf_read.close();
					double tassoDiCambio = Double.parseDouble(body);
					double valoreInBitcoin = tassoDiCambio * saldo;
					Connection.rispondi(Double.toString(valoreInBitcoin), key, buffer);
				} catch (ElementNotPresentException e) {
					Connection.rispondi("fallimento:utente non presente", key, buffer);
				} catch (MalformedURLException e) {
					Connection.rispondi("fallimento:errore nell'ottenimento del valore di cambio", key, buffer);
				} catch (IOException e) {
					Connection.rispondi("fallimento:errore nell'ottenimento del valore di cambio", key, buffer);
				}

			}
		} else if (tokens[0].equals("shoutdown") && tokens.length == 2) {
			if (!ServerMain.loggeduser.contains(tokens[1])) {
				Connection.rispondi("fallimento:utente non connesso", key, buffer);
			} else {
				if (!tokens[1].equals("admin")) {
					Connection.rispondi("fallimento:non sei admin", key, buffer);
				} else {
					Connection.rispondi("successo:server in chiusura", key, buffer);
					ServerMain.chiudi = true;
				}
			}
		} else {
			Connection.rispondi("comando errato", key, buffer);
		}

	}

}
