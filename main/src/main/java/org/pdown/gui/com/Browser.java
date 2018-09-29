package org.pdown.gui.com;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.pdown.gui.util.I18nUtil;

public class Browser extends Region {

  final WebView webView = new WebView();
  final WebEngine webEngine = webView.getEngine();
  private javafx.scene.control.MenuItem copy;
  private javafx.scene.control.MenuItem paste;

  public Browser() {
    getChildren().add(webView);
    webView.setContextMenuEnabled(false);
    //自定义webview右键菜单
    final Clipboard clipboard = Clipboard.getSystemClipboard();
    ContextMenu contextMenu = new ContextMenu();
    copy = new javafx.scene.control.MenuItem();
    copy.setOnAction(e -> {
      ClipboardContent content = new ClipboardContent();
      Object selection = webView.getEngine().executeScript("window.getSelection().toString()");
      if (selection != null) {
        content.putString(selection.toString());
        clipboard.setContent(content);
      }
    });
    paste = new javafx.scene.control.MenuItem();
    paste.setOnAction(e -> {
      Object content = clipboard.getContent(DataFormat.PLAIN_TEXT);
      if (content != null) {
        webView.getEngine().executeScript("if(document.activeElement.selectionStart>=0){"
            + "var value = document.activeElement.value;"
            + "if(!value){"
            + " document.activeElement.value='" + content + "';"
            + "}else{"
            + " document.activeElement.value=value.substring(0,document.activeElement.selectionStart)+'" + content + "'+value.substring(document.activeElement.selectionEnd);"
            + "}"
            + "var event = document.createEvent('Event');"
            + "event.initEvent('input', true, true);"
            + "document.activeElement.dispatchEvent(event);"
            + "}");
      }
    });
    refreshText();
    contextMenu.getItems().addAll(copy, paste);
    webView.setOnMousePressed(e -> {
      if (e.getButton() == MouseButton.SECONDARY) {
        contextMenu.show(webView, e.getScreenX(), e.getScreenY());
      } else {
        contextMenu.hide();
      }
    });
  }

  @Override
  protected void layoutChildren() {
    double w = getWidth();
    double h = getHeight();
    layoutInArea(webView, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
  }

  public void load(String url) {
    webEngine.load(url);
  }

  public boolean isLoad() {
    return webEngine.getLocation() != null;
  }

  public void refreshText() {
    copy.setText(I18nUtil.getMessage("gui.menu.copy"));
    paste.setText(I18nUtil.getMessage("gui.menu.paste"));
  }
}
