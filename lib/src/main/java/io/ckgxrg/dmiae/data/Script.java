package io.ckgxrg.dmiae.data;

import java.util.ArrayList;
import java.util.HashSet;

public class Script {
	public String name;
	public String author;
	public String credits;
	public ScriptProps props = new ScriptProps();
	
	public HashSet<Character> characters = new HashSet<Character>();
	public ArrayList<Line> lines = new ArrayList<Line>();
}
