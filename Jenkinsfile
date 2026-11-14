pipeline
{
    agent any
        stages
        {
            stage('Checkout')
            {
                steps
                {
                    echo 'Aeroops source code checked out'
                }
            }

            stage('Build')
            {
                steps
                {
                    sh(script: '''
                        docker run --rm \
                        -v jenkins-lab_jenkins-data:/jenkins-home \
                        -w /jenkins-home/workspace/AeroOps-CI/spring-core-demo \
                        maven:3.9-eclipse-temurin-25 \
                        mvn clean compile
                    ''')
                }
            }
        }
}