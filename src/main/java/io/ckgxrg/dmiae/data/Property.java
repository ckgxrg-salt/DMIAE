package io.ckgxrg.dmiae.data;

public class Property {
	
	String key;
	String value;
	PropDomain domain;
	
	Property(String key){
		this(key, null);
	}
	
	Property(String key, String value){
		this.value = value;
		this.key = key;
	}
}
