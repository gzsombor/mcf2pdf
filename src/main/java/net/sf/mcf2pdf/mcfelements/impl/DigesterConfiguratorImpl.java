/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import net.sf.mcf2pdf.mcfelements.*;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.AbstractConverter;
import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Substitutor;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import net.sf.mcf2pdf.mcfconfig.Clipart;
import net.sf.mcf2pdf.mcfconfig.Decoration;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfconfig.Fotoarea;
import net.sf.mcf2pdf.mcfconfig.Template;
import net.sf.mcf2pdf.mcfelements.util.DigesterUtil;


/**
 * Default Digester configurator. Configures the digester to use classes from
 * mcfelements.impl package and sets known properties (currently, mainly
 * properties required for converting the book). <br>
 * You can subclass this class and pass the new configurator to
 * <code>FotobookBuilder.readFotobook()</code> to adapt object creation to your
 * needs.
 */
public class DigesterConfiguratorImpl implements DigesterConfigurator {

	public DigesterConfiguratorImpl() {
		// registers our custom color converter
		ConvertUtils.register(colorConverter, Color.class);
	}

	@SuppressWarnings("rawtypes")
	private final static Converter colorConverter = new AbstractConverter() {
		@Override
		protected Class getDefaultType() {
			return String.class;
		}

		@Override
		protected Object convertToType(Class type, Object value) throws Throwable {
			if (value instanceof String) {
				// could be full INT when containing Alpha channel
				if (!value.toString().startsWith("#")) {
					return longToColor(Long.parseLong(value.toString()));
				}

				// could still be full INT, but in HEX notation...
				if (value.toString().length() > 7) {
					return longToColor(Long.parseLong(value.toString().substring(1), 16));
				}

				return Color.decode(value.toString());
			}

			if (value instanceof Color)
				return value;

			throw new ConversionException("Cannot convert to color: " + value);
		}

		private Color longToColor(long colorValue) {
			return new Color((int)(colorValue & 0xFFFFFFFF), true);
		}
	};

