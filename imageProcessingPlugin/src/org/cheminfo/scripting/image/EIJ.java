package org.cheminfo.scripting.image;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.cheminfo.function.Function;
import org.cheminfo.function.scripting.SecureFileManager;
import org.cheminfo.scripting.image.extraction.Coordinates;
import org.cheminfo.scripting.image.extraction.ImageObject;
import org.cheminfo.scripting.image.extraction.PillExtraction;
import org.cheminfo.scripting.image.filters.InvariantFeatureHistogramFilter;
import org.cheminfo.scripting.image.filters.LocalBinaryPartitionFilter;
import org.cheminfo.scripting.image.filters.TamuraCoarsenessFilter;
import org.cheminfo.scripting.image.filters.TamuraContrastFilter;
import org.cheminfo.scripting.image.filters.TamuraDirectionalityFilter;
import org.json.JSONObject;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.ContrastEnhancer;
import ij.plugin.Duplicator;
import ij.plugin.filter.RankFilters;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.MedianCut;

/**
 * This is an ImageJPlus extended class with extra functionality to load and
 * save in safe manner. http://www.imagingbook.com/index.php?id=98
 * 
 * @author acastillo
 * 
 */
public class EIJ extends ImagePlus implements Cloneable {

	private String basedir;
	private String key;
	int texture = 2;
	private String filename;

	static final int HSIZE = 32768;

	/**
	 * The constructor. The filename have to be the full path to file to be
	 * loaded. The security check have to be done outside this method. The
	 * reason because we need the basefit and key parameters is to safe save the
	 * file again.
	 * 
	 * @param basedir
	 * @param key
	 * @param filename
	 */
	public EIJ(String basedir, String key, String filename) {
		super(filename);
		this.basedir = basedir;
		this.key = key;
		this.filename = filename;
	}

	/**
	 * This function save the given image in jpeg format. In the options you can
	 * specify the quality of the resulting image.
	 * 
	 * @param image
	 * @param path
	 * @param options
	 *            {quality:(0-100)} only works for jpeg
	 * @return boolean: If it succeed saving or not
	 */
	public boolean save(String path, Object options) {

		path = SecureFileManager.getValidatedFilename(basedir, key, path);
		if (path == null)
			return false;
		JSONObject parameters = Function.checkParameter(options);
		int quality = parameters.optInt("quality", 100);
		FileSaver fileSaver = new FileSaver(this);
		int dotLoc = path.lastIndexOf('.');
		String format = path.substring(dotLoc + 1);
		format = format.toLowerCase(Locale.US);
		if (format.indexOf("tif") != -1) {
			if (path != null && !path.endsWith(".tiff"))
				path = updateExtension(path, ".tif");
			format = "tif";
			return fileSaver.saveAsTiff(path);
		} else if (format.indexOf("jpeg") != -1 || format.indexOf("jpg") != -1) {
			path = updateExtension(path, ".jpg");
			format = "jpeg";
			FileSaver.setJpegQuality(quality);
			return fileSaver.saveAsJpeg(path);
		} else if (format.indexOf("gif") != -1) {
			path = updateExtension(path, ".gif");
			format = "gif";
			return fileSaver.saveAsGif(path);
		} else if (format.indexOf("text") != -1 || format.indexOf("txt") != -1) {
			if (path != null && !path.endsWith(".xls"))
				path = updateExtension(path, ".txt");
			format = "txt";
			return fileSaver.saveAsText(path);
		} else if (format.indexOf("zip") != -1) {
			path = updateExtension(path, ".zip");
			format = "zip";
			return fileSaver.saveAsZip(path);
		} else if (format.indexOf("raw") != -1) {
			// path = updateExtension(path, ".raw");
			format = "raw";
			return fileSaver.saveAsRaw(path);
		} else if (format.indexOf("bmp") != -1) {
			path = updateExtension(path, ".bmp");
			format = "bmp";
			return fileSaver.saveAsBmp(path);
		} else if (format.indexOf("fits") != -1) {
			path = updateExtension(path, ".fits");
			format = "fits";
			return fileSaver.saveAsFits(path);
		} else if (format.indexOf("png") != -1) {
			path = updateExtension(path, ".png");
			format = "png";
			return fileSaver.saveAsPng(path);
		} else if (format.indexOf("pgm") != -1) {
			path = updateExtension(path, ".pgm");
			format = "pgm";
			return fileSaver.saveAsPgm(path);
		} else if (format.indexOf("lut") != -1) {
			path = updateExtension(path, ".lut");
			format = "lut";
			return fileSaver.saveAsLut(path);
		} else
			return false;
	}

