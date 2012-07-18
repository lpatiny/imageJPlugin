package org.cheminfo.scripting.image;

import java.io.File;
import java.util.Locale;

import org.cheminfo.function.Function;
import org.cheminfo.function.scripting.SecureFileManager;
import org.json.JSONObject;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.plugin.Duplicator;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

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
	 * This function scales the image to the specified width and height
	 * 
	 * @param newWidth
	 * @param newHeight
	 */
	public void resize(String size, Object options) {
		ImageProcessor im = this.getProcessor();
		String[] values = size.toLowerCase().split("x");
		int newWidth = Integer.parseInt(values[0]);
		int newHeight = Integer.parseInt(values[1]);
		this.setImage(im.createImage()
				.getScaledInstance(newWidth, newHeight, 1));
	}

	/**
	 * This function contrast the image
	 * 
	 * @param options
	 *            {saturated:(0-100), equalize:(y/n)}
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

	public EIJ copy() {
		EIJ result = (EIJ) super.clone();
		result.setImage((new Duplicator()).run(this));
		result.basedir = this.basedir;
		result.key = this.key;
		return result;
	}

	public void edge() {
		ImageProcessor ip = this.getProcessor();
		ip.convertToByte(true);
		ip.copyBits(ip, 0, 0, Blitter.COPY);
		ip.findEdges();
	}

	public void color() {

	}

	public void grey() {
		ImageProcessor ip = this.getProcessor();
		ip.convertToByte(true);
	}

	public void texture(Object options) {
		JSONObject parameters = Function.checkParameter(options);
		int maxWindowSize = parameters.optInt("maxWindowSize", 6);

		ImageProcessor ip = this.getProcessor();
		ip.convertToByte(true);

		byte[] coarseness = new byte[width * height];
		int margin = 0; // in pixels

		int offset, i;

		for (int y = margin; y < (height - margin); y++) {
			offset = y * width;
			for (int x = margin; x < (width - margin); x++) {
				i = offset + x;
				coarseness[i] = (byte) selectHighestDiff(x, y, maxWindowSize);
			}
		}

		this.getProcessor().setPixels(coarseness);
	}

	private int selectHighestDiff(int x, int y, int maxWindowSize) {
		int value;
		int maxValue = 0;
		boolean horizontal = true;
		boolean vertical = false;
		int maxK = 0;

		for (int k = 1; k <= maxWindowSize; k++) {
			value = diffBetweenNeighborhoods(x, y, k, horizontal);
			if (value > maxValue) {
				maxValue = value;
				maxK = k;
			}

			value = diffBetweenNeighborhoods(x, y, k, vertical);
			if (value > maxValue) {
				maxValue = value;
				maxK = k;
			}
		}
		int normalizedValue = 0;
		normalizedValue = (maxK) * (255 / maxWindowSize);
		return normalizedValue;
	}

	/**
	 * Calculate the difference between the non overlapping neighborhoods of a
	 * given pixel
	 * 
	 * @param x
	 *            X coordinate of pixel
	 * @param y
	 *            Y coordinate of pixel
	 * @param windowSize
	 *            Size of the neighborhood
	 * @param horizontal
	 *            The direction of analysis: Horizontal (true) or Vertical
	 *            (false)
	 * @return
	 */
	private int diffBetweenNeighborhoods(int x, int y, int windowSize,
			boolean horizontal) {
		int value = 0;
		int valueA, valueB;
		int twoKMinusOne = (int) Math.pow(2, windowSize - 1);
		int limit = (int) Math.pow(2, windowSize);

		if (horizontal && !(x + limit > this.getWidth() || x - limit < 0)) {
			valueA = average(x + twoKMinusOne, y, windowSize);
			valueB = average(x - twoKMinusOne, y, windowSize);
			value = Math.abs(valueA - valueB);
		}
		if (!horizontal && !(y + limit > this.getHeight() || y - limit < 0)) {
			valueA = average(x, y + twoKMinusOne, windowSize);
			valueB = average(x, y - twoKMinusOne, windowSize);
			value = Math.abs(valueA - valueB);
		}
		return value;
	}

	private int average(int x, int y, int windowSize) {
		int limit = (int) Math.pow(2, windowSize);
		int twoKMinusOne = (int) Math.pow(2, windowSize - 1);
		int totalPixels = (int) Math.pow(2, 2 * windowSize);
		int sum = 0;
		int value;

		for (int j = 1; j < limit; j++) {
			for (int i = 1; i < limit; i++) {
				value = this.getProcessor().getPixel(x - twoKMinusOne + i,
						y - twoKMinusOne + j);
				sum += value;
			}
		}
		return sum / totalPixels;
	}

}
