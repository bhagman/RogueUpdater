/*
 * RogueUpdater
 * 
 * Firmware updater tool for Rogue Robotics products.
 * http://www.roguerobotics.com/
 * 
 * Written by Brett Hagman
 * 
 */
package rogueupdater;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.beans.*;
import java.awt.Image;
import java.util.List;

/**
 *
 * @author Brett Hagman
 */
public class RogueUpdaterUI extends javax.swing.JFrame
                            implements PropertyChangeListener
{
  private JFileChooser chooser;
  private byte fwData[];
  private UploadTask uploadTask;

  class UploadTask extends SwingWorker<Boolean, Void>
  {
    /*
     * Uploader task. Executed in background thread.
     */
    @Override
    public Boolean doInBackground()
    {
      // send the data
      int index;
      int frameSize;
      byte receivedByte;
      byte retries = 0;

      setProgress(0);

      for (index = 0; index < fwData.length; index += frameSize)
      {
        frameSize = (fwData[index] & 0xff) << 8;
        frameSize += (fwData[index + 1] & 0xff) + 2;

        if (frameSize > 512)
        {
          errorMessage(
                  "This is not a valid Rogue Firmware update file.",
                  "Firmware File Invalid",
                  null);
          return false;
        }

        try
        {
          output.flush();

          //System.out.println("Transferring: " + index + "/" + fwData.length + " (" + int(100*index/fwData.length) + "%)");

          setProgress((int)(index * 100 / fwData.length));

          byte[] bytesToSend = new byte[frameSize];

          for (int i = 0; i < frameSize; i++)
          {
            bytesToSend[i] = fwData[index + i];
          }

          output.write(bytesToSend);

          int timeout = 0;

          // now get response
          while (input.available() <= 0)
          {

            try
            {
              Thread.sleep(20);
            }
            catch (InterruptedException ignore)
            {
            }

            if (timeout++ >= 150)
            {
              errorMessage(
                      "Timeout waiting for response. Update failed.",
                      "Update Error",
                      null);
              return false;
            }
          }

          receivedByte = (byte) input.read();

          switch (receivedByte)
          {
            case 0x11:
              retries = 0;
              break;
            case 0x22:
            default:
              retries++;
              if (retries > 4)
              {
                errorMessage("Cannot communicate with module.<br>" +
                             "Is it in Update mode?",
                             "Update Error",
                             null);
                return false;
              }
              else
              {
                index -= frameSize;
                // println("Retry");
              }
              break;
          }
        }
        catch (Exception ex)
        {
          errorMessage("Upload Error.<br>" +
                       "Maybe something got disconnected?<br>" +
                       "Is the module in Update mode?",
                       "Error",
                       ex);
          return false;
        }
      }
      
      setProgress(100);
      return true;
    }

    /*
     * Executed in event dispatch thread
     */
    @Override
    public void done()
    {
        closeSerialPort();
        buttonBrowse.setEnabled(true);
        buttonUpload.setEnabled(true);
        buttonRefresh.setEnabled(true);
        comboSerialPort.setEnabled(true);
        try
        {
          // Check the result from the background process.
          // This isn't necessary for setIndeterminate(), but just in case
          // we want to do something else in the future.
          if (get() == false)
          {
            progressBar.setIndeterminate(false);
          }
        }
        catch (Exception ignore)
        {
        }
    }

  } // UploadTask class
  
  
  /**
   * Creates new form RogueUpdaterUI
   */
  public RogueUpdaterUI()
  {
    initComponents();
    
    List<Image> icons = new ArrayList<Image>();
    icons.add(new ImageIcon(getClass().getResource("resources/Rogue128.png")).getImage());
    icons.add(new ImageIcon(getClass().getResource("resources/Rogue64.png")).getImage());
    icons.add(new ImageIcon(getClass().getResource("resources/Rogue48.png")).getImage());
    icons.add(new ImageIcon(getClass().getResource("resources/Rogue32.png")).getImage());
    icons.add(new ImageIcon(getClass().getResource("resources/Rogue16.png")).getImage());
    setIconImages(icons);
    
    // Start in the current folder.
    chooser = new JFileChooser(".");
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Rogue Firmware Files (*.rfw)", "rfw");
    chooser.setFileFilter(filter);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    progressBar = new javax.swing.JProgressBar();
    comboSerialPort = new javax.swing.JComboBox();
    textFile = new javax.swing.JTextField();
    LabelFile = new javax.swing.JLabel();
    labelSerialPort = new javax.swing.JLabel();
    buttonBrowse = new javax.swing.JButton();
    buttonUpload = new javax.swing.JButton();
    buttonExit = new javax.swing.JButton();
    buttonRefresh = new javax.swing.JButton();
    labelLogo = new javax.swing.JLabel();
    labelTitle = new javax.swing.JLabel();
    labelVersion = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Rogue Updater");
    setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    setResizable(false);
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowOpened(java.awt.event.WindowEvent evt)
      {
        formWindowOpened(evt);
      }
    });

    progressBar.setStringPainted(true);

    comboSerialPort.setToolTipText("Available serial ports.");

    textFile.setEditable(false);
    textFile.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        textFileMouseClicked(evt);
      }
    });

    LabelFile.setText("Firmware File:");
    LabelFile.setToolTipText("Select a firmware file to upload to the module (*.rfw)");

    labelSerialPort.setText("Serial Port:");

    buttonBrowse.setText("Browse");
    buttonBrowse.setToolTipText("Select the firmware file to upload.");
    buttonBrowse.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        buttonBrowseActionPerformed(evt);
      }
    });

    buttonUpload.setText("Upload");
    buttonUpload.setToolTipText("Upload firmware file to module.");
    buttonUpload.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        buttonUploadActionPerformed(evt);
      }
    });

    buttonExit.setText("Exit");
    buttonExit.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        buttonExitActionPerformed(evt);
      }
    });

    buttonRefresh.setText("Refresh");
    buttonRefresh.setToolTipText("Refresh the list of availalble serial ports.");
    buttonRefresh.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        buttonRefreshActionPerformed(evt);
      }
    });

    labelLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rogueupdater/resources/RogueLogo.png"))); // NOI18N

    labelTitle.setFont(labelTitle.getFont().deriveFont((labelTitle.getFont().getStyle() | java.awt.Font.ITALIC) | java.awt.Font.BOLD, labelTitle.getFont().getSize()+7));
    labelTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelTitle.setText("Firmware Update Tool");
    labelTitle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

    labelVersion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelVersion.setText("V1.3.0");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(labelLogo, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
          .addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(LabelFile)
              .addComponent(labelSerialPort))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(buttonUpload)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                .addComponent(buttonExit))
              .addComponent(comboSerialPort, javax.swing.GroupLayout.Alignment.TRAILING, 0, 179, Short.MAX_VALUE)
              .addComponent(textFile, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(buttonBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(buttonRefresh))))
        .addContainerGap())
      .addComponent(labelTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addGap(150, 150, 150)
        .addComponent(labelVersion, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
        .addGap(150, 150, 150))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(labelLogo)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(labelTitle)
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(textFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(buttonBrowse)
          .addComponent(LabelFile))
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(comboSerialPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(buttonRefresh)
          .addComponent(labelSerialPort))
        .addGap(18, 18, 18)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(buttonExit)
          .addComponent(buttonUpload))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(labelVersion))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

    private void buttonExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonExitActionPerformed
    {//GEN-HEADEREND:event_buttonExitActionPerformed
      closeSerialPort();
      System.exit(0);
    }//GEN-LAST:event_buttonExitActionPerformed

    private void buttonBrowseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonBrowseActionPerformed
    {//GEN-HEADEREND:event_buttonBrowseActionPerformed
      int returnVal = chooser.showOpenDialog(RogueUpdaterUI.this);

      if (returnVal == JFileChooser.APPROVE_OPTION)
      {
        firmwareFile = chooser.getSelectedFile();
        textFile.setText(firmwareFile.getName());

        textFile.setToolTipText(firmwareFile.getParent());

        System.out.println("You chose to open this file: "
                           + firmwareFile.toString());
        if (firmwareFile.length() > 200000)
        {
          errorMessage("This file is likely not a Rogue Robotics firmware update file.",
                       "Invalid File",
                       null);
          firmwareFile = null;
          textFile.setText("");
          textFile.setToolTipText("");
        }
      }
    }//GEN-LAST:event_buttonBrowseActionPerformed

    private void buttonUploadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonUploadActionPerformed
    {//GEN-HEADEREND:event_buttonUploadActionPerformed
      // Button pressed, load data from file, turn off buttons, then start uploader thread.
      // Threaded method updates Progress bar, then turns buttons on after upload completed.

      FileReader fr;
      char data[];

      buttonUpload.setEnabled(false);
      progressBar.setValue(0);
      
      if (firmwareFile == null)
      {
        errorMessage("Please select a firmware file.",
                     "No firmware file selected",
                     null);
        buttonUpload.setEnabled(true);
        return;
      }

      try
      {
        fr = new FileReader(firmwareFile);
        data = new char[(int) firmwareFile.length()];

        int charsRead = fr.read(data);

        fr.close();

        System.out.print("bytes read: ");
        System.out.println(charsRead);
      }
      catch (IOException ex)
      {
        errorMessage("An error occurred while opening the file",
                     "File Open Error",
                     ex);
        buttonUpload.setEnabled(true);
        return;
      }

      fwData = new byte[data.length / 2];

      // convert to binary
      for (int i = 0; i < data.length; i += 2)
      {
        fwData[i / 2] = (byte) ((Character.digit(data[i], 16) << 4)
                                + Character.digit(data[i + 1], 16));
      }

      // got the data, let's upload
      // need to make sure serial port is ready
      // open the port now, send data, then close the port when done

      // open the port
      if (!openSerialPort())
      {
        buttonUpload.setEnabled(true);
        return;
      }

      // We are good to upload now. Start the background task.
      
      progressBar.setIndeterminate(true);
      buttonBrowse.setEnabled(false);
      buttonRefresh.setEnabled(false);
      comboSerialPort.setEnabled(false);
      //Instances of javax.swing.SwingWorker are not reusuable, so
      //we create new instances as needed.
      uploadTask = new UploadTask();
      uploadTask.addPropertyChangeListener(this);
      uploadTask.execute();
    }//GEN-LAST:event_buttonUploadActionPerformed

    /**
     * Invoked when our task's 'progress' property changes.
     * 'progress' property is inherited from 
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
      if (evt.getPropertyName().equals("progress"))
      {
        int progress = (Integer) evt.getNewValue();
        progressBar.setIndeterminate(false);
        progressBar.setValue(progress);
      }
    }

    private void buttonRefreshActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_buttonRefreshActionPerformed
    {//GEN-HEADEREND:event_buttonRefreshActionPerformed
      try
      {
        populateComboSerial();
      }
      catch (Exception ignore)
      {
      }
    }//GEN-LAST:event_buttonRefreshActionPerformed

    private String getRXTXLibraryLocation()
    {
      String osName = System.getProperty("os.name");
      String osArch = System.getProperty("os.arch");
      String location = "none";

      System.out.println("os.name='"+osName+"'");
      System.out.println("os.arch='"+osArch+"'");
        
      // check for linux platform ..
      if (osName.toLowerCase().contains("linux"))
      {
        // check for architecture 64bit
        if (osArch.toLowerCase().contains("amd64") || osArch.toLowerCase().contains("x86_64"))
        {
          location = "lib/x86_64-unknown-linux-gnu/librxtxSerial.so";
        } 
        // else 32bit
        else
        {
          location = "lib/i686-unknown-linux-gnu/librxtxSerial.so";
        }

      } 
      // check for windows platform
      else if (osName.toLowerCase().contains("windows"))
      {
        // check for architecture 64bit
        if (osArch.toLowerCase().contains("amd64") || osArch.toLowerCase().contains("x86_64"))
        {
          // 64bit libs from http://www.cloudhopper.com/opensource/rxtx/
          location = "lib/win64/rxtxSerial.dll";
        }
        // else 32bit
        else
        {
          location = "lib/win32/rxtxSerial.dll";
        }
        /*
        osArch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        String realArch = osArch.endsWith("64") ||
                          wow64Arch != null && wow64Arch.endsWith("64") ? "64" : "32";
        if (realArch.equals("64"))
        {
          // 64bit libs from http://www.cloudhopper.com/opensource/rxtx/
          location = "lib/win64/rxtxSerial.dll";
        }
        // else 32bit
        else
        {
          location = "lib/win32/rxtxSerial.dll";
        }
        */
      } 
      // check for os x platform
      else if (osName.toLowerCase().contains("os x"))
      {
        // no arch available or required?!
        location = "lib/mac-10.5/librxtxSerial.jnilib";
      }
      /*
      // check for solaris platform
      else if (osName.toLowerCase().contains("solaris"))
      {
        // check for architecture 64bit
        if (osArch.toLowerCase().contains("amd64") || osArch.toLowerCase().contains("x86_64"))
        {
          location = "lib/sparc-sun-solaris2.10-64/librxtxSerial.so";
        } 
        // else 32bit
        else
        {
          location = "lib/sparc-sun-solaris2.10-32/librxtxSerial.so";
        }

      }
      */
      // other platforms are currently not supported ...
      else
      {
        System.out.println("Sorry, platform '"+osName+"' currently not supported...");
      }
        
      return location;
    }
    
    
    private void formWindowOpened(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowOpened
    {//GEN-HEADEREND:event_formWindowOpened
      System.out.println("Started!");

      // Maybe one day... we can load in our library on our own.
      // Anyone have any more insight into this?

      /*
      File jarfile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
      String jarpath = jarfile.getParentFile().getAbsolutePath();
      
      String libloc = jarpath + "/" + getRXTXLibraryLocation();
      
      System.out.println(libloc);
      
      System.out.println("Trying to load '"+libloc+"'...");
      
      try
      {
        System.load(libloc);
      }
      catch (Exception e)
      {
        errorMessage("Error loading Rxtx lib",
                     "load error",
                     e);
      }
      */
      
      try
      {
        //java.lang.System.load(null);
        System.out.println(gnu.io.RXTXVersion.nativeGetVersion());
        populateComboSerial();
      }
      catch (UnsatisfiedLinkError e)
      {
        String errmsg = "The RXTX Native Library was not found on your system.<br>"
                      + "Please go to <a href=\"http://rxtx.qbang.org/\">http://rxtx.qbang.org/</a><br>"
                      + "and install the latest binaries (RXTXcomm.jar and a native library for your system + architecture).";

        if (System.getProperty("os.name").startsWith("Mac"))
        {
          errmsg += "<br><br>Please make sure that you install the library with Administrator privileges (sudo).";
        }

        errorMessage(errmsg,
                     "Missing RXTX Library",
                     e);
        buttonUpload.setEnabled(false);
        buttonRefresh.setEnabled(false);
        buttonBrowse.setEnabled(false);
      }
    }//GEN-LAST:event_formWindowOpened

  private void textFileMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_textFileMouseClicked
  {//GEN-HEADEREND:event_textFileMouseClicked
    if (buttonBrowse.isEnabled())
    {
      buttonBrowseActionPerformed(null);
    }
  }//GEN-LAST:event_textFileMouseClicked

  private void populateComboSerial()
  {
    // Any Unsatisfied Linker exceptions will be caught by the parent method.
    ArrayList<String> serialPorts = serialList();

    comboSerialPort.removeAllItems();

    if (serialPorts.size() > 0)
    {
      for (int i = 0; i < serialPorts.size(); i++)
      {
        comboSerialPort.addItem(serialPorts.get(i));
      }
    }
  }

  public ArrayList<String> serialList()
  {
    ArrayList<String> serialPortList = new ArrayList<String>();

    //System.err.println("trying");
    Enumeration portList = CommPortIdentifier.getPortIdentifiers();
    //System.err.println("got port list");
    while (portList.hasMoreElements())
    {
      CommPortIdentifier pId =
              (CommPortIdentifier) portList.nextElement();
      //System.out.println(portId);

      if (pId.getPortType() == CommPortIdentifier.PORT_SERIAL)
      {
        String name = pId.getName();
        serialPortList.add(name);
      }
    }

    return serialPortList;
  }

  public void errorMessage(String msg, String title, Throwable e)
  {
    String dmsg;

    dmsg = "<html>" + msg;

    if (e != null)
    {
      System.err.println(e.getMessage());

      dmsg += "<br><br>(<i>" + e.getMessage() + "</i>)";
    }

    dmsg += "</html>";

    System.err.println(msg);

    JOptionPane.showMessageDialog(this,
                                  dmsg,
                                  title,
                                  JOptionPane.ERROR_MESSAGE);
  }

  public boolean openSerialPort()
  {
    boolean portFound = false;

    if (comboSerialPort.getSelectedItem() != null)
    {
      try
      {
        portId = CommPortIdentifier.getPortIdentifier(
                (String) comboSerialPort.getSelectedItem());
      }
      catch (Exception ex)
      {
        errorMessage("Can't get serial port identifier.",
                     "Serial Port Open Error",
                     ex);
        return false;
      }

      if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL)
      {
        try
        {
          serialPort = (SerialPort) portId.open("RUSerial", 2000);
          input = serialPort.getInputStream();
          output = serialPort.getOutputStream();
          serialPort.setSerialPortParams(9600,
                                         SerialPort.DATABITS_8,
                                         SerialPort.STOPBITS_1,
                                         SerialPort.PARITY_NONE);
          System.out.println("Port opened: "
                             + comboSerialPort.getSelectedItem());

          portFound = true;
        }
        catch (Exception ex)
        {
          errorMessage("Serial port already in use by another application.",
                       "Serial Port Open Error",
                       ex);
        }
      }
      else
      {
        errorMessage("Port selected is not a serial port.",
                     "Serial Port Open Error",
                     null);
      }

      if (!portFound)
      {
        System.out.println("Port " + comboSerialPort.getSelectedItem()
                           + " not found.");
      }
    }

    return portFound;
  }

  public void closeSerialPort()
  {
    if (serialPort != null)
    {
      serialPort.close();
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        new RogueUpdaterUI().setVisible(true);
      }
    });
  }
  
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel LabelFile;
  private javax.swing.JButton buttonBrowse;
  private javax.swing.JButton buttonExit;
  private javax.swing.JButton buttonRefresh;
  private javax.swing.JButton buttonUpload;
  private javax.swing.JComboBox comboSerialPort;
  private javax.swing.JLabel labelLogo;
  private javax.swing.JLabel labelSerialPort;
  private javax.swing.JLabel labelTitle;
  private javax.swing.JLabel labelVersion;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JTextField textFile;
  // End of variables declaration//GEN-END:variables
  private File firmwareFile;
  private SerialPort serialPort;
  private InputStream input;
  private OutputStream output;
  private CommPortIdentifier portId;
}
