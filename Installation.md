# Installing Qrator #

## What you need to install Qrator ##

Currently, Qrator only runs on Linux or MacOS X.

To install Qrator, you must have:
  1. SVN
  1. Java 1.6+
  1. Apache Maven
  1. A running PostgreSQL database
  1. A running instance of either Apache Tomcat 6+, JBoss 7.1.1 (JBoss EAP 6.2 Beta - _free with registration_), or Wildfly 8+

## How to build Qrator ##

  1. Check out a copy of Qrator through SVN by following the instructions at https://code.google.com/p/qrator/source/checkout.
  1. Create a new Postgres role **_qrator_** using **_createuser_**, with a password of your choosing.  You will need to provide the **_postgres_** role password, and a password for the new **_qrator_** role.
```
createuser -U postgres -P qrator
```
  1. Create an empty **_qrator_** database using **_createdb_**.  You will need to provide the **_postgres_** role password.
```
createdb -U postgres -O qrator qrator
```
  1. Install either the empty database schema, or the most recent Qrator database dump to your PostgreSQL database.  Both are located in the **_db_** directory. Replace **_schema\_only.sql_** with **_schema\_and\_data.sql_** if you want to include previously curated structures, annotations, and references.
```
 psql -U qrator qrator < db/schema_only.sql
```
  1. Provide your database settings in **_src/main/resources/config/persist/persist.prop_** (as shown below).  The **_model_** property shouldn't be modified unless you know what you're doing.  The **_db\_username_** property should be set to the Postgres role you created in step 2, and the **_db\_password_** should be set to the password you chose for this role.
```
# PostgreSQL
db_driver=org.postgresql.Driver
db_url=jdbc:postgresql://localhost:5432/qrator
db_username=
db_password=

model=config/qrator/model.json
```
  1. Examine **_build.sh_** and, if needed, correct the **_USERHOME_** and **_MAVENREPO_** variables.
  1. If the settings are correct, run the project build script.  This file will install all necessary Maven dependencies and run **_mvn package_**.
```
./build.sh
```
  1. The Qrator WAR file will be located in the newly created **_target_** subdirectory.  You may move the WAR file to either the Tomcat **_webapps_** directory, or to the **_deployments_** directory in JBoss/Wildfly.
  1. After deploying the WAR file, the application will be accessible (assuming the server is on port 8080 and you have deployed on _localhost_) at http://localhost:8080/Qrator-1.0/.