#!/bin/sh

USERHOME=~/
MAVENREPO=${USERHOME}.m2/repository

echo "Installing dependencies..."
mvn install:install-file -Dfile=lib/ontology-api-client-0.1-b2.jar \
                         -DgroupId=uga \
                         -DartifactId=ontology-api-client \
                         -Dversion=0.1 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}

mvn install:install-file -Dfile=lib/GOM.jar \
                         -DgroupId=uga \
                         -DartifactId=GOM \
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}
                         
mvn install:install-file -Dfile=lib/persist-0.1.jar \
                         -DgroupId=uga \
                         -DartifactId=persist \
                         -Dversion=0.1 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}
                         
mvn install:install-file -Dfile=lib/jrap.jar \
                         -DgroupId=systemsbiology \
                         -DartifactId=jrap \
                         -Dversion=4.7 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}
                         
mvn install:install-file -Dfile=lib/glycome_molecular_framework-0.1-b9.jar \
                         -DgroupId=uga \
                         -DartifactId=glycome_molecular_framework \
                         -Dversion=0.1 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}

mvn install:install-file -Dfile=lib/eurocarb-glycanbuilder-1.0rc.jar \
                         -DgroupId=eurocarb \
                         -DartifactId=eurocarb-glycanbuilder \
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}
                         
mvn install:install-file -Dfile=lib/eurocarb-glycoworkbench-1.0rc.jar \
                         -DgroupId=eurocarb \
                         -DartifactId=eurocarb-glycoworkbench \
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}
                         
mvn install:install-file -Dfile=lib/resourcesdb_interfaces.jar \
                         -DgroupId=eurocarb \
                         -DartifactId=resourcesdb_interfaces \
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath=${MAVENREPO}

echo "Done."
echo "Building project..."
mvn package
echo "Done."