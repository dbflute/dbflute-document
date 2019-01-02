def contentsRoot = new File("../web");
def output = new File("output");

def htmlTemplate = new groovy.text.SimpleTemplateEngine().createTemplate(new File('template.html').newReader("UTF-8"))
def sidemenuTemplate = null
def antBuilder = new AntBuilder()

// later wins
def containerClassMap = [
    'introduction': 'ct-introduction',
    'environment': 'ct-environment',
    'environment/setup': 'ct-setup',
    'environment/upgrade': 'ct-upgrade',
    'tutorial': 'ct-tutorial',
    'tutorial/architect.html': 'ct-architect',
    'tutorial/developer.html': 'ct-developer',
    'manual': 'ct-manual',
];

class SitemapManager {
    def getHref(file) {
        './' + file.path.substring(10) // depends on "web" directory name by jflute (2019/01/02)
    }

    def getLinkText(file) {
        def m = (file.getText('UTF-8') =~ /(?s).*<h1>(.*)<\/h1>.*/)
        if (m.matches()) m[0][1]
        else 'TODO jflute'
    }

    def recurse(dir, buf) {
        def hasIndex = false
        dir.listFiles().findAll{
            !(it.name == '.svn' || it.name == 'sidemenu.html')
        }.sort{ a, b ->
            if (a.isDirectory()) {
                if (b.isFile()) {
                    return 1
                } else {
                    return a.name <=> b.name
                }
            } else if (b.isDirectory()) {
                return -1
            } else if (a.name == 'index.html') {
                return -1
            } else if (b.name == 'index.html') {
                return 1
            } else {
                a.name <=> b.name
            }
        }.each {
            if (it.isFile()) {
                buf += '<li><a href="' + getHref(it) + '">' + getLinkText(it) + '</a>'
                if (it.name == 'index.html' && 0 < it.parentFile.listFiles().findAll{it.isFile()}.size) {
                    buf += '\n<ul>\n'
                    hasIndex = true
                } else {
                    buf += '</li>\n'
                }
            } else if (it.name != '.svn') {
                if (1 < it.listFiles().findAll{ it.isFile() }.size &&
                        0 == it.listFiles().findAll{ it.name == 'index.html' }.size) {
                    buf += "<li>TODO jflute (${getHref(it)}/index.html)</li>\n"
                    buf += '<ul>\n'
                    hasIndex = true
                }
                buf = recurse(it, buf)
                //if (0 < it.listFiles().findAll{ it.isFile() }.size) {
                    //buf += '</ul></li>\n'
                //}
            }
        }
        if (hasIndex) buf += '</ul></li>\n'
        buf
    }

    def getCategoryList(category) {
        def root = new File("../web/ja/${category}")
        def buf = '<ul>\n'
        buf = recurse(root, buf)
        buf += '\t\t</ul>'
    }

    def getEnvironmentList() {
        getCategoryList('environment')
    }
    def getIntroductionList() {
        getCategoryList('introduction')
    }
    def getManualList() {
        getCategoryList('manual')
    }
    def getTutorialList() {
        getCategoryList('tutorial')
    }
    def getLastaFluteList() {
        getCategoryList('lastaflute')
    }
}

def sitemapManager = new SitemapManager()

/**
 * traverse file and directories recursively.
 * @breadcrumbs list to push directory.
 * @m called if isFile() == true. m(file, breadcrumbs)
 */
def traverse(breadcrumbs, Closure m) {
    breadcrumbs.last().listFiles().sort{ [it.name == 'sidemenu.html', it.isFile()] }.each {
        if (it.isFile()) {
            if (it.name == 'sitemap.html') return
            m(it, breadcrumbs)
        } else if (it.name != '.svn') {
            breadcrumbs.push(it)
            traverse(breadcrumbs, m)
            breadcrumbs.pop()
        }
    }
}

/**
 * read sidemenu content.
 */
def parseSidemenuHtml(html) {
    def text = html.getText()
    def m = (text =~ /(?s).*<div id="sidebar"><!-- __sidemenu-start__ -->\s*(.*)\s*<!-- __sidemenu-end__ --><\/div>.*/)
    return new groovy.text.SimpleTemplateEngine().createTemplate(m.matches() ? m[0][1] : '')
}

/**
 * return matched text.
 */
def getMatched(text, regex) {
    def m = (text =~ regex)
    if (m.matches()) m[0][1]
    else ''
}

/**
 * builds index list tree and return it.
 */
