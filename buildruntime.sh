#!/bin/bash
#
# Builds a custom runtime image with the required JavaFX modules
#
JAVA_HOME=/usr/lib64/jvm/java-17-openjdk-17
BUILD_DIR=./target
JAVAFX_HOME=$BUILD_DIR/javafx
JLINK_PATH=$JAVA_HOME/bin/

MODULE_PATH=$JAVAFX_HOME
MODULES=javafx.controls,javafx.fxml,java.base,java.logging,java.prefs,jdk.crypto.ec

echo "Building runtime image..."
$JLINK_PATH/jlink --verbose --strip-debug --no-header-files --no-man-pages --module-path $MODULE_PATH --add-modules $MODULES\
 --output $BUILD_DIR/runtime/
