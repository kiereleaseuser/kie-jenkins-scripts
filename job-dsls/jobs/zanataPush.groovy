//Define Variables

def kieMainBranch="master"
def zanataVersion="7.8.0"
def settingsXml="org.jenkinsci.plugins.configfiles.custom.CustomConfig1457025283676"

def organization="kiegroup"
def javadk="jdk1.8"
def jaydekay="JDK1_8"
def mvnToolEnv="APACHE_MAVEN_3_3_9"
def mvnHome="${mvnToolEnv}_HOME"
def mvnOpts="-Xms2g -Xmx3g"


def zanataPushModules="""
sh \$WORKSPACE/scripts/droolsjbpm-build-bootstrap/script/zanata/zanata-pushModules.sh
"""

job("zanataPushModules-${zanataVersion}") {

    description("This job: <br> pushes the i18n files to https://vendors.zanata.redhat.com<br>IMPORTANT: Created automatically by Jenkins job DSL plugin. Do not edit manually! The changes will get lost next time the job is generated.")

    parameters {
        stringParam("kieMainBranch", "${kieMainBranch}", "please edit the name of the release branch <br> ******************************************************** <br> ")
    };
    
    scm {
        git {
            remote {
                github("${organization}/droolsjbpm-build-bootstrap")
            }
            branch ("${kieMainBranch}")
            extensions {
                relativeTargetDirectory("scripts/droolsjbpm-build-bootstrap")
            }

        }
    }

    label("kie-releases")

    logRotator {
        numToKeep(10)
    }

    triggers {
        cron("H 2 * * 0,2,4,6")
    }

    jdk("${javadk}")

    wrappers {
        timeout {
            absolute(60)
        }
        timestamps()
        preBuildCleanup()
        colorizeOutput()
        toolenv("${mvnToolEnv}", "${jaydekay}")
        configFiles {
            mavenSettings("${settingsXml}") {
                variable("ZANATA")
            }
        }
    }

    configure { project ->
        project / 'buildWrappers' << 'org.jenkinsci.plugins.proccleaner.PreBuildCleanup' {
            cleaner(class: 'org.jenkinsci.plugins.proccleaner.PsCleaner') {
                killerType 'org.jenkinsci.plugins.proccleaner.PsAllKiller'
                killer(class: 'org.jenkinsci.plugins.proccleaner.PsAllKiller')
                username 'jenkins'
            }
        }
    }

    publishers {
        mailer('mbiarnes@redhat.com', false, false)
    }

    steps {
        environmentVariables {
            envs(MAVEN_OPTS : "${mvnOpts}", MAVEN_HOME : "\$${mvnHome}", MAVEN_REPO_LOCAL : "/home/jenkins/.m2/repository", PATH : "\$${mvnHome}/bin:\$PATH")
        }
        shell(zanataPushModules)
    }
}
