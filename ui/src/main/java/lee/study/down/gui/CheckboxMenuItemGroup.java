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
    CheckboxMenuItem checkedItem = ((CheckboxMenuItem) e.getSource());
    if (e.getStateChange() == ItemEvent.SELECTED) {
      String selectedItemName = checkedItem.getName();
      for (CheckboxMenuItem item : items) {
        if (!item.getName().equals(selectedItemName)) {
          item.setState(false);
        }
      }
      if (itemListener != null) {
        itemListener.itemStateChanged(e);
      }
    } else {
      checkedItem.setState(true);
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
