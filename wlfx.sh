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
MODULES=javafx.controls,javafx.fxml,org.kordamp.ikonli.javafx,jakarta.json,com.javierllorente.jgettext

java --module-path $MODULE_PATH --add-modules $MODULES -jar $APP_JAR
