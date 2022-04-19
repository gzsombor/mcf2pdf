/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfClipart;

public class McfClipartImpl extends AbstractMcfAreaContentImpl implements McfClipart {
	
	private String uniqueName;


	private String designElementId;

	@Override
	public ContentType getContentType() {
		return ContentType.CLIPART;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public String getdesignElementId() {
		return designElementId;
	}

	public void setdesignElementId(String designElementId) {
		this.designElementId = designElementId;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}


}
