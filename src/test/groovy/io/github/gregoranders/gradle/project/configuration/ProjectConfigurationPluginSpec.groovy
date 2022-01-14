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
package io.github.gregoranders.gradle.project.configuration

import io.github.gregoranders.gradle.project.ProjectConfigurationPlugin
import org.gradle.api.JavaVersion
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import spock.lang.*

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

@Title('Project configuration plugin')
@Narrative('''
> # As a user I would like to be able to use a [Gradle][gradle-url] [plugin][plugin-url] to configure projects.

[gradle-url]: https://gradle.org
[plugin-url]: https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html
''')
@Subject(ProjectConfigurationPlugin)
@See([
    'https://gradle.org',
    'https://docs.gradle.org/current/javadoc/org/gradle/api/Plugin.html',
    'https://github.com/gregoranders/gradle-project-configuration/blob/main/src/main/groovy/io/github/gregoranders/gradle/project/ProjectConfigurationPlugin.groovy'
])
@Issue([
    '2', '3', '5'
])
class ProjectConfigurationPluginSpec extends Specification {

    def 'should not throw an exception'() {
        given: 'a simple project path'
            def path = getClass().getResource('/simple-project')
        and: 'a gradle project'
            def project = ProjectBuilder.builder().withProjectDir(Paths.get(path.toURI()).toFile()).build()
        when: 'the unit under test is applied as a plugin'
            project.plugins.apply('io.github.gregoranders.project-configuration')
        then: 'no exceptions should be thrown'
            noExceptionThrown()
    }

    def 'should configure IDEA plugin'() {
        given: 'a simple project path'
            def path = getClass().getResource('/simple-project')
        and: 'a gradle project'
            def project = ProjectBuilder.builder().withProjectDir(Paths.get(path.toURI()).toFile()).build()
        and: 'a set of required plugins'
            project.plugins.apply('java')
            project.plugins.apply('idea')
        when: 'the unit under test is applied as a plugin'
            project.properties.ext['description'] = 'description'
            project.properties.ext['author'] = 'author'
            project.properties.ext['email'] = 'email'
            project.version = 'version'
            project.java.sourceCompatibility = JavaVersion.VERSION_1_8
            project.java.targetCompatibility = JavaVersion.VERSION_1_8
            project.plugins.apply('io.github.gregoranders.project-configuration')
        then: 'the IDEA plugin configuration was applied'
            project.extensions.idea.module.downloadJavadoc
            project.extensions.idea.module.downloadSources
            project.rootProject.idea.project.vcs == 'Git'
            project.rootProject.idea.project.jdkName == 'jdk-1.8'
            project.rootProject.idea.project.languageLevel == new IdeaLanguageLevel('1.8')
        and: 'no exceptions are thrown'
            noExceptionThrown()
    }

    def 'should configure Checkstyle plugin'() {
        given: 'a simple project path'
            def path = getClass().getResource('/simple-project')
        and: 'a gradle project'
            def project = ProjectBuilder.builder().withProjectDir(Paths.get(path.toURI()).toFile()).build()
        and: 'a set of required plugins'
            project.plugins.apply('java')
            project.plugins.apply('checkstyle')
        when: 'the unit under test is applied as a plugin'
            project.properties.ext['description'] = 'description'
            project.properties.ext['author'] = 'author'
            project.properties.ext['email'] = 'email'
            project.properties.ext['checkstyleVersion'] = '1.0.0'
            project.plugins.apply('io.github.gregoranders.project-configuration')
        then: 'the Checkstyle plugin configuration was applied'
            project.extensions.checkstyle.toolVersion == '1.0.0'
            !project.extensions.checkstyle.ignoreFailures
        and: 'no exceptions are thrown'
            noExceptionThrown()
    }

