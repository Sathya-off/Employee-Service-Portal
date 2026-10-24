pipeline {
    agent any

    tools {
        jdk 'JDK_21'
        maven 'Maven_3.9.9'
    }

    environment {
        PORT = '8080'
        HEADLESS = 'true'
        BROWSER = 'chrome'
        APP_DIR = "${WORKSPACE}/employee-portal-app"
        TEST_DIR = "${WORKSPACE}/automation-testing-framework"
    }

    stages {
        stage('Checkout & Environment Inspection') {
            steps {
                echo '=================================================='
                echo 'Checking Environment & Tools'
                echo '=================================================='
                sh 'java -version || true'
                sh 'mvn -version || true'
                sh 'git --version || true'
            }
        }

        stage('Start System Under Test (SUT)') {
            steps {
                echo 'Starting Employee Portal REST API & Web Application...'
                dir("${env.APP_DIR}/backend") {
                    // Start Node server in background and save PID
                    sh '''
                        nohup node server.js > portal_server.log 2>&1 &
                        echo $! > server.pid
                        sleep 3
                        curl -s http://localhost:8080/api/health || (echo "Portal failed to start" && exit 1)
                    '''
                }
            }
        }

        stage('Execute Smoke Tests') {
            steps {
                echo 'Running Sanity Smoke Test Suite...'
                dir("${env.TEST_DIR}") {
                    sh 'mvn test -DsuiteXmlFile=testng-smoke.xml -Dheadless=true'
                }
            }
        }

        stage('Execute Full Regression Suite') {
            steps {
                echo 'Running Comprehensive UI, API, DB & BDD Regression Suite...'
                dir("${env.TEST_DIR}") {
                    sh 'mvn test -DsuiteXmlFile=testng.xml -Dheadless=true'
                }
            }
        }
    }

    post {
        always {
            echo 'Archiving Test Reports and Results...'
            dir("${env.TEST_DIR}") {
                // Publish TestNG XML Results
                junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml'
                // Publish Cucumber HTML Report
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/cucumber-reports',
                    reportFiles: 'cucumber-report.html',
                    reportName: 'Cucumber BDD Report'
                ])
                // Archive Failure Screenshots and Execution Logs
                archiveArtifacts allowEmptyArchive: true, artifacts: 'screenshots/*.png, logs/*.log, target/surefire-reports/**'
            }

            echo 'Stopping SUT Background Server...'
            dir("${env.APP_DIR}/backend") {
                sh '''
                    if [ -f server.pid ]; then
                        kill $(cat server.pid) || true
                        rm -f server.pid
                    fi
                '''
            }
        }
        success {
            echo ' Capstone Automation Regression Suite Passed Successfully!'
        }
        failure {
            echo '❌ Regression Suite Failed. Review archived screenshots and logs.'
        }
    }
}
