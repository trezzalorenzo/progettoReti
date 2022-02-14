package dataStructures;

import java.util.ArrayList;

public class Utente {
	private String nome;
	private String password;
	private ArrayList<String> tag;
	private ArrayList<String> follower;
	private ArrayList<String> following;
	private Wallet wallet;

	public Utente(String nome, String password, ArrayList<String> tag) {
		this.nome = nome;
		this.password = password;
		this.tag = tag;
		this.follower = new ArrayList<String>();
		this.following = new ArrayList<String>();
		this.wallet = new Wallet();
	}

	public synchronized String getNome() {
		return this.nome;
	}

	public synchronized String getPassword() {
		return this.password;
	}

	public synchronized ArrayList<String> getTag() {
		return this.tag;
	}

	public synchronized ArrayList<String> getFollower() {
		return this.follower;
	}

	public synchronized ArrayList<String> getFollowing() {
		return this.following;
	}

	public synchronized void aggiungiFollower(String nome) throws IllegalArgumentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.follower.add(nome);
	}

	public synchronized void aggiungiFollowing(String nome) throws IllegalArgumentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.following.add(nome);
	}

	public synchronized void togliFollower(String nome) throws IllegalArgumentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.follower.remove(nome);
	}

	public synchronized void togliFollowing(String nome) throws IllegalArgumentException {
		if (nome == null || nome.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.following.remove(nome);
	}

	public synchronized Wallet getWallet() {
		return wallet;
	}

	public synchronized String formattaNomeTag() {

		String tags = "";
		for (String s : tag) {
			tags = tags + s + ", ";
		}
		return this.nome + "\t|\t" + tags + "\n";
	}

}
