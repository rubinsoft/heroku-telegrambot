package io.github.rubinsoft.bot.librogame.storybuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.github.rubinsoft.bot.librogame.GameConstants;
import io.github.rubinsoft.bot.librogame.Oggetto;
import io.github.rubinsoft.bot.librogame.Scelta;
import io.github.rubinsoft.bot.librogame.Zaino;
import io.github.rubinsoft.bot.librogame.exception.*;

/**
 * riceve nodi gia' costruiti e a seconda del tipo di nodo lo inserisce
 * nell'apposita struttura es.:<br>
 * <LI>xmlnode <code>"cap ..."</code> va nell'hashmap dei capitoli che ha come
 * key l'id del capitolo.<br>
 * 
 * @author Firebone
 */
public class StoryBuilder {
	public final String STORY_TITLE;
	private HashMap<String, XMLNode> capitoli;
	private StoryStructure structure;
	private Zaino zaino;

	public StoryBuilder(InputStream input) throws MalformedStructureException,
			IOException {
		capitoli = new HashMap<String, XMLNode>();
		structure = new StoryStructure(new Parser(input));
		zaino = new Zaino();
		// Gestione META
		if (structure.hasMeta()) {
			XMLNode meta = structure.getMeta();// meta puo' contenere piu'
												// informazioni
			XMLNode title = meta.getSubNode("storytitle");
			if (title != null)
				STORY_TITLE = ((XMLBody) title.getSubNode("body")).getBody();
			else
				STORY_TITLE = "a story";
		} else {
			STORY_TITLE = "a story";
		}
		// Costruzione capitoli
		while (structure.hasNext()) {
			XMLNode nodo = structure.nextNode();
			if (!nodo.getRoot().equals("cap"))
				continue;
			if (nodo.getAttribute("id") == null)
				throw new MalformedStructureException("STORY_BUILDER: cap\n"
						+ nodo + "\n doesn't contain the ID field");
			if (capitoli.containsKey(nodo.getAttribute("id")))
				throw new MalformedStructureException("STORY_BUILDER: cap\n"
						+ nodo + "\n is duplicated");
			capitoli.put(nodo.getAttribute("id"), nodo);
		}
	}

	/**
	 * restituisce, se presente, il sottotitolo del capitolo corrente
	 * 
	 * @param capId
	 *            : id del capitolo interessato
	 * @return sottotitolo del capitolo corrente
	 */
	public String getCapSubtitle(String capId) {
		String subtitle = ((XMLBody) capitoli.get(capId).getSubNode("subtitle")
				.getSubNode("body")).getBody();
		return (subtitle == null) ? "" : subtitle;
	}

	/**
	 * @param capId
	 *            : id del capitolo interessato
	 * @return il testo del capitolo corrente
	 */
	public String getCapText(String capId) {
		return ((XMLBody) capitoli.get(capId).getSubNode("body")).getBody();
	}

	/**
	 * @param capId
	 *            : id del capitolo interessato
	 * @return la lista delle scelte effettuabili nel capitolo corrente; null se la storia e' terminata
	 * @throws MalformedStructureException
	 */
	public Scelta[] getListaScelte(String capId)
			throws MalformedStructureException {
		XMLNode elencoScelte = capitoli.get(capId).getSubNode("switch");
		if (elencoScelte == null)
			return null;
		LinkedList<Scelta> listaScelte = new LinkedList<Scelta>();
		int cont = 0, numScelteValide = 0;
		boolean sceltaValida;
		while (true) {
			sceltaValida = true;
			XMLNode scelta = elencoScelte.getSubNode("scelta:" + cont);
			if (scelta == null)
				break;
			cont++;
			// text
			String text = ((XMLBody) scelta.getSubNode("body")).getBody();

			// tocap
			String tocap = scelta.getAttribute("tocap");

			// lista oggetti raccolti
			String raccolti_ = scelta.getAttribute("add");
			LinkedList<Oggetto> raccolti = new LinkedList<Oggetto>();
			if (raccolti_ != null && !raccolti_.equals(""))
				for (String nome : raccolti_.split(","))
					raccolti.add(new Oggetto(nome));

			// lista oggetti richiesti
			String richiesti_ = scelta.getAttribute("use");
			LinkedList<Oggetto> richiesti = new LinkedList<Oggetto>();
			if (richiesti_ != null && !richiesti_.equals(""))
				for (String nome : richiesti_.split(","))
					richiesti.add(new Oggetto(nome));

			// *** GVAZ12.11.15 - rel v1.2 - aggiunta oggetti required - START

			// lista oggetti che abilitano la scelta
			String obbligatori_ = scelta.getAttribute("required");
			LinkedList<Oggetto> obbligatori = new LinkedList<Oggetto>();
			if (obbligatori_ != null && !obbligatori_.equals(""))
				for (String nome : obbligatori_.split(",")) {
					Oggetto o = new Oggetto(nome);
					if (zaino.containsOggetto(o))
						obbligatori.add(o);
					else {
						sceltaValida = false;
						break;
					}
				}
			
			if (!sceltaValida) continue; //se almeno un oggetto richiesto non e' nello zaino, non mostro la scelta
			// *** rel v1.2 - aggiunta oggetti required - END 
			numScelteValide++;
			// aggiungo scelta
			listaScelte.add(new Scelta(text, tocap, raccolti, richiesti,
					obbligatori));
		}
		if (numScelteValide == 0)
			throw new MalformedStructureException("STORY_BUILDER: in cap\n"
					+ capitoli.get(capId)
					+ "\n switch must have at least one subtag <scelta> valid");
		Scelta[] scelte = new Scelta[numScelteValide];
		for (int i = 0; i < numScelteValide; i++)
			scelte[i] = listaScelte.get(i);
		return scelte;
	}

	public String selezionaScelta(Scelta sceltaCorrente)
			throws MissingObjectException {
		boolean errScelta = false;
		StringBuilder sb = new StringBuilder(GameConstants.dialogRequiredObject);
		for (Oggetto richiesto : sceltaCorrente.getListaOggettiRichiesti())
			if (!zaino.containsOggetto(richiesto)) {
				sb.append(" \"" + richiesto + '"');
				errScelta = true;
			}
		if (errScelta)
			throw new MissingObjectException(sb.toString());
		// nessun oggetto mancante, rimuovo gli oggetti richiesti dallo zaino
		for (Oggetto richiesto : sceltaCorrente.getListaOggettiRichiesti())
			zaino.removeOggetto(richiesto);
		for (Oggetto raccolto : sceltaCorrente.getListaOggettiRaccolti())
			zaino.addOggetto(raccolto);
		return sceltaCorrente.nextCap();
	}

	public String getZaino() {
		return zaino.getContenuto();
	}
	
	public Zaino zaino(){
		return zaino;
	}
	
	public void setZaino(Zaino z){
		this.zaino = z;
	}

	public List<Exception> getWarningsList() {
		return structure.getWarnings();
	}

	public static void main(String[] args) throws IOException,
			MalformedStructureException {
		new StoryBuilder(new FileInputStream(new File("./story.txt")));
	}
}
