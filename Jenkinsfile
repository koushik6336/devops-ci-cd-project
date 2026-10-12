pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    environment {
        DOCKER_IMAGE    = "ashwadama/devops-project"
        DOCKER_TAG      = "${BUILD_NUMBER}"
        KUBECONFIG_PATH = "C:\\Users\\koush_8bv5nju\\.kube\\config"
        DEPLOYMENT      = "devops-project-deployment"
        CONTAINER       = "devops-project-container"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn clean package'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
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
                    bat 'echo %DOCKER_PASS%| docker login -u %DOCKER_USER% --password-stdin'
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
                script {
                    try {
                        bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n dev set image deployment/%DEPLOYMENT% %CONTAINER%=%DOCKER_IMAGE%:%DOCKER_TAG%"
                        bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n dev rollout status deployment/%DEPLOYMENT% --timeout=180s"
                    } catch (err) {
                        bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n dev rollout undo deployment/%DEPLOYMENT%"
                        error "Dev deployment failed and was rolled back: ${err}"
                    }
                }
            }
        }

        stage('Verify Dev') {
            steps {
                bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n dev get pods -l app=devops-project"
                bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n dev get deployment %DEPLOYMENT% -o jsonpath=\"{.spec.template.spec.containers[0].image}\""
            }
        }

        stage('Approve Production Deployment') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    input message: 'Dev is healthy. Deploy to Production?', ok: 'Approve'
                }
            }
        }

        stage('Deploy to Prod') {
            steps {
                script {
                    try {
                        bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n prod set image deployment/%DEPLOYMENT% %CONTAINER%=%DOCKER_IMAGE%:%DOCKER_TAG%"
                        bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n prod rollout status deployment/%DEPLOYMENT% --timeout=180s"
                    } catch (err) {
                        bat "kubectl --kubeconfig=%KUBECONFIG_PATH% -n prod rollout undo deployment/%DEPLOYMENT%"
                        error "Prod deployment failed and was rolled back: ${err}"
                    }
                }
            }
        }
    }

    post {
        always {
            bat 'docker logout'
        }
        success {
            echo 'Build, Test, Dev and Prod deployment successful!'
        }
        failure {
            echo 'Pipeline failed. Check the stage that turned red.'
        }
    }
}