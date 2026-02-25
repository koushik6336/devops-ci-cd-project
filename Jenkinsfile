pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "ashwadama/devops-project"
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Build Maven Project') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t %DOCKER_IMAGE%:%DOCKER_TAG% ."
            }
        }

        stage('Login to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat 'echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin'
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                bat "docker push %DOCKER_IMAGE%:%DOCKER_TAG%"
            }
        }

        stage('Deploy to Dev') {
            steps {
                bat """
                kubectl -n dev set image deployment/devops-project-deployment ^
                devops-project-container=%DOCKER_IMAGE%:%DOCKER_TAG%
                """
            }
        }

        stage('Approve Production Deployment') {
            steps {
                input message: 'Deploy to Production?', ok: 'Approve'
            }
        }

        stage('Deploy to Prod') {
            steps {
                bat """
                kubectl -n prod set image deployment/devops-project-deployment ^
                devops-project-container=%DOCKER_IMAGE%:%DOCKER_TAG%
                """
            }
        }
    }

    post {
        success {
            echo 'Build, Dev Deployment and Prod Deployment Successful!'
        }
        failure {
            echo 'Pipeline Failed!'
        }
    }
}
