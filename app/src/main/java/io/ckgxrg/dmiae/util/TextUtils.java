package io.ckgxrg.dmiae.util;

import java.util.HashSet;
import java.util.regex.Pattern;

import io.ckgxrg.dmiae.cli.Resolver;
import io.ckgxrg.dmiae.data.AnnotationType;
import io.ckgxrg.dmiae.data.Character;

public class TextUtils {
	
	//Remove leading spaces
	public static String rmSpaces(String s) {
		String ss = "";
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) != ' ') {
				ss = s.substring(i);
				break;
			}
		}
		for(int j = ss.length() - 1; j >= 0; j--) {
//			/System.out.println(ss.charAt(j));
			if(ss.charAt(j) != ' ') {
				return ss.substring(0, j + 1);
			}
		}
		return "";
	}
	//Remove character names
	public static String rmName(String s, String name) {
		String[] sss = s.split(name);
		if(sss.length <= 1)
			return "";
		String ss = sss[1];
		return untilLetter(ss);
	}
	
	//Remove everything until a letter appears
	public static String untilLetter(String s) {
		s = rmSpaces(s);
		for(int i = 0; i < s.length(); i++) {
			if(Pattern.matches("^[\\u4E00-\\u9FA5A-Za-z0-9]+$", "" + s.charAt(i))) {
				return s.substring(i);
			}
		}
		return "";
	}
	
	//Remove Subline leading colon
		public static String rmColon(String s) {
			if(s.startsWith(":")) return s.substring(1);
			if(s.startsWith("<:") || s.startsWith(">:")) return s.substring(2);
			return s;
		}
	
	//Intercepts the character identification if the Character's name is just separated
	//with a single space, which is ambiguous.
	public static boolean checkWeakDeclaration(String line, String name) {
		if(line.split(name).length > 1 && line.split(name)[1].startsWith(" ")) {
			if(line.split(name)[1].isBlank()) return true;
			System.out.println("[DMIAE]===>Ambiguity detected at: " + line);
			System.out.print("[DMIAE]===>Should this line be " + name + "'s? (y/N):");
			String ss = Resolver.INSTANCE.in.nextLine();
			return ss.equalsIgnoreCase("y");
		}
		return true;
	}
	
	//Identifies the Annotation Type
	public static AnnotationType identifyAnnoType(String s) {
		String ss = s.split(":")[1];
		switch(ss) {
		case "LIGHTING", "LIGHT", "LT", "L" :
			return AnnotationType.LIGHTING;
		case "AUDIO", "A", "MUSIC", "SOUND" :
			return AnnotationType.AUDIO;
		case "CHARACTER" :
			return AnnotationType.CHARACTER;
		default :
			return AnnotationType.NOTE;
		}
	}
	
	//**Sign**, why I allow Annotations with length 2 is seen as note.
	public static String getAnnoContent(String s) {
		String[] ss = s.split(":");
		return ss.length <= 2 ? ss[1] : ss[2];
	}
	
	//Returns the names of characters
	public static String getCharaName(HashSet<Character> ch) {
		StringBuilder sb = new StringBuilder();
		for(Character c : ch) {
			sb.append("@" + c.getAmbiguousName() + " / ");
		}
		sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
	}
}
