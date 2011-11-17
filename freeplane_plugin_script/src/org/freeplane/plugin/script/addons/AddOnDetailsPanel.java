package org.freeplane.plugin.script.addons;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.plugin.script.addons.ScriptAddOnProperties.Script;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class AddOnDetailsPanel extends JPanel {
	private int maxWidth = 500;
	private String warning;

	public AddOnDetailsPanel(final AddOnProperties addOn, final String warning) {
		this.warning = warning;
		setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] {
		        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
		        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
		        RowSpec.decode("top:default:grow"), }));
		JLabel title = createTitleLabel(addOn);
		add(title, "1, 2");
		JLabel author = createAuthorLabel(addOn);
		add(author, "1, 4");
		final Box box = Box.createHorizontalBox();
		box.add(new JLabel(getText("homepage")));
		box.add(createAddOnSourceLinkButton(addOn));
		add(box, "1, 6, left, default");
		JComponent details = createDetails(addOn);
		add(details, "1, 7");
	}

	public AddOnDetailsPanel(AddOnProperties addOn) {
		this(addOn, null);
	}

	private JLabel createTitleLabel(final AddOnProperties addOn) {
		return new JLabel("<html><body><b><font size='+2'>" + addOn.getTranslatedName() + " "
		        + addOn.getVersion().replaceAll("^v", "") + "</font></b></body></html>");
	}

	private JLabel createAuthorLabel(final AddOnProperties addOn) {
		return new JLabel("<html><body><b><font size='-1'>" + getText("authored.by", addOn.getAuthor())
		        + "</font></b></body></html>");
	}

	private JComponent createAddOnSourceLinkButton(final AddOnProperties addOn) {
		// parse the URI on creation of the dialog to test the URI syntax early
		try {
			return UITools.createHtmlLinkStyleButton(addOn.getHomepage().toURI(), addOn.getHomepage().toString());
		}
		catch (Exception e) {
			LogUtils.warn("add-on " + addOn + " has no valid homepage: " + e);
			return new JPanel();
		}
	}

	private JComponent createDetails(final AddOnProperties addOn) {
		final StringBuilder text = new StringBuilder(1024);
		text.append("<html><body>");
		text.append(addOn.getDescription().replaceAll("</?(html|body)>", ""));
		text.append("<p>");
		if (addOn instanceof ScriptAddOnProperties) {
			List<Script> scripts = ((ScriptAddOnProperties) addOn).getScripts();
			if (!scripts.isEmpty()) {
				text.append("<table border='1'>");
				text.append(row("th", getText("header.function"), getText("header.menu"), getText("header.shortcut")));
				for (ScriptAddOnProperties.Script script : scripts) {
					text.append(row("td", bold(TextUtils.getText(script.menuTitleKey)), formatMenuLocation(script),
					    formatShortcut(script.keyboardShortcut)));
				}
				text.append("</table>");
			}
		}
		if (warning != null) {
			text.append("<p>");
			text.append(warning.replaceAll("</?(html|body)>", ""));
		}
		text.append("</body></html>");
		return new JLabel(text.toString());
	}

	private String formatShortcut(final String shortCut) {
		KeyStroke keyStroke = UITools.getKeyStroke(shortCut);
		return UITools.keyStrokeToString(keyStroke);
	}

	private String formatMenuLocation(ScriptAddOnProperties.Script script) {
		final String location = script.menuLocation == null ? "main_menu_scripting" : script.menuLocation;
		MenuBuilder menuBuilder = Controller.getCurrentModeController().getUserInputListenerFactory().getMenuBuilder();
		// "/menu_bar/edit/menu_extensions" -> [Node Extensions, Edit]
		final List<String> pathElements = getMenuPathElements(menuBuilder, location);
		Collections.reverse(pathElements);
		pathElements.add(TextUtils.getText(script.menuTitleKey));
		return StringUtils.join(pathElements.iterator(), "->");
	}

	public static List<String> getMenuPathElements(MenuBuilder menuBuilder, final String location) {
		final ArrayList<String> pathElements = new ArrayList<String>();
		final DefaultMutableTreeNode node = menuBuilder.get(location);
		if (node != null) {
			pathElements.addAll(getMenuPathElements(node));
		}
		else {
			int index = location.lastIndexOf('/');
			if (index != -1) {
				final String lastKey = location.substring(index + 1);
				pathElements.add(TextUtils.getText(lastKey, TextUtils.getText("addons." + lastKey, lastKey)));
				// recurse
				if (index > 1)
					pathElements.addAll(getMenuPathElements(menuBuilder, location.substring(0, index)));
			}
		}
		return pathElements;
	}

	private static List<String> getMenuPathElements(DefaultMutableTreeNode node) {
		ArrayList<String> pathElements = new ArrayList<String>();
		while (node != null) {
			if (node.getUserObject() instanceof JMenuItem)
				pathElements.add(((JMenuItem) node.getUserObject()).getText());
			node = (DefaultMutableTreeNode) node.getParent();
		}
		return pathElements;
	}

	private String bold(final String text) {
		return "<b>" + text + "</b>";
	}

	private String row(final String td, final Object... columns) {
		final String separator = "</" + td + "><" + td + ">";
		return "<tr><" + td + ">" + org.apache.commons.lang.StringUtils.join(columns, separator) + "</" + td + "></tr>";
	}

	private static String getText(String key, Object... parameters) {
		if (parameters.length == 0)
			return TextUtils.getText(getResourceKey(key));
		else
			return TextUtils.format(getResourceKey(key), parameters);
	}

	private static String getResourceKey(final String key) {
		return "AddOnDetailsPanel." + key;
	}

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension preferredSize = super.getPreferredSize();
		preferredSize.width = Math.min(preferredSize.width, maxWidth);
		return preferredSize;
	}
}
