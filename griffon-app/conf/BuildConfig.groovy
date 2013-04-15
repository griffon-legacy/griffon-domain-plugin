griffon.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        griffonHome()
        mavenCentral()
    }
    dependencies {
        compile('commons-lang:commons-lang:2.6',
                'commons-validator:commons-validator:1.4.0') {
            excludes 'commons-logging'
        }
    }
}

log4j = {
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon'
    error 'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}
