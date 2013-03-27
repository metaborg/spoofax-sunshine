#! /bin/bash
/usr/libexec/java_home -v 1.7 --exec java \
		-ea:org.spoofax.sunshine -server -Xss16m -Xms1G -Xmx2G -jar dist/sunshine.jar \
		--extens app \
		--lang-jar ../../webdsl2/include/webdsl.jar ../../webdsl2/include/webdsl-java.jar \
		--lang-tbl ../../webdsl2/include/WebDSL.tbl \
		--start-symbol Unit \
		--lang-name WebDSL \
		--proj-dir ../../yellowgrass/