	/**
	 * This function scales the image to the specified width and height. Posible
	 * values: 200x100: resize to exactly 200x100; x100: proportional resize to
	 * 100 points height; 200x: proportional resize to 200 points witdh; 50%
	 * proportional resize to 50%
	 * 
	 * @param size
	 *            (wxh, p%)
	 * @param options
	 *            {method:(0->None,1->Bilinear, 2->Bicubic), background:(name
	 *            background color), average:(y/n-> Average when downsizing)}
	 * @return boolean: If it succeed saving or not
	 */
	public boolean resize(String size, Object options) {
		JSONObject parameters = Function.checkParameter(options);
		int interpolationMethod = parameters.optInt("method",
				ImageProcessor.BILINEAR);
		String backgroundColor = parameters.optString("background", "");
		String average = parameters.optString("average", "n");
		boolean averageWhenDownsizing = false;
		boolean fillWithBackground = false;
		double bgValue = 0.0;
		if (fillWithBackground) {
			Color bgc = getBackgroundColor(backgroundColor);
			if (this.getBitDepth() == 8)
				bgValue = ip.getBestIndex(bgc);
			else if (this.getBitDepth() == 24)
				bgValue = bgc.getRGB();
		} else
			bgValue = 0.0;
		if (average.toLowerCase().equals("y")) {
			averageWhenDownsizing = true;
		}
		ip.setInterpolationMethod(interpolationMethod);
		ip.setBackgroundValue(bgValue);
		int newHeight = 0;
		int newWidth = 0;
		if (size.contains("%")) {
			Double percentage = 0.0;
			try {
				percentage = Double.parseDouble(size.substring(0,
						size.indexOf("%")));
				percentage = percentage / 100;
			} catch (Exception ex) {

			}
			if (percentage <= 0.0) {
				return false;
			}
			newHeight = (int) (this.getHeight() * percentage);
			newWidth = (int) (this.getWidth() * percentage);
		} else {
			String[] values = size.toLowerCase().split("x");
			if (values.length > 0) {
				try {
					newWidth = Integer.parseInt(values[0].trim());
				} catch (Exception ex) {
				}
				try {
					newHeight = Integer.parseInt(values[1].trim());
				} catch (Exception ex) {
				}

				if (newHeight == 0 && newWidth == 0) {
					return false;
				} else if (newHeight == 0) {
					newHeight = (int) (newWidth * (double) this.getHeight() / this
							.getWidth());
				} else if (newWidth == 0) {
					newWidth = (int) (newHeight * (double) this.getWidth() / this
							.getHeight());
				}
			}
		}
		if ((newHeight != this.getHeight() || newWidth != this.getWidth())
				&& (newHeight > 0 && newWidth > 0)) {
			this.setProcessor(this.getProcessor().resize(newWidth, newHeight,
					averageWhenDownsizing));
			return true;
		}
		return false;
	}