	@Override
	public void configureDigester(Digester digester, File mcfFile)
			throws IOException {
		digester.setSubstitutor(createSubstitutor());

		// fotobook element
		digester.addObjectCreate("fotobook", getFotobookClass());
		DigesterUtil.addSetProperties(digester, "fotobook", getSpecialFotobookAttributes());
		
		// pagenumbering element
		digester.addObjectCreate("fotobook/pagenumbering", getPageNumClass());
		digester.addSetTop("fotobook/pagenumbering", "setFotobook");
//		digester.addSetProperties("fotobook/pagenumbering");
		DigesterUtil.addSetProperties(digester, "fotobook/pagenumbering", getSpecialPageNumAttributes());
		digester.addSetNext("fotobook/pagenumbering", "addPageNum", McfPageNum.class.getName());

		// page element
		digester.addObjectCreate("fotobook/page", getPageClass());
		digester.addSetTop("fotobook/page", "setFotobook");
		DigesterUtil.addSetProperties(digester, "fotobook/page", getSpecialPageAttributes());
		digester.addSetNext("fotobook/page", "addPage", McfPage.class.getName());
		
		// bundlesize element
		digester.addObjectCreate("fotobook/page/bundlesize", getBundlesizeClass());
		digester.addSetTop("fotobook/page/bundlesize", "setPage");
		digester.addSetProperties("fotobook/page/bundlesize");
		digester.addSetNext("fotobook/page/bundlesize", "setBundlesize", McfBundlesize.class.getName());

		// background element
		digester.addObjectCreate("fotobook/page/background", getBackgroundClass());
		digester.addSetTop("fotobook/page/background", "setPage");
		DigesterUtil.addSetProperties(digester, "fotobook/page/background", getSpecialBackgroundAttributes());
		digester.addSetNext("fotobook/page/background", "addBackground", McfBackground.class.getName());

		// area element
		digester.addObjectCreate("fotobook/page/area", getAreaClass());
		digester.addSetTop("fotobook/page/area", "setPage");
		DigesterUtil.addSetProperties(digester, "fotobook/page/area", getSpecialAreaAttributes());
		digester.addSetNext("fotobook/page/area", "addArea", McfArea.class.getName());

		// border element
		digester.addObjectCreate("fotobook/page/area/border", getBorderClass());
		DigesterUtil.addSetProperties(digester, "fotobook/page/area/border", getSpecialBorderAttributes());
		digester.addSetNext("fotobook/page/area/border", "setBorder");

		// border element Version 4.x
		digester.addObjectCreate("fotobook/page/area/decoration/border", getBorderClass());
		DigesterUtil.addSetProperties(digester, "fotobook/page/area/decoration/border", getSpecialBorderAttributes());
		digester.addSetNext("fotobook/page/area/decoration/border", "setBorder");

		// text element, including textFormat element
		digester.addObjectCreate("fotobook/page/area/text", getTextClass());
		digester.addSetProperties("fotobook/page/area/text");
		digester.addCallMethod("fotobook/page/area/text", "setHtmlContent", 0);
		DigesterUtil.addSetProperties(digester, "fotobook/page/area/text/textFormat", getSpecialTextFormatAttributes());
		digester.addSetNext("fotobook/page/area/text", "setContent");
		digester.addSetTop("fotobook/page/area/text", "setArea");

		// clipart element
		digester.addObjectCreate("fotobook/page/area/clipart", getClipartClass());
		digester.addSetProperties("fotobook/page/area/clipart");
		digester.addSetNext("fotobook/page/area/clipart", "setContent");
		digester.addSetTop("fotobook/page/area/clipart", "setArea");

		// image element
		digester.addObjectCreate("fotobook/page/area/image", getImageClass());
		DigesterUtil.addSetProperties(digester, "fotobook/page/area/image", getSpecialImageAttributes());
		digester.addSetNext("fotobook/page/area/image", "setContent");
		digester.addSetTop("fotobook/page/area/image", "setArea");

		// cutout element Version 4.x
		digester.addObjectCreate("fotobook/page/area/image/cutout", getCutoutClass());
		DigesterUtil.addSetProperties(digester, "fotobook/page/area/image/cutout", getSpecialCutoutAttributes());
		digester.addSetNext("fotobook/page/area/image/cutout", "setCutout", McfCutout.class.getName());


		// imagebackground element
		digester.addObjectCreate("fotobook/page/area/imagebackground", getImageBackgroundClass());
		DigesterUtil.addSetProperties(digester, "fotobook/page/area/imagebackground", getSpecialImageAttributes());
		digester.addSetNext("fotobook/page/area/imagebackground", "setContent");
		digester.addSetTop("fotobook/page/area/imagebackground", "setArea");

		// position element
		digester.addObjectCreate("fotobook/page/area/position", getPositionClass());
		DigesterUtil.addSetProperties(digester, "fotobook/page/area/position", getSpecialPositionAttributes());
		digester.addSetNext("fotobook/page/area/position", "setPosition");



		// colors config file
		digester.addObjectCreate("templates", LinkedList.class);
		digester.addObjectCreate("templates/template", Template.class);
		digester.addSetProperties("templates/template");
		digester.addSetNext("templates/template", "add");
		
		// Decorations (fotoframes)
		digester.addObjectCreate("decorations", LinkedList.class);
		digester.addObjectCreate("decorations/decoration", Decoration.class);
		DigesterUtil.addSetProperties(digester, "decorations/decoration", getSpecialDecorationAttributes());
		digester.addObjectCreate("decorations/decoration/fading", Fading.class);
		digester.addSetProperties("decorations/decoration/fading");
		digester.addSetNext("decorations/decoration/fading", "setFading");
		// old
		digester.addObjectCreate("decorations/decoration/fading/clipart", Clipart.class);
		digester.addSetProperties("decorations/decoration/fading/clipart");
		digester.addSetNext("decorations/decoration/fading/clipart", "setClipart");

		// new file with cliparts 7.1.5


		digester.addObjectCreate("decorations/decoration/clipart", Clipart.class);
		digester.addSetProperties("decorations/decoration/clipart");
		digester.addSetNext("decorations/decoration/clipart", "setClipart");


		// Decorations clipart


		digester.addObjectCreate("decorations/decoration/fading/fotoarea", Fotoarea.class);
		digester.addSetProperties("decorations/decoration/fading/fotoarea");
		digester.addSetNext("decorations/decoration/fading/fotoarea", "setFotoarea");
		digester.addSetNext("decorations/decoration", "add");
	}

	private final static Substitutor FLOAT_SUBSTITUTOR = new Substitutor() {
		@Override
		public String substitute(String bodyText) {
			return bodyText;
		}

		@Override
		public Attributes substitute(Attributes attributes) {
			AttributesImpl result = new AttributesImpl(attributes);

			for (int i = 0; i < result.getLength(); i++) {
				String name = result.getLocalName(i);
				if (name.matches("left|top|width|height")) {
					String value = result.getValue(i);
					if (value.matches("[0-9]*,[0-9]+"))
						result.setValue(i, value.replace(",", "."));
				}
			}

			return result;
		}
	};


	/**
	 * Creates a substitutor which will be used for attribute and text substitutions
	 * when parsing. Default implementation replaces a comma by a period in attributes
	 * with name <code>left, top, width, height</code>. Subclasses can override
	 * for more substitutions; use a <code>CompoundSubsitutor</code> to add the
	 * subsitutions created by the default implementation.
	 *
	 * @return A substitutor to use in the digester.
	 */
	protected Substitutor createSubstitutor() {
		return FLOAT_SUBSTITUTOR;
	}

