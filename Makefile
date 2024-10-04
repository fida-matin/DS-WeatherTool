# University of Adelaide - Distributed Systems 2024
# Fida Matin - a1798239
# 4 October 2024

# Variables
JFLAGS = -cp ".:./src:../lib/*:./lib/*:.."
SRCDIR = src
BINDIR = ../bin

JC = javac

.SUFFIXES: .java .class

# Target: all
all: compile

# Compile targets
compile: compile_as compile_cs compile_client compile_util


compile_as:
	cd Server && cp -r $(COMMON) $(SRCDIR) && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/Server/Aggregation/AggregationServer.java

compile_cs:
	cd Client/Content && cp -r $(COMMON) $(SRCDIR) && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/Client/Content/ContentServer.java

compile_client:
	cd Client/GET && cp -r $(COMMON) $(SRCDIR) && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/Client/GET/GETClient.java

compile_util:
	# compile src
	cd HTTP && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) ./*.java
	cd util && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) ./*.java

run_as:
	cd Server/Aggregation && java $(JFLAGS) $(PACKAGE).aggregationserver.AggregationServer --default

run_cs:
	cd Client/Content && java $(JFLAGS) $(PACKAGE).contentserver.ContentServer --default

run_client:
	cd Client/GET && java $(JFLAGS) $(PACKAGE).client.GETClient --default

# Clean targets
clean:
	rm -rf ./bin
