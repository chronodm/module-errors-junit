package org.dmoles;

import javafx.scene.text.Font;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

public class FontLoader {
  static Font loadFont(URI uri) {
    try {
      var font = Font.loadFont(uri.toURL().openStream(), -1);
      var family = font.getFamily();
      return font;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
