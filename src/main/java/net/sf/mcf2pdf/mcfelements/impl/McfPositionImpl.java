/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfPosition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class McfPositionImpl extends AbstractMcfAreaContentImpl implements McfPosition {
	private final static Log log = LogFactory.getLog(McfPositionImpl.class);

	private float left;

	private float top;

	private float width;

	private float height;

	private float rotation;

	private int zPosition;

	@Override
	public ContentType getContentType() {
		return ContentType.POSITION;
	}

	@Override
	public float getLeft() {
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	@Override
	public float getTop() {
		return top;
	}

	public void setTop(float top) {
		this.top = top;
	}

	@Override
	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	@Override
	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	@Override
	public int getZPosition() {
		return zPosition;
	}

	public void setZPosition(int zPosition) {
		this.zPosition = zPosition;
	}
}
