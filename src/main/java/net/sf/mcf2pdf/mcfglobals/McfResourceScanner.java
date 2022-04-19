/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfglobals;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.digester3.Digester;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mcf2pdf.mcfconfig.Decoration;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfconfig.Template;
import net.sf.mcf2pdf.mcfelements.impl.DigesterConfiguratorImpl;

/**
 * "Dirty little helper" which scans installation directory and temporary
 * directory of fotobook software for background images, cliparts, fonts,
 * and masks (fadings). As there is no (known) usable TOC for these, we just
 * take what we find.
 */
public class McfResourceScanner {
	
	private final static Log log = LogFactory.getLog(McfResourceScanner.class);

	private List<File> scanDirs = new ArrayList<File>();

	private Map<String, File> foundImages = new HashMap<String, File>();

	private Map<String, File> foundClips = new HashMap<String, File>();

	private Map<String, Font> foundFonts = new HashMap<String, Font>();
	
	private Map<String, File> foundColors = new HashMap<String, File>();

	private Map<String, Fading> foundDecorations = new HashMap<String, Fading>();

	private File foundBinding;

	public McfResourceScanner(List<File> scanDirs) {
		this.scanDirs.addAll(scanDirs);
	}

	public void scan() throws IOException {
		for (File f : scanDirs) {
			scanDirectory(f);
		}
	}

	private void scanDirectory(File dir) throws IOException {
		if (!dir.isDirectory())
			return;

		for (File f : dir.listFiles()) {
			if (f.isDirectory())
				scanDirectory(f);
			else {
				String nm = f.getName().toLowerCase(Locale.US);
				String path = f.getAbsolutePath();
				log.debug("nm="+nm);
				log.debug("path="+path);
				if (nm.matches(".+\\.(jp(e?)g|webp|bmp)")) {
					String id = nm.substring(0, nm.indexOf("."));
					foundImages.put(id, f);
				}
				else if (nm.matches(".+\\.(clp|svg)")) {
					String id = f.getName().substring(0, nm.lastIndexOf("."));
					foundClips.put(id, f);
				}
				else if (nm.equals("1_color_backgrounds.xml")) {
					log.debug("Processing 1-color backgrounds " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if (nm.equals("backgrounds_default.xml")) {
					log.debug("Processing default backgrounds " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if (nm.equals("multicolor-2012.xml")) {
					log.debug("Processing multicolor-2012 backgrounds " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if (nm.equals("multicolor-bg.xml")) {
					log.debug("Processing multicolor-bg " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if(nm.matches(".+\\.ttf")) {
					Font font = loadFont(f);
					foundFonts.put(font.getFamily(), font);
				}
				else if(nm.matches("normalbinding.*\\.png")) {
					foundBinding = f;
				}
				else if (nm.matches(".+\\.xml") && path.contains("decorations")) {
					String id = f.getName().substring(0, nm.lastIndexOf("."));
					List<Decoration> spec = loadDecoration(f);
					if (spec.size() == 1) {
						String type = spec.get(0).getType();

						String designElementId = spec.get(0).getDesignElementId();
						if (designElementId != null)
							foundDecorations.put(spec.get(0).getDesignElementId(), spec.get(0).getFading());
						else {
							if(type.equalsIgnoreCase("fading")) {
								String fadingdesignElementId = spec.get(0).getFading().getDesignElementId();
								if (fadingdesignElementId != null) {
									Fading temp = new Fading();
									temp.setFile(spec.get(0).getFading().getFile());
									foundDecorations.put(fadingdesignElementId, temp);
								}
								continue;
							}
							if(type.equalsIgnoreCase("clipart")) {
								// clipboard
								String clipartdesignElementId = spec.get(0).getClipart().getDesignElementId();
								if (clipartdesignElementId != null) {
									Fading temp = new Fading();
									temp.setClipart(spec.get(0).getClipart());
									foundDecorations.put(clipartdesignElementId, temp);
								}
								continue;
							}
						}
						continue;
					}
					if(spec.size() >1) {
						log.info("Processing decorations from: " + path);
						for(Decoration dec:spec) {
							String type = dec.getType();
							// fading
							if(type.equalsIgnoreCase("fading")) {
								String designElementId = dec.getFading().getDesignElementId();
								if (designElementId != null) {
									Fading temp = new Fading();
									temp.setFile(dec.getFading().getFile());
									foundDecorations.put(designElementId, temp);
								}
								continue;
							}
							if(type.equalsIgnoreCase("clipart")) {
									// clipboard
									String designElementId = dec.getClipart().getDesignElementId();
									if (designElementId != null) {
										Fading temp = new Fading();
										temp.setClipart(dec.getClipart());
										foundDecorations.put(designElementId, temp);
									}
									continue;
							}
						}
					} else {
						log.warn("Failed to load decorations from: " + path);
					}
				}
			}
		}
	}

	private static Font loadFont(File f) throws IOException {
		FileInputStream is = new FileInputStream(f);
		try {
			return Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (FontFormatException e) {
			throw new IOException(e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	private static List<Template> loadColorsMapping(File f) {
		Digester digester = new Digester();
		DigesterConfiguratorImpl configurator = new DigesterConfiguratorImpl();
		try {
			configurator.configureDigester(digester, f);
			return digester.parse(f);
		} catch (Exception e) {
			log.warn("Cannot parse 1-color file", e);
		}
		return Collections.emptyList();
	}
	
	private static List<Decoration> loadDecoration(File f) {
		Digester digester = new Digester();
		DigesterConfiguratorImpl configurator = new DigesterConfiguratorImpl();
		try {
			configurator.configureDigester(digester, f);
			return digester.parse(f);
		} catch (Exception e) {
			log.warn("Failed to load decorations", e);
		}
		return null;
	}


	public File getImage(String id) {
		return foundImages.get(id);
	}
	
	public File getColorImage(String name) {
		return foundColors.get(name);
	}

	public File getClip(String id) {
		return foundClips.get(id);
	}

	public File getBinding() {
		return foundBinding;
	}

	public Font getFont(String id) {
		return foundFonts.get(id);
	}

	public Fading getDecoration(String id) {
		return foundDecorations.get(id);
	}
}
