def dir = new File(config.analysisDir)
dir.mkdirs()
def file = new File(dir, "foo.txt")
file.withPrintWriter {
    it.print("hello, world")
}