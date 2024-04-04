module org.example.javafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.javafx to javafx.fxml;
    exports org.example.javafx;
}