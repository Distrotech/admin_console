import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;

public class NetworkSentryWindow extends Container implements ChangeListener {
    String newline = System.getProperty("line.separator");
    JTabbedPane mainwindow;
    SquidWin squidwin = new SquidWin();
    EmailWin emailwin=new EmailWin();
    RadiusWin radius=new RadiusWin();
    NFSWin netfile=new NFSWin();
    NetworkWin network=new NetworkWin();
    DnsWin domains = new DnsWin();
    //    GroupWin groups = new GroupWin();
    //    ApacheWin apachewin = new ApacheWin();
    SambaWin sambawin = new SambaWin();
    CronWin cronwin = new CronWin();
    //    X509Win caconfig=new X509Win();
    LDAPSQLWin ldapconfig=new LDAPSQLWin();
    NetworkSentrySerial serialkey;
    ConfigFiles conffiles;
    ConfigWindow confwindow;
    String aphost,fwtype;
    boolean servalid;
    String serkey="";
    boolean isfullconf=false;
    
    public NetworkSentryWindow(String url) throws NoSuchAlgorithmException,KeyManagementException{
        setLayout(new BorderLayout());
        mainwindow=new JTabbedPane();
        fwtype="full";
        
        conffiles=new ConfigFiles();
        confwindow=new ConfigWindow();
        conffiles.confpane.setBottomComponent(confwindow.setkey);

        aphost=url;
        if (! aphost.startsWith("http")) {
            aphost="http://"+aphost;
        }
        
        mainwindow.addTab("Network",network);
        mainwindow.addTab("DNS",domains);
        mainwindow.addTab("Radius",radius);
        mainwindow.addTab("Mounts",netfile);
        mainwindow.addTab("Email",emailwin);
        mainwindow.addTab("Proxy",squidwin);
        mainwindow.addTab("Samba",sambawin);
        mainwindow.addTab("Tasks",cronwin);
        mainwindow.addTab("LDAP/SQL/VOIP",ldapconfig);
        mainwindow.addTab("Config",conffiles);
        
        mainwindow.addChangeListener(this);
        
        LoadConfig();

        if (isfullconf && fwtype.equals("lite")) {
          mainwindow.setSelectedIndex(9);
        } else {
          mainwindow.setSelectedIndex(0);
        }

        add(mainwindow);
    }
    
    public void stateChanged(ChangeEvent event) {
        DefaultMutableTreeNode tnode;
        Vector allint;

        if (mainwindow.getTitleAt(mainwindow.getSelectedIndex()) == "Config") {
            confwindow.DrawConfig();
        } else if (mainwindow.getTitleAt(mainwindow.getSelectedIndex()) == "Proxy") {
            squidwin.setIntList(network.getIntList(true),network.dod);
        }
    }
    class ConfigFiles extends Container {
        JSplitPane confpane=new JSplitPane();
        JTree conftree;
        DefaultTreeModel FilesModel;
        DefaultMutableTreeNode rootbranch=new DefaultMutableTreeNode("Configuration");
        String Output;
        Vector UserList;
        boolean addhost=false;
        DefaultMutableTreeNode netints,netsettings,email,users,proxy,lists,fserv,current,active,ppp,zones,sqlldap,cstatus,x509,radius,voip,fax;

        public ConfigFiles() {
            setLayout(new BorderLayout());
            
            current=new DefaultMutableTreeNode("Current Config");
            active=new DefaultMutableTreeNode("Active Config (Loaded From Server)");
            cstatus=new DefaultMutableTreeNode("Server Status");
            
            rootbranch.add(current);
            rootbranch.add(active);
            rootbranch.add(cstatus);
            
            FilesModel = new DefaultTreeModel(rootbranch);
            conftree=new JTree(FilesModel);
            conftree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            conftree.setShowsRootHandles(true);
            
            confpane.setLeftComponent(new JScrollPane(conftree));
            confpane.setDividerLocation(0.3);
            
            add(confpane);
            conftree.setSelectionPath(new TreePath(rootbranch.getPath()));
            
            conftree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)conftree.getLastSelectedPathComponent();
                    if (node == null) {
                        node=rootbranch;
                    }
                    Object nodeInfo = node.getUserObject();
                    if (nodeInfo.toString() == "Configuration"){
                        confpane.setBottomComponent(confwindow.setkey);
                    } else if (nodeInfo.toString() == "Current Config"){
                        confpane.setBottomComponent(confwindow);
                    } else if (node == zones) {
                        confpane.setBottomComponent(null);
                    } else if (node.isLeaf()) {
                        if (node.getParent().toString() == "Server Status" ) {
                            ConfigFile fname=(ConfigFile)node.getUserObject();
                            confpane.setBottomComponent(new ShowConfFile(fname.RelURL,false));
                        } else {
                            ConfigFile fname=(ConfigFile)node.getUserObject();
                            confpane.setBottomComponent(new ShowConfFile(fname.RelURL,true));
                        }
                    } else {
                        confpane.setBottomComponent(null);
                    }
                    confpane.setDividerLocation(0.3);        }
            });
        }
        public void drawActFiles() {
            
            active.removeAllChildren();
            cstatus.removeAllChildren();
            
            netsettings=new DefaultMutableTreeNode("Networking Config Files");
            netints=new DefaultMutableTreeNode("Network Interface Scripts");
            users=new DefaultMutableTreeNode("System Users");
            email=new DefaultMutableTreeNode("Email Config Files");
            proxy=new DefaultMutableTreeNode("Proxy Config Files");
            lists=new DefaultMutableTreeNode("Configured Access Lists");
            fserv=new DefaultMutableTreeNode("File Server Config");
            ppp=new DefaultMutableTreeNode("PPP/Dialup Config");
            radius=new DefaultMutableTreeNode("Radius Configuration");
            fax=new DefaultMutableTreeNode("Fax Configuration");
            zones=new DefaultMutableTreeNode("Local DNS Config Files");
            x509=new DefaultMutableTreeNode("X.509 Files");
            sqlldap=new DefaultMutableTreeNode("SQL/LDAP Config");
            voip=new DefaultMutableTreeNode("Voice Over IP Config");
            
            
            netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Interface Config","rc.interface")));

            if (fwtype.equals("full")) {
              netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Interface Name Config","iftab")));
              IntDef intset;
              DefaultMutableTreeNode intdata;
              for (Enumeration e = network.intNode.children() ; e.hasMoreElements() ;) {
                intdata=(DefaultMutableTreeNode)e.nextElement();
                intset=(IntDef)intdata.getUserObject();
                netints.add(new DefaultMutableTreeNode(new ConfigFile(intset.IntName+" Startup","ifup."+intset.IntName)));
                netints.add(new DefaultMutableTreeNode(new ConfigFile(intset.IntName+" Bandwidth Manager","ifbw."+intset.IntName)));
              }
              netsettings.add(netints);
            }

            netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Firewall Rules (Core)","iptables")));
            netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Firewall Rules (Remote)","iptables2")));
            netsettings.add(new DefaultMutableTreeNode(new ConfigFile("TOS Config (Load Balancing)","rc.tos")));
            
            if (fwtype.equals("full")) {
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("DNS Server Config","named.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Dynamic DNS Script","dnsupdate")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("DHCP Server Config","dhcpd.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("DNS Config","resolv.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Hostname Config","HOSTNAME")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Hosts Config","hosts")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("MRTG Config","mrtg.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Time Server Config","ntp.conf")));
                
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("ESP Config","racoon.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("ESP Policies","racoon.policy")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Tunnel Config","tunnels")));
                
                
/*
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Apple Talk Interface Config","atalkd.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Apple Talk File Server Config","afpd.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Apple Talk Share Config","AppleVolumes.default")));
*/
            } else {
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("DNS Config","resolv.conf")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Hostname Config","HOSTNAME")));
                netsettings.add(new DefaultMutableTreeNode(new ConfigFile("Hosts Config","hosts")));
            }
            ppp.add(new DefaultMutableTreeNode(new ConfigFile("PPP Configuration","options")));
            ppp.add(new DefaultMutableTreeNode(new ConfigFile("PPP Dialer Script","diald.scr")));
            ppp.add(new DefaultMutableTreeNode(new ConfigFile("PAP/CHAP Auth Files","secret")));
            ppp.add(new DefaultMutableTreeNode(new ConfigFile("PPP Startup Script","rc.ppp")));
            ppp.add(new DefaultMutableTreeNode(new ConfigFile("PPP Shutdown Script","ipdown")));
            netsettings.add(ppp);
            
            if (fwtype.equals("full")) {
              radius.add(new DefaultMutableTreeNode(new ConfigFile("Port ID Map","port-id-map")));
              radius.add(new DefaultMutableTreeNode(new ConfigFile("Radius Server Config","radcserver")));
              radius.add(new DefaultMutableTreeNode(new ConfigFile("Radius Client Config","radiusclient.conf")));
              radius.add(new DefaultMutableTreeNode(new ConfigFile("Radius Clients","clients.conf")));
              radius.add(new DefaultMutableTreeNode(new ConfigFile("Getty Config","mgetty.conf")));
              radius.add(new DefaultMutableTreeNode(new ConfigFile("Radius Proxy/Realm Config","proxy.conf")));
              netsettings.add(radius);

              netsettings.add(zones);
              active.add(netsettings);
              email.add(new DefaultMutableTreeNode(new ConfigFile("Master Config Template","sendmail.mc")));
              email.add(new DefaultMutableTreeNode(new ConfigFile("Local Config Template","submit.mc")));
              email.add(new DefaultMutableTreeNode(new ConfigFile("Attachment Filtering","filename.rules.conf")));
              email.add(new DefaultMutableTreeNode(new ConfigFile("Multidrop POP3","fetchmailrc")));
              email.add(new DefaultMutableTreeNode(new ConfigFile("Email Startup Script","rc.mail")));
              email.add(new DefaultMutableTreeNode(new ConfigFile("Mail Content Scanner","mailscanner.conf")));
              email.add(new DefaultMutableTreeNode(new ConfigFile("POP3 Server Configuration","popper.conf")));
              active.add(email);
              proxy.add(new DefaultMutableTreeNode(new ConfigFile("Main Config File","squid.conf")));
              proxy.add(new DefaultMutableTreeNode(new ConfigFile("Filter Config File","filter.conf")));
              proxy.add(new DefaultMutableTreeNode(new ConfigFile("Transparent FTP Proxy Config","frox.conf")));
              lists.add(new DefaultMutableTreeNode(new ConfigFile("Localy Allowed URL's","local_allow_urls")));
              lists.add(new DefaultMutableTreeNode(new ConfigFile("Localy Allowed Domains's","local_allow_domains")));
              lists.add(new DefaultMutableTreeNode(new ConfigFile("Localy Allowed Keywords","local_allow_exp")));
              lists.add(new DefaultMutableTreeNode(new ConfigFile("Localy Denied URL's","local_deny_urls")));
              lists.add(new DefaultMutableTreeNode(new ConfigFile("Localy Denied Domains's","local_deny_domains")));
              lists.add(new DefaultMutableTreeNode(new ConfigFile("Localy Denied Keywords","local_deny_exp")));
              proxy.add(lists);
              active.add(proxy);
              fserv.add(new DefaultMutableTreeNode(new ConfigFile("Auto Mount Config","autofs.conf")));
              fserv.add(new DefaultMutableTreeNode(new ConfigFile("Auto Mount Binding","rc.mount")));
              fserv.add(new DefaultMutableTreeNode(new ConfigFile("File Server Config","smb.conf")));
              fserv.add(new DefaultMutableTreeNode(new ConfigFile("File Server Kerbos Config","krb5.conf")));
              fserv.add(new DefaultMutableTreeNode(new ConfigFile("File Server Logon Batch File","logon.bat")));
              fserv.add(new DefaultMutableTreeNode(new ConfigFile("NFS Exports","exports")));
              fserv.add(new DefaultMutableTreeNode(new ConfigFile("Printer Config File","printcap")));
              active.add(fserv);
              x509.add(new DefaultMutableTreeNode(new ConfigFile("CA Config File","ca.conf")));
              x509.add(new DefaultMutableTreeNode(new ConfigFile("CA Certificate","cacert.txt")));
              x509.add(new DefaultMutableTreeNode(new ConfigFile("Server Certificate Config","server.conf")));
              x509.add(new DefaultMutableTreeNode(new ConfigFile("Server Certificate","server.txt")));
              x509.add(new DefaultMutableTreeNode(new ConfigFile("Revoked Certificates","crl.txt")));
              active.add(x509);
              sqlldap.add(new DefaultMutableTreeNode(new ConfigFile("LDAP Config","slapd.conf")));
              sqlldap.add(new DefaultMutableTreeNode(new ConfigFile("SQL Password Change Script","sqlpasswd")));
              active.add(sqlldap);

              fax.add(new DefaultMutableTreeNode(new ConfigFile("FAX Config","faxconfig")));
              fax.add(new DefaultMutableTreeNode(new ConfigFile("FAX TTY Config","faxtty")));
              fax.add(new DefaultMutableTreeNode(new ConfigFile("FAX Startup Script","rc.hfax")));
              active.add(fax);

              voip.add(new DefaultMutableTreeNode(new ConfigFile("Module Config","astmod.conf")));
              voip.add(new DefaultMutableTreeNode(new ConfigFile("SIP Channel Config","sip.conf")));
              voip.add(new DefaultMutableTreeNode(new ConfigFile("IAX2 Channel Config","iax.conf")));
              voip.add(new DefaultMutableTreeNode(new ConfigFile("Voicemail Config","voicemail.conf")));
              active.add(voip);
              active.add(new DefaultMutableTreeNode(new ConfigFile("Anti Virus Deamon Config","clamav.conf")));
              active.add(new DefaultMutableTreeNode(new ConfigFile("Backup Script","backup")));
            } else {
                active.add(netsettings);
                email.add(new DefaultMutableTreeNode(new ConfigFile("Master Config Template","sendmail.mc")));
                email.add(new DefaultMutableTreeNode(new ConfigFile("Multidrop POP3","fetchmailrc")));
                email.add(new DefaultMutableTreeNode(new ConfigFile("Email Startup Script","rc.mail")));
                active.add(email);
            }
            active.add(new DefaultMutableTreeNode(new ConfigFile("Scheduled Tasks","crontab")));
            
            if (fwtype.equals("full")) {
              active.add(new DefaultMutableTreeNode(new ConfigFile("System Service/Startup Config","inittab")));
              conffiles.getZoneFiles();
            }
            
            zones.add(new DefaultMutableTreeNode(new ConfigFile("Domain Template","zones/domain.ext")));
            zones.add(new DefaultMutableTreeNode(new ConfigFile("Local Domain Public Key","zones/nsupdate.key")));
            zones.add(new DefaultMutableTreeNode(new ConfigFile("Local Domain Private Key","zones/nsupdate.private")));
            zones.add(new DefaultMutableTreeNode(new ConfigFile("Dynamic DNS Public Key","zones/dyndns.key")));
            zones.add(new DefaultMutableTreeNode(new ConfigFile("Dynamic DNS Private Key","zones/dyndns.private")));

            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Active Network Connections","an.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Active Processes","psww.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Active Processes (Tree Format)","ps.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Active Queing Disciplines","tc.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Active Users (passwd)","passwd.php")));
