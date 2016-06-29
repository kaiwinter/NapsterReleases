package com.github.kaiwinter.napsterreleases.ui.viewmodel;

/**
 * Callback which is passed to a method to carry out an action if that method succeeds. This is used for re-trying a
 * failed action e.g. after a successful login.
 */
@FunctionalInterface
public interface ActionRetryCallback {

   /**
    * Is called after the login was successful.
    */
   void retryAction();
}