def buildIndexList(text) {
    def m = (text =~ /<h([23])\s+id=['"]?(\w+)['"]?.*?>\s*(.*?)\s*<\/h\1>/)
    if (m.find()) {
        def val = '<ul class="indexlist">'
        def preLevel = '2'
        m.each {
            if (it[1] == '2') {
                if (preLevel == '3') {
                    val += '\r\n\t\t\t</ul>\r\n\t\t</li>'
                }
                val += "\r\n\t\t<li><a href=\"#${it[2]}\">${it[3]}</a></li>"
            } else {
                if (preLevel == '2') {
                    val = val.substring(0, val.length() - 5)
                    val += '\r\n\t\t\t<ul>'
                }
                val += "\r\n\t\t\t\t<li><a href=\"#${it[2]}\">${it[3]}</a></li>"
            }
            preLevel = it[1]
        }
        if (preLevel == '3') {
            val += '\r\n\t\t\t</ul>\r\n\t\t</li>'
        }
        val += '\r\n\t</ul>'
    } else ''
}

/**
 * Replace A to SPAN if href is dead link.
 * @param text contents
 * @param baseFile Current file. Used to resolve relative path.
 */
def markDeadLink(content, baseFile) {
    content.replaceAll(/(<a\s+[^>]*href=['"](([^#'"]*)(#(\S*))?)['"][^>]*>)(.*?)<\/a>/) { original, tag, href, path, anchor, name, text ->
        if (isDeadLink(path, name, baseFile)) {
            "<span class=\"working\"><!-- ${tag} -->${text}<!-- </a> --></span>"
        } else {
            original
        }
    }
}

def isDeadLink(path, name, baseFile) {
    // http:// ftp:// 等から始まる場合URLとみなす
    if (path ==~ /^\w+:\/\/.*/) {
        return false
    } else {
		if (!path) {
			file = baseFile
		} else {
        	file = new File(baseFile.parentFile, path)
        	if (file.isDirectory()) {
            	file = new File(file, 'index.html')
        	}
		}
        !(isFileExist(file) && !hasTodo(file) && isAnchorExists(file, name))
    }
}

def isFileExist(file) {
    file.exists()
}

def hasTodo(file) {
    file.eachLine { line ->
        if (0 < line.indexOf('TODO')) return true
    }
    return false
}

def isAnchorExists(file, name) {
    if (name == null) {
        return true
    }
    exists = false
    file.eachLine { line ->
        if (line ==~ /.*((name=['"]${name}['"])|(id=['"]${name}['"])).*/) {
            exists = true
        }
    }
    return exists
}

def replaceSitemap(content, sitemapManager) {
    content = content.replace('${environmentlist}', sitemapManager.environmentList)
    content = content.replace('${introductionlist}', sitemapManager.introductionList)
    content = content.replace('${manuallist}', sitemapManager.manualList)
    content = content.replace('${tutoriallist}', sitemapManager.tutorialList)
    return content
}

/* START */

if (output.exists()) output.deleteDir()
def stack = new java.util.Stack()
stack.push(contentsRoot)

def create = { file, breadcrumbs ->
    def pathname = breadcrumbs*.name.join('/') + '/' + file.name
    println pathname

    def outputFile = new File(output.getPath() + '/' + file.getAbsolutePath().substring(contentsRoot.getAbsolutePath().length()))
    if (!outputFile.getParentFile().exists()) {
        outputFile.getParentFile().mkdirs()
    }

    if (!file.name.endsWith('.html')) {
        return antBuilder.copy(file: file, tofile: outputFile)
    } else if (file.name == 'sidemenu.html') {
        sidemenuTemplate = parseSidemenuHtml(file)
        return
    }

    def map = [keywords: '', title: 'DBFlute', content: '', contextRoot: '.', subCss: '/css/sub.css', containerClass: 'ct-default'];
    def text = file.getText()

    map.keywords = getMatched(text, /(?s).*<meta\s+name="keywords"\s+content="(.*?)"\s*\/?>.*/)
    println map.keywords
    map.title = getMatched(text, /(?s).*<title>(.*)<\/title>.*/)
    map.content = getMatched(text, /(?s).*<div id="content"><!-- __content-start__ -->\r?\n?(.*)\s*<!-- __content-end__ -->\s*<\/div>.*/)

	if (file.getPath().contains('lastaflute')) {
	    map.subCss = '/css/lasub.css'
	}

	if (!map.content) {
		return antBuilder.copy(file: file, tofile: outputFile)
	}

    // DBFLUTE-634
    map.content = markDeadLink(map.content, file)

    // DBFLUTE-564
    map.content = map.content.replace('${indexlist}', buildIndexList(map.content))

    // DBFLUTE-686
    if (file.name == 'sitemap.html') {
        map.content = replaceSitemap(map.content, sitemapManager)
    }

    containerClassMap.each {
        if (pathname.contains(it.key)) map.containerClass = it.value
    }

    (1 ..< breadcrumbs.size()).each {
        map.contextRoot = map.contextRoot.length() == 1 ? ".." : map.contextRoot + "/.."
    }

    map.put('sidemenu', sidemenuTemplate != null ? sidemenuTemplate.make(map).toString() : '')

    outputFile.withWriter("UTF-8") {
        it.write(htmlTemplate.make(map).toString())
    }
}

traverse(stack, create)
stack.push(stack.last().listFiles().find{ it.name == 'ja' })
create(new File('../web/ja/sitemap.html'), stack)
