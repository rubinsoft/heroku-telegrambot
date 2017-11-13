package io.github.rubinsoft.bot.librogame.storybuilder;

public interface StoryStructureIF {
	public boolean hasNext();
	public XMLNode nextNode();
	public boolean hasMeta();
	public XMLNode getMeta();

}
