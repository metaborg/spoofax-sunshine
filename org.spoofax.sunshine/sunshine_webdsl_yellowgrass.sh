#! /bin/bash

mkdir -p stats_yg

rm -rf ./../yellowgrass/.cache

/usr/libexec/java_home -v 1.7 --exec java \
		-ea:org.spoofax.sunshine... -server -Xss16m -Xms4G -Xmx4G -jar dist/sunshine.jar \
		--extens app \
		--lang-jar ../../webdsl2/include/webdsl.jar ../../webdsl2/include/webdsl-java.jar \
		--lang-tbl ../../webdsl2/include/WebDSL.tbl \
		--start-symbol Unit \
		--lang-name WebDSL \
		--proj-dir ../../yellowgrass/ \
		--warmup 3 \
		--git-autodrive \
		--stats stats_yg/stats.csv \
		2>&1 | tee stats_yg/sunshine_webdsl_yellowgrass.log

		
