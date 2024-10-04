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
	javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/Server/Aggregation/AggregationServer.java

compile_cs:
	javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/Client/Content/ContentServer.java

compile_client:
	javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/Client/GET/GETClient.java

compile_util:
	# compile src
	javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/util/JSONObject.java
	javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/util/LamportClock.java
	javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/util/Weather.java

run_as:
	java $(JFLAGS) $(PACKAGE).aggregationserver.AggregationServer --default

run_cs:
	java $(JFLAGS) $(PACKAGE).contentserver.ContentServer --default

run_client:
	java $(JFLAGS) $(PACKAGE).client.GETClient --default

# Clean targets
clean:
	rm -rf ./bin
