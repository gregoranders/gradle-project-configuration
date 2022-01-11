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
package com.github.gregoranders.gradle.project.configuration

import org.gradle.api.Project
import spock.lang.*

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
    'https://github.com/gregoranders/gradle-project-configuration/blob/main/src/main/java/com/github/gregoranders/gradle/project/configuration/ProjectConfigurationPlugin.java'
])
@Issue([
    '2'
])
class ProjectConfigurationPluginSpec extends Specification {

    @Subject
    def testSubject = new ProjectConfigurationPlugin()

    def 'should not throw an exception'() {
        given: 'a mocked gradle project'
            def project = Mock(Project)
        when: 'the unit under test is applied to it'
            testSubject.apply(project)
        then: 'no exceptions should be thrown'
            noExceptionThrown()
    }
}
