def call() {
    node {
        stage('Build') {
            steps {
                git url: "https://github.com/carlosrv999/java-sample.git"
                sh "mvn -Dmaven.test.failure.ignore=true clean package"

            }

            post {
                success {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
                }
            }
        }
    }
}
