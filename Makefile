JAR=build/jar/NetworkSentry.jar

$(JAR):
	ant

all: $(JAR)

clean:
	ant clean

distclean: clean

install: all
	install -d ${DESTDIR}/var/spool/apache/htdocs/ns
	install -m 644 -t ${DESTDIR}/var/spool/apache/htdocs/ns $(JAR)
