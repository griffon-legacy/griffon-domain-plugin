griffon.project.dependency.resolution = {
    inherits "global"
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        griffonHome()
        mavenCentral()
    }
    dependencies {
        compile('commons-lang:commons-lang:2.6',
                'commons-validator:commons-validator:1.4.0') {
            excludes 'commons-logging'
        }
        compile('javax.persistence:persistence-api:1.0.2')
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon.codehaus.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (@griffon.version@)"
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon'
    error 'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}
