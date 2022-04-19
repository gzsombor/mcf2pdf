/*******************************************************************************
 * ${licenseText}
 * All rights reserved. This file is made available under the terms of the
 * Common Development and Distribution License (CDDL) v1.0 which accompanies
 * this distribution, and is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPreserveAspectRatio;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;

import net.sf.mcf2pdf.mcfelements.util.webp.Webp;
import net.sf.mcf2pdf.mcfelements.util.webp.WebpLib;

/**
 * Utility class for working with images in the context of the mcf2pdf project.
 */
public final class ImageUtil {

	private final static Log log = LogFactory.getLog(ImageUtil.class);
	
	/*
	 * FIXME this uses CEWE Fotobook for MY personal pictures when they do not
	 * have resolution information.
	 * Don't know if this is default, or just taken from other photos in that MCF file?
	 */
	private static final float DEFAULT_RESOLUTION = 180.0f;

	public static final float MM_PER_INCH = 25.4f;

	public static final double SQRT_2 = Math.sqrt(2);

	private static WebpLib qt5Library;

	private ImageUtil() {
	}

	/**
	 * Retrieves resolution information from the given image file. As CEWE algorithm seems to have changed, always returns default
	 * resolution for JPEG files.
	 *
	 * @return An array containing the x- and the y-resolution, in dots per inch, of the given file.
	 *
	 * @throws IOException If any I/O related problem occurs reading the file.
	 */
	public static float[] getImageResolution(File imageFile) throws IOException {

		return new float[] { DEFAULT_RESOLUTION, DEFAULT_RESOLUTION };
	}

