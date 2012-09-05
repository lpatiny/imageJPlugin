package org.cheminfo.scripting.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.io.File;
import java.util.Locale;

import org.cheminfo.function.scripting.SecureFileManager;
import org.cheminfo.scripting.image.extraction.PillExtraction;
import org.cheminfo.scripting.image.filters.InvariantFeatureHistogramFilter;
import org.cheminfo.scripting.image.filters.LocalBinaryPartitionFilter;
import org.cheminfo.scripting.image.filters.TamutaTextureFilter;
import org.json.JSONObject;

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
	private IJ ij;

	public EIJ() {
		ij = new IJ();
	}

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
	public EIJ(String basedir, String key, String filename, IJ ij) {
		super(filename);
		this.basedir = basedir;
		this.key = key;
		this.ij = ij;
	}

	/**
	 * Saves the given image in the format specified by the extension of the
	 * path. In the options you can specify the quality of the resulting image.
	 * 
	 * @param image
	 * @param path
	 * @param options
	 *            {quality:(0-100)} only works for jpeg
	 * @return boolean: If it succeed saving or not
	 */
	public boolean save(String path, Object options) {
		try {

			path = SecureFileManager.getValidatedFilename(basedir, key, path);
			if (path == null) {
				ij.appendError("EIJ::save", "The file path is null");
				return false;
			}

			JSONObject parameters = ij.checkParameter(options);
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
			} else if (format.indexOf("jpeg") != -1
					|| format.indexOf("jpg") != -1) {
				path = updateExtension(path, ".jpg");
				format = "jpeg";
				FileSaver.setJpegQuality(quality);
				return fileSaver.saveAsJpeg(path);
			} else if (format.indexOf("gif") != -1) {
				path = updateExtension(path, ".gif");
				format = "gif";
				return fileSaver.saveAsGif(path);
			} else if (format.indexOf("text") != -1
					|| format.indexOf("txt") != -1) {
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
			} else {
				ij.appendError("EIJ::save",
						"The file extension is not valid. Extension: " + format);
			}
		} catch (Exception ex) {
			ij.appendError("EIJ::save", "Error : " + ex.toString());
		}
		return false;
	}

	/**
	 * Scales the image to the specified width and height. Posible values:
	 * 200x100: resize to exactly 200x100; x100: proportional resize to 100
	 * points height; 200x: proportional resize to 200 points witdh; 50%
	 * proportional resize to 50%
	 * 
	 * @param size
	 *            (wxh, p%)
	 * @param options
	 *            {method:(0->None,1->Bilinear, 2->Bicubic), average:(y/n->
	 *            Average when downsizing)}
	 * @return boolean: If it succeed saving or not
	 */
	public boolean resize(String size, Object options) {
		try {
			JSONObject parameters = ij.checkParameter(options);
			int interpolationMethod = parameters.optInt("method",
					ImageProcessor.BILINEAR);
			String average = parameters.optString("average", "n");
			boolean averageWhenDownsizing = false;
			if (average.toLowerCase().equals("y")) {
				averageWhenDownsizing = true;
			}
			ip.setInterpolationMethod(interpolationMethod);
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
					ij.appendError("EIJ::resize",
							"The percentage must be equals or greater than 0. Entered: "
									+ size.substring(0, size.indexOf("%")));
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
						ij.appendError("EIJ::resize",
								"Invalid value for height and / or width. Entered: width "
										+ values[0].trim() + ", height "
										+ values[1].trim());
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
				this.setProcessor(this.getProcessor().resize(newWidth,
						newHeight, averageWhenDownsizing));
				return true;
			} else {
				ij.appendError("EIJ::resize",
						"Invalid value for height and / or width. Values: width "
								+ newWidth + ", height " + newHeight);
			}
		} catch (Exception ex) {
			ij.appendError("EIJ::resize", "Error: " + ex.toString());
		}
		return false;
	}

	/**
	 * Applies a contrast filter to the image
	 * 
	 * @param options
	 *            {saturated:(0-100), equalize:(y/n)}, if equalize is true
	 *            equalize the image, either stretch image histogram using
	 *            saturated, default equalize is true
	 * 
	 */
	public void contrast(Object options) {
		try {
			ContrastEnhancer ce = new ContrastEnhancer();
			JSONObject parameters = ij.checkParameter(options);
			double saturated = parameters.optDouble("saturated", 0);
			String equalize = parameters.optString("equalize", "y");
			if (equalize.toLowerCase().equals("y") && saturated == 0) {
				ce.equalize(this.getProcessor());
			} else if (saturated > 0 && saturated <= 100) {
				ce.stretchHistogram(this.getProcessor(), saturated);
			} else {
				ij.appendError("EIJ::contrast",
						"Invalid value for saturated, value: " + saturated
								+ ". Valid values: (0,100]");
			}
		} catch (Exception ex) {
			ij.appendError("EIJ::contrast", "Error: " + ex.toString());
		}
	}

	/**
	 * Returns an histogram for this image.
	 * 
	 * @param numberOfBins
	 * @return
	 */
	public int[] histogram() {
		try {
			ImageProcessor ip = this.getProcessor();
			return ip.getHistogram();
		} catch (Exception ex) {
			ij.appendError("EIJ::histogram", "Error: " + ex.toString());
		}
		return null;
	}

	/**
	 * Returns the width of the image
	 * 
	 * @see ij.ImagePlus#getWidth()
	 */
	public int getWidth() {
		return super.getWidth();
	}

	/**
	 * Returns the height of the image
	 * 
	 * @see ij.ImagePlus#getHeight()
	 */
	public int getHeight() {
		return super.getHeight();
	}

	/**
	 * Returns a copy of the EIJ Image
	 * 
	 * @return a copy of the image
	 * @throws CloneNotSupportedException
	 */
	public EIJ copy() {
		try {
			EIJ result = this.clone();
			result.setImage(this.duplicate());
			return result;
		} catch (Exception ex) {
			ij.appendError("EIJ::copy", "Error: " + ex.toString());
		}
		return null;
	}

	/**
	 * Applies a edge filter to the image
	 */
	public void edge() {
		try {
			this.grey();
			ip.findEdges();
		} catch (Exception ex) {
			ij.appendError("EIJ::edge", "Error: " + ex.toString());
		}
	}

	/**
	 * Applies a color filter to the image
	 * 
	 * @param options
	 *            {nbColor:(2-256)}
	 */
	public void color(Object options) {
		try {
			JSONObject parameters = ij.checkParameter(options);
			int nColors = parameters.optInt("nbColor", 256);
			ImageConverter ic = new ImageConverter(this);
			ic.convertRGBtoIndexedColor(nColors);
		} catch (Exception ex) {
			ij.appendError("EIJ::color", "Error: " + ex.toString());
		}
	}

	/**
	 * Applies a grey filter to the image
	 * 
	 * @param options
	 *            {nbGrey:(2-256)}
	 */
	public void grey(Object options) {
		try {
			JSONObject parameters = ij.checkParameter(options);
			// Included to manage nbGrey
			int nGrey = parameters.optInt("nbGrey", 256);
			if (nGrey < 256) {
				ImageConverter ic = new ImageConverter(this);
				ic.convertRGBtoIndexedColor(nGrey);
			}
			//
			ImageConverter ic2 = new ImageConverter(this);
			ic2.convertToGray8();
		} catch (Exception ex) {
			ij.appendError("EIJ::grey", "Error: " + ex.toString());
		}
	}

	/**
	 * Applies a grey filter to the image
	 * 
	 * 
	 */
	public void grey() {
		try {
			ImageConverter ic2 = new ImageConverter(this);
			ic2.convertToGray8();
		} catch (Exception ex) {
			ij.appendError("EIJ::grey", "Error: " + ex.toString());
		}
	}

	/**
	 * Applies a texture filter to the image
	 * 
	 */
	public void texture() {
		try {
			int texture = 2;
			switch (texture) {
			case 0:
				TamutaTextureFilter.tamura(this);
				break;
			case 1:
				this.grey();
				InvariantFeatureHistogramFilter.invariantFeatureHistogram(this
						.getProcessor());
				break;
			case 2:
				this.grey();
				LocalBinaryPartitionFilter.localBinaryPartition(getProcessor());
				break;
			}
		} catch (Exception ex) {
			ij.appendError("EIJ::texture", "Error: " + ex.toString());
		}
	}

	/**
	 * Returns the number of colors
	 * 
	 * @return Number of colors
	 */
	public int getColor() {
		try {
			final int HSIZE = this.getWidth() * this.getHeight();
			if (this.getType() != ImagePlus.COLOR_RGB)
				throw new IllegalArgumentException("Image must be RGB");
			int color16;
			int[] pixels = (int[]) ip.getPixels();

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
		} catch (Exception ex) {
			ij.appendError("EIJ::getColor", "Error: " + ex.toString());
		}
		return 0;
	}

	/**
	 * Crops a image
	 * 
	 * @param x
	 *            horizontal value from which to start cutting
	 * @param y
	 *            vertical value from which to start cutting
	 * @param width
	 *            width of the new image, if it is greater than the width of the
	 *            original image minus the value of x, it calculates the width
	 * @param height
	 *            height of the new image, if it is greater than the height of
	 *            the original image minus the value of y, it calculates the
	 *            height
	 * @return true for successful crop, false otherwise
	 */
	public boolean crop(int x, int y, int width, int height) {
		try {
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
		} catch (Exception ex) {
			ij.appendError("EIJ::crop", "Error: " + ex.toString());
		}
		return false;
	}

	/**
	 * Splits a image
	 * 
	 * @return an array with the result images, ordered from smallest to largest
	 */
	public EIJ[] split() {
		try {
			PillExtraction extraction = new PillExtraction();
			EIJ[] objects = extraction.extract2(this);
			return objects;
		} catch (Exception ex) {
			ij.appendError("EIJ::split", "Error: " + ex.toString());
		}
		return null;
	}

	/**
	 * Converts from 24-bit to 15-bit color
	 * 
	 * @param c
	 *            value of the pixel
	 * @return the rgb value
	 */
	private final int rgb(int c) {
		int r = (c & 0xf80000) >> 19;
		int g = (c & 0xf800) >> 6;
		int b = (c & 0xf8) << 7;
		return b | g | r;
	}

	/**
	 * Replaces the image
	 * 
	 * @param imp
	 */
	public void setImage(ImagePlus imp) {
		if (imp.getWindow() != null)
			imp = imp.duplicate();
		ImageStack stack2 = imp.getStack();
		if (imp.isHyperStack())
			setOpenAsHyperStack(true);
		setStack(stack2, imp.getNChannels(), imp.getNSlices(), imp.getNFrames());
	}

	/**
	 * Clones EIJ
	 */
	public EIJ clone() {
		EIJ obj = null;
		try {
			obj = (EIJ) super.clone();
		} catch (CloneNotSupportedException ex) {
		}
		return obj;
	}

	/**
	 * Checks if the path contains the format extension and adds the extension
	 * to the path if this had not extension
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
}
