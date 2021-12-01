### Shell Scripts

Contains shell scripts used by the pipelines for building Hoot and updating the project coverage reports and badges.

| **Script** | **Description** |
| ---------- | --------------- |
| build-all-mods.sh | builds all the Hoot project modules and coordinates their release |
| build-cache-check.sh | builds files used to generate checksums for the project cache |
| build-cover-badges.sh | builds the coverage badges for the tested components |
| build-cover-zip.sh | builds a ZIP files containing the coverage reports |
| build-git-tag.sh | tags the commit with the build version and pushes to origin if passed |
| build-mvn-badge.sh | builds the badge indicating overall build pass/fail |
| deploy-libs.sh | pushes the library bundles and compiler plugin to Cloudsmith |
| fetch-cover-zip.sh | pulls the coverage reports from the lab packages |
| fetch-secrets.sh | pulls the build secrets from Google secrets manager |
| get-hoot-version.sh | gets the latest built version from **libs-hoot** |
| make-release.sh | creates a release in the project home |
| push-cover-zip.sh | pushes the coverage reports to the lab packages |
| trigger-cover-pub.sh | triggers publication of the latest coverage reports |
| **Additional** | a few additional scripts used during early development |
| install-tools.sh | installs OpenJDK 8 and Maven using **apt-get** |
| install-cloudsmith.sh | installs the Cloudsmith CLI using **pip** |
| query-repo.sh | pulls the latest published JAR version number from Cloudsmith |
| pull-hoots.sh | pulls the latest published JARs from Cloudsmith |
