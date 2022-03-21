#!/bin/bash
#
# Generates a zip file with application, JavaFX and dependencies
#
name=wlfx
build_dir=./target
jar_filepath=`ls $build_dir/dist/$name*.jar`
jar_filename=$(basename -- "$jar_filepath")
version=${jar_filename:${#name}+1:-4}

zip_dir=$name
zip_name=$name-$version-nojre.zip
launcher=$name.sh

JAVAFX_HOME=javafx
lib_dir=lib
module_path=$JAVAFX_HOME:$lib_dir
modules=javafx.controls,javafx.fxml,org.kordamp.ikonli.javafx
modules+=,com.javierllorente.jwl,com.javierllorente.jgettext
modules+=,jakarta.annotation,jersey.hk2,org.jvnet.mimepull,jakarta.activation
modules+=,org.glassfish.json.jaxrs,org.glassfish.hk2.api

echo "Generating zip for $name $version"

cd $build_dir
mkdir $zip_dir
cp -R dist/* $zip_dir/cp -R javafx $zip_dir/
cp ../LICENSE $zip_dir/
cp ../$name.png $zip_dir/

cat > $zip_dir/$launcher <<EOF
#!/bin/bash
#
# Script for launching WLFX
#

java --module-path $module_path --add-modules $modules\
 --add-opens org.glassfish.json.jaxrs/org.glassfish.json.jaxrs=org.glassfish.hk2.utilities -jar $jar_filename
EOF
chmod +x $zip_dir/$launcher

zip -r $zip_name $zip_dir
rm -rf $zip_dir

