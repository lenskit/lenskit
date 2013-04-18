def file = new File("out.txt")
file.withPrintWriter {
    it.print("test ran")
}