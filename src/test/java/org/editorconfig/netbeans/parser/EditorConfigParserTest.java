package org.editorconfig.netbeans.parser;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.editorconfig.netbeans.model.EditorConfigProperty;
import org.editorconfig.netbeans.printer.EditorConfigPrinter;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class EditorConfigParserTest {

  private static final Logger LOG = Logger.getLogger(EditorConfigParserTest.class.getName());

  private final EditorConfigParser parser;

  private final String testFilePath = "org/editorconfig/example/editorconfig-test.ini";

  private final File testFile;

  Map<String, List<EditorConfigProperty>> config;

  private final String[] sampleFiles = new String[]{
    "src/main/webapp/categories.xhtml",
    "src/main/webapp/resources/js/wlc/DocumentHandler.js",
    "src/main/webapp/resources/js/wlc/Rollbar.js",
    "src/main/webapp/resources/less/sidebar-widgets.less",
    "src/main/java/com/welovecoding/Debugger.java",
    "src/main/java/com/welovecoding/StringUtils.java"
  };

  public EditorConfigParserTest() {
    parser = new EditorConfigParser();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL resource = classLoader.getResource(testFilePath);
    testFile = new File(resource.getFile());
  }

  @Before
  public void initConfig() {
    try {
      config = parser.parseConfig(testFile);
      LOG.log(Level.INFO, "\r\n{0}", EditorConfigPrinter.logConfig(config));
    } catch (EditorConfigParserException ex) {
      LOG.log(Level.SEVERE, ex.getMessage());
    }
  }

  @Test
  public void parsesConfig() throws URISyntaxException, EditorConfigParserException {
    assertEquals("it parses the correct number of sections", config.size(), 5);
    assertEquals("it parses the correct number of properties per section", config.get(".*").size(), 2);
  }

  @Test
  public void matchesEverything() {
    assertEquals(true, parser.matches("*", "DocumentHandler"));
    assertEquals(true, parser.matches("*", "src/main/webapp/resources/js/wlc/DocumentHandler.js"));
    assertEquals(true, parser.matches("*", "src/main/webapp/resources/js/wlc/DocumentHandler.py"));
  }

  @Test
  public void matchesFileEndings() {
    assertEquals(true, parser.matches("*.js", "src/main/webapp/resources/js/wlc/DocumentHandler.js"));
    assertEquals(false, parser.matches("*.js", "src/main/webapp/resources/js/wlc/DocumentHandler.py"));
  }

  @Test
  public void matchesGivenStrings() {
    assertEquals(true, parser.matches("{package.json,.travis.yml}", "package.json"));
    assertEquals(true, parser.matches("{package.json,.travis.yml}", ".travis.yml"));
    assertEquals(false, parser.matches("{package.json,.travis.yml}", "travis.yml"));
    assertEquals(false, parser.matches("{package.json,.travis.yml}", "src/package.json"));
  }

}