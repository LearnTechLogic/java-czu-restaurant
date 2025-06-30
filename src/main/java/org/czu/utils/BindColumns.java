package org.czu.utils;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.czu.pojo.Dish;

public class BindColumns {
    public static void bindColumns(TableView<Dish> tableView, TableColumn<Dish, ?>... columns) {
        double columnCount = columns.length;
        for (TableColumn<Dish, ?> column : columns) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(columnCount));
        }
    }
}
