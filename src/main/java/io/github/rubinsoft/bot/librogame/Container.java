package io.github.rubinsoft.bot.librogame;

import java.util.LinkedList;

public abstract class Container {
	protected LinkedList<Oggetto> container;

	public Container(){
		this.container=new LinkedList<Oggetto>();
	}

	/**
	 * 
	 * @param contenuto: stringa di oggetti separati da |. Se un oggetto ha una quantita' deve rispettare la forma <code>oggetto:quantita'</code>
	 */
	public Container(String contenuto){
		this.container=new LinkedList<Oggetto>();
		if(contenuto==null || contenuto.trim().equals("")) return;
		for(String ogg:contenuto.split(GameConstants.SEPARATOR)){
			if(ogg==null || ogg.trim().equals("")) continue;
			if(ogg.indexOf(':')==-1)
				addOggetto(new Oggetto(ogg));
			else{
				String[] oggs = ogg.split(":");
				addOggetto(new Oggetto(oggs[0]).setQuantita(Integer.parseInt(oggs[1])));
			}
		}
	}

	public void addOggetto(Oggetto o){
		if(container.contains(o)){//se lo contiene ne aggiunge quantita' "o"
			int index = container.indexOf(o);
			Oggetto ogg = container.get(index);
			container.remove(ogg);
			ogg.setQuantita(ogg.getQuantita()+o.getQuantita());
			container.add(index, ogg);
		}else
			container.add(o);
	}

	/**
	 * tenta di rimovere un oggetto dal contenitore
	 * @param o oggetto da analizzare
	 * @return true se l'oggetto e' stato rimosso, false se non e' stato trovato
	 */
	public boolean removeOggetto(Oggetto o){
		if(container.contains(o)){
			int index = container.indexOf(o);
			Oggetto ogg = container.get(index);
			if((ogg.getQuantita()-o.getQuantita())>0){ //se rimane piu' di 1 oggetto sottraggo
				ogg.setQuantita(ogg.getQuantita()-o.getQuantita());
				container.remove(o);
				container.add(index, ogg);
				return true;
			}else{//altrimenti rimuovo l'oggetto
				return container.remove(o);
			}
		}
		return false;
	}

	public boolean containsOggetto(Oggetto o){
		if(!container.contains(o)) return false;
		if(o.getQuantita() > 1){
			Oggetto ogg = container.get(container.indexOf(o));
			if(o.getQuantita() > ogg.getQuantita()) //se la quantita' richiesta fosse maggiore di quella posseduta 
				return false;
		}
		return true;
	}

	public String getContenuto(){
		StringBuilder sb = new StringBuilder();
		//sort
		container.sort(new Oggetto(""));
		for(Oggetto o:container)
			sb.append("-"+o+'\n');
		return sb.toString();
	}

	public void reset() {
		container=new LinkedList<Oggetto>();
	}

	public String serializza(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<container.size(); i++)
			sb.append(container.get(i).getNome()+
					//((zaino.get(i).getQuantita()==null)?"":":"+zaino.get(i).getQuantita())+
					":"+container.get(i).getQuantita()+
					((i==container.size()-1)?"":GameConstants.SEPARATOR));
		return sb.toString();
	}


}
