#!/bin/bash
#
# Script for launching WLFX
#
base_dir=target
app_version=1.0-SNAPSHOT
app_jar=$base_dir/dist/wlfx-$app_version.jar
JAVAFX_HOME=$base_dir/javafx
lib_dir=$base_dir/dist/lib

module_path=$JAVAFX_HOME:$lib_dir
modules=javafx.controls,javafx.fxml,org.kordamp.ikonli.javafx
modules+=,com.javierllorente.jwl,com.javierllorente.jgettext
modules+=,jakarta.annotation,jersey.hk2,org.jvnet.mimepull,jakarta.activation
modules+=,org.glassfish.json.jaxrs,org.glassfish.hk2.api

java --module-path $module_path --add-modules $modules\
 --add-opens org.glassfish.json.jaxrs/org.glassfish.json.jaxrs=org.glassfish.hk2.utilities -jar $app_jar
