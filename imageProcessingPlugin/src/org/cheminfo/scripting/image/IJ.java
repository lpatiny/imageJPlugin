package org.cheminfo.scripting.image;


import org.cheminfo.function.Function;
import org.cheminfo.function.scripting.SecureFileManager;
import org.json.JSONObject;

public class IJ extends Function{
	/**
	 * This function load an imageJ. Supported formats: JPEG, JPG,BMP, TIFF, PNG
	 * @param basedir
	 * @param key
	 * @param filename
	 * @return and extended ImageJ object
	 */
	public static EIJ load(String basedir, String basedirkey, String filename){
		//If it is a URL we wont check security
		if(filename.trim().matches("^https?://.*$")){
			return new EIJ(basedir,basedirkey,filename);
		}
		else{
			String fullFilename=SecureFileManager.getValidatedFilename(basedir, basedirkey, filename);
			if (fullFilename==null) return null;
			return new EIJ(basedir,basedirkey,fullFilename);
		}
	}
	
	/**
	 * This function accepts a String parameter
	 * @param args
	 * @return args+", Hello world!"
	 */
	public static String helloWorld(String args){
		return args+", Hello World!"; 
		
	}	
	
	 /**
	  * This function accepts a JSon parameter. It can be a java json or a javascript json
	  * @param object
	  * @return
	  */
	public static String processParams(Object object){
		JSONObject params = checkParameter(object);
		String name = params.optString("name","none");
		return "Your name is: "+name; 
	}

}
