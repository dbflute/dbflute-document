def parser = new XmlSlurper()
// avoid 503 error
parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
def oldChangelog = parser.parse('http://www.seasar.org/wiki/index.php?ChangeLog/DBFlute')
def root = oldChangelog.body.table.tr.td.div.find{ it.@id == 'body' }[0]
def releaseDate;
def currentRelease;
def releases = [] // list of Release
def type = 'change'
root.children.each {
	if (it.name == 'h2') {
		releaseDate = it.text().trim()
	} else if (it.name == 'h4') {
		release = new Release()
		release.name = it.text().trim()
		release.date = releaseDate;
		releases.add(release)
        type = 'change'
	} else if (it.name == 'p') {
        type = it.text().trim()
	} else if (it.name == 'ul') {
        it.children.each { li ->
            if (type == '{NEW}') {
                release.news.add(li.text().trim())
            } else if (type == '{BUG}') {
                release.bugs.add(li.text().trim())
            } else {
                release.changes.add(li.text().trim())
            }
        }
    }
}

def result = new File('../web/ja/environment/changelog/index.html')
def resultHtml = new groovy.xml.MarkupBuilder(result.newWriter('UTF-8'))
resultHtml.doubleQuotes = true

resultHtml.html {
    head {
        meta('http-equiv': 'text/html; charset=UTF-8')
        meta(name: 'keywords', content: 'changelog, release')
        title('Changelog')
    }
    body {
        div(id: 'content') {
            yieldUnescaped('<!-- __content-start__ -->')

            releases.findAll { !it.dotNet }.each { release ->
                div(class: 'release') {
                    h2(release.name)
                    p(class: 'releasedate', release.date)
                    if (!release.news.isEmpty()) {
                        div(class: 'new') {
                            h3('NEW')
                            ul {
                                release.news.each { li(it) }
                            }
                        }
                    }
                    if (!release.bugs.isEmpty()) {
                        div(class: 'bug') {
                            h3('BUG')
                            ul {
                                release.bugs.each { li(it) }
                            }
                        }
                    }
                    if (!release.changes.isEmpty()) {
                        div(class: 'change') {
                            h3('CHANGE')
                            ul {
                                release.changes.each { li(it) }
                            }
                        }
                    }
                }
            }

            yieldUnescaped('<!-- __content-end__ -->')
        }
    }
}

class Release {
	def name;
	def date;
	def news = [];
	def bugs = [];
	def changes = [];
    def boolean isDotNet() {
        name.contains('.NET')
    }
}

def buildChanges(parent, changes, label) {
    parent.div(class: label) {
        h3(label.toUpperCase())
        ul {
            changes.each { li(it) }
        }
    }
}
