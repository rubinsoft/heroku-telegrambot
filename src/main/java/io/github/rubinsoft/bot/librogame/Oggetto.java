package io.github.rubinsoft.bot.librogame;

import java.util.Comparator;

public class Oggetto implements Comparator<Oggetto>{
	private String nome;
	private int quantita;
	private String descrizione;

	public Oggetto(String nome){
		this(nome, "");
	}

	public Oggetto(String nome, String descrizione){
		nome=nome.trim();
		if(nome.indexOf(':') != -1){
			String[] attr = nome.split(":");
			this.nome = attr[0];
			int quant = Integer.parseInt(attr[1]);
			this.quantita = (quant<=0)?1:quant; 
		}else{
			this.nome=nome;
			this.quantita = 1;
		}
		this.descrizione=descrizione;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public String getNome() {
		return nome;
	}

	@Override
	public boolean equals(Object o){
		if(this==o) return true;
		if(!(o instanceof Oggetto))return false;
		Oggetto oo=(Oggetto)o;
		return this.nome.equals(oo.nome);
	}

	@Override
	public String toString(){
		return nome+':'+quantita;
	}

	/**
	 * @return the quantita
	 */
	public int getQuantita() {
		return quantita;
	}

	/**
	 * @param quantita the quantita to set
	 */
	public Oggetto setQuantita(int quantita) {
		this.quantita = quantita;
		return this;
	}

	@Override
	public int compare(Oggetto o1, Oggetto o2) {
		return o1.getNome().compareTo(o2.getNome());
	}

}
