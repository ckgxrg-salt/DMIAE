package io.ckgxrg.dmiae.data;

public class Annotation implements ILineAttached {

	public Line parent;
	public AnnotationType type;
	public String content;
	
	public Annotation(String content, AnnotationType prop) {
		this.content = content;
		this.type = prop;
	}
	
	@Override
	public Line getParent() {
		return this.parent;
	}

	@Override
	public void setParent(Line parent) {
		this.parent = parent;
	}

}
