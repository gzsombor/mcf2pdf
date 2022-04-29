/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

/**
 * TODO comment
 */
public interface McfBackground {
	
	public McfPage getPage();

	public String getTemplateName();

	public String getDesignElementId();
	public int getType();

	public int getLayout();

	public int getHue();

	public int getRotation();

	public int getFading();

}