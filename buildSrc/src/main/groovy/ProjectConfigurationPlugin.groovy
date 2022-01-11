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
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskAction

class ProjectConfigurationPlugin implements Plugin<Project> {

    static class CPDTask extends DefaultTask {

        @TaskAction
        def cpd() {
            File outDir = project.file("${project.buildDir}/reports/cpd")
            outDir.mkdirs()
            ant.taskdef(name: 'cpd', classname: 'net.sourceforge.pmd.cpd.CPDTask',
                classpath: project.configurations.pmd.asPath)
            ant.cpd(minimumTokenCount: '80', format: 'xml',
                outputFile: new File(outDir, 'main.xml')) {
                ant.fileset(dir: 'src/main/java') {
                    include(name: '**/*.java')
                }
            }
        }
    }

    @Override
    void apply(Project projectInternal) {
        processJava(projectInternal)
        processJavaLibrary(projectInternal)
        processPublish(projectInternal)
        processIDEA(projectInternal)
        processModule(projectInternal)
        processJaCoCo(projectInternal)
        processCheckStyle(projectInternal)
        processPMD(projectInternal)
        processSpotBugs(projectInternal)
        processDependencyCheck(projectInternal)
    }

    def processJava(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('java')) {
            projectInternal.test {
                useJUnitPlatform()
                testLogging {
                    events 'passed', 'skipped', 'failed'
                }
            }
        }
    }

    def processJavaLibrary(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('java-library')) {
            projectInternal.java {
                withSourcesJar()
            }
        }
    }

    def processPublish(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('maven-publish')) {
            projectInternal.publishing {
                publications {
                    //noinspection GroovyAssignabilityCheck
                    mavenJava(MavenPublication) {
                        //noinspection GroovyAssignabilityCheck
                        from projectInternal.components.java
                        pom {
                            //noinspection GroovyAssignabilityCheck
                            name = projectInternal.name
                            description = projectInternal.description
                            url = projectInternal.property('url')
                            licenses {
                                //noinspection GroovyAssignabilityCheck
                                license {
                                    name = projectInternal.property('license')
                                    url = projectInternal.property('licenseUrl')
                                }
                            }
                            developers {
                                developer {
                                    //noinspection GroovyAssignabilityCheck
                                    id = projectInternal.property('authorId')
                                    name = projectInternal.property('author')
                                    email = projectInternal.property('email')
                                }
                            }
                            scm {
                                //noinspection GroovyAssignabilityCheck
                                connection = "scm:git:git://${projectInternal.property('scmUrl')}"
                                //noinspection GroovyAssignabilityCheck
                                developerConnection = "scm:git:ssh://${projectInternal.property('scmUrl')}"
                                url = projectInternal.property('url')
                            }
                        }
                    }
                }

                if (System.getenv('CI') == 'true') {
                    repositories {
                        maven {
                            name = 'GitHubPackages'
                            url = projectInternal.rootProject.property('ghpUrl')
                            credentials {
                                username = System.getenv('GITHUB_ACTOR')
                                password = System.getenv('GITHUB_TOKEN')
                            }
                        }
                    }
                } else {
                    repositories {
                        maven {
                            url = projectInternal.layout.buildDirectory.dir('repos')
                        }
                    }
                }
            }
        }

        if (projectInternal.plugins.hasPlugin('signing')) {
            projectInternal.signing {
                if (System.getenv('GPG_KEY') && System.getenv('GPG_PASSPHRASE')) {
                    useInMemoryPgpKeys(System.getenv('GPG_KEY'), System.getenv('GPG_PASSPHRASE'))
                    sign projectInternal.publishing.publications
                }
            }
        }
    }

    def processIDEA(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('idea')) {
            projectInternal.idea {
                module {
                    downloadJavadoc = true
                    downloadSources = true
                }
            }
        }
    }

    def processModule(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('org.javamodularity.moduleplugin')) {
            projectInternal.test {
                moduleOptions {
                    runOnClasspath = false
                }
            }
        }
    }

    def processJaCoCo(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('jacoco')) {
            projectInternal.jacoco {
                toolVersion = projectInternal.rootProject.property('jacocoVersion')
                reportsDirectory = projectInternal.layout.buildDirectory.dir('reports/jacoco').get()
            }
            projectInternal.jacocoTestReport {
                dependsOn projectInternal.tasks.test
                reports {
                    xml.required = true
                    xml.destination projectInternal.layout.buildDirectory.file('reports/jacoco/main.xml').get().asFile
                    csv.required = false
                    html.destination projectInternal.layout.buildDirectory.dir('reports/jacoco/html').get().asFile
                }
            }
            projectInternal.jacocoTestCoverageVerification {
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
            projectInternal.test {
                jacoco {
                    destinationFile = projectInternal.layout.buildDirectory.file('results/jacoco/main.exec').get().asFile
                    classDumpDir = projectInternal.layout.buildDirectory.dir('results/classpathdumps').get().asFile
                }
                finalizedBy projectInternal.jacocoTestReport
            }
            projectInternal.check {
                dependsOn projectInternal.jacocoTestCoverageVerification
            }
        }
    }

    def processCheckStyle(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('checkstyle')) {
            projectInternal.checkstyle {
                toolVersion = projectInternal.rootProject.property('checkstyleVersion')
                ignoreFailures = false
                configProperties = [
                    'checkstyle.header.file'      : projectInternal.rootProject.file('config/checkstyle/checkstyle-header.txt'),
                    'checkstyle.suppressions.file': projectInternal.rootProject.file('config/checkstyle/checkstyle-suppressions.xml'),
                ]
            }
        }
    }

    def processPMD(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('pmd')) {
            //noinspection GroovyUnusedAssignment
            def cpdTask = projectInternal.tasks.register('cpd', CPDTask)
            projectInternal.pmd {
                consoleOutput = true
                incrementalAnalysis = true
                toolVersion = projectInternal.rootProject.property('pmdVersion')
                ignoreFailures = false
                ruleSets = []
                ruleSetConfig = projectInternal.rootProject.resources.text.fromFile('config/pmd/pmd-rules.xml')
            }
            projectInternal.check.configure {
                dependsOn projectInternal.tasks.cpd
            }
        }
    }

    def processSpotBugs(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('com.github.spotbugs')) {
            projectInternal.spotbugs {
                toolVersion = projectInternal.rootProject.property('spotbugsVersion')
                ignoreFailures = false
                effort = 'max'
                reportLevel = 'low'
                excludeFilter = projectInternal.rootProject.file('config/spotbugs/excludeFilter.xml')
            }
            projectInternal.spotbugsMain {
                reports {
                    html {
                        outputLocation = projectInternal.layout.buildDirectory.file('reports/spotbugs/main/spotbugs.html').get().asFile
                        stylesheet = 'fancy-hist.xsl'
                    }
                    xml {
                        outputLocation = projectInternal.layout.buildDirectory.file('reports/spotbugs/main/spotbugs.xml').get().asFile
                    }
                }
            }
            projectInternal.spotbugsTest.enabled = false
        }
    }

    def processDependencyCheck(Project projectInternal) {
        if (projectInternal.plugins.hasPlugin('org.owasp.dependencycheck')) {
            projectInternal.dependencyCheck {
                autoUpdate = true
                failBuildOnCVSS = 1
                cveValidForHours = 1
                format = 'ALL'
                outputDirectory = projectInternal.layout.buildDirectory.dir('reports/dependency-check').get().asFile
                suppressionFile = projectInternal.rootProject.file('config/dependency-check/dependency-check-suppressions.xml')
                //noinspection GroovyAssignabilityCheck
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
                projectInternal.check.configure {
                    dependsOn projectInternal.dependencyCheckAnalyze
                }
            }
        }
    }
}
