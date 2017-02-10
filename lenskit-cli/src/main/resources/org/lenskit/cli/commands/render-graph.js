var svg = Viz(dotSrc, {
    format: "svg",
    engine: "dot"
});

var writer = new java.io.FileWriter(outFile);
writer.write(svg.toString());
writer.close();
