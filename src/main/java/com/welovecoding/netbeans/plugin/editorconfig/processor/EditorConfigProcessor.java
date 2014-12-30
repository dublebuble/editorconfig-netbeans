package com.welovecoding.netbeans.plugin.editorconfig.processor;

import com.welovecoding.netbeans.plugin.editorconfig.mapper.EditorConfigPropertyMapper;
import com.welovecoding.netbeans.plugin.editorconfig.io.writer.StyledDocumentWriter;
import com.welovecoding.netbeans.plugin.editorconfig.io.exception.FileAccessException;
import com.welovecoding.netbeans.plugin.editorconfig.io.model.MappedCharset;
import com.welovecoding.netbeans.plugin.editorconfig.io.reader.FileInfoReader;
import com.welovecoding.netbeans.plugin.editorconfig.io.reader.FileObjectReader;
import com.welovecoding.netbeans.plugin.editorconfig.model.EditorConfigConstant;
import com.welovecoding.netbeans.plugin.editorconfig.model.MappedEditorConfig;
import com.welovecoding.netbeans.plugin.editorconfig.processor.operation.XFinalNewLineOperation;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;

public class EditorConfigProcessor {

  private static final Logger LOG = Logger.getLogger(EditorConfigProcessor.class.getSimpleName());
  public static final Level OPERATION_LOG_LEVEL = Level.INFO;

  public EditorConfigProcessor() {
  }

  public void applyRulesToFile(DataObject dataObject) throws Exception {
    FileObject primaryFile = dataObject.getPrimaryFile();
    String content = primaryFile.asText();
    String filePath = primaryFile.getPath();

    MappedEditorConfig config = EditorConfigPropertyMapper.createEditorConfig(filePath);

    LOG.log(Level.INFO, "Mapped rules for: {0}", filePath);
    LOG.log(Level.INFO, config.toString());

    MappedCharset mappedCharset = config.getCharset();
    boolean fileChangeNeeded = false;

    // Perform EditorConfig operations
    if (mappedCharset != null) {
      logOperation(new Object[]{
        EditorConfigConstant.CHARSET,
        mappedCharset.getName(),
        filePath
      });

      doCharset(dataObject, mappedCharset);
    }

    boolean insertFinalNewLine = config.isInsertFinalNewLine();

    if (insertFinalNewLine) {
      logOperation(new Object[]{
        EditorConfigConstant.INSERT_FINAL_NEWLINE,
        insertFinalNewLine,
        filePath
      });

      boolean changedLineEndings = XFinalNewLineOperation.doFinalNewLine(content, insertFinalNewLine, config.getEndOfLine());
      fileChangeNeeded = fileChangeNeeded || changedLineEndings;
    }

    // Construct FileInfo object
    // TODO: FileInfo duplicates values from MappedEditorConfig
    FileInfo info = new FileInfo(dataObject);
    info.setContent(new StringBuilder(content));

    if (mappedCharset != null) {
      info.setCharset(mappedCharset.getCharset());
    } else {
      info.setCharset(StandardCharsets.UTF_8);
    }

    EditorCookie cookie = getEditorCookie(dataObject);
    boolean isOpenedInEditor = (cookie != null) && (cookie.getDocument() != null);
    info.setOpenedInEditor(isOpenedInEditor);
    info.setCookie(cookie);

    // Apply EditorConfig operations
    if (fileChangeNeeded) {
      // flushFile(info);
    }

  }

  private void logOperation(Object[] values) {
    LOG.log(Level.INFO, "\"{0}\": {1} ({2})", values);
  }

  private void doCharset(DataObject dataObject, MappedCharset requestedCharset) {
    FileObject fo = dataObject.getPrimaryFile();
    MappedCharset currentCharset = FileInfoReader.readCharset(fo);

    LOG.log(Level.INFO, "\u00ac Current charset: {0}", currentCharset.getName());

    if (currentCharset != requestedCharset) {
      LOG.log(Level.INFO, "\u00ac Changing charset from \"{0}\" to \"{1}\"",
              new Object[]{currentCharset.getName(), requestedCharset.getName()});

      String content = FileObjectReader.read(fo, currentCharset.getCharset());
      // FileObjectWriter.writeWithAtomicAction(dataObject, requestedCharset.getCharset(), content);

    } else {
      /*
       try {
       // TODO: A bit dangerous atm!
       // ConfigWriter.rewrite(dataObject, currentCharset, requestedCharset);
       } catch (IOException ex) {
       Exceptions.printStackTrace(ex);
       }
       */
      LOG.log(Level.INFO, "No charset change needed.");
    }
  }

  private void flushFile(FileInfo info) {
    if (info.isOpenedInEditor()) {
      updateChangesInEditorWindow(info);
    } else {
      updateChangesInFile(info);
    }
  }

  /*
   private boolean doCharset(FileObject fileObject, String charset) {
   boolean hasToBeChanged = false;
   Charset currentCharset = FileInfoReader.guessCharset(fileObject);
   Charset requestedCharset = EditorConfigPropertyMapper.mapCharset(charset);
   if (!currentCharset.equals(requestedCharset)) {
   LOG.log(Level.INFO, "Charset change needed from {0} to {1}",
   new Object[]{currentCharset.name(), requestedCharset.name()});
   hasToBeChanged = true;
   }
   return hasToBeChanged;
   }
   */
  /*
   private boolean doEndOfLine(DataObject dataObject, String ecLineEnding) {
   FileObject fileObject = dataObject.getPrimaryFile();
   String javaLineEnding = EditorConfigPropertyMapper.mapLineEnding(ecLineEnding);
   boolean wasChanged = false;
   try {
   StringBuilder content = new StringBuilder(fileObject.asText());
   wasChanged = XLineEndingOperation.doLineEndings(content, javaLineEnding);
   } catch (IOException ex) {
   Exceptions.printStackTrace(ex);
   }
   StyledDocument document = NbDocument.getDocument(dataObject);
   if (document != null && wasChanged) {
   if (!document.getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP).equals(javaLineEnding)) {
   document.putProperty(BaseDocument.READ_LINE_SEPARATOR_PROP, javaLineEnding);
   LOG.log(Level.INFO, "Action: Changed line endings in Document.");
   } else {
   LOG.log(Level.INFO, "Action not needed: Line endings are already set to: {0}", ecLineEnding);
   }
   }
   return wasChanged;
   }
   */
  private void flushStyles(FileObject fileObject) {
    try {
      Preferences codeStyle = CodeStylePreferences.get(fileObject, fileObject.getMIMEType()).getPreferences();
      codeStyle.flush();
    } catch (BackingStoreException ex) {
      LOG.log(Level.SEVERE, "Error applying code style: {0}", ex.getMessage());
    }
  }

  private EditorCookie getEditorCookie(DataObject dataObject) {
    return dataObject.getLookup().lookup(EditorCookie.class);
  }

  private void updateChangesInEditorWindow(FileInfo info) {
    LOG.log(Level.INFO, "Update changes in Editor window for: {0}", info.getPath());

    EditorCookie cookie = info.getCookie();
    NbDocument.runAtomic(cookie.getDocument(), () -> {
      try {
        StyledDocumentWriter.writeWithEditorKit(info);
      } catch (FileAccessException ex) {
        LOG.log(Level.SEVERE, ex.getMessage());
      }
    });
  }

  private void updateChangesInFile(FileInfo info) {
    LOG.log(Level.INFO, "Write content (with all rules applied) to file: {0}", info.getFileObject().getPath());

    WriteStringToFileTask task = new WriteStringToFileTask(info);
    task.run();
  }
}
