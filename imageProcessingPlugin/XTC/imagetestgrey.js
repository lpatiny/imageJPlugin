clearLog();
var list = dir("/XTC/images/png", {
	filter : ".*png"
});
var size = 1;
for ( var i = 0; i < size; i++) {
	var image = IJ.load(list[i]);
	image.grey();
	image.save(list[i] + "-1", "{quality:100}");
}
