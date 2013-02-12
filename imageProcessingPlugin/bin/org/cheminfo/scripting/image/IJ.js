/**
 * @object IJ
 * Library that provides methods for image manipulation.
 */
var IJ = {
	/**
	 * @function helloWorld
	 * This function prints the hello world message. You can pass a parameter. 
	 * That returns  args+", Hello World!"
	 * @param  args	The parameter
	 */
	helloWorld: function(args) {
		return IJAPI.helloWorld(args);
	},
	
	/**
	 * @function load(filename)
	 * This function loads and returns an extended ImageJ.
	 */
	load: function(filename) {
		return new EIJ(IJAPI.load(Global.basedir, Global.basedirkey, filename));
	}
		
};


var EIJ = function (newEIJ) {
	this.EIJ=newEIJ;
	


/**
 * @object	EIJ
 * Library allowing to manipulate image.
 */

		/**
		 * @function		save(path, options)
		 * Saves the given image in the format specified by the extension of the
		 * path. In the options you can specify the quality of the resulting image.
		 * @param 		path		physical path in which to save the image
		 * @option		quality 	Quality for jpeg image, possible values between 0 and 100 (Default 100)
		 * @return 		boolean		If it succeed saving or not
		 */
		this.save=function(path, options) {
			return this.EIJ.save(path, options);
		}	
	
		/**
		 * @function		resize(size, options)
		 * Scales the image to the specified width and height.
		 * @param		size 		New image size in the format widthxheight or percentage%)
		 * @option		method		Resize method, possible values 0->None,1->Bilinear, 2->Bicubic (Default 1)
		 * @option 		average		Average pixels when downsizing, possible values y/n (Default n)
		 * @example		resize(200x100)		resize to exactly 200x100
		 * @example 	resize(x100)		proportional resize to 100 points height
		 * @example		resize(200x)		proportional resize to 200 points witdh
		 * @example		resize(50%)			proportional resize to 50%
		 * @return 		boolean 	If it succeed resizing or not
		 */
		this.resize=function(size, options) {
			return this.EIJ.resize(size, options);
		}	
	
		/**
		 * @function 		contrast(options)
		 * Applies a contrast filter to the image
		 * @option		equalize	equalize the image, possible values y/n (Default y)
		 * @option		saturated	stretch image histogram with saturated pixels, possible values greater than 0 and less than 100 (Default 0)
		 */
		this.contrast=function(options) {
			return this.EIJ.contrast(options);
		}
		
		/**
		 * @function 		createMask(options)
		 * Generates a mask based on the options
		 * @option		method	Name of the method to use to determine the threshold: Default, Huang, IJ_IsoData, Intermodes, IsoData, Li, MaxEntropy, Mean, MinError, Minimum, Moments, Otsu, Percentile, RenyiEntropy, Shanbhag, Triangle, Yen (Default: Default)
		 * @return		EIJ	A new image containing the mast
		 */
		this.createMask=function(options) {
			return this.EIJ.createMask(options);
		}
	
		/**
		 * @function		analyzeImage(mask, options)
		 * Returns a JSON (to parse) containing all the information about an image
		 * Can also be called with options: analyzeImage(Roi[] rois)
		 * @mask	an EIJ picture that has a threshold and should be generated with "createMask"
		 * @option	minLength	minimum length of the selected area
		 * @option	maxLength	maximal length of the selected area
		 * @option	minWidth	minimum width of the selected area
		 * @option	maxWidth	maximal width of the selected area
		 * @option	minHeight	minimum height of the selected area
		 * @option	maxHeight	maximal height of the selected area
		 * @option	minSurface	minimum surface of the selected area
		 * @option	maxSurface	maximal surface of the selected area
		 * @option	scale		scale the ROI with a defined factor
		 * @option	sortBy	how the results should be sorted. Possible values: "x", "y", "xy", "length"
		 * 
		 * @return 		JSON String
		 */
		this.analyzeImage=function(mask, options) {
			return JSON.parse(this.EIJ.analyzeImage(mask, options));
		}
	
		/**
		 * @function		split(mask, options)
		 * Returns an array of images based on a mask
		 * Can also be called with options: split(Roi[] rois)
		 * @mask	an EIJ picture that has a threshold and should be generated with "createMask"
		 * @option	minLength	minimum length of the selected area
		 * @option	maxLength	maximal length of the selected area
		 * @option	minWidth	minimum width of the selected area
		 * @option	maxWidth	maximal width of the selected area
		 * @option	minHeight	minimum height of the selected area
		 * @option	maxHeight	maximal height of the selected area
		 * @option	minSurface	minimum surface of the selected area
		 * @option	maxSurface	maximal surface of the selected area
		 * @option	scale		scale the ROI with a defined factor
		 * 
		 * @return 		EIJ[]
		 */
		this.split=function(options) {
			return this.EIJ.split(options);
		}
		
		/**
		 * @function		paintMask(mask)
		 * Returns an image with painted mask
		 * @option	strokeColor	color of the stroke
		 * @option	strokeSize	with of the stroke
		 * Returns an image with painted mask
		 * 
		 * @return 		EIJ
		 */
		this.paintMask=function(mask) {
			return this.EIJ.paintMask(mask);
		}
	
		/**
		 * @function		paintRois(Roi[])
		 * Returns an image with painted Roi[]
		 * @option	strokeColor	color of the stroke
		 * @option	strokeSize	with of the stroke
		 * Returns an image with painted mask
		 * 
		 * @return 		EIJ
		 */
		this.paintRois=function(rois) {
			return this.EIJ.paintRois(rois);
		}

		/**
		 * @function		splitRGB()
		 * Returns an array of 3 images in 256 gray scale: red, green, blue
		 * 
		 * @return 		EIJ[3]
		 */
		this.splitRGB=function() {
			return this.EIJ.splitRGB();
		}
	
		/**
		 * @function		splitHSB()
		 * Returns an array of 3 images in 256 gray scale: hue, saturation, brightness
		 * 
		 * @return 		EIJ[3]
		 */
		this.splitHSB=function() {
			return this.EIJ.splitHSB();
		}
		
		/**
		 * @function 		histogram()
		 * Returns an histogram for this image. Returns a luminosity histogram for RGB images 
		 * @return array
		 */
		this.histogram=function() {
			return this.EIJ.histogram();
		}
		
		/**
		 * @function		getWidth()
		 * Returns the width of the image
		 * @return 		number
		 */
		this.getWidth=function() {
			return this.EIJ.getWidth();
		}
		
		/**
		 * @function 		getHeight()
		 * Returns the height of the image
		 * @return 		number
		 */
		this.getHeight=function() {
			return this.EIJ.getHeight();
		}
		
		/**
		 * @function		getRois(options)
		 * Returns an array of regio of interest based on a mask
		 * @mask	an EIJ picture that has a threshold and should be generated with "createMask"
		 * @option	minLength	minimum length of the selected area
		 * @option	maxLength	maximal length of the selected area
		 * @option	minWidth	minimum width of the selected area
		 * @option	maxWidth	maximal width of the selected area
		 * @option	minHeight	minimum height of the selected area
		 * @option	maxHeight	maximal height of the selected area
		 * @option	minSurface	minimum surface of the selected area
		 * @option	maxSurface	maximal surface of the selected area
		 * @option	scale		scale the ROI with a defined factor
		 * @example	mask.getRois();
		 * @return 		Roi[]
		 */
		this.getRois=function(options) {
			return this.EIJ.getRois(options);
		}
		
		/**
		 * @function		copy()
		 * Returns a copy of the EIJ Image
		 * @return 		EIJ
		 */
		this.copy=function() {
			return new EIJ(this.EIJ.copy());
		}
		
		/**
		 * @function		edge()
		 * Applies a edge filter to the image
		 */
		this.edge=function() {
			return this.EIJ.edge();
		}
		
		/**
		 * @function		color(options)
		 * Applies a color filter to the image
		 * @option		nbColor		Number of colors, possible values between 2 and 256 (Default 256)
		 */
		this.color=function(options) {
			return this.EIJ.color(options);
		}

		/**
		 * @function		grey(options)
		 * Applies a grey filter to the image
		 * @option		nbGrey		Number of greys, possible values between 2 and 256 (Default 256)
		 */
		this.grey=function() {
			return this.EIJ.grey();
		}
		
		/**
		 * @function		texture()
		 * Applies a texture filter to the image
		 */
		this.texture=function() {
			return this.EIJ.texture();
		}
		
		/**
		 * @function 		getColor()
		 * Returns the number of colors
		 * @return 		number
		 */
		this.getColor=function() {
			return this.EIJ.getColor();
		}
		
		/**
		 * @function		crop(x, y, width, height)
		 * Crops a image
		 * @param 		x		horizontal value from which to start cutting
		 * @param 		y		vertical value from which to start cutting
		 * @param 		width	width of the new image, if it is greater than the width of the original image minus the value of x, it calculates the width
		 * @param 		height	height of the new image, if it is greater than the height of the original image minus the value of y, it calculates the height
		 * @return 		boolean 	true for successful crop, false otherwise
		 */
		this.crop=function(x, y, width, height) {
			return this.EIJ.crop(x, y, width, height);
		}

};
