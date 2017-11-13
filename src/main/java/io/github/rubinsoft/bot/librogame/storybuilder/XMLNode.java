package io.github.rubinsoft.bot.librogame.storybuilder;

import java.util.Collection;
import java.util.HashMap;

public class XMLNode {
	private String root;
	private HashMap<String, String> attributes;
	private HashMap<String, XMLNode> subNodes;
	
	public XMLNode(String root){
		this.root=root.toLowerCase();
		attributes=new HashMap<String,String>();
		subNodes=new HashMap<String,XMLNode>();
	}

	/**
	 * 
	 * @param name : if a node has <b>id</b> attribute, the name is <code>"name:id"</code>
	 * @return
	 */
	public XMLNode getSubNode(String name) {
		return subNodes.get(name);
	}
	
	public Collection<XMLNode> getSubNodeList(){
		return subNodes.values();
	}

	public XMLNode addSubNode(XMLNode subNode) {
		if(subNodes.containsKey(subNode.getRoot()))
			throw new IllegalArgumentException("XMLNODE: in node:\n"+ this.toString()+"\n a subnode is duplicated!");
		String key=subNode.getRoot()+
				((subNode.getAttribute("id")!=null)?
				":"+subNode.getAttribute("id"):
				"");
		subNodes.put(key,subNode);
		return this;
	}

	public String getAttribute(String name) {
		return attributes.get(name);
	}

	public XMLNode addAttribute(String key, String value) {
		key=key.toLowerCase();
		if(attributes.containsKey(key))
			throw new IllegalArgumentException("XMLNODE: in node:\n"+ this.toString()+ "\nan attribute is duplicated!");
		attributes.put(key, value);
		return this;
	}

	public String getRoot() {
		return root;
	}
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder("<"+root+' '+attributes.toString()+">\n");
		for(XMLNode x:subNodes.values())
			sb.append(x.toString());
		sb.append("\n</"+root+'>');
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if(o==this) return true;
		if(!(o instanceof XMLNode)) return false;
		XMLNode x=(XMLNode)o;
		if(this.root.equalsIgnoreCase(x.root)){
			if(this.getAttribute("id")!=null && x.getAttribute("id")!=null)
				return this.getAttribute("id").equalsIgnoreCase(x.getAttribute("id"));
			if(this.getAttribute("id")==null && x.getAttribute("id")==null)
				return true;
		}
		return false;
	}
}
