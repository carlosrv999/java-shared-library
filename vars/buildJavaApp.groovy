def call(Map pipelineParams) {
  pipeline {
    environment {
      dockerImage = ''
      registry = "testproject-276315/test-jenkins"
      registryCredential = 'gcr-credentials'
    }
    agent any

    tools {
      // Install the Maven version configured as "M3" and add it to the path.
      maven "M3"
    }

    stages {
      stage('Build') {
        steps {
          // Get some code from a GitHub repository
          git pipelineParams.gitrepo

          // Run Maven on a Unix agent.
          sh "mvn -Dmaven.test.failure.ignore=true clean package"
          sh "printenv | sort"
          // To run Maven on a Windows agent, use
          // bat "mvn -Dmaven.test.failure.ignore=true clean package"
        }

        post {
          // If Maven was able to run the tests, even if some of the test
          // failed, record the test results and archive the jar file.
          success {
              junit '**/target/surefire-reports/TEST-*.xml'
              archiveArtifacts 'target/*.jar'
          }
        }
      }
      stage('Building image') {
        steps{
          script {
            dockerImage = docker.build registry + ":$BUILD_NUMBER"
          }
        }
      }
      stage('Send to GCR'){
        steps{
          script {
            docker.withRegistry('https://gcr.io', registryCredential ) {
              dockerImage.push()
            }
          }
        }
      }
    }
  }
}
