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
        if ( feature.attachments.size > 0 ) {
            out << '\n#### ' << 'See:' << '\n\n'
            feature.attachments.each { value ->
                if (value.name.contains('src/main')) {
                  out << '* [' << value.name.replaceAll('.*main\\/java\\/', '').replaceAll('\\/', '.').replaceAll('\\.java$', '') << '](' << value.url << ')\n'
                } else {
                  out << '* [' << value.name << '](' << value.url << ')\n'
                }
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
 if (result != "IGNORED") {
      if ( utils.isUnrolled( delegate ) ) {
          writeExtraInfo( utils.nextSpecExtraInfo( data ) )
      } else {
          // collapse all iterations
          (1..iterations.size()).each {
              writeExtraInfo( utils.nextSpecExtraInfo( data ) )
          }
     }
 }
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
        def executedIterations = iterations.findAll { it.dataValues || it.errors }
        
        if ( params && executedIterations ) {
            def iterationReportedTimes = executedIterations.collect { it.time ?: 0L }
                        .collect { fmt.toTimeDuration( it ) }
            def maxTimeLength = iterationReportedTimes.collect { it.size() }.sort().last()
 %>
 | ${params.join( ' | ' )} | ${' ' * maxTimeLength} |
 |${params.collect { ( '-' * ( it.size() + 2 ) ) + '|' }.join()}${'-' * ( maxTimeLength + 2 )}|
<%
            executedIterations.eachWithIndex { iteration, i -> 
%> | ${( iteration.dataValues + [ iterationReportedTimes[ i ] ] ).join( ' | ' )} | ${iteration.errors ? '(FAIL)' : '(PASS)'}
<%          }
        }
        def problems = executedIterations.findAll { it.errors }
        if ( problems ) {
            out << "\nThe following problems occurred:\n\n"
            for ( badIteration in problems ) {
                if ( badIteration.dataValues ) {
                    out << '* ' << badIteration.dataValues << '\n'
                }
                for ( error in badIteration.errors ) {
                    out << '```\n' << error << '\n```\n'
                }
            }
        }
    }
 %>