/*
            if (fwtype.equals("full")) {
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Apple Talk Hosts","atalk.php")));
            }
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Connection State Table","ct.php")));
*/

            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("CPU Information","cpu.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Disk Usage","df.php")));
            if (fwtype.equals("full")) {
//                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("DNS Entries For Domain","dns.php")));
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("File Server Browse List/Shares","smbl.php")));
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("File Server Connections","smb.php")));
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("FTP Server Connections","ftp.php")));
            }
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Interface Information","if.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Interface Addresses","ifaddr.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Interface Wireless Info","iw.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Interupt Information","in.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("IO Information","io.php")));

/*
            if (fwtype.equals("full")) {
              cstatus.add(new DefaultMutableTreeNode(new ConfigFile("IP Wireless Status/Info","3gipw.php")));
              cstatus.add(new DefaultMutableTreeNode(new ConfigFile("ISDN Information","capi.php")));
            }
*/
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Kernel Modules","lm.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Mail Box Size/Details","msize.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Mounted File Systems","mount.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Network Statistics","ns.php")));
            if (fwtype.equals("full")) {
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("ICMP Ping Gateway Address","gwping.php")));
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("NFS Statistics","nfs.php")));
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("NTP Server Status","ntp.php")));
            }
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Open Sockets","li.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("PCI Information","pci.php")));
            if (fwtype.equals("full")) {
                cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Radius Sessions","rad.php")));
            }
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("Routing Information","rt.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("System Usage","top.php")));
            cstatus.add(new DefaultMutableTreeNode(new ConfigFile("USB Information","usb.php")));
            
            FilesModel.reload(active);
            FilesModel.reload(cstatus);
        }
        
        public void getZoneFiles() {
            String datain;
            
            zones.removeAllChildren();
            try {
                URL url=new URL(aphost+"/ns/config/ednszones");
                URLConnection.setDefaultAllowUserInteraction(true);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                connection.setDoOutput(true);
                connection.connect();
                
                BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((datain = in.readLine()) != null) {
                    zones.add(new DefaultMutableTreeNode(new ConfigFile(datain,"zones/"+datain)));
                }
                in.close();
            } catch (MalformedURLException mue) {
                mue.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            FilesModel.reload(zones);
        }
        class ShowConfFile extends Container {
            JTextArea conffile;
            String datain;
            String newline = System.getProperty("line.separator");
            
            public ShowConfFile(String floc,boolean isconfig){
                URL url;
                
                setLayout(new BorderLayout());
                conffile = new JTextArea();
                conffile.setEditable(false);
                conffile.setFont(new Font("Monospaced", Font.PLAIN,12));
                
                try {
                    if (isconfig) {
                        url=new URL(aphost+"/ns/config/"+floc);
                    } else {
                        url=new URL(aphost+"/ns/status/"+floc);
                    }
                    
                    URLConnection.setDefaultAllowUserInteraction(true);
                    HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                    connection.setDoOutput(true);
                    connection.connect();
                    
                    BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while((datain = in.readLine()) != null) {
                        conffile.append(datain+newline);
                    }
                    in.close();
                    conffile.select(0,0);
                } catch (MalformedURLException mue) {
                    mue.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                add(new JScrollPane(conffile));
            }
        }
    }
    class ConfigWindow extends Container implements ActionListener {
        JTextArea viewwin=new JTextArea();
        JTextField serial=new JTextField("",10);
        JScrollPane sp;
        JButton save,reload;
        setSerial setkey=new setSerial();        

        public ConfigWindow(){
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints layout = new GridBagConstraints();
            
            setLayout(gridbag);
            
            layout.weightx=1;
            layout.weighty=1;
            
            layout.fill=GridBagConstraints.BOTH;
            layout.anchor=GridBagConstraints.NORTH;
            layout.gridwidth=GridBagConstraints.REMAINDER;
            
            viewwin.setEditable(true);
            DrawConfig();
            sp=new JScrollPane(viewwin);
            gridbag.setConstraints(sp,layout);
            add(sp);
            
            layout.weighty=0;
            save=new JButton("Save This Config");
            layout.gridwidth=1;
            gridbag.setConstraints(save,layout);
            save.setActionCommand("Save Config");
            save.addActionListener(this);
            add(save);

            layout.gridwidth=GridBagConstraints.REMAINDER;
            reload=new JButton("Load Server Config");
            layout.gridwidth=1;
            gridbag.setConstraints(reload,layout);
            reload.setActionCommand("Reload Config");
            reload.addActionListener(this);
            add(reload);
            
/*            JButton defconf=new JButton("Load Default Config");
            gridbag.setConstraints(defconf,layout);
            defconf.setActionCommand("Default Config");
            defconf.addActionListener(this);
            add(defconf);
*/            
        }
        public void actionPerformed(ActionEvent event){
            if (event.getActionCommand() == "Save Config") {
                SaveConfig();
                LoadConfig();
                DrawConfig();
            } else if (event.getActionCommand() == "Reload Config") {
                LoadConfig();
                DrawConfig();
            } else if (event.getActionCommand() == "Default Config") {
                LoadDefaultConfig();
            }
        }
        public void DrawConfig() {
            viewwin.setText(network.getConfig());
            if (fwtype.equals("full")) {
                viewwin.append(domains.getConfig());
                viewwin.append(radius.getConfig());
                viewwin.append(netfile.getConfig());
                viewwin.append(emailwin.getConfig());
                viewwin.append(squidwin.getConfig());
                viewwin.append(sambawin.getConfig());
                viewwin.append(cronwin.getConfig());
                viewwin.append(ldapconfig.getConfig());
            } else {
                viewwin.append(emailwin.getConfig());
                cronwin.setDefault(fwtype);
                viewwin.append(cronwin.getConfig());
            }
            if (serkey.length() == 44) {
                viewwin.append("Serial "+network.tcpconf.skey+newline);
            }
            viewwin.select(0,0);
        }
        class setSerial extends Container implements ActionListener {
          JLabel textlabel;

          public setSerial(){
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints layout = new GridBagConstraints();

            setLayout(gridbag);

            layout.anchor=GridBagConstraints.NORTH;
            layout.weightx=1;
            layout.weighty=1;

            layout.weighty=0;

            layout.fill=GridBagConstraints.NONE;
            layout.gridwidth=GridBagConstraints.REMAINDER;
            textlabel=new JLabel("Enter Serial Number");
            gridbag.setConstraints(textlabel,layout);
            add(textlabel);

            layout.fill=GridBagConstraints.HORIZONTAL;
            layout.anchor=GridBagConstraints.NORTHWEST;
 
            addLabel(new JLabel("Serial Number"),serial,gridbag,layout);

           

            layout.weighty=1;
            layout.fill=GridBagConstraints.NONE;
            layout.anchor=GridBagConstraints.NORTH;
            layout.gridwidth=GridBagConstraints.REMAINDER;
            JButton adduser = new JButton("Change Serial Number");
            adduser.setActionCommand("Add Trust User");
            adduser.addActionListener(this);
            gridbag.setConstraints(adduser,layout);
            add(adduser);

          }
          private void addLabel(JLabel label,JTextField textfield,GridBagLayout gridbag,GridBagConstraints layout){
            layout.gridwidth=1;
            gridbag.setConstraints(label,layout);
            add(label);
            layout.gridwidth=GridBagConstraints.REMAINDER;
            gridbag.setConstraints(textfield,layout);
            add(textfield);
          }
          public void actionPerformed(ActionEvent event) {
            try {
              URL url=new URL(aphost+"/ns/setserial.php");
              URLConnection connection=url.openConnection();
              connection.setDoOutput(true);

              PrintWriter out=new PrintWriter(connection.getOutputStream());
              out.print("snum="+URLEncoder.encode(serial.getText(),"UTF-8"));
              out.close();

              BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
              in.close();
            } catch (MalformedURLException mue) {
              mue.printStackTrace();
            } catch (IOException ioe) {
              ioe.printStackTrace();
            }
            LoadConfig();
          }
        }
    }
    private void LoadDefaultConfig() {
        network.setDefault();
        domains.setDefault();
        radius.setDefault();
        netfile.setDefault();
        emailwin.setDefault();
        squidwin.setDefault();
        sambawin.setDefault();
        cronwin.setDefault(fwtype);
        ldapconfig.setDefault();
        confwindow.DrawConfig();
    }
    private void SaveConfig() {
        String datain;
        
        try {
            URL url=new URL(aphost+"/ns/saveconf.php");
            URLConnection connection=url.openConnection();
            connection.setDoOutput(true);
            
            PrintWriter out=new PrintWriter(connection.getOutputStream());
            out.print("datain="+URLEncoder.encode(confwindow.viewwin.getText(),"UTF-8"));
            out.print(URLEncoder.encode("System Type "+fwtype+newline,"UTF-8"));
            out.close();
            
            BufferedReader in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            in.close();
            
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    private void LoadConfig() {
        String datain,ipaddr;
        int ipsnm=0;
        DefaultMutableTreeNode tmpnode;
        IntDef intnet;
        boolean failload=false;

        network.delConfig();
        domains.delConfig();
        radius.delConfig();
        netfile.delConfig();
        emailwin.delConfig();
        squidwin.delConfig();
        sambawin.delConfig();
        cronwin.delConfig();
        ldapconfig.delConfig();
        BufferedReader in;

        try {
            URL url=new URL(aphost+"/ns/loadconf.php");
            URLConnection connection=url.openConnection();
            connection.setDoOutput(true);
            in=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            confwindow.viewwin.setText("");
            while((datain = in.readLine()) != null) {
                confwindow.viewwin.append(getConfig(datain));
            }
            in.close();
            confwindow.viewwin.select(0,0);
        } catch (SSLHandshakeException ssle) {
            failload=true;
        } catch (MalformedURLException mue) {
            failload=true;
//            mue.printStackTrace();
        } catch (IOException ioe) {
            failload=true;
        }

        if (network.tcpconf.intint != null) {
            intnet=(IntDef)network.tcpconf.intint.getUserObject();
            ipaddr=intnet.IPAddress+"+"+intnet.IPSubnet;
            ipsnm=Integer.parseInt(intnet.IPSubnet);
        } else if (network.lnetwork.getChildCount() > 0) {
            tmpnode=network.getInterface("eth0");
            intnet=(IntDef)tmpnode.getUserObject();
            ipaddr=intnet.IPAddress+"+"+intnet.IPSubnet;
            ipsnm=Integer.parseInt(intnet.IPSubnet);
        } else {
            ipaddr="";
        }
        
        serialkey=new NetworkSentrySerial(serkey,network.tcpconf.hname+"."+network.tcpconf.dname,ipaddr);
        confwindow.serial.setText(serkey);

        network.tcpconf.skey=serkey;
        
        servalid=serialkey.isValid().equals("All In Order Captain My Captain");
        if ((servalid) || (ipsnm >= 28)) {
            fwtype="full";
        } else {
            fwtype="lite";
        }

        if (fwtype.equals("full")) {      
            domains.DrawWindow();
            squidwin.DrawWindow();
            sambawin.DrawWindow();
            cronwin.DrawWindow();
            ldapconfig.DrawWindow();
            mainwindow.setEnabledAt(0,true);
            mainwindow.setEnabledAt(1,true);
            mainwindow.setEnabledAt(2,true);
            mainwindow.setEnabledAt(3,true);
            mainwindow.setEnabledAt(4,true);
            mainwindow.setEnabledAt(5,true);
            mainwindow.setEnabledAt(6,true);
            mainwindow.setEnabledAt(7,true);
            mainwindow.setEnabledAt(8,true);
            confwindow.save.setEnabled(true);
        } else if ((! isfullconf) && (! failload)){
            mainwindow.setEnabledAt(0,true);
            mainwindow.setEnabledAt(1,false);
            mainwindow.setEnabledAt(2,false);
            mainwindow.setEnabledAt(3,false);
            mainwindow.setEnabledAt(5,false);
            mainwindow.setEnabledAt(4,true);
            mainwindow.setEnabledAt(6,false);
            mainwindow.setEnabledAt(7,false);
            mainwindow.setEnabledAt(8,false);
            confwindow.save.setEnabled(true);
        } else {
            mainwindow.setEnabledAt(0,false);
            mainwindow.setEnabledAt(1,false);
            mainwindow.setEnabledAt(2,false);
            mainwindow.setEnabledAt(3,false);
            mainwindow.setEnabledAt(4,false);
            mainwindow.setEnabledAt(5,false);
            mainwindow.setEnabledAt(6,false);
            mainwindow.setEnabledAt(7,false);
            mainwindow.setEnabledAt(8,false);
            confwindow.save.setEnabled(false);
        }
        network.setSystype(fwtype);
        network.setSerValid(servalid);
        emailwin.setSystype(fwtype);

        if ((fwtype.equals("full")) & (network.lbnode.isLeaf())) {
          network.addDefaultAdslLB();
        }        

        if ((fwtype.equals("full")) & (emailwin.attfilter.isLeaf())) {
          emailwin.setAFDefault();
        }

        radius.DrawWindow();
        netfile.DrawWindow();
        emailwin.DrawWindow();
        network.DrawWindow();
        conffiles.drawActFiles();
    }
    
    private String getConfig(String confString) {
        String newline = System.getProperty("line.separator");
        DefaultMutableTreeNode tmpnode;
        String tmp;
        String idata[];
        String pidata[];
        
        tmp=confString;
        if (tmp.startsWith("IP ")) {
            tmp=tmp.substring(3,tmp.length());
            if (tmp.startsWith("Modem ")) {
                tmp=tmp.substring(6,tmp.length());
                if (tmp.startsWith("ComPort ")) {
                    network.modemconf.ComPort=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("Speed ")) {
                    network.modemconf.Speed=tmp.substring(6,tmp.length());
                } else if (tmp.startsWith("FlowControl ")) {
                    tmp=tmp.substring(12,tmp.length());
                    if (tmp.equals("crtscts")) {
                        network.modemconf.FlowControl="Hardware (RTS/CTS)";
                    } else if (tmp.equals("cdtrcts")) {
                        network.modemconf.FlowControl="Hardware (DTR/CTS)";
                    } else if (tmp.equals("xonxoff")) {
                        network.modemconf.FlowControl="Software";
                    } else {
                        network.modemconf.FlowControl="None";
                    }
                } else if (tmp.startsWith("Connection ")) {
                    network.modemconf.ConnType=tmp.substring(11,tmp.length());
                } else if (tmp.startsWith("Init1 ")) {
                    network.modemconf.InitString1=tmp.substring(6,tmp.length());
                } else if (tmp.startsWith("Init2 ")) {
                    network.modemconf.InitString2=tmp.substring(6,tmp.length());
                } else if (tmp.startsWith("DialString ")) {
                    network.modemconf.DialString=tmp.substring(11,tmp.length());
                } else if (tmp.startsWith("Number ")) {
                    network.modemconf.Number=tmp.substring(7,tmp.length());
                } else if (tmp.startsWith("Gateway ")) {
                    network.modemconf.DestIP=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("Address ")) {
                    network.modemconf.LocalIP=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("Username ")) {
                    network.modemconf.UserName=tmp.substring(9,tmp.length());
                } else if (tmp.startsWith("Password ")) {
                    network.modemconf.Password=tmp.substring(9,tmp.length());
                } else if (tmp.startsWith("MTU ")) {
                    network.modemconf.MTU=tmp.substring(4,tmp.length());
                } else if (tmp.startsWith("IdleTimeout ")) {
                    network.modemconf.IdleTimeout=tmp.substring(12,tmp.length());
                } else if (tmp.startsWith("Holdoff ")) {
                    network.modemconf.HoldoffTime=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("LinkTest ")) {
                    network.modemconf.LinkTest=tmp.substring(9,tmp.length());
                } else if (tmp.startsWith("Maxfail ")) {
                    network.modemconf.MaxFail=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("NoCarrier")) {
                    network.modemconf.NoCarrier=true;
                } else if (tmp.startsWith("NoDialtone")) {
                    network.modemconf.NoDialtone=true;
                } else if (tmp.startsWith("Error")) {
                    network.modemconf.Error=true;
                } else if (tmp.startsWith("Busy")) {
                    network.modemconf.Busy=true;
                } else if (tmp.startsWith("Deflate")) {
                    network.modemconf.Deflate=true;
                } else if (tmp.startsWith("BSD")) {
                    network.modemconf.BSD=true;
                } else if (tmp.startsWith("ConnectDelay ")) {
                    network.modemconf.ConnectDelay=tmp.substring(13,tmp.length());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("FAX ")) {
                tmp=tmp.substring(4,tmp.length());
                idata=tmp.split(" ");
                if (idata.length >= 2) {
                    tmp=tmp.substring(idata[0].length()+1,tmp.length());
                    network.faxconf.setVal(idata[0],tmp);
                } else if (idata.length == 1) {
                    network.faxconf.setVal(idata[0],"");
                }
            } else if (tmp.startsWith("LDAP ")) {
                tmp=tmp.substring(5,tmp.length());
                if (tmp.startsWith("Server ")) {
                    network.tcpconf.ldapserver=tmp.substring(7,tmp.length());
                } else if (tmp.startsWith("Login ")) {
                    network.tcpconf.ldaplogin=tmp.substring(6,tmp.length());
                } else if (tmp.startsWith("Password ")) {
                    network.tcpconf.ldappassword=tmp.substring(9,tmp.length());
                    network.tcpconf.ldaporigpass=tmp.substring(9,tmp.length());
                }
            } else if (tmp.startsWith("SysConf ")) {
                tmp=tmp.substring(8,tmp.length());
                if (tmp.startsWith("Nexthop ")) {
                    network.tcpconf.nexthop=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("VPNNet ")) {
                    network.tcpconf.vpnrange=tmp.substring(7,tmp.length());
                } else if (tmp.startsWith("OVPNNet ")) {
                    network.tcpconf.ovpnrange=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("L2TPNet ")) {
                    network.tcpconf.l2tprange=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("Bridge ")) {
                    network.tcpconf.bridgeint=tmp.substring(7,tmp.length());
                } else if (tmp.startsWith("DHCPLease ")) {
                    network.tcpconf.lease=tmp.substring(10,tmp.length());
                } else if (tmp.startsWith("DHCPMaxLease ")) {
                    network.tcpconf.maxlease=tmp.substring(13,tmp.length());
                } else if (tmp.startsWith("NTPServer ")) {
                    network.tcpconf.ntpserver=tmp.substring(10,tmp.length());
                } else if (tmp.startsWith("PrimaryDns ")) {
                    network.tcpconf.pdns=tmp.substring(11,tmp.length());
                } else if (tmp.startsWith("SecondaryDns ")) {
                    network.tcpconf.sdns=tmp.substring(13,tmp.length());
                } else if (tmp.startsWith("PrimaryWins ")) {
                    network.tcpconf.pwins=tmp.substring(12,tmp.length());
                } else if (tmp.startsWith("SecondaryWins ")) {
                    network.tcpconf.swins=tmp.substring(14,tmp.length());
                } else if (tmp.startsWith("Internal ")) {
                    tmp=tmp.substring(9,tmp.length());
                    network.tcpconf.intint=network.getInterface(tmp);
                } else if (tmp.startsWith("External ")) {
                    tmp=tmp.substring(9,tmp.length());
                    if (tmp.startsWith("Dialup")) {
                        network.tcpconf.extint=network.dod;
                    } else {
                        tmpnode=network.getInterface(tmp);
                        if (tmpnode != null) {
                            network.tcpconf.extint=tmpnode;
                        }
                    }
                } else if (tmp.startsWith("OVPNInt ")) {
                    tmp=tmp.substring(8,tmp.length());
                    if (tmp.startsWith("Dialup")) {
                        network.tcpconf.vpnint=network.dod;
                    } else {
                        tmpnode=network.getInterface(tmp);
                        if (tmpnode != null) {
                            network.tcpconf.vpnint=tmpnode;
                        }
                    }
/*
                } else if (tmp.startsWith("ATalkNStart ")) {
                    network.tcpconf.ANetStart=tmp.substring(12,tmp.length());
                } else if (tmp.startsWith("ATalkNFin ")) {
                    network.tcpconf.ANetFin=tmp.substring(10,tmp.length());
                } else if (tmp.startsWith("ATalkPhase ")) {
                    network.tcpconf.ANetPhase=tmp.substring(11,tmp.length());
*/
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Interface ")) {
                tmp=tmp.substring(10,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 4) {
                    network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],"","",idata[3],"","","","");
                } else if (idata.length == 6) {
                    if (idata[4].equals("-") | idata[5].equals("-")) {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],"","",idata[3],"","","","");
                    } else {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[4],idata[5],idata[3],"","","","");
                    }
                } else if (idata.length == 7) {
                    if (idata[4].equals("-") | idata[5].equals("-")) {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],"","",idata[3],"","",idata[6],"");
                    } else {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[4],idata[5],idata[3],"","",idata[6],"");
                    }
                } else if (idata.length == 8) {
                    if (idata[4].equals("-") | idata[5].equals("-")) {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],"","",idata[3],idata[6],idata[7],"","");
                    } else {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[4],idata[5],idata[3],idata[6],idata[7],"","");
                    }
                } else if (idata.length == 9) {
                    if (idata[4].equals("-") | idata[5].equals("-")) {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],"","",idata[3],idata[6],idata[7],idata[8],"");
                    } else {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[4],idata[5],idata[3],idata[6],idata[7],idata[8],"");
                    }
                } else if (idata.length == 10) {
                    if (idata[4].equals("-") | idata[5].equals("-")) {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],"","",idata[3],idata[6],idata[7],idata[8],idata[9]);
                    } else {
                      network.addInterface(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[4],idata[5],idata[3],idata[6],idata[7],idata[8],idata[9]);
                    }
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("TC ")) {
                tmp=tmp.substring(3,tmp.length());
                if (tmp.startsWith("Ingress ")) {
                    network.tcpconf.ingress=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("Egress ")) {
                    network.tcpconf.egress=tmp.substring(7,tmp.length());
                }
/*            } else if (tmp.startsWith("Proto ")) {
                tmp=tmp.substring(6,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 8 ) {
                    network.addFwProto(idata[0],idata[1],idata[2].replaceAll("_"," "),idata[3],idata[4],idata[5].replaceAll("_"," "),
                    idata[6],idata[7]);
                } else {
                    System.out.println(tmp);
                    confString="";
                }
*/
            } else if (tmp.startsWith("FW ")) {
                tmp=tmp.substring(3,tmp.length());
                if (tmp.startsWith("SourceNetwork ")) {
                    tmp=tmp.substring(14,tmp.length());
                    idata=tmp.split(" ");
                    if ( ! idata[3].equals("Modem")) {
                        tmpnode=network.getInterface(idata[3]);
                        if (tmpnode == null) {
                          tmpnode=network.getGreTunnel(idata[3]);
                        }
                    } else {
                        tmpnode=network.modemrules;
                    }
                    if ((tmpnode != null) & (idata.length == 4)) {
                        network.addSourceNetwork(tmpnode,idata[0].replaceAll("_"," "),idata[1],idata[2],"","");
                    } else if ((tmpnode != null) & (idata.length == 6)) {
                        network.addSourceNetwork(tmpnode,idata[0].replaceAll("_"," "),idata[1],idata[2],idata[4],idata[5]);
                    } else {
                        System.out.println(tmp);
                        confString="";
                    }
                } else if (tmp.startsWith("Rule ")) {
                    tmp=tmp.substring(5,tmp.length());
                    idata=tmp.split(" ");
                    if (idata.length >= 10 ) {
                        if ( ! idata[9].equals("Modem")) {
                            tmpnode=network.getSourceNetwork(network.getInterface(idata[9]),idata[7].replaceAll("_"," "));
                            if (tmpnode == null) {
                              tmpnode=network.getSourceNetwork(network.getGreTunnel(idata[9]),idata[7].replaceAll("_"," "));
                            }
                        } else {
                            tmpnode=network.getSourceNetwork(network.modemrules,idata[7].replaceAll("_"," "));
                        }
                        if (((network.getInterface(idata[8]) != null) | (idata[8].equals("-")) | (idata[8].equals("+")) | (idata[8].equals("="))) & (tmpnode != null)) {
                            if (idata.length == 12) {
                                network.addFwRule(tmpnode,idata[4],idata[5],idata[6].replaceAll("_"," "),idata[8],idata[1],
                                idata[2],idata[3],idata[0].replaceAll("_"," "),idata[10],idata[11],"Normal-Service","High");
                            } else if (idata.length == 13) {
                                network.addFwRule(tmpnode,idata[4],idata[5],idata[6].replaceAll("_"," "),idata[8],idata[1],
                                idata[2],idata[3],idata[0].replaceAll("_"," "),idata[10],idata[11],idata[12],"High");
                            } else if (idata.length == 14) {
                                network.addFwRule(tmpnode,idata[4],idata[5],idata[6].replaceAll("_"," "),idata[8],idata[1],
                                idata[2],idata[3],idata[0].replaceAll("_"," "),idata[10],idata[11],idata[12],idata[13]);
                            } else {
                                pidata=idata[4].split("_");
                                network.addFwRule(tmpnode,pidata[0],idata[5],idata[6].replaceAll("_"," "),idata[8],idata[1],
                                idata[2],idata[3],idata[0].replaceAll("_"," "),"New",pidata[1],"Normal-Service","");
                            }
                        } else {
                            System.out.println(tmp);
                            confString="";
                        }
                    } else {
                        System.out.println(tmp);
                        confString="";
                    }
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Route ")) {
                tmp=tmp.substring(6,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 7) {
                    network.addIpRoute(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[5],idata[6],idata[3],idata[4]);
                }else if (idata.length == 5) {
                    network.addIpRoute(idata[0].replaceAll("_"," "),idata[1],idata[2],"","",idata[3],idata[4]);
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("GenRoute ")) {
                tmp=tmp.substring(9,tmp.length());
                idata=tmp.split(" ");
                network.addGenRoute(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[3]);

            } else if (tmp.startsWith("WiFi ")) {
                tmp=tmp.substring(5,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 8) {
                  network.addWiFi(idata[0],idata[1].replaceAll("_"," "),idata[2].replaceAll("_"," "),idata[3],
                                  idata[4],idata[5],idata[6],idata[7]);
                } else if (idata.length == 4) {
                  network.addLiteWiFi(idata[1].replaceAll("_"," "),idata[2],idata[3]);
                }
            } else if (tmp.startsWith("ADSL ")) {
                tmp=tmp.substring(5,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 7 ) {
                  network.addAdslLink(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[3],idata[4],idata[5],idata[6],"","","");
                }else if (idata.length == 8 ) {
                  network.addAdslLink(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[3],idata[4],idata[5],idata[6],idata[7].replaceAll("_"," "),"","");
                }else if (idata.length == 10 ) {
                  network.addAdslLink(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[3],idata[4],idata[5],idata[6],idata[7].replaceAll("_"," "),idata[8],idata[9]);
                }
            } else if (tmp.startsWith("ADSL_USER ")) {
                tmp=tmp.substring(10,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 4 ) {
                  network.addAdslACC(idata[0],idata[1],Integer.parseInt(idata[2]));
                } else if (idata.length == 3 ) {
                  network.addAdslACC(idata[0],idata[1],Integer.parseInt(idata[2]));
                }
            } else if (tmp.startsWith("TOS ")) {
                tmp=tmp.substring(4,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 6 ) {
                  network.addAdslLB(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[3],idata[4],idata[5],"");
                } else if (idata.length == 7 ) {
                  network.addAdslLB(idata[0].replaceAll("_"," "),idata[1],idata[2],idata[3],idata[4],idata[5],idata[6]);
                }
            } else if (tmp.startsWith("GRE ")) {
                tmp=tmp.substring(4,tmp.length());
                if (tmp.startsWith("Tunnel ")) {
                    tmp=tmp.substring(7,tmp.length());
                    idata=tmp.split(" ");
                    if (idata.length == 3) {
                        network.addGreTunnel(idata[0],idata[1],idata[2],"",0,"");
                    }else if (idata.length == 4) {
                        network.addGreTunnel(idata[0],idata[1],idata[2],"",Integer.parseInt(idata[3]),"");
                    }else if (idata.length == 5) {
                        network.addGreTunnel(idata[0],idata[1],idata[2],idata[4],Integer.parseInt(idata[3]),"");
                    }else if (idata.length == 6) {
                        network.addGreTunnel(idata[0],idata[1],idata[2],idata[4],Integer.parseInt(idata[3]),idata[5]);
                    } else {
                        System.out.println(tmp);
                        confString="";
                    }
                } else if (tmp.startsWith("Route ")) {
                    tmp=tmp.substring(6,tmp.length());
                    idata=tmp.split(" ");
                    network.setGreRoute(idata[0],idata[1],idata[2]);
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("VOIP ")) {
              tmp=tmp.substring(5,tmp.length());
              if (tmp.startsWith("IAX ")) {
                tmp=tmp.substring(4,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 6) {
                  network.addIaxReg(idata[0],idata[1],idata[2],idata[3],idata[5],idata[4]);
                } else if (idata.length == 5) {
                  network.addIaxReg(idata[0],idata[1],idata[2],idata[3],"",idata[4]);
                } else {
                  System.out.println(tmp);
                  confString="";
                }
              } else if (tmp.startsWith("SIP ")) {
                tmp=tmp.substring(4,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 3) {
                  network.addSipReg(idata[0],idata[1],idata[2],"","");
                } else if (idata.length == 4) {
                  network.addSipReg(idata[0],idata[1],idata[2],idata[3],"");
                } else if (idata.length == 5) {
                  network.addSipReg(idata[0],idata[1],idata[2],idata[3],idata[4]);
                } else {
                  System.out.println(tmp);
                  confString="";
                }
              } else if (tmp.startsWith("FWD ")) {
                tmp=tmp.substring(4,tmp.length());
                idata=tmp.split(" ");
                network.voipdefconf.fwduser=idata[0];
                network.voipdefconf.fwdpass=idata[1];
              } else if (tmp.startsWith("VBOX ")) {
                tmp=tmp.substring(5,tmp.length());
                idata=tmp.split(" ");
                network.voipdefconf.vbox=idata[0];
                network.voipdefconf.vboxpass=idata[1];
                if (idata.length >= 4) {
                  network.voipdefconf.vboxip=idata[2];
                  network.voipdefconf.vboxp=idata[3];
                }
                if (idata.length >= 5) {
                  network.voipdefconf.vboxpre=idata[4];
                }
                if (idata.length >= 6) {
                  network.voipdefconf.h323gkid=idata[5];
                }
                if (idata.length >= 7) {
                  network.voipdefconf.vboxreg=idata[6].equals("true");
                }
                if (idata.length >= 8) {
                  network.voipdefconf.vboxdtmf=idata[7];
                }
                if (idata.length >= 9) {
                  network.voipdefconf.vboxfuser=idata[8].equals("true");
                }
                if (idata.length >= 10) {
                  network.voipdefconf.vboxvideo=idata[9].equals("true");
		}
                if (idata.length >= 11) {
                  network.voipdefconf.vboxsrtp=idata[10].equals("true");
		}
              } else if (tmp.startsWith("GOSSIP ")) {
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                network.voipdefconf.gosuser=idata[0];
                network.voipdefconf.gospass=idata[1];
              } else if (tmp.startsWith("FRESHTEL ")) {
                tmp=tmp.substring(9,tmp.length());
                idata=tmp.split(" ");
                network.voipdefconf.fteluser=idata[0];
                network.voipdefconf.ftelpass=idata[1];
              } else if (tmp.startsWith("IAXTEL ")) {
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                network.voipdefconf.iaxuser=idata[0];
                network.voipdefconf.iaxpass=idata[1];
                if (idata.length == 3) {
                  network.voipdefconf.iaxnumber=idata[2];
                }
              } else {
                System.out.println(tmp);
                confString="";
              }
            } else if (tmp.startsWith("ESP ")) {
                tmp=tmp.substring(4,tmp.length());
                if (tmp.startsWith("Tunnel ")) {
                    tmp=tmp.substring(7,tmp.length());
                    idata=tmp.split(" ");
                    if (idata.length == 7) {
                        network.addEspTunnel(idata[0],idata[1],idata[2],idata[3],idata[4],idata[5],idata[6]);
                    } else if (idata.length == 3) {
                        network.addEspTunnel(idata[0],idata[1],idata[2],"","");
                    } else if (idata.length == 4) {
                        network.addEspTunnel(idata[0],idata[1],idata[2],"",idata[3]);
                    } else if (idata.length == 5) {
                        network.addEspTunnel(idata[0],idata[1],idata[2],idata[3],idata[4]);
                    } else {
                        System.out.println(tmp);
                        confString="";
                    }
                } else if (tmp.startsWith("Access ")) {
                    tmp=tmp.substring(7,tmp.length());
                    idata=tmp.split(" ");
                    if (idata.length == 2) {
                        network.addEspTunnel(idata[0],idata[1]);
                    } else if (idata.length == 3) {
                        network.addEspTunnel(idata[0],idata[1],idata[2]);
                    } else {
                        System.out.println(tmp);
                        confString="";
                    }
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else {
                System.out.println("IP "+tmp);
                confString="";
            }
        } else if (tmp.startsWith("DNS ")) {
            tmp=tmp.substring(4,tmp.length());
            if (tmp.startsWith("Domain ")) {
                domains.dnsconf.Domain=tmp.substring(7,tmp.length());
                network.tcpconf.dname=tmp.substring(7,tmp.length());
            } else if (tmp.startsWith("Hostname ")) {
                domains.dnsconf.Hostname=tmp.substring(9,tmp.length());
                network.tcpconf.hname=tmp.substring(9,tmp.length());
            } else if (tmp.startsWith("Search ")) {
                domains.dnsconf.Search=tmp.substring(7,tmp.length());
            } else if (tmp.startsWith("Serial ")) {
                domains.dnsconf.Serial=tmp.substring(7,tmp.length());
            } else if (tmp.startsWith("System ")) {
                domains.dnsconf.Serial=tmp.substring(7,tmp.length());
            } else if (tmp.startsWith("Refresh ")) {
                domains.dnsconf.Refresh=tmp.substring(8,tmp.length());
            } else if (tmp.startsWith("Retry ")) {
                domains.dnsconf.Retry=tmp.substring(6,tmp.length());
            } else if (tmp.startsWith("Expire ")) {
                domains.dnsconf.Expire=tmp.substring(7,tmp.length());
            } else if (tmp.startsWith("DefaultTTL ")) {
                domains.dnsconf.DefaultTTL=tmp.substring(11,tmp.length());
            } else if (tmp.startsWith("DynServ ")) {
                domains.dnsconf.DynDNSIP=tmp.substring(8,tmp.length());
            } else if (tmp.startsWith("DynZone ")) {
                domains.dnsconf.DynDNSDomain=tmp.substring(8,tmp.length());
            } else if (tmp.startsWith("DynKey ")) {
                domains.dnsconf.DynDNSSecret=tmp.substring(7,tmp.length());
            } else if (tmp.startsWith("SmartKey ")) {
                domains.dnsconf.DynDNSSecret2=tmp.substring(9,tmp.length());
            } else if (tmp.startsWith("Backup ")) {
                domains.dnsconf.Backup=tmp.substring(7,tmp.length()).equals("true");
            } else if (tmp.startsWith("Auth ")) {
                domains.dnsconf.Auth=tmp.substring(5,tmp.length()).equals("true");
            } else if (tmp.startsWith("AuthX ")) {
                domains.dnsconf.AuthX=tmp.substring(6,tmp.length()).equals("true");
            } else if (tmp.startsWith("IntFirst ")) {
                domains.dnsconf.Intfirst=tmp.substring(9,tmp.length()).equals("true");
            } else if (tmp.startsWith("Usepeer ")) {
                domains.dnsconf.Usepeer=tmp.substring(8,tmp.length()).equals("true");
            } else if (tmp.startsWith("ExtServ ")) {
                domains.dnsconf.Recursion=tmp.substring(8,tmp.length()).equals("true");
            } else if (tmp.startsWith("Host ")) {
                tmp=tmp.substring(5,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 2) {
                    domains.addIHost(idata[1],idata[0],"");
                } else if (idata.length == 3) {
                    domains.addIHost(idata[1],idata[0],idata[2]);
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Hosted ")) {
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 1) {
                    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ZoneInfo(idata[0],"",false));
                    domains.treeModel.insertNodeInto(childnode,domains.hosted,domains.hosted.getChildCount());
                } else if ((idata.length == 2) && ((idata[1].equals("true")) || (idata[1].equals("false")))){
                    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ZoneInfo(idata[0],"",idata[1].equals("true")));
                    domains.treeModel.insertNodeInto(childnode,domains.hosted,domains.hosted.getChildCount());
                } else if (idata.length == 3) {
                    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ZoneInfo(idata[0],idata[1],idata[2].equals("true")));
                    domains.treeModel.insertNodeInto(childnode,domains.hosted,domains.hosted.getChildCount());
                } else if (idata.length == 2) {
                    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ZoneInfo(idata[0],idata[1],false));
                    domains.treeModel.insertNodeInto(childnode,domains.hosted,domains.hosted.getChildCount());
                }
            } else if (tmp.startsWith("NameServer ")) {
                tmp=tmp.substring(11,tmp.length());
                idata=tmp.split(" ");
                domains.addAuthNS(domains.getDomain(domains.hosted,idata[0]),idata[1]);
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("Radius ")){
            tmp=tmp.substring(7,tmp.length());
            if (tmp.startsWith("Client ")){
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                radius.addRadClient(idata[0],idata[1],idata[2]);
            } else if (tmp.startsWith("Realm ")){
                tmp=tmp.substring(6,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 6) {
                    radius.addRadRealm(idata[0],idata[5],idata[1],idata[2],idata[3].equals("true"),idata[4].equals("true"));
                } else if (idata.length == 5) {
                    radius.addRadRealm(idata[0],"",idata[1],idata[2],idata[3].equals("true"),idata[4].equals("true"));
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("RAS ")){
                tmp=tmp.substring(4,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 7) {
                    radius.addRadLink(idata[0],idata[1],idata[2],idata[3],idata[4],idata[5],idata[6]);
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Secret ")){
                tmp=tmp.substring(7,tmp.length());
                radius.ssecret=tmp;
            } else if (tmp.startsWith("Server ")){
                tmp=tmp.substring(7,tmp.length());
                radius.radserver=tmp;
            } else if (tmp.startsWith("AuthPort ")){
                tmp=tmp.substring(9,tmp.length());
                radius.radauport=tmp;
            } else if (tmp.startsWith("AccPort ")){
                tmp=tmp.substring(8,tmp.length());
                radius.radacport=tmp;
            } else if (tmp.startsWith("Wireless ")){
                tmp=tmp.substring(9,tmp.length());
                idata=tmp.split(" ");
                radius.wirerange=idata[0];
                if (idata.length == 4) {
                  radius.wrangenat=idata[1].equals("true");
                }
            } else if (tmp.startsWith("Hotspot ")){
                tmp=tmp.substring(8,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 2) {
                  radius.hspotrange=idata[0];
                  radius.hspotint=idata[1];
                }
            } else if (tmp.startsWith("PPPoEIF ")){
                tmp=tmp.substring(8,tmp.length());
                radius.pppoeint=tmp;
            } else if (tmp.startsWith("PPPoE ")){
                tmp=tmp.substring(6,tmp.length());
                idata=tmp.split(" ");
                radius.wirerange=idata[0];
                radius.wrangenat=idata[1].equals("true");
            } else if (tmp.startsWith("Ingress ")){
                tmp=tmp.substring(8,tmp.length());
                radius.ingress=tmp;
            } else if (tmp.startsWith("Egress ")){
                tmp=tmp.substring(7,tmp.length());
                radius.egress=tmp;
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("NFS ")){
            tmp=tmp.substring(4,tmp.length());
            if (tmp.startsWith("Share ")){
                tmp=tmp.substring(6,tmp.length());
                idata=tmp.split(" ");
                netfile.addNFSShare(idata[0],idata[1],idata[2],idata[3],
                idata[4].equals("true"),idata[5].equals("true"));
            } else if (tmp.startsWith("Mount ")){
                tmp=tmp.substring(6,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 4) {
                    netfile.addNFSMount(idata[0].replaceAll(" ","_"),idata[1],idata[2].replaceAll(" ","_"),
                    idata[3],"","","","",false,false,false);
                } else if (idata.length == 5) {
                    netfile.addNFSMount(idata[0].replaceAll(" ","_"),idata[1],idata[2].replaceAll(" ","_"),
                    idata[3],"","","","",false,idata[4].equals("true"),false);
                } else if (idata.length == 6) {
                    netfile.addNFSMount(idata[0].replaceAll(" ","_"),idata[1],idata[2].replaceAll(" ","_"),
                    idata[3],"","","","",false,idata[4].equals("true"),idata[5].equals("true"));
                } else if (idata.length == 9) {
                    netfile.addNFSMount(idata[0].replaceAll(" ","_"),idata[1],idata[2].replaceAll(" ","_"),
                    idata[3],idata[4],idata[5],idata[6],idata[7],idata[8].equals("true"),false,false);
                } else if (idata.length == 10) {
                    netfile.addNFSMount(idata[0].replaceAll(" ","_"),idata[1],idata[2].replaceAll(" ","_"),
                    idata[3],idata[4],idata[5],idata[6],idata[7],idata[8].equals("true"),idata[9].equals("true"),false);
                } else if (idata.length == 11) {
                    netfile.addNFSMount(idata[0].replaceAll(" ","_"),idata[1],idata[2].replaceAll(" ","_"),
                    idata[3],idata[4],idata[5],idata[6],idata[7],idata[8].equals("true"),idata[9].equals("true"),
                    idata[10].equals("true"));
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else {
                System.out.println(tmp);
                confString="";
            }
/*
    } else if (tmp.startsWith("User ")){
      tmp=tmp.substring(5,tmp.length());
      idata=tmp.split(" ");
      if (idata.length == 2 ) {
        users.addTrustAccount(idata[0],idata[1].replaceAll("_"," "));
      } else if ((idata.length == 6) & (idata[3].equals("true") | idata[3].equals("false"))){
        users.addStdAccount(idata[0],idata[1],idata[2].replaceAll("_"," "),"",idata[5].replaceAll("_"," "),
                            idata[3].equals("true"),idata[4].equals("true"));
      } else if (idata.length == 6){
        users.addStdAccount(idata[0],idata[1],idata[2].replaceAll("_"," "),idata[3].replaceAll("_"," "),"",
                            idata[4].equals("true"),idata[5].equals("true"));
      } else if (idata.length == 7){
        users.addStdAccount(idata[0],idata[1],idata[2].replaceAll("_"," "),idata[3].replaceAll("_"," "),idata[6].replaceAll("_"," "),
                            idata[4].equals("true"),idata[5].equals("true"));
      } else if (idata.length == 5) {
        users.addStdAccount(idata[0],idata[1],idata[2].replaceAll("_"," "),"","",
                            idata[3].equals("true"),idata[4].equals("true"));
      } else {
        System.out.println(tmp);
        confString="";
      }
    } else if (tmp.startsWith("Group ")) {
      tmp=tmp.substring(6,tmp.length());
      idata=tmp.split(" ");
      if (idata.length == 1) {
        groups.treeModel.insertNodeInto(new DefaultMutableTreeNode(idata[0]),groups.usergroups,groups.usergroups.getChildCount());
      } else if ((idata.length == 2) & (idata[0].equals("www"))) {
        StdUser user=users.getValidUser(idata[1]);
        if (user != null ) {
          groups.treeModel.insertNodeInto(new DefaultMutableTreeNode(idata[1]),groups.wwwgrp,groups.wwwgrp.getChildCount());
        } else {
          System.out.println(tmp);
          confString="";
        }
      } else if ((idata.length == 2) & (idata[0].equals("smbadm"))) {
        StdUser user=users.getValidUser(idata[1]);
        if (user != null ) {
          groups.treeModel.insertNodeInto(new DefaultMutableTreeNode(idata[1]),groups.smbgrp,groups.smbgrp.getChildCount());
        } else {
          System.out.println(tmp);
          confString="";
        }
      } else if (idata.length == 2) {
        StdUser user=users.getValidUser(idata[1]);
        tmpnode=groups.getUserGroup(idata[0]);
        if ((user != null ) & (tmpnode != null)) {
          groups.treeModel.insertNodeInto(new DefaultMutableTreeNode(idata[1]),tmpnode,tmpnode.getChildCount());
        } else {
          System.out.println(tmp);
          confString="";
        }
      } else {
        System.out.println(tmp);
        confString="";
      }
    } else if (tmp.startsWith("Alias ")) {
      tmp=tmp.substring(6,tmp.length());
      if (tmp.startsWith("System ")) {
        tmp=tmp.substring(7,tmp.length());
        idata=tmp.split(" ");
        tmpnode=emailwin.getAliasNode(emailwin.sysaliases,idata[0]);
        if (idata.length == 1) {
          if (tmpnode == null) {
            emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(idata[0]),emailwin.sysaliases,emailwin.sysaliases.getChildCount());
            confString="";
          }
        } else if ((idata.length == 2) & (tmpnode != null)) {
          emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(idata[1]),tmpnode,tmpnode.getChildCount());
        } else {
          System.out.println(tmp);
          confString="";
        }
      } else if (tmp.startsWith("Vdomain ")) {
        tmp=tmp.substring(8,tmp.length());
        emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(tmp),emailwin.valiases,emailwin.valiases.getChildCount());
      } else if (tmp.startsWith("Virtual ")) {
        tmp=tmp.substring(8,tmp.length());
        idata=tmp.split(" ");
        tmpnode=emailwin.getAliasNode(emailwin.valiases,idata[0]);
        if ((idata.length == 3) & (tmpnode != null)){
          emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new VirtualAlias(idata[2],idata[1])),
                                           tmpnode,tmpnode.getChildCount());
        } else if ((idata.length == 2) & (tmpnode != null)) {
          emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new VirtualAlias("",idata[1])),
                                           tmpnode,tmpnode.getChildCount());
        } else {
          System.out.println(tmp);
          confString="";
        }
      } else {
        System.out.println(tmp);
        confString="";
      }
 */
        } else if (tmp.startsWith("Email ")) {
            tmp=tmp.substring(6,tmp.length());
            if (tmp.startsWith("Redirect ")) {
                tmp=tmp.substring(9,tmp.length());
                emailwin.emailconf.Redirect=tmp;
            } else if (tmp.startsWith("Domain ")) {
                tmp=tmp.substring(7,tmp.length());
                emailwin.emailconf.Domain=tmp;
            } else if (tmp.startsWith("DNS ")) {
                tmp=tmp.substring(4,tmp.length());
                emailwin.emailconf.DNS=tmp;
            } else if (tmp.startsWith("Delivery ")) {
                tmp=tmp.substring(9,tmp.length());
                emailwin.emailconf.Dmethod=tmp;
            } else if (tmp.startsWith("Smarthost ")) {
                tmp=tmp.substring(10,tmp.length());
                emailwin.emailconf.SmartHost=tmp;
            } else if (tmp.startsWith("MaxSize ")) {
                tmp=tmp.substring(8,tmp.length());
                emailwin.emailconf.MaxMsgSize=tmp;
            } else if (tmp.startsWith("ScanChildren ")) {
                tmp=tmp.substring(13,tmp.length());
                emailwin.emailconf.MSChild=tmp;
            } else if (tmp.startsWith("ZipLevel ")) {
                tmp=tmp.substring(9,tmp.length());
                emailwin.emailconf.Ziplevel=tmp;
            } else if (tmp.startsWith("Rescan ")) {
                tmp=tmp.substring(7,tmp.length());
                emailwin.emailconf.Rescan=tmp;
            } else if (tmp.startsWith("MaxScore ")) {
                tmp=tmp.substring(9,tmp.length());
                emailwin.emailconf.MaxScore=tmp;
            } else if (tmp.startsWith("MinScore ")) {
                tmp=tmp.substring(9,tmp.length());
                emailwin.emailconf.MinScore=tmp;
            } else if (tmp.startsWith("AntiSpam ")) {
                tmp=tmp.substring(9,tmp.length());
                emailwin.emailconf.Orbs=tmp.equals("true");
            } else if (tmp.startsWith("Quarantine ")) {
                tmp=tmp.substring(11,tmp.length());
                emailwin.emailconf.Quarantine=tmp.equals("true");
            } else if (tmp.startsWith("AlertSender ")) {
                tmp=tmp.substring(12,tmp.length());
                emailwin.emailconf.AlertSender=tmp.equals("true");
            } else if (tmp.startsWith("IFrame ")) {
                tmp=tmp.substring(7,tmp.length());
                emailwin.emailconf.IFrame=tmp.equals("true");
            } else if (tmp.startsWith("Object ")) {
                tmp=tmp.substring(7,tmp.length());
                emailwin.emailconf.Object=tmp.equals("true");
            } else if (tmp.startsWith("HTML ")) {
                tmp=tmp.substring(5,tmp.length());
                emailwin.emailconf.HTML=tmp.equals("true");
            } else if (tmp.startsWith("Archive ")) {
                tmp=tmp.substring(8,tmp.length());
                emailwin.emailconf.Archive=tmp.equals("true");
            } else if (tmp.startsWith("Backup ")) {
                tmp=tmp.substring(7,tmp.length());
                emailwin.emailconf.Backup=tmp.equals("true");
            } else if (tmp.startsWith("MSBackup ")) {
                tmp=tmp.substring(9,tmp.length());
                emailwin.emailconf.MSBackup=tmp.equals("true");
            } else if (tmp.startsWith("MailExchange1 ")) {
                emailwin.emailconf.MX1=tmp.substring(14,tmp.length());
            } else if (tmp.startsWith("MailExchange2 ")) {
                emailwin.emailconf.MX2=tmp.substring(14,tmp.length());
/*
      } else if (tmp.startsWith("Relay ")) {
        tmp=tmp.substring(6,tmp.length());
        idata=tmp.split(" ");
        emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new EmailRelay(idata[0],idata[1].equals("true"))),
                                          emailwin.relaynode,emailwin.relaynode.getChildCount());
 */
            } else if (tmp.startsWith("POP3 ")) {
                tmp=tmp.substring(5,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 4) {
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],"",
                    idata[3],"",false,"pop3","")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 5) & (! idata[4].equals("true"))) {
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],"",
                    "",idata[3],false,"pop3","")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 5) & (idata[4].equals("true"))) {
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],"",
                    idata[3],"",false,"pop3","")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 6) & (idata[4].equals("true"))) {
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],idata[5],
                    idata[3],"",false,"pop3","")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 6) & (! idata[4].equals("true"))) {
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],idata[5],
                    "",idata[3],false,"pop3","")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 7) & (idata[4].equals("true"))) {
                    if (idata[6].equals("true")) {
                      idata[6]="imap";
                    } else if (idata[6].equals("false")) {
                      idata[6]="pop3";
                    }
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],"",
                    idata[3],"",idata[5].equals("true"),
                    idata[6],"")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 7) & (! idata[4].equals("true"))) {
                    if (idata[6].equals("true")) {
                      idata[6]="imap";
                    } else if (idata[6].equals("false")) {
                      idata[6]="pop3";
                    }
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],"",
                    "",idata[3],idata[5].equals("true"),
                    idata[6],"")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 8) & (idata[4].equals("true"))) {
                    if (idata[7].equals("true")) {
                      idata[7]="imap";
                    } else if (idata[7].equals("false")) {
                      idata[7]="pop3";
                    }
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],idata[5],
                    idata[3],"",idata[6].equals("true"),
                    idata[7],"")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 8) & (! idata[4].equals("true"))) {
                    if (idata[7].equals("true")) {
                      idata[7]="imap";
                    } else if (idata[7].equals("false")) {
                      idata[7]="pop3";
                    }
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],idata[5],
                    "",idata[3],idata[6].equals("true"),
                    idata[7],"")),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 9) & (idata[4].equals("true"))) {
                    if (idata[7].equals("true")) {
                      idata[7]="imap";
                    } else if (idata[7].equals("false")) {
                      idata[7]="pop3";
                    }
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],idata[1],
                    idata[2],idata[5],
                    idata[3],"",idata[6].equals("true"),
                    idata[7],idata[8])),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());
                } else if ((idata.length == 9) & (! idata[4].equals("true"))) {
                    if (idata[7].equals("true")) {
                      idata[7]="imap";
                    } else if (idata[7].equals("false")) {
                      idata[7]="pop3";
                    }
                    emailwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new POP3Collect(idata[0],
                    idata[1],idata[2],idata[5],"",idata[3],idata[6].equals("true"),idata[7],idata[8])),
                    emailwin.multidrop,emailwin.multidrop.getChildCount());

