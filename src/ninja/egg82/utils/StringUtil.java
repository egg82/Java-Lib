package ninja.egg82.utils;

import java.util.List;

public final class StringUtil {
	//vars
	private static final char[] subset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+{}|:\"<>?`-=[]\\;',./".toCharArray();
	
	//constructor
	public StringUtil() {
		
	}
	
	//public
	public static String randomString(int length) {
		char buffer[] = new char[length];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = subset[MathUtil.fairRoundedRandom(0, subset.length - 1)];
		}
		return new String(buffer);
	}
	
	public static void stripSpecialChars(List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			list.set(i, list.get(i).replaceAll("[^a-zA-Z0-9]", ""));
		}
	}
	public static void stripCommonWords(List<String> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		
		for (int i = list.size() - 1; i >= 0; i--) {
			String word = list.get(i);
			if (
				word.isEmpty()
				|| word.equalsIgnoreCase("the")
				|| word.equalsIgnoreCase("of")
				|| word.equalsIgnoreCase("to")
				|| word.equalsIgnoreCase("and")
				|| word.equalsIgnoreCase("a")
				|| word.equalsIgnoreCase("in")
				|| word.equalsIgnoreCase("is")
				|| word.equalsIgnoreCase("it")
				|| word.equalsIgnoreCase("you")
				|| word.equalsIgnoreCase("that")
				|| word.equalsIgnoreCase("he")
				|| word.equalsIgnoreCase("was")
				|| word.equalsIgnoreCase("for")
				|| word.equalsIgnoreCase("on")
				|| word.equalsIgnoreCase("are")
				|| word.equalsIgnoreCase("with")
				|| word.equalsIgnoreCase("as")
				|| word.equalsIgnoreCase("I")
				|| word.equalsIgnoreCase("his")
				|| word.equalsIgnoreCase("they")
				|| word.equalsIgnoreCase("be")
				|| word.equalsIgnoreCase("at")
				|| word.equalsIgnoreCase("one")
				|| word.equalsIgnoreCase("have")
				|| word.equalsIgnoreCase("this")
				|| word.equalsIgnoreCase("from")
				|| word.equalsIgnoreCase("or")
				|| word.equalsIgnoreCase("had")
				|| word.equalsIgnoreCase("by")
				|| word.equalsIgnoreCase("hot")
				|| word.equalsIgnoreCase("but")
				|| word.equalsIgnoreCase("some")
				|| word.equalsIgnoreCase("what")
				|| word.equalsIgnoreCase("there")
				|| word.equalsIgnoreCase("we")
				|| word.equalsIgnoreCase("can")
				|| word.equalsIgnoreCase("out")
				|| word.equalsIgnoreCase("other")
				//|| word.equalsIgnoreCase("where")
				//|| word.equalsIgnoreCase("all")
				|| word.equalsIgnoreCase("your")
				|| word.equalsIgnoreCase("when")
				|| word.equalsIgnoreCase("up")
				|| word.equalsIgnoreCase("use")
				//|| word.equalsIgnoreCase("word")
				|| word.equalsIgnoreCase("how")
				//|| word.equalsIgnoreCase("said")
				|| word.equalsIgnoreCase("an")
				//|| word.equalsIgnoreCase("each")
				|| word.equalsIgnoreCase("she")
				//|| word.equalsIgnoreCase("which")
				|| word.equalsIgnoreCase("do")
				|| word.equalsIgnoreCase("their")
				//|| word.equalsIgnoreCase("time") Not this one
				|| word.equalsIgnoreCase("if")
				//|| word.equalsIgnoreCase("will")
				|| word.equalsIgnoreCase("way")
				//|| word.equalsIgnoreCase("about")
				//|| word.equalsIgnoreCase("many")
				|| word.equalsIgnoreCase("then")
				|| word.equalsIgnoreCase("them")
				|| word.equalsIgnoreCase("would")
				//|| word.equalsIgnoreCase("write") Nope
				|| word.equalsIgnoreCase("like")
				|| word.equalsIgnoreCase("so")
				//|| word.equalsIgnoreCase("these") Nope
				|| word.equalsIgnoreCase("her")
				//|| word.equalsIgnoreCase("long") Nope
				//|| word.equalsIgnoreCase("make") Nope
				//|| word.equalsIgnoreCase("thing") Nope
				|| word.equalsIgnoreCase("see")
				|| word.equalsIgnoreCase("him")
				|| word.equalsIgnoreCase("two")
				|| word.equalsIgnoreCase("has")
				//|| word.equalsIgnoreCase("look") Nope
				//|| word.equalsIgnoreCase("more") Nope
				//|| word.equalsIgnoreCase("day") Nope
				|| word.equalsIgnoreCase("could")
				|| word.equalsIgnoreCase("go")
				//|| word.equalsIgnoreCase("come") Nope
				|| word.equalsIgnoreCase("did")
				|| word.equalsIgnoreCase("my")
				//|| word.equalsIgnoreCase("sound") Nope
				|| word.equalsIgnoreCase("no")
				|| word.equalsIgnoreCase("most")
				//|| word.equalsIgnoreCase("number") Nope
				|| word.equalsIgnoreCase("who")
				//|| word.equalsIgnoreCase("over") Nope
				//|| word.equalsIgnoreCase("know") Nope
				//|| word.equalsIgnoreCase("water") Nope
				|| word.equalsIgnoreCase("than")
				//|| word.equalsIgnoreCase("call") Nope
				//|| word.equalsIgnoreCase("first") Nope
				//|| word.equalsIgnoreCase("people") Nope
				|| word.equalsIgnoreCase("may")
				//|| word.equalsIgnoreCase("down") Nope
				//|| word.equalsIgnoreCase("side") Nope
				|| word.equalsIgnoreCase("been")
				//|| word.equalsIgnoreCase("now") Nope
				//|| word.equalsIgnoreCase("find") Nope
			) {
				list.remove(i);
			}
		}
	}
	
	public static String repeatChar(char character, int length) {
		if (length <= 0) {
			return "";
		}
		
		StringBuffer retVal = new StringBuffer(length);
		
		for (int i = 0; i < length; i++) {
			retVal.append(character);
		}
		
		return retVal.toString();
	}
	
	//private
	
}
