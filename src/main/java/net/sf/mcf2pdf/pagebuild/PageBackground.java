/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.sf.mcf2pdf.mcfelements.McfBackground;
import net.sf.mcf2pdf.mcfelements.util.ImageUtil;


public class PageBackground implements PageDrawable {

	private List<? extends McfBackground> leftBg;

	private List<? extends McfBackground> rightBg;

	public PageBackground(List<? extends McfBackground> leftBg,
			List<? extends McfBackground> rightBg) {
		this.leftBg = leftBg;
		this.rightBg = rightBg;
	}

	@Override
	public boolean isVectorGraphic() {
		return false;
	}

	@Override
	public void renderAsSvgElement(Writer writer, PageRenderContext context) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedImage renderAsBitmap(PageRenderContext context,
			Point drawOffsetPixels, int widthPX, int heightPX) throws IOException {
		File fLeft = extractBackground(leftBg, context);
		File fRight = extractBackground(rightBg, context);

		BufferedImage img = new BufferedImage(widthPX, heightPX, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		if (fLeft != null && fLeft.equals(fRight)) {
			// draw the background image on whole page
			int hue = extractHueFromBackGround(leftBg);
			drawBackground(fLeft, g2d, 0, 0, img.getWidth(), img.getHeight(),hue);
		}
		else {
			// process background parts separate
			if (fLeft != null) {
				int hueleft = extractHueFromBackGround(leftBg);
				drawBackground(fLeft, g2d, 0, 0, img.getWidth() / 2, img.getHeight(), hueleft);
			}
			if (fRight != null) {
				int hueright = extractHueFromBackGround(rightBg);
				drawBackground(fRight, g2d, img.getWidth() / 2, 0, img.getWidth() / 2, img.getHeight(), hueright);
			}
		}

		g2d.dispose();
		return img;
	}

	private void drawBackground(File f, Graphics2D g2d, int x, int y, int width, int height, int hue) throws IOException {
		BufferedImage img = ImageUtil.readImage(f);
		if (img == null) {
			throw new IOException("Could not read image file: " + f.getAbsolutePath());
		}
		/// aply hue 270
		BufferedImage applied=null;
		if(f.getName().equalsIgnoreCase("6898.webp")) {
			 applied=applyHue(img, hue/360.0f);
		} else
			applied = img;
		float tgtRatio = width / (float)height;

		float imgRatio = img.getWidth() / (float)img.getHeight();
		float scale;
		boolean xVar;

		if (imgRatio > tgtRatio) {
			// scale image Y to target Y
			scale = height / (float)img.getHeight();
			xVar = true;
		}
		else {
			// scale image X to target X
			scale = width / (float)img.getWidth();
			xVar = false;
		}

		int sx = (int)(xVar ? ((img.getWidth() - (width / scale)) / 2) : 0);
		int sy = (int)(xVar ? 0 : ((img.getHeight() - (height / scale)) / 2));

		int sw = (int)(width / scale);
		int sh = (int)(height / scale);

		g2d.drawImage(applied, x, y, x + width, y + height, sx, sy, sx + sw, sy + sh, null);
	}

	private BufferedImage applyHue(BufferedImage image, float hue) {
		int width = image.getWidth();
		int height = image.getHeight();
		WritableRaster raster = image.getRaster();
		BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster resRast = res.getRaster();

		for (int xx = 0; xx < width; xx++) {
			for (int yy = 0; yy < height; yy++) {
				//Color color = new Color(Color.HSBtoRGB(hue, 0.7f, 0.7f));
				int RGB=image.getRGB(xx,yy);
				//int[] pixels = raster.getPixel(xx, yy, (int[]) null);
				int R = (RGB >> 16) & 0xff;
				int G = (RGB >> 8) & 0xff;
				int B = (RGB) & 0xff;
				float HSV[]=new float[3];
				Color.RGBtoHSB(R,G,B,HSV);
				//pixels[0] = color.getRed();
				//pixels[1] = color.getGreen();
				//pixels[2] = color.getBlue();
				//float newhue = (hue)%1.0f;
				//float newsat = HSV[1];
				float newsat = (float) Math.sqrt(HSV[1]*100)/100;
				res.setRGB(xx,yy,Color.getHSBColor(hue,newsat,HSV[2]).getRGB());
				//resRast.setPixel(xx, yy, pixels);
			}
		}
		return res;
	}

	private File extractBackground(List<? extends McfBackground> bgs,
			PageRenderContext context) throws IOException {
		for (McfBackground bg : bgs) {
			String tn = bg.getTemplateName();
			String designElementId = bg.getDesignElementId();
			if (designElementId != null) {
				tn = designElementId;
			} else {
				if (tn == null || !tn.matches("[a-zA-Z0-9_]+,normal(,.*)?"))
				continue;
			if(designElementId == null)
				tn = tn.substring(0, tn.indexOf(","));
			}
			File f = context.getBackgroundImage(tn);
			if (f == null) {
				f = context.getBackgroundColor(tn);
			}
			if (f == null)
				context.getLog().warn("Background not found for page " + bg.getPage().getPageNr() + ": " + tn);
			else
				return f;
		}

		return null;
	}

	private int extractHueFromBackGround(List<? extends McfBackground> bgs) {
		int hue=0;
		for (McfBackground bg : bgs) {
			if(bg.getHue()>0) {
				hue=bg.getHue();
			};
		}

		return hue;
	}

	@Override
	public int getZPosition() {
		return 0;
	}

	@Override
	public float getLeftMM() {
		return 0;
	}

	@Override
	public float getTopMM() {
		return 0;
	}

}
