cp resources/application.conf resources/app.conf
envsubst < resources/application.conf > resources/app2.conf
mv resources/app2.conf resources/application.conf
./gradlew appengineDeploy
mv resources/app.conf resources/application.conf
