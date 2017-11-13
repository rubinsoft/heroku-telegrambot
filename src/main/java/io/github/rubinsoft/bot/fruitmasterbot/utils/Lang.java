package io.github.rubinsoft.bot.fruitmasterbot.utils;

import com.pengrad.telegrambot.model.Message;


/**
 * classe che contiene le stringhe di comunicazione con l'utente. 
 * Deve essere inizializzata ogni volta che si richiama un metodo che presente comunicazioni con il chiamante
 * @author Firebone
 */
public class Lang {

	public String PLAY_SINGLE_1_MESSAGE = "gioca";
	public String START_MESSAGE = "Benvenuto, sono LetterMaster bot. Sono la versione con le lettere del classico gioco \"<b>MasterMind</b>\".\nSegui le istruzioni sottostanti per muoverti.";
	public String MAINTENANCE_MESSAGE = "scusa, sono in manutenzione. Saro' di nuovo attivo tra poco. A piu' tardi!";
	public String SORRY_MESSAGE = "Scusa ma preferirei delle lettere (es. abcd)";
	public String WIP_MESSAGE = "Scusa ma al momento questa funzione non e' disponibile, riprova piu' tardi.\nGrazie";
	public String HELP_MESSAGE = "aiuto";
	public String CONTACT_US = "contatta il mio creatore";
	public String TEXT_MENU_1 = "Scegli un'opzione dal menu";
	public String GENERATE_FRUIT_MIX_MESSAGE = "Sto generando il mix di lettere segreto da indovinare..";
	public String END_FRUIT_MIX_P1_MESSAGE = "Fatto! Adesso a te il compito di indovinarlo. Hai ";
	public String END_FRUIT_MIX_P2_MESSAGE = " tentativi a disposizione.\nBuona fortuna!";
	public String TRY_NR = "Tentativo n.";
	public String WIN_MESSAGE = "gioco terminato! Hai Vinto!";
	public String RETRY_MESSAGE = "gioco terminato! Hai terminato il numero di tentativi.\nRitenta!";
	public String E001_GAME_NOT_EXISTS = "la partita non esiste. Contattare @firebone";
	public String E002_NO_INPUT_VALID = "Input non valido ";
	public String E003_COMBO_NULL = "combo null";
	public String E000_GENERIC_ERROR = "si e' verificato un errore. Contattare @firebone";
	
	public String WIMPB_SORRY_MESSAGE = "Scusa non ho capito";
	public String WIMPB_START_MESSAGE = "Benvenuto, sono un bot il cui scopo è studiare il Dilemma del prigioniero ripetuto.\n\nScegli se giocare contro uno dei miei Cervelli o costruirne uno tuo e farlo gareggiare!";
	public String WIMPB_PLAYER_VS_COMPUTER = "giocatore vs cervello (pcv)";
	public String WIMPB_COMPUTER_VS_COMPUTER = "cervello vs cervello (cvc)";
	public String WIMPB_HELP_LABEL = "regolamento";
	public String WIMPB_CONTACT_US = "contatta lo sviluppatore";
	public String WIMPB_TEXT_MENU_1 = "Scegli un'opzione dal menu";
	public String WIMPB_COMPUTER_LIST = "lista dei cervelli";
	public String WIMPB_PVC_MESSAGE = "in progress...";
	public String WIMPB_E000_JAR_ERROR = "Questo cervello ha qualcosa che non va: ";
	public String WIMPB_NO_COMPUTER_FOUND = "Non ho trovato il tuo Cervello. Tiralo fuori, su!";
	public String WIMPB_E001_GENERIC_ERROR = "Si e' verificato un errore. Contattare @firebone";
	public String WIMPB_NO_ARGS_FOUND = "Inserisci il numero del Cervello da sfidare (es. cvc 1).\nPer conoscere quelli disponibili, visita la lista dei cervelli";
	public String WIMPB_NO_RIVAL_COMPUTER_FOUND = "Il Cervello richiesto non e' ancora stato inventato";
	public String WIMPB_LOAD_BRAIN = "carica un cervello";
	public String WIMPB_LOAD_YOUR_BRAIN = "Per inviarmi un nuovo Cervello compila <a href=\"http://fruitmaster-botfactory.rhcloud.com/brainloader?chatid=&1\">questo form</a>";
	public String WIMP_RULES = "Per approfondire come funziona il dilemma del prigioniero, visita <a href=\"https://it.wikipedia.org/wiki/Dilemma_del_prigioniero\">questo link</a>";
	
	public String LGB_START_MESSAGE = "Benvenuto, sono <b>Librogame</b> bot. Sono un simulatore per telegram dei libri gioco, protagonisti della gioventu' anni 80-90. Puoi giocare una delle mie storie o scriverne una tutta tua da far giocare alla nostra community! Buon divertimento";
	public String LGB_PLAY_SINGLE_1_MESSAGE = "gioca";
	public String LGB_HELP_MESSAGE = "aiuto";
	public String LGB_HELP_MESSAGE_RESP = "comandi:\n-gioca: inizia una nuova partita\n-zaino:restituisce la lista degli oggetti nello zaino";
	public String LGB_BACKPACK = "zaino";
	public String LGB_CONTACT_US = "contatta il mio sviluppatore";
	public String LGB_TEXT_MENU_1 = "Scegli un'opzione dal menu";
	public String LGB_CANNOT_SELECT_1 = "Per effettuare questa scelta devi avere con te &1 per almeno &2 unita' ";
	public String LGB_END_OF_STORY = "<fine storia>";
	public String LGB_NO_STORY = "Nessuna storia trovata";
	public String LGB_STORY_LIST = "Scegli una storia da giocare:";
	public String LGB_SORRY_MESSAGE = "";
	public String LGB_LOAD_STORY = "carica una storia";
	public String LGB_LOAD_YOUR_STORY = "Per inviarmi una nuova Storia compila <a href=\"https://fruitmaster-botfactory.rhcloud.com/storyloader?chatid=&1\">questo form</a>";
	public String LGB_E001_GENERIC_ERROR = "Si e' verificato un errore. Contattare @firebone";
	public String LGB_CANNOT_USE_1 = "Per effettuare questa scelta devi possedere e utilizzare &1 per almeno &2 unita' ";
	public String LGB_STAT = "stat";
	public Lang(Message message){
		
	}
	public Lang(){}
}
