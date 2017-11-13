package io.github.rubinsoft.bot.librogame;


public class Zaino extends Container{

	public Zaino(){
		super();
	}

	public Zaino(String contenuto){
		super(contenuto);
	}

	public int equipSize(){
		int count=0;
		for(Oggetto o:container){
			if(!o.getNome().toUpperCase().startsWith("[H]")
					&&!o.getNome().toUpperCase().startsWith("[S]"))
				count++;
		}
		return count;
	}

	@Override
	public String getContenuto(){
		String intestazioneStat = GameConstants.stringStat + "\n";
		String intestazioneZaino = GameConstants.stringZaino
				.replaceAll("&1", ""+equipSize())
				.replaceAll("&2", (equipSize()==1)? GameConstants.singolareMaschile:GameConstants.pluraleMaschile)
				+ "\n";
		StringBuilder sb = new StringBuilder();
		//sort
		container.sort(new Oggetto(""));
		//stampa
		boolean stampaStat = false;
		for(int i = 0; i<container.size();i++)
			if(container.get(i).getNome().toUpperCase().startsWith("[S]")){
				if(!stampaStat){ //statistica
					sb.append(intestazioneStat);
					stampaStat=true;
				}
				String o = container.get(i).toString();
				sb.append("-"+o.substring(3,o.length())+'\n');
			}

		boolean stampaZaino = false;
		for(Oggetto o:container){
			if(o.getNome().toUpperCase().startsWith("[H]")) continue; //oggetto nascosto
			if(o.getNome().toUpperCase().startsWith("[S]")) continue; //stat
			if(!stampaZaino){//equipaggiamento
				if(stampaStat) sb.append('\n');
				sb.append(intestazioneZaino);
				stampaZaino=true;
			}
			sb.append("-"+o+'\n');
		}

		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		GameConstants.inizialize();
		Zaino z = new Zaino("[S]Forza:6"+GameConstants.SEPARATOR+
				"[S]Destrezza:8"+GameConstants.SEPARATOR
				+ "[S]Intelligenza:8"+
				GameConstants.SEPARATOR+ "[H]karma-");
//		z.addOggetto(new Oggetto("[H]test"));
//		z.addOggetto(new Oggetto("[S]test:5"));
//		z.addOggetto(new Oggetto("cipolla:10"));
//		z.addOggetto(new Oggetto("ape:2"));
//		z.addOggetto(new Oggetto("[s]rododendro:5"));
//		z.addOggetto(new Oggetto("[h]asdasd:5"));
//		z.addOggetto(new Oggetto("[p]caccolino:5"));
		System.out.println(z.getContenuto());
	}

}
