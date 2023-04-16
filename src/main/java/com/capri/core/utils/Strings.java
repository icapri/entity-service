package com.capri.core.utils;

/**
 * Defines an abstract class with utility methods for validating
 * strings.
 */
public abstract class Strings {
  /**
   * Checks whether the string is null or empty.
   * @param value Contains the string value.
   * @return whether the string is null or empty.
   */
  public static final boolean isNullOrEmpty(String value) {
    return value == null || value.length() == 0;
  }

  /**
   * Checks whether the string is null or white spaces.
   * @param value Contains the string value.
   * @return whether the string is null or white spaces.
   */
  public static final boolean isNullOrWhiteSpace(String value) {
    return value == null || value.trim().length() == 0;
  }
}

