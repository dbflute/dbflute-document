new File('../web').eachFileRecurse{if (it.name ==~ /\..*\.html$/) it.delete()}
