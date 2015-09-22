package com.github.kaiwinter.napsterreleases.ui.controller;

import java.net.URL;
import java.util.Objects;

/**
 * Paths for icons which can be shown in the NotificationPane of the application.
 */
public enum NotificationPaneIcon {

	INFO(NotificationPaneIcon.class.getResource("../notification-pane-info.png")), //
	WARNING(NotificationPaneIcon.class.getResource("../notification-pane-warning.png"));

	private final String iconPath;

	private NotificationPaneIcon(URL iconPath) {
		Objects.requireNonNull(iconPath, "Could not find notification icon at the specified path");
		this.iconPath = iconPath.toExternalForm();
	}

	public String getIconPath() {
		return iconPath;
	}
}