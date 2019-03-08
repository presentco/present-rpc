group = GROUP
version = VERSION_NAME

task sourcesJar(type: Jar) {
    from sourceSets.main.allJava
    archiveClassifier = 'sources'
}

task javadocJar(type: Jar) {
    from javadoc
    archiveClassifier = 'javadoc'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            artifactId = project.name
            from components.java
            artifact sourcesJar
            artifact javadocJar
            versionMapping {
                usage('java-api') {
                    fromResolutionOf('runtimeClasspath')
                }
                usage('java-runtime') {
                    fromResolutionResult()
                }
            }
            pom {
                name = project.name
                description = project.description
                url = 'http://github.com/presentco/present-rpc'
                licenses {
                    license {
                        name = 'The Apache License, Version 2.0'
                        url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }
                developers {
                    developer {
                        id = 'present'
                        name = 'Present Company'
                        email = 'hello@present.co'
                    }
                }
                scm {
                    connection = 'scm:git:git@github.com:presentco/present-rpc.git'
                    developerConnection = 'scm:git:git@github.com:presentco/present-rpc.git'
                    url = 'http://github.com/presentco/present-rpc'
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            def releasesRepoUrl = "$buildDir/repos/releases"
            def snapshotsRepoUrl = "$buildDir/repos/snapshots"
            url = version.endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl
        }
    }
}

//signing {
//  sign publishing.publications.mavenJava
//}

javadoc {
    if (JavaVersion.current().isJava9Compatible()) {
        options.addBooleanOption('html5', true)
    }
}
