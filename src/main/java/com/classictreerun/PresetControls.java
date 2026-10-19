package com.classictreerun;

import java.awt.Dimension;
import java.util.function.Consumer;
import javax.swing.*;

/** The main tab selects runs; all creation and management live in the native cog. */
final class PresetControls extends JPanel
{
    PresetControls(ClassicTreeRunConfig config, ClassicTreeRunPanel.Preferences preferences, Runnable loaded,
        String selectedName, Consumer<String> selected, Consumer<String> status)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        PresetLibrary library = new PresetLibrary(preferences);
        JLabel label = new JLabel("Choose your run"); add(label);
        JComboBox<String> names = new JComboBox<>(library.names().toArray(new String[0]));
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        renderer.putClientProperty("html.disable", Boolean.TRUE);
        names.setRenderer(renderer); // Treat custom names as text, including names starting with <html>.
        names.setName("savedPresets");
        // A placeholder avoids silently presenting the first saved preset as the active run.
        if (library.contains(selectedName)) { names.setSelectedItem(selectedName); }
        else if (library.contains(config.runName())) { names.setSelectedItem(config.runName()); }
        else { names.setSelectedIndex(-1); }
        names.setToolTipText("Load a saved run with a fresh checklist. Click Start run to filter bank supplies. Create and save presets in the plugin cog.");
        names.setMaximumSize(new Dimension(Integer.MAX_VALUE, names.getPreferredSize().height));
        names.addActionListener(event -> {
            String name = (String) names.getSelectedItem();
            if (name == null) { return; }
            try
            {
                library.load(name);
                selected.accept(name);
                loaded.run();
                status.accept("Loaded: " + name + ". Click Start run for the bank supplies filter.");
            }
            catch (IllegalArgumentException | IllegalStateException error) { status.accept(error.getMessage()); }
        });
        add(names);
        for (java.awt.Component child : getComponents()) { ((JComponent) child).setAlignmentX(LEFT_ALIGNMENT); }
    }
}
