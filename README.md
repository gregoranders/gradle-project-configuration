# Gradle Project Configuration Plugin

### Following the principle of *convention over configuration* this Gradle plugin provides default configurations for various plugins of a Gradle JVM project so that you do not need to copy boilerplate code throughout your projects.

#### Compiled using JAVA 1.8 and Gradle 7.3.3

[![Release][release-image]][release-url]

[![License][license-image]][license-url]
[![Issues][issues-image]][issues-url]

[![ReleaseMain Build][release-build-image]][release-url]
[![Main Build][main-build-image]][main-url]

[//]: # ([![Development Build][development-build-image]][development-url])

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=bugs)][sonarcloud-url]
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=code_smells)][sonarcloud-url]
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=coverage)][sonarcloud-url]
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=vulnerabilities)][sonarcloud-url]

[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=duplicated_lines_density)][sonarcloud-url]
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=ncloc)][sonarcloud-url]
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=sqale_index)][sonarcloud-url]

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=alert_status)][sonarcloud-url]
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=sqale_rating)][sonarcloud-url]
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=reliability_rating)][sonarcloud-url]
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=gregoranders_gradle-project-configuration&metric=security_rating)][sonarcloud-url]

[![Main Language][language-image]][code-metric-url] [![Languages][languages-image]][code-metric-url] [![Code Size][code-size-image]][code-metric-url] [![Repo-Size][repo-size-image]][code-metric-url]

## Supported plugins:

* [IDEA][idea-plugin-url]
* [Checkstyle][checkstyle-plugin-url]
* [PMD][pmd-plugin-url]
* [JaCoCo][jacoco-plugin-url]
* [SpotBugs][spotbugs-plugin-url]
* [OWASP Dependency Check][dependencycheck-plugin-url]
* [Maven Publish][maven-publish-plugin-url]
* [Signing][signing-plugin-url]

### IDEA

required configuration properties

* sourceCompatibility

```groovy
idea {
  project {
    vcs = 'Git'
    jdkName = "jdk-${project.sourceCompatibility}"
    languageLevel = project.sourceCompatibility
  }
  module {
    downloadJavadoc = true
    downloadSources = true
  }
}
```

### Checkstyle

required configuration properties

* checkstyleVersion

```groovy
checkstyle {
  toolVersion = project.rootProject.property('checkstyleVersion')
  ignoreFailures = false
}
```

### PMD

required configuration properties

* pmdVersion

optional configuration properties

* cpdMinimumTokenCount *used for the Copy/Paste detection - default is 10*

```groovy
pmd {
  consoleOutput = true
  incrementalAnalysis = true
  toolVersion = project.rootProject.property('pmdVersion')
  ignoreFailures = false
  ruleSets = []
  ruleSetConfig = project.rootProject.resources.text.fromFile('config/pmd/pmd-rules.xml')
}
project.tasks.register('cpd', CPDTask)
check.configure {
  dependsOn project.tasks.cpd
}
```

### JaCoCo

required configuration properties

* jacocoVersion

```groovy
jacoco {
  toolVersion = project.rootProject.property('jacocoVersion')
}
jacocoTestReport {
  dependsOn project.tasks.test
  reports {
    xml.required = true
    csv.required = false
    html.required = true
  }
}
jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        counter = 'INSTRUCTION'
        minimum = 1.0
      }
      limit {
        counter = 'BRANCH'
        minimum = 1.0
      }
      limit {
        counter = 'LINE'
        minimum = 1.0
      }
      limit {
        counter = 'METHOD'
        minimum = 1.0
      }
      limit {
        counter = 'CLASS'
        minimum = 1.0
      }
    }
  }
}
test {
  finalizedBy project.tasks.jacocoTestReport
}
check {
  dependsOn project.tasks.jacocoTestCoverageVerification
}
```

### SpotBugs

required configuration properties

* spotbugsVersion

```groovy
spotbugs {
  toolVersion = project.rootProject.property('spotbugsVersion')
  ignoreFailures = false
  effort = 'max'
  reportLevel = 'low'
  excludeFilter = project.rootProject.file('config/spotbugs/excludeFilter.xml')
}
spotbugsMain {
  reports {
    html {
      outputLocation = project.layout.buildDirectory.file('reports/spotbugs/main/spotbugs.html').get().asFile
      stylesheet = 'fancy-hist.xsl'
    }
    xml {
      outputLocation = project.layout.buildDirectory.file('reports/spotbugs/main/spotbugs.xml').get().asFile
    }
  }
}
spotbugsTest.enabled = false
```

### OWASP Dependency Check

