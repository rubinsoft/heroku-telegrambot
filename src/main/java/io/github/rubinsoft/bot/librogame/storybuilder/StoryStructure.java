package io.github.rubinsoft.bot.librogame.storybuilder;

import java.util.LinkedList;
import java.util.List;

import io.github.rubinsoft.bot.librogame.exception.WarningException;

public class StoryStructure implements StoryStructureIF {
	private XMLNode meta;
	private LinkedList<XMLNode> completeNodes;
	private LinkedList<Exception> warningList;

	public StoryStructure(Parser parser) {
		warningList = new LinkedList<Exception>();
		completeNodes = new LinkedList<XMLNode>();
		// Looking for meta
		meta = lookForMeta(parser);
		while (parser.hasNext()) {
			// System.out.println(parser.look());
			completeNodes.add(createSubNode(parser.next(), parser));
		}
	}

	private XMLNode lookForMeta(Parser parser) {
		if (!parser.look().contains("meta")) {
			warningList.add(new WarningException("no <meta> tag found!"));
			return null;
		}
		parser.next();// tolgo il tag meta
		XMLNode meta = new XMLNode("meta");
		while (!parser.look().contains("/meta")) {
			String token = parser.next();
			// System.out.println(token);
			if (token.charAt(0) != '<')
				continue;// scarta
			meta.addSubNode(createSubNode(token, parser));
		}
		parser.next();// tolgo la chiusura di meta
		return meta;
	}

	private XMLNode createSubNode(String tagOpening, Parser parser) {
		// System.out.println(tagOpening);
		if (tagOpening.charAt(0) != '<')
			throw new IllegalArgumentException(
					"STORY_STRUCTURE: Illegal tagOpening: " + tagOpening);
		tagOpening = "<" + tagOpening.substring(1).trim();
		int rootEndIndex = (tagOpening.indexOf(' ') > 0) ? tagOpening
				.indexOf(' ') : tagOpening.indexOf('>');
		String root = tagOpening.substring(1, rootEndIndex);
		// elimino root e parentesi uncinate dal tag ancora da analizzare
		tagOpening = tagOpening
				.substring(rootEndIndex, tagOpening.indexOf('>')).trim();
		XMLNode currentNode = new XMLNode(root);
		// aggiunta attributi al nodo
		while (!tagOpening.equals("")) {
			int equalsIndex = tagOpening.indexOf('=');
			if (equalsIndex == -1)
				System.out.println(tagOpening);
			String attributeName = tagOpening.substring(0, equalsIndex).trim();
			tagOpening = tagOpening.substring(tagOpening.indexOf('"') + 1)
					.trim();
			String attributeValue = tagOpening.substring(0,
					tagOpening.indexOf('"')).trim();
			tagOpening = tagOpening.substring(tagOpening.indexOf('"') + 1)
					.trim();
			currentNode.addAttribute(attributeName, attributeValue);
		}
		// aggiunta sottonodi al nodo

		while (!parser.look().contains("/" + root)) {
			if ((forgetableRoots(root)) && parser.look().contains('<'+root)) {
				warningList.add(new WarningException(
						"Cannot find closing tag for node: " + currentNode));
				break;
			}
			String nextTagOpening = parser.next().trim();
			// e' un sottonodo
			if (nextTagOpening.charAt(0) == '<')
				currentNode.addSubNode(createSubNode(nextTagOpening, parser));
			// e' un body
			else {
				currentNode.addSubNode(new XMLBody(nextTagOpening)).getSubNode(
						"body");
			}
		}
		parser.next();// elimino </root>
		return currentNode;
	}

	private boolean forgetableRoots(String root) {
		return root.equals("cap") || root.equals("switch");
	}

	@Override
	public boolean hasNext() {
		return !completeNodes.isEmpty();
	}

	@Override
	public XMLNode nextNode() {
		return completeNodes.remove();
	}

	@Override
	public boolean hasMeta() {
		return meta != null;
	}

	@Override
	public XMLNode getMeta() {
		return meta;
	}

	public List<Exception> getWarnings() {
		return warningList;
	}

}
