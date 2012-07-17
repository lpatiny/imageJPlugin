package org.cheminfo.scripting.image;

import org.cheminfo.function.Function;
import org.cheminfo.function.scripting.SecureFileManager;
import org.json.JSONObject;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
/**
 * This is an ImageJPlus extended class with extra functionality to load and save in safe manner. 
 * http://www.imagingbook.com/index.php?id=98
 * @author acastillo
 *
 */
public class EIJ extends ImagePlus{
	
	private String basedir;
	private String key;
	
	/**
	 * The constructor. The filename have to be the full path to file to be loaded. The security check have to be done outside this method. 
	 * The reason because we need the basefit and key parameters is to safe save the file again.
	 * @param basedir
	 * @param key
	 * @param filename
	 */
	public EIJ(String basedir, String key, String filename){
		super(filename);
		this.basedir=basedir;
		this.key=key;
	}
	
	/**
	 * This function save the given image in jpeg format. In the options you can specify the quality
	 * of the resulting image.
	 * @param image
	 * @param path
	 * @param options{quality:(0-100)}
	 * @return boolean: If it succeed saving or not
	 */
	public boolean save(String filename, Object options){
		
		String fullFilename=SecureFileManager.getValidatedFilename(basedir, key, filename);
		if (fullFilename==null) return false;
		
		JSONObject parameters = Function.checkParameter(options);
		int quality = parameters.optInt("quality", 100);
		FileSaver fileSaver = new FileSaver(this);
		FileSaver.setJpegQuality(quality);
		fileSaver.saveAsJpeg(fullFilename);
		return true;
	}
	
	/**
	 * This function scales the image to the specified width and height
	 * @param newWidth
	 * @param newHeight
	 */
	public void scale(int newWidth, int newHeight){
		ImageProcessor im = this.getProcessor();
		this.setImage(im.createImage().getScaledInstance(newWidth, newHeight, 1));
	}
	
	/**
	 * This function contrast the image at the maximum possible level
	 */
	public void contrast(){
		ImageProcessor ip = this.getProcessor();
		int w = ip.getWidth();
		int h = ip.getHeight();

		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int a = (int) (ip.get(u, v) * 1.5 + 0.5);
				if (a > 255)
					a = 255; 		// clamp to max. value
				ip.set(u, v, a);
			}
		}
	}
	
	/**
	 * This function returns an histogram for this image.
	 * @param numberOfBins
	 * @return
	 */
	public int[] histogram(){
		ImageProcessor ip = this.getProcessor();

		return ip.getHistogram();
	}
}
