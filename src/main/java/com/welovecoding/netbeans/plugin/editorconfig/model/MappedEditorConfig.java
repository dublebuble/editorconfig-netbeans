package com.welovecoding.netbeans.plugin.editorconfig.model;

import com.welovecoding.netbeans.plugin.editorconfig.io.model.MappedCharset;
import java.text.MessageFormat;

public class MappedEditorConfig {

  // charset
  private MappedCharset charset;
  // end_of_line
  private String endOfLine;
  // indent_size
  private int indentSize;
  // indent_style
  private String indentStyle;
  // insert_final_newline
  private boolean insertFinalNewLine;
  // tab_width
  private int tabWidth;
  // trim_trailing_whitespace
  private boolean trimTrailingWhiteSpace;

  public MappedEditorConfig() {
  }

  public String readableString() {
    String lineEnding = "CRLF";

    if (endOfLine != null) {
      switch (endOfLine) {
        case "\r":
          lineEnding = "CR";
          break;
        case "\n":
          lineEnding = "LF";
          break;
      }
    } else {
      lineEnding = null;
    }

    return lineEnding;
  }

  @Override
  public String toString() {
    String template
            = "1. charset: {0}"
            + System.lineSeparator()
            + "2. end_of_line: {1}"
            + System.lineSeparator()
            + "3. indent_size: {2}"
            + System.lineSeparator()
            + "4. indent_style: {3}"
            + System.lineSeparator()
            + "5. insert_final_newline: {4}"
            + System.lineSeparator()
            + "6. tab_width: {5}"
            + System.lineSeparator()
            + "7. trim_trailing_whitespace: {6}"
            + System.lineSeparator();

    String charsetName = null;
    if (charset != null) {
      charsetName = charset.getName();
    }

    Object[] values = new Object[]{
      charsetName,
      readableString(),
      indentSize,
      indentStyle,
      insertFinalNewLine,
      tabWidth,
      trimTrailingWhiteSpace
    };

    return MessageFormat.format(template, values);
  }

  // <editor-fold defaultstate="collapsed" desc="Getter & Setter">
  public MappedCharset getCharset() {
    return charset;
  }

  public void setCharset(MappedCharset charset) {
    this.charset = charset;
  }

  public String getEndOfLine() {
    return endOfLine;
  }

  public void setEndOfLine(String endOfLine) {
    this.endOfLine = endOfLine;
  }

  public int getIndentSize() {
    return indentSize;
  }

  public void setIndentSize(int indentSize) {
    this.indentSize = indentSize;
  }

  public String getIndentStyle() {
    return indentStyle;
  }

  public void setIndentStyle(String indentStyle) {
    this.indentStyle = indentStyle;
  }

  public boolean isInsertFinalNewLine() {
    return insertFinalNewLine;
  }

  public void setInsertFinalNewLine(boolean insertFinalNewLine) {
    this.insertFinalNewLine = insertFinalNewLine;
  }

  public int getTabWidth() {
    return tabWidth;
  }

  public void setTabWidth(int tabWidth) {
    this.tabWidth = tabWidth;
  }

  public boolean isTrimTrailingWhiteSpace() {
    return trimTrailingWhiteSpace;
  }

  public void setTrimTrailingWhiteSpace(boolean trimTrailingWhiteSpace) {
    this.trimTrailingWhiteSpace = trimTrailingWhiteSpace;
  }
// </editor-fold>
}
