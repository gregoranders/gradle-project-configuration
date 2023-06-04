<%
  def seeMap = [:] 

  data.info.attachments.each {
    seeMap[it.name] = it.url
  }

  def stats = utils.stats( data )
  def title = utils.getSpecClassName( data )
  def specTitle = utils.specAnnotation( data, spock.lang.Title )?.value()
  def testSubject = utils.specAnnotation( data, spock.lang.Subject )?.value()
%>---
layout: default
title: ${title}
---
<%
if (specTitle) {
  specTitle.split('\n').each { out << '# ' << it << '\n' }
}

if ( data.info.narrative ) {
  out << data.info.narrative << '\n'
}%>

| Total Runs         | Success Rate                           | Failures          | Errors          | Skipped          | Total time (ms)                   |
|--------------------|----------------------------------------|-------------------|-----------------|------------------|-----------------------------------|
| ${stats.totalRuns} | ${fmt.toPercentage(stats.successRate)} | ${stats.failures} | ${stats.errors} | ${stats.skipped} | ${fmt.toTimeDuration(stats.time)} |

<%
if (testSubject) {
  out << '#### TestSubject: ' << '\n'

  testSubject.each { subject ->
    def fqn = "${subject.packageName}.${subject.simpleName}"
    def link = seeMap.find { it.value.contains(fqn.replace('.', '/')) }
    if (link) {
      out << '- ' << '[' << fqn << '](' << link.value << ')\n'
    } else {
      out << '- ' << '**' << fqn << '**\n'
    }
  }
}

    def writeTagOrAttachment = { feature ->
        def tagsByKey = feature.tags.groupBy( { t -> t.key } )
        tagsByKey.each { key, values ->
            out << '\n#### ' << key.capitalize() << 's:\n\n'
            values.each { tag ->
                out << '* [#' << tag.name << '](' << tag.url << ')\n'
            }
        }
    }
    def writePendingFeature = { pendingFeature ->
        if ( pendingFeature ) {
            out << '\n> Pending Feature\n'
        }
    }
    def writeHeaders = { headers ->
        if ( headers ) {
            headers.each { h ->
                out << '> ' << h << '\n'
            }
        }
    }
    def writeExtraInfo = { extraInfo ->
        if ( extraInfo ) {
            extraInfo.each { info ->
                out << '* ' << info << '\n'
            }
        }
    }

    writeHeaders( utils.specHeaders( data ) )
    writeTagOrAttachment data.info
%>

## Features
<%
features.eachFeature { name, result, blocks, iterations, params ->
  out << '- [' << name << '](#' << name.replace(' ', '-').toLowerCase() << ')' << '\n'
}
%>

----------------------------------

<%
    features.eachFeature { name, result, blocks, iterations, params ->
%>
# $name
<%
 writePendingFeature( featureMethod.getAnnotation( spock.lang.PendingFeature ) )
 writeTagOrAttachment( delegate )
 def iterationTimes = iterations.collect { it.time ?: 0L }
 def totalTime = fmt.toTimeDuration( iterationTimes.sum() )
%>
Result: **$result**
Time: $totalTime
<%
        for ( block in blocks ) {
 %>
* __${block.kind.replace(':', '')}__ ${block.text}
<%
          if ( block.sourceCode ) {
              out << "\n```groovy\n"
              block.sourceCode.each { codeLine ->
                  out << codeLine << '\n'
              }
              out << "```\n"
          }
        } 
%>

<%
}
%>
