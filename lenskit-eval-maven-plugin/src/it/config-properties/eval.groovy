def dir = new File(config.analysisDir)
dir.mkdirs()
def file = new File(dir, "out.txt")
file.withPrintWriter {
    // write the data dir to the output file
    it.print(new File(config.dataDir).getAbsolutePath())
}