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

import groovy.xml.XmlSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class CPDTask extends DefaultTask {

    CPDTask() {
        super()
        setDescription("Run copy/paste analysis for main classes")
        setGroup("verification")
    }

    @TaskAction
    def cpd() {
        def cpdMinimumTokenCount = project.rootProject.hasProperty('cpdMinimumTokenCount') ? project.rootProject.property('cpdMinimumTokenCount') : 10
        def reportPath = project.file("${project.buildDir}/reports/cpd")
        reportPath.mkdirs()
        def outputFile = project.file( "${reportPath}/main.xml")
        ant.taskdef(name: 'cpd', classname: 'net.sourceforge.pmd.cpd.CPDTask',
            classpath: project.configurations.pmd.asPath)
        ant.cpd(minimumTokenCount: cpdMinimumTokenCount,
            ignoreIdentifiers: 'true',
            ignoreLiterals: 'true',
            ignoreAnnotations: 'true',
            language: 'java',
            format: 'xml',
            outputFile: outputFile) {
            ant.fileset(dir: 'src/main/java') {
                include(name: '**/*.java')
            }
        }
        def rootNode = new XmlSlurper().parse(outputFile)
        if (rootNode.duplication && rootNode.duplication.size() > 0) {
            def duplications = rootNode.duplication.size()
            def duplicationsAsString = duplications == 1 ? 'duplication' : 'duplications'
            throw new GradleException("Copy/Paste analysis found ${duplications} ${duplicationsAsString}. See the report at: file:///${outputFile.toString()}")
        }
    }
}
