
class Anchor {
	def href
    def target
	def exists
    def file
	public boolean isExternal() {
		href ==~ /^\w+:\/\/.*/
	}
}

class Document {
    def location
    def name
	def anchors = []
	def todo

	def Document(File file) {
        location = file.parentFile.path
        name = file.name
		parse(file)
	}

	def parse(file) {
		def text = file.getText()
		def content
		def t = (file.text =~ /(?s).*<div id="content"><!-- __content-start__ -->\r?\n?(.*)\s*<!-- __content-end__ -->\s*<\/div>.*/)
		if (t.matches()) {
			content = t[0][1]
		} else {
			content = ''
		}

        (content =~ /<a\s+[^>]*href=['"](([^#'"]*)(#(\S*))?)['"][^>]*>/).each {
			def a = new Anchor()
			a.href = it[1]
            if (it[1] ==~ /^#.*/) a.href = file.name + it[1]
			else if (it[1] ==~ /(\.?\.\/)*[^\.]+$/) a.href = "${it[1]}/index.html"

			def m = (a.href =~ /(\.\/)?([^#]*)#(.*)/)
			if (m.matches()) {
				def f = new File(file.parentFile, m[0][2])
                a.file = f
				if (f.exists()) {
					if (f.isDirectory()) {
						throw new RuntimeException("${file.path}: href is invalid \"${a.href}\"")
					}
					a.exists = (f.getText() ==~ /(?s).*id=['"]${m[0][3]}['"].*/)
				} else {
					a.exists = false
				}
			} else {
                a.file = new File(file.parentFile, a.href)
				a.exists = a.file.exists()
			}
			anchors << a
		}
		todo = (text ==~ /(?s).*<div id="content"><!-- __content-start__ -->.*((href=['"]#['"])|(TODO jflute)).*<!-- __content-end__ --><\/div>.*/)
	}
}

def countSupporsedHtml(list) {
	def result = []

	list.each {

	}
}

def linkMark(anchor) {
	if (anchor.external) '-'
	else if (anchor.exists) 'o'
	else if (anchor.file.exists()) 'v'
	else 'x'
}

/** START */

def contentsRoot = new File("../web");
def documents = []

contentsRoot.eachFileRecurse {
	if (it.isDirectory() || !it.name.endsWith('.html')) {
		return
	}
	documents << new Document(it)
}

def supposedSet = [] as Set
documents.each {doc->
    doc.anchors.each {
        supposedSet << it.file.toURI().normalize()
    }
}

def manage = new File('../web/manage.txt')
if (manage.exists()) manage.delete()

manage.withWriter {
    it.println " existing html = ${documents.size()} (todo: ${documents.findAll{it.todo}.size})"
	it.println "    valid href = ${documents.sum{it.anchors.findAll{a->a.exists}.size}} (total: ${documents.sum{it.anchors.findAll{!it.external}.size}})"
    it.println " supposed html = ${supposedSet.findAll{it.path ==~ /.*\.html$/}.size()}"
	it.println ''
	documents.sort{ a, b ->
		def val = a.location.compareTo(b.location)
		if (val == 0) val = a.name.compareTo(b.name)
		val
    }.each {doc->
		it.println "${!doc.todo ? 'o' : 'x'} .${doc.location.substring(11)}/${doc.name}\t${doc.anchors.findAll{it.exists}.size}/${doc.anchors.findAll{!it.external}.size}"
		doc.anchors.each {a->
			it.println "\t${linkMark(a)} ${a.href}"
		}
	}
}

