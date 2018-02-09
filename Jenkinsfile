node {
   def mvnHome
   def scannerHome

   stage('Preparation') {
      git 'https://github.com/pingunaut/nexus3-crowd-plugin.git'
      checkout scm
      mvnHome = tool 'M3'
      scannerHome = tool 'sonarqube-scanner'
   }
   stage('Build') {
      sh "'${mvnHome}/bin/mvn' clean install"
   }
   stage('QA') {
      withSonarQubeEnv('sonar') {
        sh "${scannerHome}/bin/sonar-scanner -Dsonar.branch.name=${env.BRANCH_NAME}"
      }
   }
}
