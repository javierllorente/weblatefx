#!/bin/bash
#
# Assembles a custom runtime image with the required JavaFX modules
#
JAVA_HOME=/usr/lib64/jvm/java-17-openjdk-17
build_dir=../target
JAVAFX_HOME=$build_dir/javafx
jlink_path=$JAVA_HOME/bin/

module_path=$JAVAFX_HOME
modules=javafx.controls,javafx.fxml,java.base,java.logging,java.prefs,jdk.crypto.ec

echo "Building runtime image..."
$jlink_path/jlink --verbose --strip-debug --no-header-files --no-man-pages\
 --module-path $module_path --add-modules $modules --output $build_dir/runtime/
