load('classpath:META-INF/resources/webjars/viz.js/1.5.1/viz.js');

var svg = Viz(dotSrc, {
    format: "svg",
    engine: "dot"
});

var writer = new java.io.FileWriter(outFile);
writer.write(svg.toString());
writer.close();
