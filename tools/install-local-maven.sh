
version='1.0-SNAPSHOT'

cd ../common
gradle build

mvn install:install-file -DgroupId=localrepo.pursuer -DartifactId=xplatj-commonj -Dversion=$version -Dpackaging=jar -Dfile=build/libs/xplatj-commonj.jar
