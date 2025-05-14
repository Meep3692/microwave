package ca.awoo.microwave;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class GameLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return parent.getSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return parent.getMinimumSize();
    }

    @Override
    public void layoutContainer(Container parent) {
        for(Component comp : parent.getComponents()){
            comp.setSize(parent.getSize());
            comp.setLocation(0, 0);
        }
    }
    
}
