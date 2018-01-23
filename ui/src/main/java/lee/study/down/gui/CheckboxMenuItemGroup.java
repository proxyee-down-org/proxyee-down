package lee.study.down.gui;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

public class CheckboxMenuItemGroup implements ItemListener {

  private Set<CheckboxMenuItem> items = new HashSet<>();
  private ItemListener itemListener;

  public void add(CheckboxMenuItem cbmi) {
    cbmi.addItemListener(this);
    cbmi.setState(false);
    items.add(cbmi);
  }

  public void addActionListener(ItemListener itemListener) {
    this.itemListener = itemListener;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      String itemAffected = (String) e.getItem();
      for (CheckboxMenuItem item : items) {
        if (!item.getLabel().equals(itemAffected)) {
          item.setState(false);
        }
      }
      if (itemListener != null) {
        itemListener.itemStateChanged(e);
      }
    }
  }

  public void selectItem(CheckboxMenuItem itemToSelect) {
    for (CheckboxMenuItem item : items) {
      item.setState(item == itemToSelect);
    }
  }

  public CheckboxMenuItem getSelectedItem() {
    for (CheckboxMenuItem item : items) {
      if (item.getState()) {
        return item;
      }
    }
    return null;
  }
}
