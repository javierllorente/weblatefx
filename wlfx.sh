#!/bin/bash
#
# Script for launching WLFX
#
BASE_DIR=target
APP_VERSION=1.0-SNAPSHOT
APP_JAR=$BASE_DIR/wlfx-$APP_VERSION.jar
JAVAFX_HOME=/usr/share/openjfx/lib
LIB_DIR=$BASE_DIR/lib/

MODULE_PATH=$JAVAFX_HOME:$LIB_DIR
MODULES=javafx.controls,javafx.fxml,org.kordamp.ikonli.javafx
MODULES+=,com.javierllorente.jwl,com.javierllorente.jgettext
MODULES+=,jakarta.annotation,jersey.hk2,org.jvnet.mimepull,jakarta.activation
MODULES+=,org.glassfish.json.jaxrs,org.glassfish.hk2.api

java --module-path $MODULE_PATH --add-modules $MODULES\
 --add-opens org.glassfish.json.jaxrs/org.glassfish.json.jaxrs=org.glassfish.hk2.utilities -jar $APP_JAR
