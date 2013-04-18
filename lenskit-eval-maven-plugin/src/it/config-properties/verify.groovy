File file = new File("${basedir}/target/analysis", "out.txt")
File dataPath = new File("${basedir}/data")
if (!file.isFile()) {
    throw new FileNotFoundException("could not find out.txt")
}
def text = file.getText()
if (text != dataPath.getAbsolutePath()) {
    throw new RuntimeException("invalid data path: " + text)
}