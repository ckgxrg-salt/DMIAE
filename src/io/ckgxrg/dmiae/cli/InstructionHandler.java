package io.ckgxrg.dmiae.cli;

import java.lang.reflect.Field;

import io.ckgxrg.dmiae.data.Character;

public class InstructionHandler {
	//Called if a character declaration is read.
	public static Character handleCharacter(String s) {
		String[] ss = s.split(":");
		String desc = "";
		if(ss.length != 1) desc = ss[1];
		String[] name = ss[0].split(",");
		Character c = new Character(name);
		c.addDesc(desc);
		System.out.println("Character: @" + s);
		return c;
	}
	
	//Called when a declaration-time property statement is read.
	public static void handleInst(String s) {
		String key = s.split(":")[0].toLowerCase();
		String value = s.split(":")[1];
		switch(key) {
		//General properties
		case "name" :
			System.out.println("Script: " + value);
			Resolver.INSTANCE.sc.name = value;
			break;
		case "author" :
			System.out.println("Author: " + value);
			Resolver.INSTANCE.sc.author = value;
			break;
		case "credit" :
			System.out.println("Credits: " + value);
			Resolver.INSTANCE.sc.credits = value;
			break;
		//Other properties
		default :
			try {
				System.out.println("Property #" + key + " set to " + value);
				Field f = Resolver.INSTANCE.sc.props.getClass().getDeclaredField(key);
				f.setAccessible(true);
				f.set(Resolver.INSTANCE.sc.props, value);
			} catch (NoSuchFieldException e) {
				System.out.println("[DMIAE Resolver] The property " + key + " is unknown, skipping...");
			} catch (SecurityException e) {
				System.out.println("[DMIAE] Unexpected error");
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				System.out.println("[DMIAE] Unexpected error");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				System.out.println("[DMIAE] Unexpected error");
				e.printStackTrace();
			}
		}
	}
}
