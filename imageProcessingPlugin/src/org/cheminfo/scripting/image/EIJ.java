package org.cheminfo.scripting.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.RankFilters;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.AutoThresholder;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import org.cheminfo.function.scripting.SecureFileManager;
import org.cheminfo.scripting.image.filters.InvariantFeatureHistogramFilter;
import org.cheminfo.scripting.image.filters.LocalBinaryPartitionFilter;
import org.cheminfo.scripting.image.filters.TamutaTextureFilter;
import org.json.JSONArray;
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

	public EIJ(EIJ model, ImageProcessor newImageProcessor) {
		super();
		this.basedir=model.basedir;
		this.key=model.key;
		this.ij=model.ij;
		this.setProcessor(newImageProcessor);
	}
	
	public boolean save(String path) {
		return save(path, null);
	}
	
	/**
	 * Saves the given image in the format specified by the extension of the
	 * path. In the options you can specify the quality of the resulting image.
	 * 
	 * @param image
	 * @param fullName
	 * @param options
	 *            {quality:(0-100)} only works for jpeg
	 * @return boolean: If it succeed saving or not
	 */
	public boolean save(String name, Object options) {
		try {

			String fullName = SecureFileManager.getValidatedFilename(basedir, key, name);
			if (fullName == null) {
				ij.appendError("EIJ::save", "The file path is null");
				return false;
			}
			SecureFileManager.mkdir(basedir, key, name.replaceAll("[^/]*$", ""));
			
			JSONObject parameters = ij.checkParameter(options);
			int quality = parameters.optInt("quality", 100);
			FileSaver fileSaver = new FileSaver(this);
			int dotLoc = fullName.lastIndexOf('.');
			String format = fullName.substring(dotLoc + 1);
			format = format.toLowerCase(Locale.US);
			if (format.indexOf("tif") != -1) {
				if (fullName != null && !fullName.endsWith(".tiff"))
					fullName = updateExtension(fullName, ".tif");
				format = "tif";
				return fileSaver.saveAsTiff(fullName);
			} else if (format.indexOf("jpeg") != -1
					|| format.indexOf("jpg") != -1) {
				fullName = updateExtension(fullName, ".jpg");
				format = "jpeg";
				FileSaver.setJpegQuality(quality);
				return fileSaver.saveAsJpeg(fullName);
			} else if (format.indexOf("gif") != -1) {
				fullName = updateExtension(fullName, ".gif");
				format = "gif";
				return fileSaver.saveAsGif(fullName);
			} else if (format.indexOf("text") != -1
					|| format.indexOf("txt") != -1) {
				if (fullName != null && !fullName.endsWith(".xls"))
					fullName = updateExtension(fullName, ".txt");
				format = "txt";
				return fileSaver.saveAsText(fullName);
			} else if (format.indexOf("zip") != -1) {
				fullName = updateExtension(fullName, ".zip");
				format = "zip";
				return fileSaver.saveAsZip(fullName);
			} else if (format.indexOf("raw") != -1) {
				// path = updateExtension(path, ".raw");
				format = "raw";
				return fileSaver.saveAsRaw(fullName);
			} else if (format.indexOf("bmp") != -1) {
				fullName = updateExtension(fullName, ".bmp");
				format = "bmp";
				return fileSaver.saveAsBmp(fullName);
			} else if (format.indexOf("fits") != -1) {
				fullName = updateExtension(fullName, ".fits");
				format = "fits";
				return fileSaver.saveAsFits(fullName);
			} else if (format.indexOf("png") != -1) {
				fullName = updateExtension(fullName, ".png");
				format = "png";
				return fileSaver.saveAsPng(fullName);
			} else if (format.indexOf("pgm") != -1) {
				fullName = updateExtension(fullName, ".pgm");
				format = "pgm";
				return fileSaver.saveAsPgm(fullName);
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
	 * @return boolean: If it succeed resizing or not
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
	 * @return histogram array
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
	

	public EIJ createMask() {
		return createMask(null);
	}

	
	// check http://rsb.info.nih.gov/ij/developer/api/ij/process/ImageProcessor.html
	
	
	
	/**
	 * 
	 * @param options 
	 * @return
	 */
	
	public EIJ createMask(Object options) {
		try {
			JSONObject parameters = ij.checkParameter(options);
			String method=parameters.has("method")?parameters.getString("method"):"Default";
			EIJ mask = this.duplicate();
			
			// mask.getProcessor().setAutoThreshold(AutoThresholder.Method.Li,true);
			
			mask.getProcessor().setAutoThreshold(AutoThresholder.Method.valueOf(method), true);
		//	result.ip.autoThreshold();
		//	RankFilters rf = new RankFilters();
		//	rf.rank(result.ip, 50.0, RankFilters.OUTLIERS);
			return mask;
		} catch (Exception ex) {
			ij.appendError("EIJ::createMask", "Error: " + ex.toString());
		}
		return null;
	}
	
	
	public EIJ paintMask(EIJ mask) {
		return paintMask(mask, null);
	}
	
	public String analyzeImage(EIJ mask) {
		return analyzeImage(mask, null);
	}
	
	
	public String analyzeImage(Roi[] rois, Object object) {
		return analyzeImage(rois);
	}
	
	public String analyzeImage(Roi[] rois) {
		EIJ[] hsb=this.splitHSB();
		EIJ[] rgb=this.splitRGB();
		JSONArray results=new JSONArray();
		ImageStatistics is;
	    for (Roi currentRoi : rois) {
	    	JSONObject stat=new JSONObject();
	    	results.put(stat);
    		ip.setRoi(currentRoi);
    		is=ip.getStatistics();
    		stat.put("x", is.roiX);
    		stat.put("y", is.roiY);
    		stat.put("height", is.roiHeight);
    		stat.put("width", is.roiWidth);
    		stat.put("surface", is.pixelCount);
    		stat.put("xCenterOfMass", is.xCenterOfMass);
    		stat.put("yCenterOfMass", is.yCenterOfMass);
    		stat.put("xCentroid", is.xCentroid);
    		stat.put("yCentroid", is.yCentroid);
    		stat.put("histogram", is.histogram);
    		stat.put("contour", currentRoi.getLength());
    		stat.put("roundRectArcSize",currentRoi.getRoundRectArcSize());
    		
    		rgb[0].setRoi(currentRoi);
    		stat.put("red", rgb[0].getProcessor().getHistogram());
    		rgb[1].setRoi(currentRoi);
    		stat.put("green", rgb[1].getProcessor().getHistogram());
    		rgb[2].setRoi(currentRoi);
    		stat.put("blue", rgb[2].getProcessor().getHistogram());
    		
    		hsb[0].setRoi(currentRoi);
    		stat.put("hue", hsb[0].getProcessor().getHistogram());
    		hsb[1].setRoi(currentRoi);
    		stat.put("saturation", hsb[1].getProcessor().getHistogram());
    		hsb[2].setRoi(currentRoi);
    		stat.put("brightness", hsb[2].getProcessor().getHistogram());
	    }
		return results.toString();
	}
	
	public String analyzeImage(EIJ mask, Object options) {
		JSONObject parameters = ij.checkParameter(options);
		Roi[] rois=this.getRois(mask, parameters);
		return analyzeImage(rois);
	}
	
	public EIJ paintRois(Roi[] rois) {
		return paintRois(rois, null);
	}
	
	public EIJ paintRois(Roi[] rois, Object options) {
		JSONObject parameters = ij.checkParameter(options);
		Color strokeColor=parameters.has("strokeColor")?Color.getColor(parameters.getString("strokeColor")):Color.RED;
		int strokeSize=parameters.has("strokeSize")?parameters.getInt("strokeSize"):3;
		ThresholdToSelection tts = new ThresholdToSelection();
	    EIJ markedImage=this.duplicate();
	    ImageProcessor markedIP=markedImage.getProcessor();
	   //  markedIP.setRoi(roi);
	    markedIP.setColor(strokeColor);
	    markedIP.setLineWidth(strokeSize);
	    for (int i=0; i<rois.length; i++) {
	    	markedIP.draw(rois[i]);
	    }
	    
	    return markedImage;
	}
	
	public EIJ paintMask(EIJ mask, Object options) {
		JSONObject parameters = ij.checkParameter(options);
		Color strokeColor=parameters.has("strokeColor")?Color.getColor(parameters.getString("strokeColor")):Color.RED;
		int strokeSize=parameters.has("strokeSize")?parameters.getInt("strokeSize"):3;
		ThresholdToSelection tts = new ThresholdToSelection();
	    Roi roi = tts.convert(mask.getProcessor());
	    EIJ markedImage=this.duplicate();
	    ImageProcessor markedIP=markedImage.getProcessor();
	//    markedIP.setRoi(roi);
	    markedIP.setColor(strokeColor);
	    markedIP.setLineWidth(strokeSize);
	    markedIP.draw(roi);
	    return markedImage;
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

	public void color() {
		color(null);
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
		grey(null);
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

	public EIJ[] splitHSB() {
		EIJ imageCopy=this.duplicate();
		ColorProcessor cp=(ColorProcessor)(imageCopy.getProcessor());
		ImageStack imageStack=cp.getHSBStack();
		Vector<EIJ> images=new Vector<EIJ>();
		for (int i=0; i<3; i++) {
			EIJ component=new EIJ(this,imageStack.getProcessor(i+1));
			images.add(component);
		}
		return images.toArray(new EIJ[images.size()]);
	}

	public EIJ[] splitRGB() {
		EIJ imageCopy=this.duplicate();
		ImageConverter converter=new ImageConverter(imageCopy);
		converter.convertToRGBStack();
		Vector<EIJ> images=new Vector<EIJ>();
		for (int i=0; i<3; i++) {
			EIJ component=new EIJ(this, imageCopy.getStack().getProcessor(i+1));
			images.add(component);
		}
		return images.toArray(new EIJ[images.size()]);
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

	public EIJ[] split() {
		return split((Object)null);
	}
	
	public EIJ[] split(Object options) {
		// we don't specifiy the mask ! We create a default one ...
		EIJ mask=this.createMask();
		return split(mask, options);
	}
	
	
	public Roi[] getRois() {
		return getRois(this);
	}
	
	public Roi[] getRois(Object options) {
		return getRois(this, options);
	}
	
	public EIJ[] split(ImagePlus mask) {
		return split(mask, null);
	}

	private Roi[] getRois(ImagePlus mask) {
		return getRois(mask, (Object)null);
	}
	
	private Roi[] getRois(ImagePlus mask, Object options) {
		JSONObject parameters = ij.checkParameter(options);
		return getRois(mask, parameters);
	}
	
	private Roi[] getRois(ImagePlus mask, JSONObject parameters) {
		int minLength=parameters.has("minLength")?parameters.getInt("minLength"):0;
		int maxLength=parameters.has("maxLength")?parameters.getInt("maxLength"):Integer.MAX_VALUE;
		int minHeight=parameters.has("minHeight")?parameters.getInt("minHeight"):0;
		int maxHeight=parameters.has("maxHeight")?parameters.getInt("maxHeight"):Integer.MAX_VALUE;
		int minWidth=parameters.has("minWidth")?parameters.getInt("minWidth"):0;
		int maxWidth=parameters.has("maxWidth")?parameters.getInt("maxWidth"):Integer.MAX_VALUE;
		int minSurface=parameters.has("minSurface")?parameters.getInt("minSurface"):0;
		int maxSurface=parameters.has("maxSurface")?parameters.getInt("maxSurface"):Integer.MAX_VALUE;
		double scale=parameters.has("scale")?parameters.getDouble("scale"):1;

		
		int sorting=0;
		if (parameters.has("sortBy")) {
			String sortingKey=parameters.getString("sortBy");
			if (sortingKey.equalsIgnoreCase("y")) {
				sorting=1;
			} else if (sortingKey.equalsIgnoreCase("xy")) {
				sorting=2;
			} else if (sortingKey.equalsIgnoreCase("length")) {
				sorting=3;
			}
		}
		ThresholdToSelection tts = new ThresholdToSelection();
		ImageProcessor ip=this.getProcessor();
		Vector<Roi> selectedRois=new Vector<Roi>();
	    Roi roi = tts.convert(mask.getProcessor());
	    Roi[] rois=((ShapeRoi)roi).getRois();
	    
	    
	    
	    if (scale!=1) {
		    for (int i=0; i<rois.length; i++) {
		    	rois[i]=scaleROI(rois[i], scale);
		    }
	    }

	    
	    ImageStatistics is;
	    for (Roi currentRoi : rois) {
	    	boolean isArea=currentRoi.isArea();
	    	double length=currentRoi.getLength();
	    	double width=currentRoi.getBounds().getWidth();
	    	double height=currentRoi.getBounds().getHeight();
	    	
	    	double surface=0;
	    	if (minSurface>0 || maxSurface<Integer.MAX_VALUE) {
	    		ip.setRoi(currentRoi);
	    		is=ip.getStatistics();
	    		surface=is.pixelCount;
	    	}
    		
	    	if ((length>=minLength && length<=maxLength) &&
	    			(width>=minWidth && width<=maxWidth) &&
	    			(height>=minHeight) && (height<=maxHeight) &&
	    			(surface>=minSurface) && (surface<=maxSurface)) {
	    		selectedRois.add(currentRoi);
	    	}
	    }
		
		rois=selectedRois.toArray(new Roi[selectedRois.size()]);

	    if (sorting==0) {
	    	RoiSorterByX.sort(rois);
	    } else if (sorting==1) {
	    	RoiSorterByY.sort(rois);
	    } else if (sorting==2) {
	    	RoiSorterByXY.sort(rois);
	    } else if (sorting==3) {
	    	RoiSorterByLength.sort(rois);
	    }
	    
	    return rois;
	}
	
	// sortBy: 0: by X, 1: by Y, 2: by Length
	public EIJ[] split(ImagePlus mask, Object options) {
		JSONObject parameters = ij.checkParameter(options);
		Roi[] rois=this.getRois(mask, parameters);
	    
	    return split(rois);
	}
	
	public EIJ[] split(Roi[] rois) {
	    Vector<EIJ> images=new Vector<EIJ>();
	    
	    for (Roi currentRoi : rois) {
    		ip.setRoi(currentRoi);
    		ImageProcessor crop=ip.crop();
    		images.add(new EIJ(this,crop));
	    }
	    return images.toArray(new EIJ[images.size()]);
	}
	
	
	/*
	 * Splits a image
	 * 
	 * @return an array with the result images, ordered from smallest to largest
	 *
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
	*/

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
	public EIJ duplicate() {
		ImagePlus imageCopy = super.duplicate();
		return new EIJ(this, imageCopy.getProcessor());
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
	
	private ShapeRoi scaleROI(Roi currentRoi, double scale) {
		Polygon polygon=currentRoi.getPolygon();
		double xTranslate=currentRoi.getBounds().getCenterX();
		double yTranslate=currentRoi.getBounds().getCenterY();
		AffineTransform myTransform=AffineTransform.getTranslateInstance(xTranslate, yTranslate);
		myTransform.scale(scale,scale);
		myTransform.translate(-xTranslate, -yTranslate);
		Shape newShape=myTransform.createTransformedShape(polygon);
		return new ShapeRoi(newShape);
	}
}

class RoiSorterByX implements Comparator<Roi> {
	public static void sort(Roi[] rois) {
		Arrays.sort(rois, new RoiSorterByX());
	}
	public int compare(Roi roi1, Roi roi2) {
		return (roi1.getBounds().x-roi2.getBounds().x);
	}
}

class RoiSorterByY implements Comparator<Roi> {
	public static void sort(Roi[] rois) {
		Arrays.sort(rois, new RoiSorterByY());
	}
	public int compare(Roi roi1, Roi roi2) {
		return (roi1.getBounds().y-roi2.getBounds().y);
	}
}

class RoiSorterByXY implements Comparator<Roi> {
	public static void sort(Roi[] rois) {
		Arrays.sort(rois, new RoiSorterByXY());
	}
	public int compare(Roi roi1, Roi roi2) {
		return ((roi1.getBounds().y+roi1.getBounds().x)-(roi2.getBounds().y+roi2.getBounds().x));
	}
}

 
class RoiSorterByLength implements Comparator<Roi> {
	public static void sort(Roi[] rois) {
		Arrays.sort(rois, new RoiSorterByLength());
	}
	public int compare(Roi roi1, Roi roi2) {
		return (int)(roi1.getLength()-roi2.getLength());
	}
}
