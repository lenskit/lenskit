statusListener(OnConsoleStatusListener)

def logFile = System.getProperty("log.file")
def appenders = ["CONSOLE"]

appender("CONSOLE", ConsoleAppender) {
    target = System.err
    filter(ThresholdFilter) {
        level = System.getProperty("log.level", "INFO")
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%highlight([%-5level]) %date{HH:mm:ss.SSS} {%yellow(%thread)} %cyan(%logger{24}) %msg%n"
    }
}

if (logFile != null) {
    appender("LOGFILE", FileAppender) {
        file = logFile
        encoder(PatternLayoutEncoder) {
            pattern = "%date{HH:mm:ss.SSS} %-5level {%thread} %logger{24}: %msg%n"
        }
    }
    appenders << "LOGFILE"
}

logger("org.grouplens.graph", WARN)
root(DEBUG, appenders)