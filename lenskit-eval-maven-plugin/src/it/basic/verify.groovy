File file = new File(basedir, "out.txt")
if (!file.isFile()) {
    throw new FileNotFoundException("could not find out.txt")
}
def text = file.getText()
if (text != "test ran") {
    throw new RuntimeException("invalid source text: " + text)
}