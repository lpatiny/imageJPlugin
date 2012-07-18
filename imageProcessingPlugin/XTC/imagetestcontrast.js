clearLog();
var list = dir("/XTC/images/png", {
	filter : ".*png"
});
var size = 50;
for ( var i = 0; i < size; i++) {
	var image = IJ.load(list[i]);
	image.contrast("{}");
	image.save(list[i] + "-1", "{quality:100}");
}
