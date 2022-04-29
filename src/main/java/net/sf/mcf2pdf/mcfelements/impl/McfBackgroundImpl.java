/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfBackground;
import net.sf.mcf2pdf.mcfelements.McfPage;

public class McfBackgroundImpl implements McfBackground {
	
	private McfPage page;
	
	private String templateName;

	private String designElementId;
	private int type;

	private int layout;

	private int Hue;
	private int Fading;
	private int Rotation;
	
	public McfPage getPage() {
		return page;
	}

	public void setPage(McfPage page) {
		this.page = page;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLayout() {
		return layout;
	}

	@Override
	public int getHue() {
		return Hue;
	}

	public void setHue(int hue) {this.Hue = hue;};

	public void setRotation(int rotation) {this.Rotation = rotation;};

	public void setFading(int fading) {this.Fading = fading;};

	@Override
	public int getRotation() {
		return Rotation;
	}

	@Override
	public int getFading() {
		return Fading;
	}

	public void setLayout(int layout) {
		this.layout = layout;
	}

	public void setDesignElementId(String designElementId) {
		this.designElementId = designElementId;
	}
	@Override
	public String getDesignElementId() {
		return designElementId;
	}
}
