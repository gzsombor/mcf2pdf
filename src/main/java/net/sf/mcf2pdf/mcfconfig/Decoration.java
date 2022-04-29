package net.sf.mcf2pdf.mcfconfig;

public class Decoration {
	private Fading fading;

	public Clipart getClipart() {
		return clipart;
	}

	public void setClipart(Clipart clipart) {
		this.clipart = clipart;
	}

	// new for cliparts
	private Clipart clipart;

	public void setId(String id) {
		this.id = id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setDesignElementId(String designElementId) {
		this.designElementId = designElementId;
	}

	private String id;

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getDesignElementId() {
		return designElementId;
	}

	private String type;
	private String designElementId;

	public Fading getFading() {
		return fading;
	}
	public void setFading(Fading fading) {
		this.fading = fading;
	}

}