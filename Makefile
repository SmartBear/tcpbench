ifndef TCP_TORRENT
$(error TCP_TORRENT is not set)
endif

JAVA_FILES = $(shell find src/main -name "*.java")

svgs: results/rtptorrent/CloudifySource@cloudify.svg \
      results/rtptorrent/DSpace@DSpace.svg \
      results/rtptorrent/Graylog2@graylog2-server.svg \
      results/rtptorrent/SonarSource@sonarqube.svg \
      results/rtptorrent/adamfisk@LittleProxy.svg \
      results/rtptorrent/apache@sling.svg \
      results/rtptorrent/brettwooldridge@HikariCP.svg \
      results/rtptorrent/deeplearning4j@deeplearning4j.svg \
      results/rtptorrent/doanduyhai@Achilles.svg \
      results/rtptorrent/dynjs@dynjs.svg \
      results/rtptorrent/eclipse@jetty.project.svg \
      results/rtptorrent/facebook@buck.svg \
      results/rtptorrent/jOOQ@jOOQ.svg \
      results/rtptorrent/jcabi@jcabi-github.svg \
      results/rtptorrent/jsprit@jsprit.svg \
      results/rtptorrent/julianhyde@optiq.svg \
      results/rtptorrent/l0rdn1kk0n@wicket-bootstrap.svg \
      results/rtptorrent/neuland@jade4j.svg \
      results/rtptorrent/square@okhttp.svg \
      results/rtptorrent/thinkaurelius@titan.svg

results/rtptorrent/%.csv: scripts/tcpbench.sh target/tcpbench-0.0.1-SNAPSHOT.jar
	mkdir -p $(@D)
	$< \
		--rtptorrent $(TCP_TORRENT)/$$(basename $(@D)) \
		--engine $$(basename $@ .csv) \
		--training 10 \
		--prediction 30 \
		> $@.tmp
	mv $@.tmp $@
.PRECIOUS: results/rtptorrent/%.csv

results/rtptorrent/%.svg: scripts/boxplot.plg \
      results/rtptorrent/%/com.smartbear.tcpbench.engines.OptimalOrder.csv \
      results/rtptorrent/%/com.smartbear.tcpbench.engines.InitialOrder.csv \
      results/rtptorrent/%/com.smartbear.tcpbench.engines.RandomOrder.csv \
      $(if $(COMET_URL), results/rtptorrent/%/com.smartbear.tcpbench.engines.Comet.csv)

	gnuplot -e "filenames='$(wordlist 2, $(words $^), $^)'" $< > $@

target/tcpbench-0.0.1-SNAPSHOT.jar: $(JAVA_FILES) pom.xml
	mvn package
