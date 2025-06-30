package org.czu.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.Objects;

public class Root {
    public Parent root(String page ) {
        try {
            Parent root;
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource(page)));
            return root;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