/*
    public POP3Collect(popserver,User,Pass,PopEnvelope, ldomain,uname ssl,mdproto,smtprel){
*/

                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("FMPer ")) {
                tmp=tmp.substring(6,tmp.length());
                emailwin.emailconf.FMPer=new Integer(tmp);
            } else if (tmp.startsWith("FMSHour ")) {
                tmp=tmp.substring(8,tmp.length());
                emailwin.emailconf.FMSHour=new Integer(tmp);
            } else if (tmp.startsWith("FMEHour ")) {
                tmp=tmp.substring(8,tmp.length());
                emailwin.emailconf.FMEHour=new Integer(tmp);
            } else if (tmp.startsWith("FMDay ")) {
                tmp=tmp.substring(6,tmp.length());
                emailwin.emailconf.FMDay=new Integer(tmp);
            } else if (tmp.startsWith("Filter ")) {
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 4) {
                    emailwin.AddFilterRule(idata[0],idata[1].replaceAll("_"," "),idata[3].replaceAll("_"," "),idata[2].replaceAll("_"," "));
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("LDAP ")) {
                tmp=tmp.substring(5,tmp.length());
                if (tmp.startsWith("Login ")) {
                    tmp=tmp.substring(6,tmp.length());
                    emailwin.emailconf.LDAPUN=tmp;
                } else if (tmp.startsWith("Password ")) {
                    tmp=tmp.substring(9,tmp.length());
                    emailwin.emailconf.LDAPPW=tmp;
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("Proxy ")) {
            tmp=tmp.substring(6,tmp.length());
            if (tmp.startsWith("CacheSize ")) {
                squidwin.squidconf.CacheSize=tmp.substring(10,tmp.length());
            } else if (tmp.startsWith("Parent ")) {
                squidwin.squidconf.Parent=tmp.substring(7,tmp.length());
            } else if (tmp.startsWith("Login ")) {
                squidwin.squidconf.Login=tmp.substring(6,tmp.length());
            } else if (tmp.startsWith("Pass ")) {
                squidwin.squidconf.Pass=tmp.substring(5,tmp.length());
            } else if (tmp.startsWith("Redir ")) {
                squidwin.squidconf.Redir=tmp.substring(6,tmp.length());
            } else if (tmp.startsWith("Redirect ")) {
                squidwin.squidconf.FilterRedirect=tmp.substring(9,tmp.length());
            } else if (tmp.startsWith("LogFQDN ")) {
                tmp=tmp.substring(8,tmp.length());
                squidwin.squidconf.fqdnlog=tmp.equals("true");
            } else if (tmp.startsWith("Access ")) {
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 3) {
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ProxyACL(idata[0],idata[1],
                    idata[2].equals("true"))),
                    squidwin.ipaccess,squidwin.ipaccess.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Bypass ")) {
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 2 ) {
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ProxyBypass(idata[0],idata[1])),
                    squidwin.ipbypass,squidwin.ipbypass.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("FilterList ")) {
                tmp=tmp.substring(11,tmp.length());
                squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(tmp),
                squidwin.flists,squidwin.flists.getChildCount());
            } else if (tmp.startsWith("Allow ")) {
                tmp=tmp.substring(6,tmp.length());
                if (tmp.startsWith("URL ")) {
                    tmp=tmp.substring(4,tmp.length());
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ListItem(tmp)),
                    squidwin.allowurl,squidwin.allowurl.getChildCount());
                } else if (tmp.startsWith("Keyword ")) {
                    tmp=tmp.substring(8,tmp.length());
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ListItem(tmp)),
                    squidwin.allowkey,squidwin.allowkey.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Deny ")) {
                tmp=tmp.substring(5,tmp.length());
                if (tmp.startsWith("URL ")) {
                    tmp=tmp.substring(4,tmp.length());
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ListItem(tmp)),
                    squidwin.denyurl,squidwin.denyurl.getChildCount());
                } else if (tmp.startsWith("Keyword ")) {
                    tmp=tmp.substring(8,tmp.length());
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ListItem(tmp)),
                    squidwin.denykey,squidwin.denykey.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("TimeGroup ")) {
                tmp=tmp.substring(10,tmp.length());
                idata=tmp.split(" ");
                tmpnode=squidwin.getGroupNode(squidwin.timezones,idata[0].replaceAll("_"," "));
                if (idata.length == 1) {
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ListItem(tmp.replaceAll("_"," "))),
                    squidwin.timezones,squidwin.timezones.getChildCount());
                } else if ((idata.length >= 4) && (tmpnode != null)){
                    String days="";
                    for (int dcnt=3;dcnt < idata.length;dcnt++) {
                        days=days+" "+idata[dcnt];
                    }
                    days=days.trim()+" ";
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new TimeSpace(days,idata[1],
                    idata[2])),
                    tmpnode,tmpnode.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("SourceGroup ")) {
                tmp=tmp.substring(12,tmp.length());
                idata=tmp.split(" ");
                tmpnode=squidwin.getGroupNode(squidwin.filtergrps,idata[0].replaceAll("_"," "));
                if (idata.length == 1) {
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new ListItem(tmp.replaceAll("_"," "))),
                    squidwin.filtergrps,squidwin.filtergrps.getChildCount());
                } else if ((idata.length == 3) && (tmpnode != null)) {
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FilterMember(idata[1],idata[2])),
                    tmpnode,tmpnode.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("ACL ")) {
                String defaulta="";
                tmp=tmp.substring(4,tmp.length());
                idata=tmp.split(" ");
                DefaultMutableTreeNode tznode=squidwin.getGroupNode(squidwin.timezones,idata[1].replaceAll("_"," "));
                DefaultMutableTreeNode agnode=squidwin.getGroupNode(squidwin.filtergrps,idata[0].replaceAll("_"," "));


                if (idata.length == 4) {
                    if (idata[3].equals("none")) {
                        defaulta="Deny All Other Sites";
                    } else if (idata[3].equals("all")) {
                        defaulta="Accept All Other Sites";
                    } else {
                        defaulta="Pass To Other Rules";
                    }
                } else if (idata.length == 6) {
                    if (idata[5].equals("none")) {
                        defaulta="Deny All Other Sites";
                    } else if (idata[5].equals("all")) {
                        defaulta="Accept All Other Sites";
                    } else {
                        defaulta="Pass To Other Rules";
                    }
                } else {
                    defaulta="Pass To Other Rules";
                }

                tmpnode=squidwin.getFilterACLNode(idata[0].replaceAll("_"," "),idata[1].replaceAll("_"," "),
                                                  defaulta,idata[2].equals("true"));
                if ((tmpnode == null) && (agnode != null) && (tznode !=null) && (idata.length >=4)) {
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FilterACL(idata[0].replaceAll("_"," "),
                                                          idata[1].replaceAll("_"," "),defaulta,idata[2].equals("true"))),
                                                      squidwin.afilter,squidwin.afilter.getChildCount());
                    tmpnode=squidwin.getFilterACLNode(idata[0].replaceAll("_"," "),idata[1].replaceAll("_"," "),
                                                      defaulta,idata[2].equals("true"));
                }
                if ((idata.length > 4) && (tmpnode != null)) {
                    String fclist="";
                    if (idata[3].equals("local_allow")) {
                        fclist="Local Allowed";
                    } else if (idata[3].equals("local_deny")) {
                        fclist="Local Denied";
                    } else if (idata[3].equals("in-addr")) {
                        fclist="Non Resolved Sites";
                    } else {
                        fclist=idata[3];
                    }
                    
                    squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FilterCTRL(fclist,idata[4].equals("true"))),
                    tmpnode,tmpnode.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else {
                System.out.println(tmp);
                confString="";
            }
