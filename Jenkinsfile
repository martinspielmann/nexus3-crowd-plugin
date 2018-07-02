pipeline {
    agent any
    tools {
        jdk 'jdk10'
        maven 'M3' 
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
		script {
		    def scannerHome = tool 'sonarqube-scanner'
                    withSonarQubeEnv('sonar') {
                    	sh "${scannerHome}/bin/sonar-scanner"
                    }
		}
            }
        }
    }
}
