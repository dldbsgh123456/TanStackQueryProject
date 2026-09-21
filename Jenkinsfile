pipeline {

    agent any

    environment {

        // =====================================================
        // Docker Hub
        // =====================================================
        IMAGE_NAME = "leeyunho1234/react-app:latest"

        // =====================================================
        // AWS EC2
        // =====================================================
        EC2_USER = "ubuntu"
        EC2_HOST = "13.125.160.99"

        // EC2 배포 디렉터리
        EC2_APP_DIR = "/home/ubuntu/app"
    }

    stages {

        // =====================================================
        // 1. Git Checkout : 소스 체크 
        // =====================================================
        stage('Git Checkout') {

            steps {

                echo "===== Git Checkout ====="

                checkout scm
            }
        }


        // =====================================================
        // 2. Gradle Build => 권한부여 / jar파일 생성 
        // =====================================================
        stage('Gradle Build') {

            steps {

                sh '''
                    echo "======================================"
                    echo " Gradle Build"
                    echo "======================================"

                    chmod +x gradlew

                    ./gradlew clean build -x test

                    echo "======================================"
                    echo " JAR 확인"
                    echo "======================================"

                    ls -al build/libs
                '''
            }
        }


        // =====================================================
        // 3. Docker Build
        // =====================================================
        stage('Docker Build') {

            steps {

                sh '''
                    echo "======================================"
                    echo " Docker Build"
                    echo "======================================"

                    docker build -t ${IMAGE_NAME} .

                    echo "======================================"
                    echo " Docker Image 확인"
                    echo "======================================"

                    docker images | grep react-app
                '''
            }
        }


        // =====================================================
        // 4. Docker Hub Push
        // =====================================================
        stage('Docker Hub Push') {

            steps {

                withCredentials([

                    usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )

                ]) {

                    sh '''
                        echo "======================================"
                        echo " Docker Hub Login"
                        echo "======================================"

                        echo "$DOCKER_PASSWORD" | docker login \
                            -u "$DOCKER_USERNAME" \
                            --password-stdin

                        echo "======================================"
                        echo " Docker Hub Push"
                        echo "======================================"

                        docker push ${IMAGE_NAME}

                        docker logout
                    '''
                }
            }
        }


        // =====================================================
        // 5. EC2 배포 파일 전송
        // =====================================================
        stage('Copy Deploy Files to EC2') {

            steps {

                sshagent(['ec2-ssh']) {

                    sh '''
                        echo "======================================"
                        echo " EC2 배포 파일 전송"
                        echo "======================================"

                        ssh -o StrictHostKeyChecking=no \
                            ${EC2_USER}@${EC2_HOST} \
                            "mkdir -p ${EC2_APP_DIR}"

                        scp -o StrictHostKeyChecking=no \
                            docker-compose.yml \
                            ${EC2_USER}@${EC2_HOST}:${EC2_APP_DIR}/docker-compose.yml

                        scp -o StrictHostKeyChecking=no \
                            nginx.conf \
                            ${EC2_USER}@${EC2_HOST}:${EC2_APP_DIR}/nginx.conf

                        echo "EC2 파일 전송 완료"
                    '''
                }
            }
        }


        // =====================================================
        // 6. EC2 .env 생성
        // =====================================================
        stage('Create EC2 .env') {

            steps {

                withCredentials([

                    string(
                        credentialsId: 'oracle_url',
                        variable: 'DB_URL'
                    ),

                    string(
                        credentialsId: 'oracle_name',
                        variable: 'DB_USERNAME'
                    ),

                    string(
                        credentialsId: 'oracle_pwd',
                        variable: 'DB_PASSWORD'
                    )

                ]) {

                    sshagent(['ec2-ssh']) {

                        sh '''
                            echo "======================================"
                            echo " EC2 .env 생성"
                            echo "======================================"

                            ssh -o StrictHostKeyChecking=no \
                                ${EC2_USER}@${EC2_HOST} \
                                "cat > ${EC2_APP_DIR}/.env <<EOF
SPRING_PROFILES_ACTIVE=prod
DB_URL=${DB_URL}
DB_USERNAME=${DB_USERNAME}
DB_PASSWORD=${DB_PASSWORD}
EOF
chmod 600 ${EC2_APP_DIR}/.env
"

                            echo "EC2 .env 생성 완료"
                        '''
                    }
                }
            }
        }


        // =====================================================
        // 7. EC2 Docker Compose 배포
        // =====================================================
        stage('Deploy to EC2') {

    steps {

        sshagent(['ec2-ssh']) {

            sh '''
                echo "======================================"
                echo " AWS EC2 배포"
                echo "======================================"

                ssh -o StrictHostKeyChecking=no \\
                    ${EC2_USER}@${EC2_HOST} << EOF

                    echo "======================================"
                    echo " EC2 접속 성공"
                    echo "======================================"

                    cd ${EC2_APP_DIR}

                    echo "현재 위치"
                    pwd


                    echo "======================================"
                    echo " Docker Compose 설정 확인"
                    echo "======================================"

                    docker compose config


                    echo "======================================"
                    echo " Docker Image Pull"
                    echo "======================================"

                    docker pull ${IMAGE_NAME}


                    echo "======================================"
                    echo " 기존 컨테이너 확인"
                    echo "======================================"

                    docker compose ps


                    echo "======================================"
                    echo " Spring Boot 2개 배포"
                    echo "======================================"

                    docker compose up -d \\
                        --scale app=2 \\
                        --force-recreate


                    echo "======================================"
                    echo " 컨테이너 상태 확인"
                    echo "======================================"

                    docker compose ps


                    echo "======================================"
                    echo " Health Check 대기"
                    echo "======================================"

                    sleep 30


                    echo "======================================"
                    echo " Health Check 결과"
                    echo "======================================"

                    docker compose ps


                    echo "======================================"
                    echo " Nginx 설정 문법 검사"
                    echo "======================================"

                    docker exec nginx nginx -t


                    echo "======================================"
                    echo " Nginx Restart"
                    echo "======================================"

                    docker restart nginx


                    echo "======================================"
                    echo " Nginx 상태 확인"
                    echo "======================================"

                    docker ps --filter "name=nginx"


                    echo "======================================"
                    echo " Nginx 로그 확인"
                    echo "======================================"

                    docker logs nginx --tail 20


                    echo "======================================"
                    echo " 최종 컨테이너 상태"
                    echo "======================================"

                    docker compose ps


                    echo "======================================"
                    echo " EC2 배포 완료"
                    echo "======================================"

EOF
            '''
                }
            }
        }
    }


    // =========================================================
    // Pipeline 결과
    // =========================================================
    post {

        success {

            echo '''
========================================
 Jenkins → AWS EC2 배포 성공
========================================
'''
        }

        failure {

            echo '''
========================================
 Jenkins → AWS EC2 배포 실패
========================================
'''
        }
    }
}