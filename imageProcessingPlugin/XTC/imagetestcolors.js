clearLog();
var list = dir("/XTC/images/png", {
	filter : ".*jpg"
});
var size = list.length;
var aSize = new Array(size);
for ( var i = 0; i < size; i++) {
	var image = IJ.load(list[i]);
	aSize[i] = list[i]+"="+image.getColor();
}
jexport('size', aSize, 'matrix');
