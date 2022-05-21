/*
 * MIT License
 *
 * Copyright (c) 2022 Gregor Anders
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.gregoranders.gradle.project

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class ProjectConfigurationPlugin implements Plugin<Project> {

    @Override
    void apply(Project internalProject) {
        applyJava(internalProject)
        applyIDEA(internalProject)
        applyCheckStyle(internalProject)
        applyPMD(internalProject)
        applySpotBugs(internalProject)
        applyJaCoCo(internalProject)
        applyDependencyCheck(internalProject)
        applyMavenPublish(internalProject)
        applySigning(internalProject)
    }

    def applyJava(Project internalProject) {
        if (internalProject.plugins.hasPlugin('java')) {
            internalProject.jar {
                def manifestAttributes = [
                    'Created-By'              : "Gradle ${internalProject.gradle.gradleVersion}",
                    'Specification-Title'     : "${internalProject.property('description')}",
                    'Specification-Version'   : internalProject.version,
                    'Specification-Vendor'    : "${internalProject.property('author')} <${internalProject.property('email')}>",
                    'Implementation-Title'    : "${internalProject.property('description')}",
                    'Implementation-Version'  : internalProject.version,
                    'Implementation-Vendor'   : "${internalProject.property('author')} <${internalProject.property('email')}>",
                    'Implementation-Vendor-Id': "${internalProject.property('author')} <${internalProject.property('email')}>",
                ]
                manifest {
                    attributes(manifestAttributes)
                }
            }
            internalProject.java {
                withSourcesJar()
            }
            internalProject.test {
                useJUnitPlatform()
                testLogging {
                    events 'passed', 'skipped', 'failed'
                    showExceptions true
                    showCauses true
                    showStackTraces true
                }
            }
        }
    }

    def applyIDEA(Project internalProject) {
        if (internalProject.plugins.hasPlugin('idea')) {
            internalProject.rootProject.idea {
                project {
                    vcs = 'Git'
                    jdkName = "jdk-${internalProject.java.sourceCompatibility}"
                    languageLevel = internalProject.java.targetCompatibility
                }
            }
            internalProject.idea {
                module {
                    downloadJavadoc = true
                    downloadSources = true
                }
            }
        }
    }

    def applyCheckStyle(Project internalProject) {
        if (internalProject.plugins.hasPlugin('checkstyle')) {
            internalProject.checkstyle {
                toolVersion = internalProject.rootProject.property('checkstyleVersion')
                ignoreFailures = false
            }
            internalProject.tasks.checkstyleTest.enabled = false
        }
    }

    def applyPMD(Project internalProject) {
        if (internalProject.plugins.hasPlugin('pmd')) {
            internalProject.pmd {
                toolVersion = internalProject.rootProject.property('pmdVersion')
                ignoreFailures = false
                consoleOutput = true
                incrementalAnalysis = true
                ruleSets = []
                ruleSetConfig = internalProject.rootProject.resources.text.fromFile('config/pmd/pmd-rules.xml')
            }
            internalProject.tasks.register('cpdMain', CPDTask)
            internalProject.tasks.pmdTest.enabled = false
            internalProject.check {
                dependsOn internalProject.tasks.cpdMain
            }
        }
    }

    def applySpotBugs(Project internalProject) {
        if (internalProject.plugins.hasPlugin('com.github.spotbugs')) {
            internalProject.spotbugs {
                toolVersion = internalProject.rootProject.property('spotbugsVersion')
                ignoreFailures = false
                effort = 'max'
                reportLevel = 'low'
                excludeFilter = internalProject.rootProject.file('config/spotbugs/excludeFilter.xml')
            }
            internalProject.spotbugsMain {
                reports {
                    html {
                        outputLocation = internalProject.layout.buildDirectory.file('reports/spotbugs/main/spotbugs.html').get().asFile
                        stylesheet = 'fancy-hist.xsl'
                    }
                    xml {
                        outputLocation = internalProject.layout.buildDirectory.file('reports/spotbugs/main/spotbugs.xml').get().asFile
                    }
                }
            }
            internalProject.tasks.spotbugsTest.enabled = false
        }
    }

    def applyJaCoCo(Project internalProject) {
        if (internalProject.plugins.hasPlugin('jacoco')) {
            internalProject.jacoco {
                toolVersion = internalProject.rootProject.property('jacocoVersion')
            }
            internalProject.jacocoTestReport {
                dependsOn internalProject.project.tasks.test
                reports {
                    xml.required = true
                    csv.required = false
                    html.required = true
                }
            }
            internalProject.jacocoTestCoverageVerification {
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
            internalProject.test {
                finalizedBy internalProject.tasks.jacocoTestReport
            }
            internalProject.check {
                dependsOn internalProject.tasks.jacocoTestCoverageVerification
            }
        }
    }

    def applyDependencyCheck(Project internalProject) {
        if (internalProject.plugins.hasPlugin('org.owasp.dependencycheck')) {
            internalProject.dependencyCheck {
                autoUpdate = true
                failBuildOnCVSS = 1
                cveValidForHours = 1
                format = 'ALL'
                outputDirectory = internalProject.layout.buildDirectory.dir('reports/dependency-check').get().asFile
                suppressionFile = internalProject.rootProject.file('config/dependency-check/suppressions.xml')
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
                internalProject.check {
                    dependsOn internalProject.tasks.dependencyCheckAnalyze
                }
            }
        }
    }

    def applyMavenPublish(Project internalProject) {
        if (internalProject.plugins.hasPlugin('maven-publish')) {
            internalProject.publishing {
                publications {
                    mavenJava(MavenPublication) {
                        from internalProject.components.java
                        pom {
                            name = internalProject.name
                            description = internalProject.description
                            url = internalProject.property('url')
                            licenses {
                                license {
                                    name = internalProject.property('license')
                                    url = internalProject.property('licenseUrl')
                                }
                            }
                            developers {
                                developer {
                                    id = internalProject.property('authorId')
                                    name = internalProject.property('author')
                                    email = internalProject.property('email')
                                }
                            }
                            scm {
                                connection = "scm:git:git://${internalProject.property('scmUrl')}"
                                developerConnection = "scm:git:ssh://${internalProject.property('scmUrl')}"
                                url = internalProject.property('url')
                            }
                        }
                    }
                }
                repositories {
                    maven {
                        name = 'Build'
                        url = internalProject.layout.buildDirectory.dir('repos')
                    }
                }
            }
        }
    }

    def applySigning(Project internalProject) {
        if (internalProject.plugins.hasPlugin('signing')) {
            if (System.getenv('GPG_KEY') && System.getenv('GPG_PASSPHRASE')) {
                internalProject.signing {
                    useInMemoryPgpKeys(System.getenv('GPG_KEY'), System.getenv('GPG_PASSPHRASE'))
                    if (internalProject.plugins.hasPlugin('maven-publish')) {
                        sign internalProject.publishing.publications
                    }
                }
            }
        }
    }
}
