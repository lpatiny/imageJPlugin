clearLog();
var list = dir("/XTC/images/png", {
	filter : ".*tif"
});
var size = list.length;
var colors = new Array(size);
for ( var i = 0; i < size; i++) {
	var image = IJ.load(list[i]);
	colors[i] = list[i]+"="+image.getColor();
}
jexport('colors', colors, 'matrix');