```groovy
dependencyCheck {
  autoUpdate = true
  failBuildOnCVSS = 1
  cveValidForHours = 1
  format = 'ALL'
  outputDirectory = project.layout.buildDirectory.dir('reports/dependency-check').get().asFile
  suppressionFile = project.rootProject.file('config/dependency-check/suppressions.xml')
  analyzers {
    experimentalEnabled = true
    archiveEnabled = true
    jarEnabled = true
    centralEnabled = true
    nexusEnabled = false
    nexusUsesProxy = false
    nuspecEnabled = false
    assemblyEnabled = false
    msbuildEnabled = false
    golangDepEnabled = false
    golangModEnabled = false
    cocoapodsEnabled = false
    swiftEnabled = false
    swiftPackageResolvedEnabled = false
    bundleAuditEnabled = false
    pyDistributionEnabled = false
    pyPackageEnabled = false
    rubygemsEnabled = false
    opensslEnabled = false
    cmakeEnabled = false
    autoconfEnabled = false
    composerEnabled = false
    cpanEnabled = false
    nodeEnabled = false
  }
}
if (System.getenv('CI') == 'true') {
  check {
    dependsOn project.tasks.dependencyCheckAnalyze
  }
}
```

### Maven Publish

required configuration properties

* url
* license
* licenseUrl
* authorId
* author
* email
* scmUrl

```groovy
publishing {
  publications {
    mavenJava(MavenPublication) {
      from project.components.java
      pom {
        name = project.name
        description = project.description
        url = property('url')
        licenses {
          license {
            name = property('license')
            url = property('licenseUrl')
          }
        }
        developers {
          developer {
            id = property('authorId')
            name = property('author')
            email = property('email')
          }
        }
        scm {
          connection = "scm:git:git://${property('scmUrl')}"
          developerConnection = "scm:git:ssh://${property('scmUrl')}"
          url = property('url')
        }
      }
    }
  }
  repositories {
    maven {
      url = project.layout.buildDirectory.dir('repos')
    }
  }
}
```

### Signing

required environment variables

* GPG_KEY
* GPG_PASSPHRASE

```groovy
signing {
  useInMemoryPgpKeys(System.getenv('GPG_KEY'), System.getenv('GPG_PASSPHRASE'))
  if (project.plugins.hasPlugin('maven-publish')) {
    sign project.publishing.publications
  }
}
```

[project-url]: https://github.com/users/gregoranders/projects/1

[release-url]: https://github.com/gregoranders/gradle-project-configuration/releases

[main-url]: https://github.com/gregoranders/gradle-project-configuration/tree/main

[development-url]: https://github.com/gregoranders/gradle-project-configuration/tree/development

[code-metric-url]: https://github.com/gregoranders/gradle-project-configuration/search?l=JAVA

[license-url]: https://github.com/gregoranders/gradle-project-configuration/blob/main/LICENSE

[license-image]: https://img.shields.io/github/license/gregoranders/gradle-project-configuration.svg

[issues-url]: https://github.com/gregoranders/gradle-project-configuration/issues

[issues-image]: https://img.shields.io/github/issues-raw/gregoranders/gradle-project-configuration.svg

[release-image]: https://img.shields.io/github/release/gregoranders/gradle-project-configuration

[release-build-image]: https://github.com/gregoranders/gradle-project-configuration/workflows/Release%20CI/badge.svg

[main-build-image]: https://github.com/gregoranders/gradle-project-configuration/workflows/Main%20CI/badge.svg

[development-build-image]: https://github.com/gregoranders/gradle-project-configuration/workflows/Development%20CI/badge.svg

[language-image]: https://img.shields.io/github/languages/top/gregoranders/gradle-project-configuration

[languages-image]: https://img.shields.io/github/languages/count/gregoranders/gradle-project-configuration

[code-size-image]: https://img.shields.io/github/languages/code-size/gregoranders/gradle-project-configuration

[repo-size-image]: https://img.shields.io/github/repo-size/gregoranders/gradle-project-configuration

[sonarcloud-url]: https://sonarcloud.io/summary/new_code?id=gregoranders_gradle-project-configuration

[spock-url]: https://spockframework.org

[checkstyle-plugin-url]: https://docs.gradle.org/current/userguide/checkstyle_plugin.html

[pmd-plugin-url]: https://docs.gradle.org/current/userguide/pmd_plugin.html

[idea-plugin-url]: https://docs.gradle.org/current/userguide/idea_plugin.html

[jacoco-plugin-url]: https://docs.gradle.org/current/userguide/jacoco_plugin.html

[spotbugs-plugin-url]: https://github.com/spotbugs/spotbugs-gradle-plugin

[dependencycheck-plugin-url]: https://github.com/jeremylong/DependencyCheck

[maven-publish-plugin-url]: https://docs.gradle.org/current/userguide/publishing_maven.html

[signing-plugin-url]: https://docs.gradle.org/current/userguide/signing_plugin.html
