cp resources/application.conf resources/app.conf
envsubst < resources/application.conf > resources/app2.conf
cp resources/app2.conf resources/applicationpp.conf

# ./gradlew appengineDeploy
# cp resources/app.conf resources/application.conf