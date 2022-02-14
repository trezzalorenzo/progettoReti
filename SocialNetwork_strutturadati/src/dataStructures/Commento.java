package dataStructures;

public class Commento {
	private String nome;
	private String testo;
	
	public Commento(String nome,String testo) {
		this.nome=nome;
		this.testo=testo;
	}

	public String getNome() {
		return this.nome;
	}

	public String getTesto() {
		return this.testo;
	}
	
}

