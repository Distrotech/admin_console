import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;

class NetworkSentry extends JFrame {
  public NetworkSentry(String host) throws NoSuchAlgorithmException,KeyManagementException {
    Authenticator.setDefault(new NetSentryAuthImpl());
    X509TrustManager TrustAllX509 = new NetsentryTrust();
    TrustManager TrustAll[] = {TrustAllX509};
    SSLContext sslcon=SSLContext.getInstance("SSL");
    sslcon.init(null,TrustAll, null );
    SSLSocketFactory sf = sslcon.getSocketFactory();
    HttpsURLConnection.setDefaultSSLSocketFactory(sf);
    URLConnection.setDefaultAllowUserInteraction(true);
    NetworkSentryWindow mainwindow=new NetworkSentryWindow(host);
    getContentPane().add(mainwindow);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
  }

  public static void main(String[] args) throws NoSuchAlgorithmException,KeyManagementException {
    NetworkSentry window;
    String systype;
    
    Properties prop = System.getProperties(); 
    prop.put("http.proxyHost","10.10.255.1");
    prop.put("http.proxyPort","3128"); 
    prop.put("https.proxyHost","10.10.255.1");
    prop.put("https.proxyPort","3128"); 

    if (args.length > 0) {
      window = new NetworkSentry(args[0]); 
    } else {
      window = new NetworkSentry("https://firewall.networksentry.co.za"); 
    }
    window.pack();
    window.setTitle("Network Sentinel Solutions Firewall Manager");
    window.setSize(775,570);
    window.setVisible(true);
  }
}

class NetSentryAuthImpl extends Authenticator {
    
    protected PasswordAuthentication getPasswordAuthentication() {
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(2,2));
        panel.add(new JLabel("User Name"));
        panel.add(username);
        panel.add(new JLabel("Password") );
        panel.add(password);
        int option = JOptionPane.showConfirmDialog(null, new Object[] {
            "Site: "+getRequestingHost(),
            "Realm: "+getRequestingPrompt(), panel},
            "Enter Network Password",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if ( option == JOptionPane.OK_OPTION ) {
                String user = username.getText();
                char pass[] = password.getText().toCharArray();
                return new PasswordAuthentication(user, pass);
            } else {
                return null;
            }
    }
    
}

class NetsentryTrust implements X509TrustManager {
    
    NetsentryTrust() {
    }
    
    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificate, String str) throws java.security.cert.CertificateException {
    }
    
    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificate, String str) throws java.security.cert.CertificateException {
    }
    
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }
    
    public boolean isClientTrusted(java.security.cert.X509Certificate[] what) {
        return true;
    }
    
    public boolean isServerTrusted(java.security.cert.X509Certificate[] what) {
        return true;
    }
    
}