/*
        } else if (tmp.startsWith("WWW Site ")) {
            tmp=tmp.substring(9,tmp.length());
            idata=tmp.split(" ");
            if (idata.length >= 4) {
              StdUser user=users.getValidUser(idata[1]);
              if (user != null ) {
                if (idata.length == 5) {
                    apachewin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new HostedSite(idata[0],idata[1],idata[2],idata[3],
                    idata[4].equals("true"),"","","","","","","")),
                    apachewin.hostedsite,apachewin.hostedsite.getChildCount());
                } else if (idata.length == 4) {
                    apachewin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new HostedSite(idata[0],idata[1],idata[2],idata[3],
                    false,"","","","","","","")),
                    apachewin.hostedsite,apachewin.hostedsite.getChildCount());
                } else if (idata.length == 12) {
                    apachewin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new HostedSite(idata[0],idata[1],idata[2],idata[3],
                    idata[4].equals("true"),idata[5].replaceAll("_"," "),idata[6].replaceAll("_"," "),
                    idata[7].replaceAll("_"," "),idata[8].replaceAll("_"," "),idata[9].replaceAll("_"," "),
                    idata[10].replaceAll("_"," "),idata[11].replaceAll("_"," "))),
                    apachewin.hostedsite,apachewin.hostedsite.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
              } else {
                System.out.println(tmp);
                confString="";
              }
            } else {
                System.out.println(tmp);
                confString="";
            }
*/
        } else if (tmp.startsWith("WWW Redirect ")) {
            tmp=tmp.substring(13,tmp.length());
            idata=tmp.split(" ");
            if (idata.length == 3) {
                squidwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new RedirectedSite(idata[0],idata[1],idata[2])),
                squidwin.redirsite,squidwin.redirsite.getChildCount());
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("Serial ")) {
            serkey=tmp.substring(7,tmp.length());
        } else if (tmp.startsWith("FileServer ")) {
            tmp=tmp.substring(11,tmp.length());
            if (tmp.startsWith("Config ")) {
                tmp=tmp.substring(7,tmp.length());
                if (tmp.startsWith("netbios name = ")) {
                    sambawin.sambaconf.ServerName=tmp.substring(15,tmp.length());
                } else if (tmp.startsWith("server string = ")) {
                    sambawin.sambaconf.Description=tmp.substring(16,tmp.length());
                } else if (tmp.startsWith("os level = ")) {
                    sambawin.sambaconf.OSLevel=tmp.substring(11,tmp.length());
                } else if (tmp.startsWith("preferred master = ")) {
                    sambawin.sambaconf.PMaster=tmp.substring(19,tmp.length()).equals("Yes");
                } else if (tmp.startsWith("local master = ")) {
                    sambawin.sambaconf.LMaster=tmp.substring(15,tmp.length()).equals("Yes");
                } else if (tmp.startsWith("domain master = ")) {
                    sambawin.sambaconf.DMaster=tmp.substring(16,tmp.length()).equals("Yes");
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Option ")) {
                tmp=tmp.substring(7,tmp.length());
                if (tmp.startsWith("Domain ")) {
                    sambawin.sambaconf.WorkGroup=tmp.substring(7,tmp.length());
                } else if (tmp.startsWith("Security ")) {
                    sambawin.sambaconf.Authentication=tmp.substring(9,tmp.length());
                } else if (tmp.startsWith("Winbind ")) {
                    sambawin.sambaconf.Winbind=tmp.substring(8,tmp.length());
                } else if (tmp.startsWith("ADSRealm ")) {
                    sambawin.sambaconf.ADSRealm=tmp.substring(9,tmp.length());
                } else if (tmp.startsWith("ADSServer ")) {
                    sambawin.sambaconf.ADSServer=tmp.substring(10,tmp.length());
                } else if (tmp.startsWith("RemoteSync ")) {
                    sambawin.sambaconf.RemoteBrowse=tmp.substring(11,tmp.length());
                }
            } else if (tmp.startsWith("LPT1 ")) {
                tmp=tmp.substring(5,tmp.length());
                sambawin.sambaconf.Printer1=tmp;
            } else if (tmp.startsWith("LPT2 ")) {
                tmp=tmp.substring(5,tmp.length());
                sambawin.sambaconf.Printer2=tmp;
            } else if (tmp.startsWith("AVMaxSize ")) {
                tmp=tmp.substring(10,tmp.length());
                sambawin.sambaconf.AVMaxSize=tmp;
            } else if (tmp.startsWith("AVMaxThread ")) {
                tmp=tmp.substring(12,tmp.length());
                sambawin.sambaconf.AVMaxThread=tmp;
            } else if (tmp.startsWith("AVHome")) {
                sambawin.sambaconf.AVHome=true;
            } else if (tmp.startsWith("AVShare")) {
                sambawin.sambaconf.AVShare=true;
            } else if (tmp.startsWith("BUHome")) {
                sambawin.sambaconf.BUHome=true;
            } else if (tmp.startsWith("BUShare")) {
                sambawin.sambaconf.BUShare=true;
            } else if (tmp.startsWith("BUWWW")) {
                sambawin.sambaconf.BUWWW=true;
            } else if (tmp.startsWith("BUFTP")) {
                sambawin.sambaconf.BUFTP=true;
            } else if (tmp.startsWith("UProfile")) {
                sambawin.sambaconf.UProfile=true;
            } else if (tmp.startsWith("Controler ")) {
                tmp=tmp.substring(10,tmp.length());
                sambawin.sambaconf.DControl=true;
                idata=tmp.split(" ");
                sambawin.sambaconf.HomeDrive=new Character(idata[0].toCharArray()[0]);
                sambawin.sambaconf.ShareDrive=new Character(idata[1].toCharArray()[0]);
                sambawin.sambaconf.Authentication="USER";
		sambawin.sambaconf.ADSServer="";
		sambawin.sambaconf.ADSRealm="";
            } else if (tmp.startsWith("Share ")) {
                tmp=tmp.substring(6,tmp.length());
                idata=tmp.split(" ");
                if (idata.length >= 4) {
                    idata[0].replaceAll("_"," ");
                    idata[1].replaceAll("_"," ");
                    idata[3].replaceAll("_"," ");
                    //          if ((! idata[3].equals("www")) & (!idata[3].equals("smbadm")) & (! idata[3].equals("users"))) {
                    //            idata[3]=groups.getValidGroup(idata[3]);
                    //          }
                    if (idata[3] != null) {
                        if (idata.length == 4) {
                            sambawin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FileShare(idata[0],idata[1],idata[3],
                            idata[2].equals("true"),false,false,false,"")),
                            sambawin.shares,sambawin.shares.getChildCount());
                        } else if (idata.length == 5) {
                            sambawin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FileShare(idata[0],idata[1],idata[3],
                            idata[2].equals("true"),idata[4].equals("true"),false,false,"")),
                            sambawin.shares,sambawin.shares.getChildCount());
                            
                        } else if (idata.length == 6) {
                            sambawin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FileShare(idata[0],idata[1],idata[3],
                            idata[2].equals("true"),idata[4].equals("true"),idata[5].equals("true"),
                            false,"")),
                            sambawin.shares,sambawin.shares.getChildCount());
                        } else if (idata.length == 7) {
                            if ((idata[6].equals("true")) | (idata[6].equals("false"))) {
                              sambawin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FileShare(idata[0],idata[1],idata[3],
                              idata[2].equals("true"),idata[4].equals("true"),idata[5].equals("true"),
                              idata[6].equals("true"),"")),
                              sambawin.shares,sambawin.shares.getChildCount());
                            } else {
                              sambawin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FileShare(idata[0],idata[1],idata[3],
                              idata[2].equals("true"),idata[4].equals("true"),idata[5].equals("true"),
                              false,idata[6])),
                              sambawin.shares,sambawin.shares.getChildCount());           
                            }
                        } else if (idata.length == 8) {
                            sambawin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new FileShare(idata[0],idata[1],idata[3],
                            idata[2].equals("true"),idata[4].equals("true"),idata[5].equals("true"),
                            idata[6].equals("true"),idata[7])),
                            sambawin.shares,sambawin.shares.getChildCount());
                        } else {
                            System.out.println(tmp);
                            confString="";
                        }
                    } else {
                        System.out.println(tmp);
                        confString="";
                    }
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Mapping ")) {
                tmp=tmp.substring(8,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 3) {
                    idata[0].replaceAll("_"," ");
                    idata[1].replaceAll("_"," ");
                    sambawin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new MappedDrive(idata[0],idata[1],idata[2])),
                    sambawin.maps,sambawin.maps.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("Cron ")) {
            tmp=tmp.substring(5,tmp.length());
            idata=tmp.split(" ");
            if (idata.length == 5) {
                if (idata[4].equals("*")) {
                    idata[4]="Everyday";
                } else if (idata[4].equals("mon-fri")) {
                    idata[4]="Monday To Friday";
                } else if (idata[4].equals("mon-sat")) {
                    idata[4]="Monday To Saturday";
                } else if (idata[4].equals("sat")) {
                    idata[4]="Saturday";
                } else if (idata[4].equals("sun")) {
                    idata[4]="Sunday";
                }
                idata[0]=idata[0].replaceAll("_"," ");
                cronwin.treeModel.insertNodeInto(new DefaultMutableTreeNode(new CronJob(idata[0],idata[1],idata[2],idata[3],idata[4])),
                cronwin.topbranch,cronwin.topbranch.getChildCount());
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("X509 ")) {
            tmp=tmp.substring(5,tmp.length());
            if (tmp.startsWith("Config ")) {
                tmp=tmp.substring(7,tmp.length());
                idata=tmp.split(" ");
                if (tmp.startsWith("Country ")) {
                    tmp=tmp.substring(8,tmp.length());
                    network.caconf.Country=tmp;
                } else if (tmp.startsWith("State ")) {
                    tmp=tmp.substring(6,tmp.length());
                    network.caconf.State=tmp;
                } else if (tmp.startsWith("City ")) {
                    tmp=tmp.substring(5,tmp.length());
                    network.caconf.City=tmp;
                } else if (tmp.startsWith("Company ")) {
                    tmp=tmp.substring(8,tmp.length());
                    network.caconf.Company=tmp;
                } else if (tmp.startsWith("Division ")) {
                    tmp=tmp.substring(9,tmp.length());
                    network.caconf.Division=tmp;
                } else if (tmp.startsWith("Name ")) {
                    tmp=tmp.substring(5,tmp.length());
                    network.caconf.Name=tmp;
                } else if (tmp.startsWith("Email ")) {
                    tmp=tmp.substring(6,tmp.length());
                    network.caconf.Email=tmp;
                } else if (tmp.startsWith("Locked ")) {
                    tmp=tmp.substring(7,tmp.length());
                    network.caconf.Changeable=tmp.equals("true");
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("LDAP ")) {
            tmp=tmp.substring(5,tmp.length());
            if (tmp.startsWith("AnonRead ")) {
                tmp=tmp.substring(9,tmp.length());
                ldapconfig.guestaccess=tmp.equals("true");
            } else if (tmp.startsWith("ReplicateDN ")) {
                tmp=tmp.substring(12,tmp.length());
                ldapconfig.repdn=tmp;
            } else if (tmp.startsWith("Backup ")) {
                tmp=tmp.substring(7,tmp.length());
                ldapconfig.ldapbu=tmp.equals("true");
            } else if (tmp.startsWith("Addressbook ")) {
                tmp=tmp.substring(12,tmp.length());
                idata=tmp.split(" ");
                if (idata.length == 2) {
                    ldapconfig.treeModel.insertNodeInto(new DefaultMutableTreeNode(new LDAPpath(idata[0],idata[1].equals("true"))),
                    ldapconfig.abooks,ldapconfig.abooks.getChildCount());
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else if (tmp.startsWith("Replica ")) {
                tmp=tmp.substring(8,tmp.length());
                idata=tmp.split(" ");
/*
                if (idata.length == 4) {
                    ldapconfig.treeModel.insertNodeInto(new DefaultMutableTreeNode(new LDAPReplicate(idata[0],idata[1],idata[2].equals("true"))),
                    ldapconfig.replica,ldapconfig.replica.getChildCount());
                } else if (idata.length == 2) {
*/
                if (idata.length == 2) {
                  ldapconfig.EditRep.Host=idata[0];
                  ldapconfig.EditRep.RID="1";
                  ldapconfig.EditRep.UseSSL=idata[1].equals("true");
                } else if (idata.length == 3) {
                  ldapconfig.EditRep.Host=idata[0];
                  ldapconfig.EditRep.RID=idata[1];
                  ldapconfig.EditRep.UseSSL=idata[2].equals("true");
                } else {
                    System.out.println(tmp);
                    confString="";
                }
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("SQL ")) {
            tmp=tmp.substring(4,tmp.length());
            if (tmp.startsWith("Password ")) {
                tmp=tmp.substring(9,tmp.length());
                ldapconfig.sqlpass=tmp;
            } else if (tmp.startsWith("WebmailPass ")) {
                tmp=tmp.substring(12,tmp.length());
                ldapconfig.hordepass=tmp;
            } else if (tmp.startsWith("IDPass ")) {
                tmp=tmp.substring(7,tmp.length());
                ldapconfig.ulogdpass=tmp;
            } else if (tmp.startsWith("Server ")) {
                tmp=tmp.substring(7,tmp.length());
                ldapconfig.sqlserv=tmp;
            } else if (tmp.startsWith("Radius ")) {
                tmp=tmp.substring(7,tmp.length());
                ldapconfig.radiuspass=tmp;
            } else if (tmp.startsWith("RadiusServ ")) {
                tmp=tmp.substring(11,tmp.length());
                ldapconfig.radiusserver=tmp;
            } else if (tmp.startsWith("Control ")) {
                tmp=tmp.substring(8,tmp.length());
                ldapconfig.sqlcontpass=tmp;
            } else if (tmp.startsWith("Forum ")) {
                tmp=tmp.substring(6,tmp.length());
                ldapconfig.sqlforumpass=tmp;
            } else if (tmp.startsWith("Backup ")) {
                tmp=tmp.substring(7,tmp.length());
                ldapconfig.sqlbu=tmp.equals("true");
            } else if (tmp.startsWith("PGAdmin ")) {
                ldapconfig.pgadmin=tmp.substring(8,tmp.length());
            } else if (tmp.startsWith("PGExchange ")) {
                ldapconfig.pgexchange=tmp.substring(11,tmp.length());
            } else if (tmp.startsWith("Cubit ")) {
                ldapconfig.cubitpass=tmp.substring(6,tmp.length());
            } else if (tmp.startsWith("Asterisk ")) {
                ldapconfig.asteriskpass=tmp.substring(9,tmp.length());
            } else if (tmp.startsWith("AsteriskServ ")) {
                ldapconfig.asteriskserver=tmp.substring(13,tmp.length());
            } else if (tmp.startsWith("MAsterisk ")) {
                ldapconfig.asteriskmpass=tmp.substring(10,tmp.length());
            } else if (tmp.startsWith("MAsteriskServ ")) {
                ldapconfig.asteriskmserver=tmp.substring(14,tmp.length());
            } else if (tmp.startsWith("OpSecret ")) {
                ldapconfig.voipsecret=tmp.substring(9,tmp.length());
            } else if (tmp.startsWith("OpHostname ")) {
                ldapconfig.voiphostname=tmp.substring(11,tmp.length());
            } else if (tmp.startsWith("VBackup ")) {
                tmp=tmp.substring(8,tmp.length());
                ldapconfig.voipbu=tmp.equals("true");
            } else {
                System.out.println(tmp);
                confString="";
            }
        } else if (tmp.startsWith("System ")) {
            tmp=tmp.substring(7,tmp.length());
            if (tmp.startsWith("Type ")) {
                tmp=tmp.substring(5,tmp.length());
                isfullconf=tmp.equals("full");
                confString="";
            } else {
              System.out.println("System "+tmp);
              confString="";
            }
        } else {
          System.out.println(tmp);
          confString="";
        }
        
        if (confString.length() > 0) {
            confString=confString+newline;
        }
        return confString;
    }
}

class EmailConf {
    String Dmethod,SmartHost,MaxMsgSize,MX1,MX2,Redirect,Domain,DNS,MSChild,Ziplevel,Rescan,MaxScore,MinScore,LDAPUN,LDAPPW;
    boolean Orbs,Quarantine,AlertSender,IFrame,Object,HTML,Archive,Backup,MSBackup;
    Integer FMPer,FMSHour,FMEHour,FMDay;
    public EmailConf(){
        delConfig();
    }
    public void delConfig() {
        FMPer=new Integer(20);
        FMSHour=new Integer(8);
        FMEHour=new Integer(18);
        FMDay=new Integer(0);
        Dmethod="Deffered";
        SmartHost="";
        MaxMsgSize="10";
        MX1="";
        MX2="";
        LDAPUN="";
        LDAPPW="";
        Redirect="";
        Orbs=true;
        Quarantine=false;
        AlertSender=false;
        IFrame=false;
        Object=false;
        HTML=false;
        Archive=false;
        Backup=false;
        MSBackup=false;
        Domain="";
        DNS="";
        MSChild="1";
        Ziplevel="0";
        Rescan="2";
        MaxScore="4";
        MinScore="2";
    }
}

class SquidConf {
    String CacheSize,Parent,FilterRedirect,Redir,Login,Pass;
    boolean fqdnlog;
    public SquidConf(){
        delConfig();
    }
    public void delConfig(){
        CacheSize="1";
        Parent="";
        FilterRedirect="squidguard.php?clientaddr=%a&clientname=%n&clientident=%i&clientgroup=%s&destinationgroup=%t&url=%u";
        fqdnlog=true;
	Redir="5";
        Login="";
        Pass="";
    }
}

class DnsConf {
    String Domain,Hostname,Serial,Refresh,Retry,Expire,DefaultTTL,DynDNSIP,DynDNSDomain,DynDNSSecret,DynDNSSecret2,Search;
    boolean Backup,Auth,Usepeer,Recursion,AuthX,Intfirst;
    public DnsConf(){
        delConfig();
    }
    public void delConfig() {
        Domain="";
        Hostname="";
        Search="";
        Serial="1";
        Refresh="3600";
        Retry="1800";
        Expire="604800";
        DefaultTTL="3600";
        DynDNSIP="";
        DynDNSDomain="";
        DynDNSSecret="secret";
        DynDNSSecret2="secret";
        Backup=false;
        Auth=true;
        AuthX=false;
        Usepeer=false;
        Recursion=false;
        Intfirst=false;
    }
}

class IntDef {
    String Description,IPAddress,IPSubnet,IPStart,IPEnd,IntName,Ingress,Egress,MAC,IPGateway;
    public IntDef(String Descrip,String Addr,String Snet,String IPSt,String IPE,String IName,
                  String bwin,String bwout,String maddr,String gateway){
        Description=Descrip;
        IPAddress=Addr;
        IPSubnet=Snet;
        IPStart=IPSt;
        IPEnd=IPE;
        IntName=IName;
        Ingress=bwin;
        Egress=bwout;
        if (maddr.equals("")) {
          MAC="00:00:00:00:00:00";
        } else {
          MAC=maddr;
        }
        IPGateway=gateway;
    }
    public String toString(){
        String Output=Description+" ("+IntName+" "+IPAddress+"/"+IPSubnet+")";
        return Output;
    }
    public String getIfName(){
        String Output=Description+" ("+IntName+" "+IPAddress+"/"+IPSubnet+")";
        return Output;
    }
}


class HostInf {
    String IPAddress,HostName,MACAddr;
    public HostInf(String Addr,String Hname,String MACA){
        IPAddress=Addr;
        HostName=Hname;
        MACAddr=MACA;
    }
    public String toString(){
        String Output=HostName+" ("+IPAddress+")";
        return Output;
    }
}

class RecordInf {
    String Record,Type,Entry;
    public RecordInf(String rname,String rtype,String rentry){
        Record=rname;
        Type=rtype;
        Entry=rentry;
    }
    public String toString(){
        String Rout;
        
        if (Record.length() == 0) {
            Rout=" - ";
        }
        else {
            Rout=Record;
        }
        String Output=Rout+" ("+Type+" "+Entry+")";
        return Output;
    }
}

class POP3Collect {
    String Server,UserName,Password,Envelope,Domain,SMTP,MProto;
    boolean Multidrop,UseSSL;
    public POP3Collect(String popserver,String User,String Pass,String PopEnvelope,String ldomain,boolean mdrop,
    boolean ssl,String mdproto,String smtprel){
        Server=popserver;
        if (User.equals("")) {
            UserName="-";
        } else {
          UserName=User;
        }
        if (Pass.equals("")) {
          Password="-";
        } else {
          Password=Pass;
        }
        Envelope=PopEnvelope;
        UseSSL=ssl;
        MProto=mdproto;
        SMTP=smtprel;
        
        if (Envelope.equals("-")) {
            Envelope="";
        }
        
        Domain=ldomain;
        Multidrop=mdrop;
        
    }

    public POP3Collect(String popserver,String User,String Pass,String PopEnvelope,String ldomain,String uname,
    boolean ssl,String mdproto,String smtprel){
        Server=popserver;
        if (User.equals("")) {
            UserName="-";
        } else {
          UserName=User;
        }
        if (Pass.equals("")) {
          Password="-";
        } else {
          Password=Pass;
        }
        Envelope=PopEnvelope;
        UseSSL=ssl;
        MProto=mdproto;
        SMTP=smtprel;
        
        if (Envelope.equals("-")) {
            Envelope="";
        }
        
        if (ldomain.equals("")) {
          Domain=uname;
          Multidrop=false;
        } else {
          Domain=ldomain;
          Multidrop=true;
        }
        
    }
    public String toString(){
        String Output;
        if (Multidrop) {
            Output=Server+" "+"("+UserName+" Deliver For "+Domain+" [Multidrop])";
        } else {
            Output=Server+" "+"("+UserName+" Deliver To "+Domain+")";
        }
        return Output;
    }
}

class EmailRelay {
    String Domain;
    boolean Accept;
    public EmailRelay(String domainname,boolean allowrelay){
        Domain=domainname;
        Accept=allowrelay;
    }
    public String toString(){
        String Output;
        if (Accept) {
            Output="Allow "+Domain;
        } else {
            Output="Deny "+Domain;
        };
        return Output;
    }
}

class VirtualAlias {
    String Alias,Address;
    public VirtualAlias(String valias,String vaddress){
        Alias=valias;
        Address=vaddress;
    }
    public String toString(){
        String Output;
        Output=Alias+"("+Address+")";
        return Output;
    }
}

class CronJob {
    String Task,Period,StartHour,EndHour,Days;
    public CronJob(String task,String period,String fromh,String toh,String days){
        Task=task;
        Period=period;
        StartHour=fromh;
        EndHour=toh;
        Days=days;
    }
    public String toString(){
        String Output;
        Output=Task+" (Every "+Period+" Minutes From "+StartHour+" To "+EndHour+" "+Days+")";
        return Output;
    }
}

class WanDef {
    String Description,IPAddress,IPSubnet,IPStart,IPEnd,LGateway,RGateway;
    public WanDef(String Descrip,String Addr,String Snet,String IPSt,String IPE,String Lgw,String Rgw){
        Description=Descrip;
        IPAddress=Addr;
        IPSubnet=Snet;
        IPStart=IPSt;
        IPEnd=IPE;
        LGateway=Lgw;
        RGateway=Rgw;
    }
    public String toString(){
        String Output=Description+" ("+LGateway+"-->"+RGateway+"-->"+IPAddress+"/"+IPSubnet+")";
        return Output;
    }
}


class GenralRoute {
    String Description,IPAddress,IPSubnet,Gateway;
    public GenralRoute(String Descrip,String Addr,String Snet,String Lgw){
        Description=Descrip;
        IPAddress=Addr;
        IPSubnet=Snet;
        Gateway=Lgw;
    }
    public String toString(){
        String Output=Description+" ("+Gateway+"-->"+IPAddress+"/"+IPSubnet+")";
        return Output;
    }
}


class WiFiConfig {
    String key,apmac,channel,essid,name,power,rate,device;
    public WiFiConfig(String idevice, String iessid,String iname,String ikey,
                      String iapmac,String ichannel,String ipower,String irate){
        device=idevice;
        essid=iessid;
        name=iname;
        key=ikey;
        apmac=iapmac;        
        channel=ichannel;
        power=ipower;
	rate=irate;
    }
    public String toString(){
        String Output=device+" ("+name+"/"+essid+" "+rate+"Mb/s ch "+channel+"/"+power+"mW)";
        return Output;
    }
}


class ExtraAdslLink {
    String Description,User,Pass,Ingress,Egress,TOS,RIP,VIP,Port,Service;
    public ExtraAdslLink(String Descrip,String Username,String Password,String Inlimit,
                         String Outlimit,String mTOS,String port,String service,String virtip,String remip){
        Description=Descrip;
        User=Username;
        Pass=Password;
        Ingress=Inlimit;
        Egress=Outlimit;
        TOS=mTOS;
        Port=port;
        Service=service;
	VIP=virtip;
	RIP=remip;
    }
    public String toString(){
        String Output=Description+" ("+Port+" "+Service+" "+User+" "+Ingress+"kbps (In) "+Egress+"kbps (Out))";
        return Output;
    }
}

class TOSConfig {
    String Description,Address,Dest,Src,Protocol,TOS,Ingress;
    public TOSConfig(String Descrip,String Addr,String DestP,String SrcP,
                         String PCL,String TVAL,String IGRE){
        Description=Descrip;
        Address=Addr;
        Dest=DestP;
        Src=SrcP;
        Protocol=PCL;
        TOS=TVAL;
        Ingress=IGRE;
    }
    public String toString(){
        String Output=Description+" ("+Protocol+" "+Src+" --> "+Address+":"+Dest+" With "+TOS+" "+Ingress+" Priority)";
        return Output;
    }
}

class GreDef {
    String LocalIP,RemoteIP,LocalINT,MTU,CRLURL;
    int Ipsec;
    static String ipsectype[]={"Negotiate Encryption","Dont Use Encryption","Enforce Encryption"};
    public GreDef(String localip,String remoteip,String lint,String mtu,int ipsec,String crlurl) {
        LocalIP=localip;
        RemoteIP=remoteip;
        LocalINT=lint;
        MTU=mtu;
        Ipsec=ipsec;
        CRLURL=crlurl;
    }
    public String toString(){
        String Output=RemoteIP+" <-> "+LocalINT+" <-> "+LocalIP;
        if (Ipsec == 0) {
          Output=Output+" Attempt Encryption";
        } else if (Ipsec == 1) {
          Output=Output+" Force Encryption";
        } else {
          Output=Output+" No Encryption";
        }
        return Output;
    }
}

class EspDef {
    String Address,Mode,IDType,IDVal,PIDType,PIDVal,Cipher,Hash,DHGroup,PFSGroup,Local,Remote,Test,InitType,Interface;
    boolean shortconf;

    public EspDef(String addr,String local,String remote,String test,String cipher,String hash,
                  String dhgroup) {
      Address=addr;
/*
      Mode=mode;
      IDType=idtype;
      IDVal=idval;
      PIDType=pidtype;
      PIDVal=pidval;
      PFSGroup=pfsgroup;
*/
      Cipher=cipher;
      Hash=hash;
      DHGroup=dhgroup;
      Local=local;
      Remote=remote;
      Test=test;
      shortconf=false;
      Interface="";
    }
    public EspDef(String addr,String local,
                  String remote,String test,String ctype) {
      Address=addr;
      Local=local;
      Remote=remote;
      Test=test;
      shortconf=true;
      InitType=ctype;
      Interface="";
    }
    public EspDef(String local,String remote) {
      Local=local;
      Remote=remote;
      Address="";
      Test="";
      shortconf=true;
      Interface="";
    }
    public EspDef(String local,String remote,String iface) {
      Local=local;
      Remote=remote;
      Address="";
      Test="";
      shortconf=true;
      Interface=iface;
    }
    public String getConf(){
      String Output=Address+" ";
      if (! shortconf) {
        Output=Address+" "+Local+" "+Remote+" "+Test+" "+Cipher+" "+Hash+" "+DHGroup;
      } else if (Test.length() > 0) {
        Output=Address+" "+Local+" "+Remote+" "+Test+" "+InitType;
      } else if (Address.length() > 0) {
        Output=Address+" "+Local+" "+Remote+" "+InitType;
      } else {
        Output=Local+" "+Remote+" "+Interface;
      }
      return Output;
    }
    public String toString(){
	String Output;
	if (Address.length() > 0) {
          Output=Address+" <-> "+Remote+" <-> "+Local;
          if (! shortconf) {
            Output=Output+" ("+InitType+" "+Cipher+")";
          } else {
            Output=Output+" ("+InitType+")";
          }
        } else if (Interface.length() > 0) {
          Output=Local+" <--> "+Remote+" ("+Interface+")";
        } else {
          Output=Local+" <--> "+Remote;
        }
        return Output;
    }
}

class VoipReg {
    String Username,Password,Address,Authtype,Key,AuthContext,Extension;
    boolean isiax2;
    public VoipReg(String user,String pass,String addr,String atype,String keyname,
                   String acont) {
      Username=user;
      Password=pass;
      Address=addr;
      Authtype=atype;
      Key=keyname;
      AuthContext=acont;
      isiax2=true;
    }
    public VoipReg(String user,String pass,String addr,String context,String exten) {
      Username=user;
      Password=pass;
      Address=addr;
      AuthContext=context;
      Extension=exten;
      isiax2=false;
    }
    public String getConf(){
      String Output="IP VOIP ";
      if (isiax2) {
        Output=Output+"IAX "+Username+" "+Password+" "+Address+" "+Authtype+" "+AuthContext+" "+Key;
      } else {
        Output=Output+"SIP "+Username+" "+Password+" "+Address+" "+AuthContext;
        if (Extension.length() > 0) {
          Output=Output+" "+Extension;
        }
      }
      return Output;
    }
    public String toString(){
      String Output=Username+"@"+Address;
      if (isiax2) {
        Output=Output+" ("+AuthContext+")";
      } else if (Extension.length() > 0) {
        Output=Output+" ("+Extension+"@"+AuthContext+")";
      } else {
        Output=Output+" ("+AuthContext+")";
      }
      return Output;
    }
}



class TrustUser {
    String PCName,Description;
    public TrustUser(String PCN,String Descrip){
        PCName=PCN;
        Description=Descrip;
    }
    public String toString(){
        String Output=PCName+" "+Description;
        return Output;
    }
}


class StdUser {
    String UserName,Password,FirstName,LastName,NetAlias;
    boolean Proxy,Radius;
    public StdUser(String User,String Pass,String Fname,String Lname,String NAlias,boolean Paccess,boolean RAS){
        UserName=User;
        Password=Pass;
        FirstName=Fname;
        LastName=Lname;
        NetAlias=NAlias;
        Proxy=Paccess;
        Radius=RAS;
    }
    public String toString(){
        String Output=UserName+" "+"("+FirstName+" "+LastName+")";
        return Output;
    }
}

class RadClient {
    String Host,Secret,Alias;
    public RadClient(String nas,String secret,String alias){
        Host=nas;
        Secret=secret;
        Alias=alias;
    }
    public String toString(){
        String Output=Host+" "+"("+Alias+")";
        return Output;
    }
}

class RadRealm {
    String Realm,Secret,Auth,Acct;
    boolean RoundRobin,NoStrip;
    public RadRealm(String domain,String secret,String auth,String acct,boolean rrobin,boolean strip){
        Realm=domain;
        Secret=secret;
        Auth=auth;
        Acct=acct;
        RoundRobin=rrobin;
        NoStrip=strip;
    }
    public String toString(){
        String Output=Realm+" "+"("+Auth+" "+Acct+")";
        return Output;
    }
}

class RadLink {
    String TTY,Remote,Local,CType,Speed,PType,MTU;
    public RadLink(String tty,String remip,String locip,String ctype,
                    String speed,String ptype,String mtu){
        TTY=tty;
        Remote=remip;
        Local=locip;
        CType=ctype;
        Speed=speed;
        PType=ptype;
        MTU=mtu;
    }
    public String toString(){
        String Output=TTY+" "+"("+CType+" "+PType+" "+Speed+" "+Local+":"+Remote+")";
        return Output;
    }
}

class NFSMount {
    String Name,Folder,Source,Bind,User,Pass,UID,GID;
    boolean Read,Backup,VScan;
    public NFSMount(String dscrip,String folder,String source,String bind,String smbuname,
    String smbpass,String smbuid,String smbgid,boolean smbro,boolean fbup,boolean vscan){
        Name=dscrip.replaceAll("_"," ");
        Folder=folder;
        Source=source.replaceAll("_","");
        if (bind.equals("")) {
            Bind="-";
        } else {
            Bind=bind;
        }
        if (Source.startsWith("//")) {
            if (smbuname.equals("-")) {
                User="";
            } else {
                User=smbuname;
            }
            if (smbpass.equals("-")) {
                Pass="";
            } else {
                Pass=smbpass;
            }
            if (smbuid.equals("-")) {
                UID="";
            } else {
                UID=smbuid;
            }
            if (smbgid.equals("-")) {
                GID="";
            } else {
                GID=smbgid;
            }
            Read=smbro;
            Backup=fbup;
            VScan=vscan;
        } else {
            User="";
            Pass="";
            UID="";
            GID="";
            Read=false;
            Backup=fbup;
            VScan=vscan;
        }
    }
    public String toString(){
        String Output=Name+" "+"("+Source+" --> "+Folder;
        if ((! Bind.equals("-")) && (Bind.length() > 0)) {
            Output=Output+" --> "+Bind+")";
        } else {
            Output=Output+")";
        }
        return Output;
    }
    public String confOut(){
        if (Bind.length() == 0) {
            Bind="-";
        }
        String Output=Name.replaceAll(" ","_")+" "+Folder+" "+Source.replaceAll(" ","_")+" "+Bind;
        if (Source.startsWith("//")) {
            if (User.length() == 0) {
                User="-";
            }
            if (Pass.length() == 0) {
                Pass="-";
            }
            if (UID.length() == 0) {
                UID="-";
            }
            if (GID.length() == 0) {
                GID="-";
            }
            Output=Output+" "+User+" "+Pass+" "+UID+" "+GID+" "+Read+" "+Backup+" "+VScan;
        } else {
          Output=Output+" "+Backup+" "+VScan;
        }
        return Output;
    }
    
}

class NFSShare {
    String Server,Path,UID,GID;
    boolean Read,Squash;
    public NFSShare(String server,String path,String suid,String sgid,boolean nfsro,boolean nfssquash){
        Server=server;
        Path=path;
        if (suid.equals("-")) {
            UID="";
        } else {
            UID=suid;
        }
        if (sgid.equals("-")) {
            GID="";
        } else {
            GID=sgid;
        }
        Read=nfsro;
        Squash=nfssquash;
    }
    public String toString(){
        String Output=Server+" "+Path;
        if (Read) {
            Output=Output+" [RO]";
        } else {
            Output=Output+" [RW]";
        }
        if (UID.length() > 0) {
            Output=Output+"(uid:"+UID+" gid:"+GID;
            if (Squash) {
                Output=Output+" [All])";
            } else {
                Output=Output+")";
            }
        } else {
            GID="";
        }
        return Output;
    }
}

class ProxyACL {
    String IPAddr,Netmask;
    boolean Accept;
    public ProxyACL(String source,String nm,boolean allowaccess){
        IPAddr=source;
        Netmask=nm;
        Accept=allowaccess;
    }
    public String toString(){
        String Output;
        if (Accept) {
            Output="Allow "+IPAddr+"/"+Netmask;
        } else {
            Output="Deny "+IPAddr+"/"+Netmask;
        };
        return Output;
    }
}

class HostedSite {
    String Domain,User,Bandwidth,MinBandwidth,sslc,ssls,ssll,sslo,sslou,sslip,sslemail;
    boolean Avirus;
    public HostedSite(String domainname,String owner,String bw,String mbw,boolean dazuko,
    String count,String state,String city,String company,String division,
    String ipaddr,String email){
        Domain=domainname;
        User=owner;
        Bandwidth=bw;
        MinBandwidth=mbw;
        Avirus=dazuko;
        sslc=count;
        ssls=state;
        ssll=city;
        sslo=company;
        sslou=division;
        sslip=ipaddr;
        sslemail=email;
    }
    public String toString(){
        String Output;
        if ((sslo.length() > 0) && (sslc.length() > 0) && (ssls.length() > 0) &&
        (sslou.length() > 0) && (sslip.length() > 0) && (sslemail.length() > 0)) {
            Output="https://";
        } else {
            Output="http://";
        }
        Output=Output+Domain+" ("+User+" "+Bandwidth+"B/s ("+MinBandwidth+"B/s)";
        if (Avirus) {
            Output=Output+" [Anti Virus Access Controll])";
        } else {
            Output=Output+")";
        }
        return Output;
    }
}

class RedirectedSite {
    String Domain,IPAddr,IName;
    public RedirectedSite(String domainname,String rdirip,String iface){
        Domain=domainname;
        IPAddr=rdirip;
        IName=iface;
    }
    public String toString(){
        String Output;
        Output=Domain+" ("+IPAddr+" Via "+IName+")";
        return Output;
    }
}


class FilterACL {
    String Group,TimeSpace,Default;
    boolean Accept;
    public FilterACL(String grpnme,String timenme,String ACLDefault,boolean allowaccess){
        Group=grpnme;
        TimeSpace=timenme;
        Accept=allowaccess;
        Default=ACLDefault;
    }
    public String toString(){
        String Output;
        if (Accept) {
            Output=Group+" Dureing "+TimeSpace+" And "+Default;
        } else {
            Output=Group+" Outside "+TimeSpace+" And "+Default;
        };
        return Output;
    }
}

class FilterCTRL {
    String List;
    boolean Accept;
    public FilterCTRL(String listname,boolean allowaccess){
        List=listname;
        Accept=allowaccess;
    }
    public String toString(){
        String Output;
        if (Accept) {
            Output="Deny Access To "+List;
        } else {
            Output="Allow Access To "+List;
        };
        return Output;
    }
}


class FilterMember {
    String IPAddr,Netmask;
    public FilterMember(String source,String nm){
        IPAddr=source;
        Netmask=nm;
    }
    public String toString(){
        String Output;
        Output=IPAddr+"/"+Netmask;
        return Output;
    }
}

class TimeSpace {
    String Days,TStart,TEnd;
    public TimeSpace(String dayssel,String starttime,String endtime){
        Days=dayssel;
        TStart=starttime;
        TEnd=endtime;
    }
    public String toString(){
        String Output;
        Output=Days+"("+TStart+" --> "+TEnd+")";
        return Output;
    }
}

class ProxyBypass {
    String IPAddr,Netmask;
    public ProxyBypass(String source,String nm){
        IPAddr=source;
        Netmask=nm;
    }
    public String toString(){
        String Output;
        Output="Bypass "+IPAddr+"/"+Netmask;
        return Output;
    }
}

class ListItem {
    String Entry;
    public ListItem(String litem){
        Entry=litem;
    }
    public String toString(){
        return Entry;
    }
}

class SambaConf {
    String RemoteBrowse,WorkGroup,ServerName,Description,Authentication,OSLevel,ADSServer,ADSRealm,Printer1,Printer2,AVMaxSize,AVMaxThread,Winbind;
    boolean AVHome,AVShare,DControl,DMaster,LMaster,PMaster,avshare,avhome,UProfile,BUShare,BUHome,BUWWW,BUFTP;
    Character HomeDrive,ShareDrive;    
    public SambaConf(){
        delConfig();
    }
    public void delConfig(){
        WorkGroup="WORKGROUP";
        ServerName="SENTRY";
        Description="NETSENTRY INTERNET SERVER";
        Authentication="USER";
        Winbind="Both";
        ADSServer="";
        ADSRealm="";
        OSLevel="65";
        RemoteBrowse="";
        AVMaxSize="2M";
        AVMaxThread="100";
        DMaster=true;
        LMaster=true;
        PMaster=true;
        AVHome=false;
        AVShare=false;
        BUShare=false;
        BUHome=false;
        BUWWW=false;
        BUFTP=false;
        UProfile=false;
        Printer1="";
        Printer2="";
        HomeDrive=new Character('U');
        ShareDrive=new Character('S');
    }
}

class ConfigFile {
    String Description,RelURL;
    public ConfigFile(String descrip,String servfile) {
        Description=descrip;
        RelURL=servfile;
    }
    public String toString(){
        return Description;
    }
}



