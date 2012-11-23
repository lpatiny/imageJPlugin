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
		return IJAPI.load(Global.basedir, Global.basedirkey, filename);
	}
		
};


/**
 * @object	EIJ
 * Library allowing to manipulate image.
 * This library contains these functions: 
 * 		Utilities: <b>save</b>, <b>resize</b>, <b>histogram</b>, <b>getWidth</b>, <b>getHeight</b>, <b>copy</b>, <b>getColor</b>, <b>crop</b>.
 * 		Filters: <b>contrast</b>, <b>edge</b>, <b>color</b>, <b>grey</b>, <b>texture</b>
 * 		Main Function: <b>split</b>
 */

		/**
		 * @function		save(path, options)
		 * Saves the given image in the format specified by the extension of the
		 * path. In the options you can specify the quality of the resulting image.
		 * @param 		path		physical path in which to save the image
		 * @option		quality 	Quality for jpeg image, possible values between 0 and 100 (Default 100)
		 * @return 		boolean		If it succeed saving or not
		 */

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
		
		/**
		 * @function 		contrast(options)
		 * Applies a contrast filter to the image
		 * @option		equalize	equalize the image, possible values y/n (Default y)
		 * @option		saturated	stretch image histogram with saturated pixels, possible values greater than 0 and less than 100 (Default 0)
		 */

		/**
		 * @function 		histogram()
		 * Returns an histogram for this image. Returns a luminosity histogram for RGB images 
		 * @return array
		 */

		/**
		 * @function		getWidth()
		 * Returns the width of the image
		 * @return 		number
		 */

		/**
		 * @function 		getHeight()
		 * Returns the height of the image
		 * @return 		number
		 */

		/**
		 * @function		copy()
		 * Returns a copy of the EIJ Image
		 * @return 		object
		 */

		/**
		 * @function		edge()
		 * Applies a edge filter to the image
		 */

		/**
		 * @function		color(options)
		 * Applies a color filter to the image
		 * @option		nbColor		Number of colors, possible values between 2 and 256 (Default 256)
		 */

		/**
		 * @function		grey(options)
		 * Applies a grey filter to the image
		 * @option		nbGrey		Number of greys, possible values between 2 and 256 (Default 256)
		 */

		/**
		 * @function		texture()
		 * Applies a texture filter to the image
		 */

		/**
		 * @function 		getColor()
		 * Returns the number of colors
		 * @return 		number
		 */

		/**
		 * @function		crop(x, y, width, height)
		 * Crops a image
		 * @param 		x		horizontal value from which to start cutting
		 * @param 		y		vertical value from which to start cutting
		 * @param 		width	width of the new image, if it is greater than the width of the original image minus the value of x, it calculates the width
		 * @param 		height	height of the new image, if it is greater than the height of the original image minus the value of y, it calculates the height
		 * @return 		boolean 	true for successful crop, false otherwise
		 */

		/**
		 * @function 		split()
		 * Splits a image
		 * @return		array		an array with the result images, ordered from smallest to largest
		 */
