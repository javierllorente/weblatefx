module com.javierllorente.weblatefx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.logging;
    requires java.prefs;
    requires com.javierllorente.jweblate;
    requires com.javierllorente.jgettext;
    requires org.kordamp.ikonli.javafx;
    requires io.github.javadiffutils;
    requires jakarta.ws.rs;

    opens com.javierllorente.weblatefx to javafx.fxml;
    exports com.javierllorente.weblatefx;
}