	private Color getBackgroundColor(String backgroundColor) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Apply a contrast filter to the image
	 * 
	 * @param options
	 *            {saturated:(0-100), equalize:(y/n)}, if equalize is true
	 *            equalize the image, either stretch image histogram using
	 *            saturated
	 */
	public void contrast(Object options) {
		ContrastEnhancer ce = new ContrastEnhancer();
		JSONObject parameters = Function.checkParameter(options);
		double saturated = parameters.optDouble("saturated", 0.35);
		String equalize = parameters.optString("equalize", "n");
		if (equalize.toLowerCase().equals("y")) {
			ce.equalize(this.getProcessor());
		} else {
			ce.stretchHistogram(this.getProcessor(), saturated);
		}

	}

	/**
	 * This function returns an histogram for this image.
	 * 
	 * @param numberOfBins
	 * @return
	 */
	public int[] histogram() {
		ImageProcessor ip = this.getProcessor();

		return ip.getHistogram();
	}

	/**
	 * This function returns the width of the image
	 * 
	 * @see ij.ImagePlus#getWidth()
	 */
	public int getWidth() {
		return super.getWidth();
	}

	/**
	 * This function returns the height of the image
	 * 
	 * @see ij.ImagePlus#getHeight()
	 */
	public int getHeight() {
		return super.getHeight();
	}

	/**
	 * Return a copy of the EIJ Image
	 * 
	 * @return a copy of the image
	 * @throws CloneNotSupportedException
	 */
	public EIJ copy() {
		EIJ result = this.clone();
		result.setImage(this.duplicate());
		return result;
	}

	/**
	 * Apply a edge filter to the image
	 */
	public void edge() {
		this.grey();
		ip.findEdges();
	}

	/**
	 * Apply a color filter to the image
	 * 
	 * @param options
	 *            {nbColor:(2-256)}
	 */
	public void color(Object options) {
		JSONObject parameters = Function.checkParameter(options);
		int nColors = parameters.optInt("nbColor", 256);
		ImageConverter ic = new ImageConverter(this);
		ic.convertRGBtoIndexedColor(nColors);
	}

	/**
	 * Apply a grey filter to the image
	 */
	public void grey() {
		ImageConverter ic = new ImageConverter(this);
		ic.convertToGray8();
	}

	/**
	 * Apply a texture filter to the image
	 * 
	 */
	public void texture() {
		switch (texture) {
		case 0:
			tamura();
			break;
		case 1:
			this.grey();
			invariantFeatureHistogram();
			break;
		case 2:
			this.grey();
			localBinaryPartition();
			break;
		}
	}

	/**
	 * Check if the path contains the format extension
	 * 
	 * @param path
	 * @param extension
	 * @return The path with the extension
	 */
	static String updateExtension(String path, String extension) {
		if (path == null)
			return null;
		int dotIndex = path.lastIndexOf(".");
		int separatorIndex = path.lastIndexOf(File.separator);
		if (dotIndex >= 0 && dotIndex > separatorIndex
				&& (path.length() - dotIndex) <= 5) {
			if (dotIndex + 1 < path.length()
					&& Character.isDigit(path.charAt(dotIndex + 1)))
				path += extension;
			else
				path = path.substring(0, dotIndex) + extension;
		} else
			path += extension;
		return path;
	}

