String getChangeString() {
    String changeString = ""

    currentBuild.changeSets.each { changeLogEntries ->
        changeLogEntries.each { changeLogEntry ->
            changeString += "- " + changeLogEntry.msg + " [" + changeLogEntry.author + "]" + "\n"
        }
    }

    return changeString
}

pipeline {
    agent any

    stages {
        stage("Clean") {
            steps {
                withMaven(maven: "maven-3") {
                    sh "mvn clean"
                }
            }
        }

        stage("Package") {
            steps {
                withMaven(maven: "maven-3") {
                    sh "mvn package"
                }
            }
        }

         stage("Publish") {
            steps {
                sshPublisher(
                        failOnError: true,
                        publishers: [
                                sshPublisherDesc(
                                        configName: "Areson",
                                        transfers: [
                                                sshTransfer(
                                                        sourceFiles: "**/*.jar",
                                                        remoteDirectory: "/home/minecraft/test/plugins/",
                                                        flatten: true,
                                                        excludes: "**/*original*.jar"
                                                )
                                        ]
                                )
                        ]
                )
            }
        }

    }

    post {
        always {
            discordSend(
                    webhookURL: "https://discordapp.com/api/webhooks/771495644971401246/k7Q2O_kEUI4MQqoLVLRCk5IBmUNTFGmtT1UfQdW798cmH48qkc2VAUyYWQwAs4OFiC9E",
                    result: currentBuild.currentResult,
                    title: currentBuild.fullDisplayName,
                    description: getChangeString(),
                    footer: readMavenPom().getVersion(),
                    link: RUN_DISPLAY_URL
            )
        }
    }
}
