package io.github.rubinsoft.bot.librogame;

import java.awt.Color;

public class GameConstants {

	public static String stringZaino;
	public static Color endStoryBackgroundColor;
	public static Color endStoryForegroundColor;
	public static String stringMenuUno;
	public static String stringMenuLoadStory;
	public static String stringMenuStat;
	public static String stringMenuExit;
	public static String statPanelTitle;
	public static String defaultStatPanelContent;
	public static String dialogRequiredObject;
	public static String singolareMaschile;
	public static String pluraleMaschile;
	public static String stringStat; 
	public static final String SEPARATOR = "/";
	
	/**
	 * TODO si potrebbe creare un metodo "inizialize" che accetta un file o un metatag per settare le variabili globali
	 * @return
	 */
	public static boolean inizialize(){
		stringZaino = "Zaino (&1 oggett&2):";
		stringStat = "Statistiche giocatore:";
		singolareMaschile = "o";
		pluraleMaschile = "i";
		endStoryBackgroundColor = Color.BLACK;
		endStoryForegroundColor = Color.RED;
		stringMenuUno = "Inventario";
		stringMenuLoadStory = "Carica storia da file";
		stringMenuStat = "Statistiche giocatore";
		stringMenuExit = "Esci";
		statPanelTitle = "Statistiche giocatore";
		defaultStatPanelContent = "<nessuna statistica>";
		dialogRequiredObject = "Questa scelta necessita dell'oggetto:";
		return true;
	}
}
