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