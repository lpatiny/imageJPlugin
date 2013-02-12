clearLog();


// var image = IJ.load("/tests/images/XTC1.png");

var image = IJ.load("/tests/images/BIO1.JPG");


image.grey();
var mask=image.createMask();
image.save("ORI.png");
mask.save("TEST.png");