	protected Class<? extends McfFotobook> getFotobookClass() {
		return McfFotobookImpl.class;
	}

	protected List<String[]> getSpecialFotobookAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "productname", "productName" });
		result.add(new String[] { "imagedir", "imageDir" });
		result.add(new String[] { "normalpages", "normalPages" });
		result.add(new String[] { "totalpages", "totalPages" });
		result.add(new String[] { "producttype", "productType" });
		result.add(new String[] { "programversion", "programVersion" });
		return result;
	}
	
	protected Class<? extends McfPageNum> getPageNumClass() {
		return McfPageNumImpl.class;
	}
	
	protected List<String[]> getSpecialPageNumAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "textstring", "textString" });
		result.add(new String[] { "margin", "horizontalMargin" });
		result.add(new String[] { "textcolor", "textColor" });
		result.add(new String[] { "bgcolor", "bgColor" });
		result.add(new String[] { "fontsize", "fontSize" });
		result.add(new String[] { "fontbold", "fontBold" });
		result.add(new String[] { "fontitalics", "fontItalics" });
		result.add(new String[] { "fontfamily", "fontFamily"});
		return result;
	}

	protected Class<? extends McfPage> getPageClass() {
		return McfPageImpl.class;
	}

	protected List<String[]> getSpecialPageAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "pagenr", "pageNr" });
		return result;
	}
	
	protected Class<? extends McfBundlesize> getBundlesizeClass() {
		return McfBundlesizeImpl.class;
	}

	protected Class<? extends McfBackground> getBackgroundClass() {
		return McfBackgroundImpl.class;
	}

	protected List<String[]> getSpecialBackgroundAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "templatename", "templateName" });
		result.add(new String[] { "designElementId", "designElementId" });
		result.add(new String[] { "fading", "fading" });
		result.add(new String[] { "hue", "hue" });
		result.add(new String[] { "rotation", "rotation" });
		return result;
	}

	protected Class<? extends McfImage> getImageClass() {
		return McfImageImpl.class;
	}

	protected List<String[]> getSpecialImageAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "filenamemaster", "fileNameMaster" });
		result.add(new String[] { "filename", "fileName" });
		result.add(new String[] { "fading", "fadingFile" });
		result.add(new String[] { "top", "top" });
		result.add(new String[] { "left", "left" });
		result.add(new String[] { "scale", "scale" });
		result.add(new String[] { "passepartoutDesignElementId", "passepartoutDesignElementId" });
		return result;
	}

	protected Class<? extends McfArea> getAreaClass() {
		return McfAreaImpl.class;
	}

	protected List<String[]> getSpecialAreaAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "areatype", "areaType" });
		result.add(new String[] { "borderenabled", "borderEnabled" });
		result.add(new String[] { "sizeborder", "borderSize" });
		result.add(new String[] { "colorborder", "borderColor" });
		result.add(new String[] { "backgroundcolor", "backgroundColor" });
		return result;
	}

	protected Class<? extends McfText> getTextClass() {
		return McfTextImpl.class;
	}

	protected List<String[]> getSpecialTextFormatAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "backgroundColor", "backgroundColorAlpha" });
		result.add(new String[] { "VerticalIndentMargin", "verticalIndentMargin" });
		result.add(new String[] { "IndentMargin", "indentMargin" });
		return result;
	}

	protected Class<? extends McfClipart> getClipartClass() {
		return McfClipartImpl.class;
	}

	protected Class<? extends McfImageBackground> getImageBackgroundClass() {
		return McfImageBackgroundImpl.class;
	}

	protected Class<? extends McfBorder> getBorderClass() {
		return McfBorderImpl.class;
	}

	protected List<String[]> getSpecialBorderAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "color", "color" });
		result.add(new String[] { "offset", "offset" });
		result.add(new String[] { "width", "width" });
		result.add(new String[] { "enabled", "enabled" });
		return result;
	}

	protected Class<? extends McfPosition> getPositionClass() {
		return McfPositionImpl.class;
	}

	protected List<String[]> getSpecialPositionAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "height", "height" });
		result.add(new String[] { "left", "left" });
		result.add(new String[] { "rotation", "rotation" });
		result.add(new String[] { "top", "top" });
		result.add(new String[] { "width", "width" });
		result.add(new String[] { "zposition", "zposition" });
		return result;
	}

	protected Class<? extends McfCutout> getCutoutClass() {
		return McfCutoutImpl.class;
	}

	protected List<String[]> getSpecialCutoutAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[]{"left", "left"});
		result.add(new String[]{"scale", "scale"});
		result.add(new String[]{"top", "top"});
		return result;
	}

	protected List<String[]> getSpecialDecorationAttributes() {
		List<String[]> result = new Vector<String[]>();
		result.add(new String[] { "type", "type" });
		result.add(new String[] { "id", "id" });
		result.add(new String[] { "designElementId", "designElementId" });
		return result;
	}

}
