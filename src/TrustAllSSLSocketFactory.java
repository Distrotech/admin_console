import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import javax.net.ssl.*;
import java.security.*;

public class TrustAllSSLSocketFactory extends SSLSocketFactory {
  protected SSLSocketFactory _factory;
  public TrustAllSSLSocketFactory(String arg) throws GeneralSecurityException {
    X509TrustManager TrustAllX509 = new NetsentryTrust();
    TrustManager TrustAll[] = {TrustAllX509};

    SSLContext ctx = SSLContext.getInstance(arg); // or "SSL" ?
    ctx.init(null,TrustAll, null);
    _factory = ctx.getSocketFactory();
  }
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return _factory.createSocket(host, port);
  }
  public Socket createSocket(String host, int port) throws IOException {
    return _factory.createSocket(host, port);
  }
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
    return _factory.createSocket(host, port, localHost, localPort);
  }
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
    return _factory.createSocket(address, port, localAddress, localPort);
  }
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
    return _factory.createSocket(socket, host, port, autoClose);
  }
  public String[] getDefaultCipherSuites() {
    return _factory.getDefaultCipherSuites();
  }
  public String[] getSupportedCipherSuites() {
      return _factory.getSupportedCipherSuites();
  }
}

