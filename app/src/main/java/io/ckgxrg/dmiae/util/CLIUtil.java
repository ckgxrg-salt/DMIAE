package io.ckgxrg.dmiae.util;

public class CLIUtil {
	
	public static void log(String s) {
		System.out.print("\033[2K" + s);
	}
}