	/**
	 * Apply a tamura filter
	 */
	public void tamura() {
		/* Tamura Coarseness */

		TamuraCoarsenessFilter tamuraCoarseness = new TamuraCoarsenessFilter(
				this.getProcessor().convertToByte(true));
		byte[] coarseness = tamuraCoarseness.performExtraction();

		ImagePlus iCoarseness = NewImage.createByteImage("Coarseness", width,
				height, 1, NewImage.FILL_BLACK);
		iCoarseness.getProcessor().setPixels(coarseness);

		/* Tamura Contrast */

		TamuraContrastFilter tamuraContrast = new TamuraContrastFilter(this
				.getProcessor().convertToByte(true));
		byte[] contrast = tamuraContrast.performExtraction();

		ImagePlus iContrast = NewImage.createByteImage("Contrast", width,
				height, 1, NewImage.FILL_BLACK);
		iContrast.getProcessor().setPixels(contrast);

		TamuraDirectionalityFilter tamuraDirectionality = new TamuraDirectionalityFilter(
				this.getProcessor().convertToByte(true));
		byte[] directionality = tamuraDirectionality.performExtraction();

		ImagePlus iDirectionality = NewImage.createByteImage("Directionality",
				width, height, 1, NewImage.FILL_BLACK);
		iDirectionality.getProcessor().setPixels(directionality);

		ImageStack stack1 = new ImageStack(width, height);
		stack1.addSlice("directionality", iDirectionality.getProcessor());

		ImageStack stack2 = new ImageStack(width, height);
		stack2.addSlice("coarseness", iCoarseness.getProcessor());

		ImageStack stack3 = new ImageStack(width, height);
		stack3.addSlice("contrast", iContrast.getProcessor());

		ij.plugin.RGBStackMerge merge = new ij.plugin.RGBStackMerge();
		ImageStack result = merge.mergeStacks(width, height, 1, stack1, stack2,
				stack3, true);
		int[] pixels = (int[]) result.getPixels(1);
		this.getProcessor().setPixels(pixels);
		this.grey();
	}

	/**
	 * Apply a invariant feature filter
	 */
	public void invariantFeatureHistogram() {
		InvariantFeatureHistogramFilter invariantTexture = new InvariantFeatureHistogramFilter(
				this.getProcessor().convertToByte(true));
		byte[] bytes = invariantTexture.performExtraction();
		this.getProcessor().setPixels(bytes);
	}

	/**
	 * Apply a local Binary Partition filter
	 */
	public void localBinaryPartition() {
		LocalBinaryPartitionFilter localbinary = new LocalBinaryPartitionFilter(
				this.getProcessor().convertToByte(true));
		byte[] bytes = localbinary.performExtraction();
		this.getProcessor().setPixels(bytes);

	}

	public int getColor() {
		if (this.getType() != ImagePlus.COLOR_RGB)
			throw new IllegalArgumentException("Image must be RGB");
		int color16;
		int[] pixels = (int[]) ip.getPixels();

		// build 32x32x32 RGB histogram
		int[] hist = new int[HSIZE];
		for (int i = 0; i < width * height; i++) {
			color16 = rgb(pixels[i]);
			hist[color16]++;
		}
		int count = 0;
		for (int i = 0; i < HSIZE; i++)
			if (hist[i] > 0)
				count++;
		return count;
	}

	public boolean crop(int x, int y, int width, int height) {
		if (x >= this.getWidth())
			return false;

		if (y >= this.getHeight())
			return false;

		if (width > this.getWidth()) {
			width = this.getWidth() - x;
		}

		if (height > this.getHeight()) {
			height = this.getHeight() - y;
		}

		setRoi(x, y, width, height);
		this.setProcessor(this.getProcessor().crop());
		return true;
	}

	public EIJ[] split() {
		PillExtraction extraction = new PillExtraction();
		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(this.filename);
		EIJ[] objects = extraction.extract2(this);
		return objects;
	}

	// Convert from 24-bit to 15-bit color
	private final int rgb(int c) {
		int r = (c & 0xf80000) >> 19;
		int g = (c & 0xf800) >> 6;
		int b = (c & 0xf8) << 7;
		return b | g | r;
	}

	public void setImage(ImagePlus imp) {
		if (imp.getWindow() != null)
			imp = imp.duplicate();
		ImageStack stack2 = imp.getStack();
		if (imp.isHyperStack())
			setOpenAsHyperStack(true);
		setStack(stack2, imp.getNChannels(), imp.getNSlices(), imp.getNFrames());
	}

	public EIJ clone() {
		EIJ obj = null;
		try {
			obj = (EIJ) super.clone();
		} catch (CloneNotSupportedException ex) {
		}
		return obj;
	}
}
