package com.github.kaiwinter.napsterreleases.ui;

import org.junit.Test;

import com.github.kaiwinter.napsterreleases.ui.NotificationPaneIcon;

/**
 * Tests if the notification pane icon files are available.
 */
public final class NotificationPaneIconTest {

   /**
    * Cause the initialization of the enum value to load the resource. In case the file does not exist an Exception will
    * be thrown.
    */
   @Test
   public void testInfo() {
      NotificationPaneIcon.INFO.getIconPath();
   }

   /**
    * Cause the initialization of the enum value to load the resource. In case the file does not exist an Exception will
    * be thrown.
    */
   @Test
   public void testWarning() {
      NotificationPaneIcon.WARNING.getIconPath();
   }
}
