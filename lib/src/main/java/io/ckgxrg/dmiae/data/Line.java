package io.ckgxrg.dmiae.data;

import java.util.ArrayList;
import java.util.HashSet;

public class Line {
	
	public String text;
	public HashSet<Character> characters;
	ArrayList<Annotation> annobefore = new ArrayList<Annotation>();
	ArrayList<Annotation> annoafter = new ArrayList<Annotation>();
	ArrayList<Subline> subbefore = new ArrayList<Subline>();
	ArrayList<Subline> subafter = new ArrayList<Subline>();
	
	protected Line(String text) {
		this.text = text;
	}
	
	public Line(String text, HashSet<Character> ch) {
		this.text = text;
		this.characters = ch;
	}
	
	public void addAnnotationAfter(Annotation a) {
		annoafter.add(a);
	}
	public void addAnnotationBefore(Annotation a) {
		annobefore.add(a);
	}
	public void addSublineAfter(Subline sub) {
		subafter.add(sub);
	}
	public void addSublineBefore(Subline sub) {
		subbefore.add(sub);
	}
}
