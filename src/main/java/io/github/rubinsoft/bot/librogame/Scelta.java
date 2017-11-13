package io.github.rubinsoft.bot.librogame;

import java.util.LinkedList;
import java.util.List;

public class Scelta {
	private String text, tocap;
	private List<Oggetto> raccolti, richiesti, obbligatori;

	public Scelta(String text, String tocap, List<Oggetto> raccolti,
			List<Oggetto> richiesti, LinkedList<Oggetto> obbligatori) {
		this.text = text;
		this.tocap = tocap;
		this.raccolti = raccolti;
		this.richiesti = richiesti;
		this.obbligatori = obbligatori;
	}

	public String getText() {
		return text;
	}

	public String nextCap() {
		return tocap;
	}

	public List<Oggetto> getListaOggettiRaccolti() {
		return raccolti;
	}

	public List<Oggetto> getListaOggettiRichiesti() {
		return richiesti;
	}
	
	public List<Oggetto> getListaOggettiObbligatori() {
		return obbligatori;
	}

	@Override
	public String toString() {
		return "Scelta{nextCap:" + tocap + ",used:" + richiesti
				+ ",picked:" + raccolti + "required" + obbligatori +"}";
	}
}
