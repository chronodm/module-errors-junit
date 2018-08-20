package org.dmoles;

import javafx.scene.text.Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FontLoaderTest {

  private FontLoader fontLoader;

  @BeforeEach
  void setUp() {
    fontLoader = new FontLoader();
  }

  @Test
  void loadsFonts() {
    var uri = URI.create("https://github.com/google/fonts/blob/master/ofl/muli/Muli-Bold.ttf?raw=true");
    Font font = FontLoader.loadFont(uri);
    var family = font.getFamily();
    assertEquals(family, "Muli");
  }
}
