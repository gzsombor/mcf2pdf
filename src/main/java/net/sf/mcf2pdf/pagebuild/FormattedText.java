/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;

public class FormattedText {
	
	private String text;
	
	private boolean bold;
	private boolean italic;
	private boolean underline;
	
	private Color textColor;
	
	private String fontFamily;
	private float fontSize;

	private int margintop;
	private int marginleft;
	private int marginbottom;
	private int marginright;

	public FormattedText(String text, boolean bold, boolean italic,
			boolean underline, Color textColor, String fontFamily, float fontSize,
						 int margintop,int marginright,int marginbottom,int marginleft) {
		this.text = text;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.textColor = textColor;
		this.fontFamily = fontFamily;
		this.fontSize = fontSize;
		this.margintop =margintop;
		this.marginright =marginright;
		this.marginbottom = marginbottom;
		this.marginleft = marginleft;
	}

	public String getText() {
		return text;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public boolean isUnderline() {
		return underline;
	}

	public Color getTextColor() {
		return textColor;
	}
	
	public String getFontFamily() {
		return fontFamily;
	}

	public float getFontSize() {
		return fontSize;
	}

	public int getMargintop() { return margintop;}

	public int getMarginleft() { return marginleft;}

	public int getMarginbottom() {return marginbottom;}

	public int getMarginright() {return marginright;}

}
