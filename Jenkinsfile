pipeline {
    agent any
    tools {
	jdk 'jdk10'
        maven 'apache-maven-3.0.1' 
    }
    stages {
        stage('Preparation') {
            steps {
		git 'https://github.com/pingunaut/nexus3-crowd-plugin.git'
		checkout scm
            }
        }
        stage('Build') {
            steps {
		sh "mvn clean install"
            }
        }
        stage('QA') {
            steps {
		def scannerHome = tool 'sonarqube-scanner'
                withSonarQubeEnv('sonar') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }
            }
        }
    }
}
