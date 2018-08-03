package org.pdown.gui.com;

import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Components {

  /**
   * 弹出提示窗，窗口置顶
   */
  public static void alert(String msg) {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("提示");
    alert.setHeaderText(null);
    alert.setContentText(msg);

    DialogPane root = alert.getDialogPane();
    Stage dialogStage = new Stage();

    for (ButtonType buttonType : root.getButtonTypes()) {
      ButtonBase button = (ButtonBase) root.lookupButton(buttonType);
      button.setOnAction(evt -> dialogStage.close());
    }

    root.getScene().setRoot(new Group());
    root.setPadding(new Insets(10, 0, 10, 0));

    Scene scene = new Scene(root);
    dialogStage.setScene(scene);
    dialogStage.initModality(Modality.APPLICATION_MODAL);
    dialogStage.setAlwaysOnTop(true);
    dialogStage.setResizable(false);
    dialogStage.showAndWait();
  }

  /**
   * 弹出文件选择框
   */
  public static File fileChooser() {
    Stage stage = buildBackgroundTopStage();
    FileChooser chooser = new FileChooser();
    chooser.setTitle("选择文件");
    File file = chooser.showOpenDialog(stage);
    stage.close();
    return file;
  }

  /**
   * 弹出文件夹选择框
   */
  public static File dirChooser() {
    Stage stage = buildBackgroundTopStage();
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("选择文件夹");
    File file = chooser.showDialog(stage);
    stage.close();
    return file;
  }

  private static Stage buildBackgroundTopStage() {
    Stage stage = new Stage();
    stage.setAlwaysOnTop(true);
    stage.setWidth(1);
    stage.setHeight(1);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.show();
    return stage;
  }
}