    def 'should configure PMD plugin'() {
        given: 'a simple project path'
            def path = getClass().getResource('/simple-project')
        and: 'a gradle project'
            def project = ProjectBuilder.builder().withProjectDir(Paths.get(path.toURI()).toFile()).build()
        and: 'a set of required plugins'
            project.plugins.apply('java')
            project.plugins.apply('pmd')
        when: 'the unit under test is applied as a plugin'
            project.properties.ext['description'] = 'description'
            project.properties.ext['author'] = 'author'
            project.properties.ext['email'] = 'email'
            project.properties.ext['pmdVersion'] = '1.0.0'
            project.plugins.apply('io.github.gregoranders.project-configuration')
        then: 'the PMD plugin configuration was applied'
            project.extensions.pmd.toolVersion == '1.0.0'
            project.extensions.pmd.consoleOutput
            project.extensions.pmd.incrementalAnalysis
            !project.extensions.pmd.ignoreFailures
            project.extensions.pmd.ruleSetConfig != ''
            project.tasks.cpdMain != null
        and: 'no exceptions are thrown'
            noExceptionThrown()
    }

    def 'should configure JaCoCo plugin'() {
        given: 'a simple project path'
            def path = getClass().getResource('/simple-project')
        and: 'a gradle project'
            def project = ProjectBuilder.builder().withProjectDir(Paths.get(path.toURI()).toFile()).build()
        and: 'a set of required plugins'
            project.plugins.apply('java')
            project.plugins.apply('jacoco')
        when: 'the unit under test is applied as a plugin'
            project.properties.ext['description'] = 'description'
            project.properties.ext['author'] = 'author'
            project.properties.ext['email'] = 'email'
            project.properties.ext['jacocoVersion'] = '1.0.0'
            project.plugins.apply('io.github.gregoranders.project-configuration')
        then: 'the JaCoCo plugin configuration was applied'
            project.extensions.jacoco.toolVersion == '1.0.0'
        and: 'no exceptions are thrown'
            noExceptionThrown()
    }

    def 'should build project with plugin enabled'() {
        given: 'a simple project path'
            def path = getClass().getResource('/simple-project')
        when: 'the project is build'
            def result = GradleRunner.create()
                .withProjectDir(Paths.get(path.toURI()).toFile())
                .withPluginClasspath()
                .withEnvironment([
                    'CI'            : 'true',
                    'GPG_KEY'       : 'test',
                    'GPG_PASSPHRASE': 'test',
                ])
                .withArguments(getGradleJVMArgs(), 'clean', 'check', 'build')
                .build()
        then: 'the build result contains :checkstyleMain'
            result.output.contains(':checkstyleMain')
        and: 'the build result contains :checkstyleTest SKIPPED'
            result.output.contains(':checkstyleTest SKIPPED')
        and: 'it contains :pmdMain'
            result.output.contains(':pmdMain')
        and: 'it contains :pmdTest SKIPPED'
            result.output.contains(':pmdTest SKIPPED')
        and: 'it contains :cpd'
            result.output.contains(':cpdMain')
        and: 'it contains :spotbugsMain'
            result.output.contains('spotbugsMain')
        and: 'it contains :spotbugsTest SKIPPED'
            result.output.contains('spotbugsTest SKIPPED')
        and: 'it contains :jacocoTestReport'
            result.output.contains(':jacocoTestReport')
        and: 'it contains :jacocoTestCoverageVerification'
            result.output.contains(':jacocoTestCoverageVerification')
        and: 'it contains :dependencyCheckAnalyze'
            result.output.contains(':dependencyCheckAnalyze')
        and: 'it contains :sourcesJar'
            result.output.contains(':sourcesJar')
        and: 'no exceptions are thrown'
            noExceptionThrown()
    }

    def getGradleJVMArgs() {
        "-Dorg.gradle.jvmargs=${loadJaCocoRuntimeProperties()}"
    }

    def loadJaCocoRuntimeProperties() {
        def path = getClass().getResource('/jacocoAgentJVMArgs.properties')
        def bytes = Files.readAllBytes(Paths.get(path.toURI()))
        new String(bytes, StandardCharsets.UTF_8)
    }
}
