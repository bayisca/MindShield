package com.mindshield.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class BlogController {
    @FXML 
    private Button btnWrite;
    
    @FXML
    private javafx.scene.layout.VBox blogContainer;

    public void hideWriteButton() {
        if (btnWrite != null) {
            btnWrite.setVisible(false);
            btnWrite.setManaged(false);
        }
    }
}