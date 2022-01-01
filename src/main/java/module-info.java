module com.example.simpleqrcodecreator {

    requires com.opencsv;
    requires org.apache.pdfbox;
    requires org.jfree.svg;
    requires java.desktop;
    requires com.google.zxing;


    opens main.java to javafx.fxml;
    exports main.java;
}