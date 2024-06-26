package net.sf.mcf2pdf.mcfelements.impl;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.mcf2pdf.mcfelements.McfCorner;
import net.sf.mcf2pdf.mcfelements.McfCorners;

public class McfCornersImpl implements McfCorners {

	private final static Logger log = LoggerFactory.getLogger(McfCornersImpl.class);
	private List<McfCorner> corners = new Vector<McfCorner>();

	@Override
	public List<? extends McfCorner> getCorners() {
		return corners;
	}

	public void addCorner(McfCorner corner) {
		log.debug("add corner");
		corners.add(corner);
	}
}
