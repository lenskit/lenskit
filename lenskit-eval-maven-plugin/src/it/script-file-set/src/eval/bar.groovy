def dir = new File(config.analysisDir)
dir.mkdirs()
def file = new File(dir, "bar.txt")
file.withPrintWriter {
    it.print("good night moon")
}