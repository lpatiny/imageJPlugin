clearLog();
var list = dir("/XTC/images/png/split", {
	filter : ".*tif"
});
var size = 1;
for ( var i = 0; i < size; i++) {
	var image = IJ.load(list[i]);
	var split = image.split();
	var sizeS = split.length;
	for (var a = 0; a < sizeS; a++){
		var auxImage = split[a];
		auxImage.save(list[i] + "-" + i + "-" + a, "{quality:100}");
	}
	
}
