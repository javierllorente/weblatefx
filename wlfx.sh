#!/bin/bash
#
# Script for launching WLFX
#
name=wlfx
base_dir=target
jar_filepath=`ls $base_dir/dist/$name*.jar`
jar_filename=$(basename -- "$jar_filepath")
version=${jar_filename:${#name}+1:-4}
JAVAFX_HOME=$base_dir/javafx
lib_dir=$base_dir/dist/lib

module_path=$JAVAFX_HOME:$lib_dir
modules=javafx.controls,javafx.fxml,org.kordamp.ikonli.javafx
modules+=,com.javierllorente.jwl,com.javierllorente.jgettext
modules+=,jakarta.annotation,jersey.hk2,org.jvnet.mimepull,jakarta.activation
modules+=,org.glassfish.json.jaxrs,org.glassfish.hk2.api

java --module-path $module_path --add-modules $modules\
 --add-opens org.glassfish.json.jaxrs/org.glassfish.json.jaxrs=org.glassfish.hk2.utilities -jar $jar_filepath
