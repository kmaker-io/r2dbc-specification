# Define the Maven wrapper command
MVN = ./mvnw

# Default target (called by 'make' without arguments)
all: clean install

# Clean the project
clean:
	$(MVN) clean

# Install the project (compile, test, package, and install to local repository)
install:
	$(MVN) install

# Run unit tests
test:
	$(MVN) test site

# Run Checkstyle validation
checkstyle:
	$(MVN) checkstyle:check

# Build the project and skip tests
skip-tests:
	$(MVN) install -DskipTests

# Build the project and create a release (skip tests, generate JAR)
release:
	$(MVN) clean deploy -DskipTests -Prelease

# Deploy to a remote repository (e.g., Maven Central or Nexus)
deploy:
	$(MVN) deploy

# Version bump task (could automate version updates for new releases)
version-bump:
	$(MVN) versions:set -DnewVersion=$(VERSION)
	$(MVN) versions:commit

# Generate Javadoc for the project
javadoc:
	$(MVN) javadoc:javadoc

# Run the full build pipeline, including Javadoc and Checkstyle
full-build: clean checkstyle javadoc install

# Build and deploy (run full build and deploy to remote Maven repo)
publish:
	$(MAKE) full-build
	$(MAKE) deploy
