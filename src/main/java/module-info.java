module com.javierllorente.wlfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires java.base;
    requires java.logging;
    requires java.prefs;
    requires com.javierllorente.jwl;
    requires com.javierllorente.jgettext;
    requires org.kordamp.ikonli.javafx;
    requires io.github.javadiffutils;

    opens com.javierllorente.wlfx to javafx.fxml;
    exports com.javierllorente.wlfx;
}