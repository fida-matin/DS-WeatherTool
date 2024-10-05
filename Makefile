# University of Adelaide - Distributed Systems 2024
# Fida Matin - a1798239
# 4 October 2024

# Compiler and flags
JFLAGS = -g
JC = javac
SRC_DIR = src
BIN_DIR = bin
TEST_DIR = bin/tests
JUNIT_CP = lib/junit-platform-console-standalone-1.7.2.jar # Adjust to match your JUnit JAR

# Automatically find all .java files in SRC_DIR (excluding Test files)
SRC = $(shell find $(SRC_DIR) -name '*.java' ! -name '*Test.java')

# Automatically find all test files in SRC_DIR
TEST_SRC = $(shell find $(SRC_DIR) -name '*Test.java')

# Convert .java file paths to corresponding .class file paths in BIN_DIR
CLASSES = $(SRC:$(SRC_DIR)/%.java=$(BIN_DIR)/%.class)

# Convert test .java files to .class files in TEST_DIR
TEST_CLASSES = $(TEST_SRC:$(SRC_DIR)/%.java=$(TEST_DIR)/%.class)

# Default target to compile all classes and tests
default: prepare classes tests

# Create bin and test directories if they don't exist
prepare:
	@mkdir -p $(BIN_DIR) $(TEST_DIR)

# Rule to compile all source .class files
classes: $(CLASSES)

# Rule to compile all test .class files
tests: $(TEST_CLASSES)

# Rule for compiling each source .java file into corresponding .class file
$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(dir $@)
	$(JC) $(JFLAGS) -d $(BIN_DIR) -cp $(SRC_DIR) $<

# Rule for compiling each test .java file into corresponding .class file
$(TEST_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(dir $@)
	$(JC) $(JFLAGS) -d $(TEST_DIR) -cp $(SRC_DIR):$(JUNIT_CP) $<

# Run tests using JUnit
test: tests
	java -cp $(TEST_DIR):$(BIN_DIR):$(JUNIT_CP) org.junit.platform.console.ConsoleLauncher --scan-class-path

# Clean target to remove all .class files
clean:
	rm -rf $(BIN_DIR) $(TEST_DIR)

# Run main Java class
run: classes
	java -cp $(BIN_DIR) Server.Aggregation.AggregationServer # Adjust the class name to your main class

# Run ContentServer
run-content: classes
	java -cp $(BIN_DIR) Client.Content.ContentServer

# Run GETClient
run-client: classes
	java -cp $(BIN_DIR) Client.GET.GETClient

