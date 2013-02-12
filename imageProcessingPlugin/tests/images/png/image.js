clearLog();
var list = dir("/XTC/png",{filter:".*png"});
var size=list.length;
var histograms = new Array(size);
for(var i=0;i<size;i++){
 	var image = IJ.load(list[i]);
  	histograms[i]=image.histogram();
}

var comparator = Distance.getComparator('AOSimilarity',{});
//Creating the similarity matrix
var similarity = new Array(size);
for (i=0;i<size;i++) {
	similarity[i]=new Array(size);
}
//Calculating similarities...
for (i=0;i<size;i=i+1) {
	for (j=i;j<size;j=j+1) {
		similarity[i][j]=comparator.quickSimilarity(histograms[i],histograms[j]);
		similarity[j][i]=similarity[i][j];
	}
}

var labels=new Array(size);
var batch="";
for(i=0;i<size;i++){
  batch=list[i].substring(9,list[i].indexOf("-"));
  var label = {colorBatch:hex_md5(batch)substring(0,6),location:list[i],label:list[i]};
  labels[i]=label;
}

jexport('sim',similarity,'matrix');
//Create the dendrogram	
var dendrogram = Distance.clustering(similarity,labels);
//Return to the client the JSON of the dendrogram
jexport('dendrogram',dendrogram,'dendrogram'); 