	public static BufferedImage readImage(File imageFile) throws IOException {
		int rotation = getImageRotation(imageFile);
		BufferedImage img = internalRead(imageFile);

		if (rotation == 0) {
			return img;
		}

		boolean swapXY = rotation != 180;

		BufferedImage rotated = new BufferedImage(swapXY ? img.getHeight() : img.getWidth(), swapXY ? img.getWidth() : img.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = rotated.createGraphics();
		g2d.translate((rotated.getWidth() - img.getWidth()) / 2, (rotated.getHeight() - img.getHeight()) / 2);
		g2d.rotate(Math.toRadians(rotation), img.getWidth() / 2, img.getHeight() / 2);

		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();

		return rotated;
	}

	private static int getImageRotation(File imageFile) throws IOException {
		try {
			Metadata md = ImageMetadataReader.readMetadata(imageFile);

			ExifDirectoryBase ed = md.getFirstDirectoryOfType(ExifDirectoryBase.class);

			if (ed != null) {
				if (ed.containsTag(ExifDirectoryBase.TAG_ORIENTATION)) {
					int o = ed.getInt(ExifDirectoryBase.TAG_ORIENTATION);
					switch (o) {
					case 3:
						return 180;
					case 6:
						return 90;
					case 8:
						return 270;
					}
				}
			}
			return 0;
		} catch (ImageProcessingException e) {
			throw new IOException(e);
		} catch (MetadataException e) {
			return 0;
		}

	}

	/**
	 * Loads the given CLP or SVG file and creates a BufferedImage with the given dimensions. As CLP files contain Vector images,
	 * they can be scaled to every size needed. The contents are scaled to the given width and height, <b>not</b> preserving any
	 * ratio of the image.
	 *
	 * @param clpFile CLP or SVG file.
	 * @param widthPixel The width, in pixels, the resulting image shall have.
	 * @param heightPixel The height, in pixels, the resulting image shall have.
	 *
	 * @return An image displaying the contents of the loaded CLP file.
	 *
	 * @throws IOException If any I/O related problem occurs reading the file.
	 */
	public static BufferedImage loadClpFile(File clpFile, int widthPixel, int heightPixel) throws IOException {
		
		UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
        BridgeContext bridgeContext = new BridgeContext(userAgentAdapter);

        SVGDocument svgDocument;
        GraphicsNode rootSvgNode;
        svgDocument = getSVGDocument(clpFile);
        rootSvgNode = getRootNode(svgDocument, bridgeContext);
        float[] vb = getViewBox(bridgeContext, svgDocument);
		//


// Finally, stream out SVG to the standard output using UTF-8
// character to byte encoding
		if(clpFile.getName().contains("clp")) {
			boolean useCSS = true; // we want to use CSS style attribute
			Writer out;
			try {
				SVGGraphics2D graphics = new SVGGraphics2D(svgDocument);
				out = new OutputStreamWriter(new FileOutputStream(clpFile.getName().replace("clp","svg")), "UTF-8");
				graphics.stream(svgDocument.getDocumentElement(), out,useCSS,false);
				out.flush();
				out.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (SVGGraphics2DIOException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		AffineTransform usr2dev = ViewBox.getPreserveAspectRatioTransform(vb, SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE,
				true, widthPixel, heightPixel);
		// ugly fix for some clips
		AffineTransform result = new AffineTransform();
		if(usr2dev.getTranslateX()<0) {
			log.debug("usr2dev="+usr2dev.toString());
			log.debug("fixing for "+clpFile);
			//usr2dev.translate(0,usr2dev.getTranslateY());
			result = new AffineTransform();
			result.scale((double)(widthPixel / vb[2]), (double)(heightPixel / vb[3]));
			if(vb[0]>0)
				vb[0]=0;
			result.translate((double)(-vb[0]), (double)(-vb[1]));
			usr2dev = result;
		}

		BufferedImage img = new BufferedImage(widthPixel, heightPixel, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();

		g2d.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
		g2d.fillRect(0, 0, widthPixel, heightPixel);
		g2d.transform(usr2dev);

		// fixes "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" - part 1
		final Object oldBufferedImage = g2d
				.getRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE);
		g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE,
				new WeakReference<BufferedImage>(img));
		rootSvgNode.paint(g2d);
		// fixes "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" - part 2
		if (oldBufferedImage != null)
			g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE,
					oldBufferedImage);
		else
			g2d.getRenderingHints().remove(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE);

		g2d.dispose();
		return img;
	}

	 /**
     * Returns SVG document from File
      * @param  clpFile to file clp/svg.
     *
      *
     * @return SVG Document
      * @throws IOException If any I/O related problem occurs reading the file.
     */
     public static SVGDocument getSVGDocument(File clpFile)
                  throws IOException, UnsupportedEncodingException {
           FileInputStream fis = new FileInputStream(clpFile);
           ClpInputStream cis = null;
           InputStream in = clpFile.getName().toLowerCase().endsWith(".clp") ? (cis = new ClpInputStream(fis)) : fis;
           SVGDocument svgDocument;
           try {
         String parser = XMLResourceDescriptor.getXMLParserClassName();
         SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
                  svgDocument = (SVGDocument)factory.createDocument(clpFile.toURI().toString(), new InputStreamReader(in, "ISO-8859-1"));
           }
           finally {
                  IOUtils.closeQuietly(cis);
                  IOUtils.closeQuietly(fis);
           }
           return svgDocument;
     }
	
	/**
     * Create default array view box for scaling svg or clp files based on width/height of image attributes
      * @param svgDocument SVGDocument.
     *
     * @return float array with default viewbox for clp/or/svg file based on width/height of SVGDocument
     *
     */
     private static float[] createDefaultViewBox(SVGDocument svgDocument) {

           Element svgRoot = svgDocument.getDocumentElement();
           Attr width =null;
           Attr height = null;
           width = svgRoot.getAttributeNodeNS(null, "width");   
           if(width!=null) {
                  log.debug("width in file="+width.getValue());
           }
           height = svgRoot.getAttributeNodeNS(null, "height");
           if(height!=null) {
                  log.debug("height in file="+height.getValue());
           }
           log.debug("Creating default viewbox with 0f,0.0f,"+width.getValue()+","+height.getValue());
           return new float[] {0.0f,0.0f,Float.parseFloat(width.getValue()),Float.parseFloat(height.getValue())};
     }

     /**
     * Returns viewbox array from document or create default
      * @param svgDocument SVGDocument.
     * @param bridgeContext BridgeContext
     *
     * @return float array with default viewbox for clp/or/svg file based on width/height of SVGDocument
     *
     */
     public static float[] getViewBox(BridgeContext bridgeContext,
                  SVGDocument svgDocument) {
           float[] vb = ViewBox.parseViewBoxAttribute(svgDocument.getRootElement(),
                         svgDocument.getRootElement().getAttribute("viewBox"), bridgeContext);
           if( vb == null )
           {
                  log.debug("viewbox empty");
                  vb = createDefaultViewBox(svgDocument);
           }
           return vb;
     }
	
	private static GraphicsNode getRootNode(SVGDocument document, BridgeContext bridgeContext) {
		// Build the tree and get the document dimensions
		GVTBuilder builder = new GVTBuilder();
		return builder.build(bridgeContext, document);
	}

	/**
	 * Rotates the given buffered image by the given angle, and returns a newly
	 * created image, containing the rotated image.
	 *
	 * @param img Image to rotate.
	 * @param angle Angle, in radians, by which to rotate the image.
	 * @param drawOffset Receives the offset which is required to draw the image,
	 * relative to the original (0,0) corner, so that the center of the image is
	 * still on the same position.
	 *
	 * @return A newly created image containing the rotated image.
	 */
	public static BufferedImage rotateImage(BufferedImage img, float angle, Point drawOffset) {
		int w = img.getWidth();
		int h = img.getHeight();

		AffineTransform tf = AffineTransform.getRotateInstance(angle,
				w / 2.0,  h / 2.0);

		// get coordinates for all corners to determine real image size
		Point2D[] ptSrc = new Point2D[4];
		ptSrc[0] = new Point(0, 0);
		ptSrc[1] = new Point(w, 0);
		ptSrc[2] = new Point(w, h);
		ptSrc[3] = new Point(0, h);

		Point2D[] ptTgt = new Point2D[4];
		tf.transform(ptSrc, 0, ptTgt, 0, ptSrc.length);

		Rectangle rc = new Rectangle(0, 0, w, h);

		for (Point2D p : ptTgt) {
			if (p.getX() < rc.x) {
				rc.width += rc.x - p.getX();
				rc.x = (int) p.getX();
			}
			if (p.getY() < rc.y) {
				rc.height += rc.y - p.getY();
				rc.y = (int) p.getY();
			}
			if (p.getX() > rc.x + rc.width)
				rc.width = (int) (p.getX() - rc.x);
			if (p.getY() > rc.y + rc.height)
				rc.height = (int) (p.getY() - rc.y);
		}

		BufferedImage imgTgt = new BufferedImage(rc.width, rc.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = imgTgt.createGraphics();

		// create a NEW rotation transformation around new center
		tf = AffineTransform.getRotateInstance(angle, rc.getWidth() / 2, rc.getHeight() / 2);
		g2d.setTransform(tf);
		g2d.drawImage(img, -rc.x, -rc.y, null);
		g2d.dispose();

		drawOffset.x += rc.x;
		drawOffset.y += rc.y;

		return imgTgt;
	}

	private static BufferedImage internalRead(File f) throws IOException {
		// special treatment for webp files
		if (f.getName().toLowerCase(Locale.US).endsWith(".webp")) {
			if (qt5Library == null) {
				qt5Library = Webp.loadLibrary();
			}

			return Webp.loadWebPImage(f, qt5Library);
		}

		BufferedImage ret = null;
		try {
			ret = ImageIO.read(f);
		} catch (IOException o){
			if( o.getMessage().contains("Unsupported JPEG process: SOF type")) {
				log.error("Cant read file "+f.toString()+ " convert file to png/tiff/gif");
				throw o;
			}
		}
		return ret;
	}

}
