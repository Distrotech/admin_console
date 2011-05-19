import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.security.*;

public class NetworkSentryLaunch extends JApplet {
  boolean WindowOpen;
  Thread windowThread;
  public void init() {
  String surl;
  try {
      if (getCodeBase().getPort() > 0) {
        surl=getCodeBase().getProtocol()+"://"+getCodeBase().getHost()+":"+getCodeBase().getPort();
      } else {
        surl=getCodeBase().getProtocol()+"://"+getCodeBase().getHost()+":80";
      }
      getContentPane().add(new NetworkSentryWindow(surl), BorderLayout.CENTER);
  } catch ( NoSuchAlgorithmException al) {
    al.printStackTrace();
  } catch (KeyManagementException  ke) {
    ke.printStackTrace();
  }
 }
}
