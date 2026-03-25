pipeline {
    agent any

    triggers {
      githubPush()
    }
    tools {
        maven 'Maven3'    
        jdk 'JDK17'

    }

    environment {
        SONAR_HOST_URL = 'http://sonarqube:9000'
        SONAR_TOKEN    = credentials('sonar-token')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }


        stage('Build & Test') {
            steps {
                sh 'mvn -B clean verify'

            }
        }

        stage('SonarQube Analysis') {
            steps {

                withSonarQubeEnv('sonarqube-server') {
                    sh """
                      mvn sonar:sonar \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_TOKEN}
                    """
                }
            }
        }
    }
}