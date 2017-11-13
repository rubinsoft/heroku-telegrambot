package io.github.rubinsoft.bot.librogame.storybuilder;

import java.util.Collection;

/**
 * e' un XMLNode foglia.
 * @author Firebone
 *
 */
public class XMLBody extends XMLNode {
	private String body;

	public XMLBody(String bodyText) {
		super("body");
		this.body=bodyText;
	}
	
	@Override
	public Collection<XMLNode> getSubNodeList(){
		throw new UnsupportedOperationException("XMLBODY: a XMLBody can't have subnodes");
	}

	@Override
	public XMLNode addSubNode(XMLNode subNode) {
		throw new UnsupportedOperationException("XMLBODY: a XMLBody can't have subnodes");
	}

	@Override
	public XMLNode addAttribute(String key, String value) {
		throw new UnsupportedOperationException("XMLBODY: a XMLBody can't have attributes");
	}

	@Override
	public String getRoot() {
		return "body";
	}

	public String getBody() {
		return body;
	}

	@Override
	public String toString(){
		return "<body>"+body+"</body>";
	}

	@Override
	public boolean equals(Object o){
		if(o==this) return true;
		if(!(o instanceof XMLBody)) return false;
		XMLBody x=(XMLBody)o;
		return body.equals(x.getBody());
	}
}
