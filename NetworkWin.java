import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;


public class NetworkWin extends Container {
  DefaultTreeModel treeModel;
  final JSplitPane mainwindow=new JSplitPane();
  final JTree intwindow;
  final TcpConf tcpconf=new TcpConf();
  boolean ruleswin;
  ModemConf modemconf=new ModemConf();
  voipdefreg voipdefconf=new voipdefreg();
  FaxConf faxconf=new FaxConf();
  DefaultMutableTreeNode topbranch,intNode,iwconfig,wanNode,genroute,grenode,espnode,espaccnode,modemnode,rulenode,modemrules,lnetwork,sslcert,faxnode;
//protoconf,lwireless
  DefaultMutableTreeNode adslacc,adsllink,lbnode,voipnode,voipsip,voipiax;
  String Output="";
  String systype="lite";
  DefaultMutableTreeNode dod=new DefaultMutableTreeNode("Modem");
  DefaultMutableTreeNode tun=new DefaultMutableTreeNode("GRE Tunnel");
  DefaultMutableTreeNode ovpn=new DefaultMutableTreeNode("Open VPN");
  CaConf caconf=new CaConf();
  boolean servalid;

  public NetworkWin() {
    setLayout(new BorderLayout());

    JTree ruleswindow=new JTree();

    topbranch = new DefaultMutableTreeNode("Global Settings");

    intNode=new DefaultMutableTreeNode("Network Interface");
    iwconfig=new DefaultMutableTreeNode("Wireless Config");
//    lwireless=new DefaultMutableTreeNode(new WiFiConfig("lite","","","","","","",""));
    lnetwork=new DefaultMutableTreeNode("Network Interface");
    wanNode=new DefaultMutableTreeNode("Wan Routing/Nodes");
    genroute=new DefaultMutableTreeNode("Other Routes");

    modemnode=new DefaultMutableTreeNode("Modem Config");
    modemrules=new DefaultMutableTreeNode("Modem Firewall Rules");
    adsllink=new DefaultMutableTreeNode("Additional ADSL Links");
    adslacc=new DefaultMutableTreeNode("ADSL Account Pool");
    lbnode=new DefaultMutableTreeNode("Default TOS/Priority Config");
    faxnode=new DefaultMutableTreeNode("FAX Config");
//    protoconf=new DefaultMutableTreeNode("Preconfigured Protocols");
    grenode=new DefaultMutableTreeNode("GRE VPN Tunnels");
    espnode=new DefaultMutableTreeNode("ESP VPN Tunnels");
    espaccnode=new DefaultMutableTreeNode("ESP Remote Access");

    voipsip=new DefaultMutableTreeNode("SIP Providers");	
    voipiax=new DefaultMutableTreeNode("IAX Providers");
    voipnode=new DefaultMutableTreeNode("VOIP Registrations");
    voipnode.add(voipsip);
    voipnode.add(voipiax);


    sslcert = new DefaultMutableTreeNode("Security Certificate Config");

    treeModel = new DefaultTreeModel(topbranch);

    setSystype(systype);

    intwindow=new JTree(treeModel);
    intwindow.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    intwindow.setShowsRootHandles(true);
 
    mainwindow.setLeftComponent(new JScrollPane(intwindow));    
    intwindow.setSelectionPath(new TreePath(topbranch.getPath()));
    mainwindow.setBottomComponent(new NetworkGlobal());
    mainwindow.setDividerLocation(0.3);

    add(mainwindow);

    intwindow.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DrawWindow();
      }
    });
  }

  public void setSystype(String stypein) {
    DefaultMutableTreeNode fint,lint;
    IntDef intset;

    systype=stypein;
    topbranch.removeAllChildren();
    lnetwork.removeAllChildren();

    if (systype.equals("full")) {
      topbranch.add(intNode);
      topbranch.add(iwconfig);
      topbranch.add(wanNode);
      topbranch.add(genroute);
    } else {
      iwconfig.removeAllChildren();
      fint=getInterface("eth0");
      if (fint != null) {
        intset=(IntDef)fint.getUserObject();
        intset.IPStart="";
        intset.IPEnd="";
        lint=new DefaultMutableTreeNode(intset);
        treeModel.insertNodeInto(lint,lnetwork,lnetwork.getChildCount());    
      } else {
        lint=addInterface("Internal","","","","","eth0","","","","");
        treeModel.insertNodeInto(lint,lnetwork,lnetwork.getChildCount());    
      }
/*
      if (lwireless == null) {
        addLiteWiFi("","","");
      }
*/
      topbranch.add(lnetwork);
      topbranch.add(iwconfig);
    }
    topbranch.add(modemnode);
    if (systype.equals("full")) {
      topbranch.add(modemrules);
      topbranch.add(adsllink);
      topbranch.add(adslacc);
      topbranch.add(lbnode);
      topbranch.add(faxnode);
//      topbranch.add(protoconf);
      topbranch.add(grenode);
      topbranch.add(espnode);
      topbranch.add(espaccnode);
      topbranch.add(voipnode);
      topbranch.add(sslcert);
    }
    treeModel.reload(topbranch);
  }

  public void DrawWindow() {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)intwindow.getLastSelectedPathComponent();
    if (node == null) {
      intwindow.setSelectionPath(new TreePath(topbranch.getPath()));
      node=topbranch;
    }

    int Depth=intwindow.getSelectionPath().getPathCount();
    Object nodeInfo = node.getUserObject();

    if ((Depth == 1) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new NetworkGlobal());
    } else if ((Depth == 1) && (! systype.equals("full"))) {
      mainwindow.setBottomComponent(new getSerial());
    } else if (nodeInfo == "Network Interface") {
      mainwindow.setBottomComponent(new IntConfig());
    } else if (nodeInfo == "Wan Routing/Nodes") {
      mainwindow.setBottomComponent(new WanConfig(false));
    } else if (node.isNodeAncestor(wanNode)) {
      mainwindow.setBottomComponent(new WanConfig(true));
    } else if (nodeInfo == "Other Routes") {
      mainwindow.setBottomComponent(new RouteConfig(null,false));
    } else if (node.isNodeAncestor(genroute)) {
      mainwindow.setBottomComponent(new RouteConfig(node,true));
    } else if (node == iwconfig) {
      if (systype.equals("full")) {
        mainwindow.setBottomComponent(new WirelessConfig(null,false));
//      } else {
//        mainwindow.setBottomComponent(new WirelessConfig(lwireless,true));
      }
    } else if ((node.isNodeAncestor(iwconfig)) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new WirelessConfig(node,true));
    } else if (nodeInfo == "ESP VPN Tunnels") {
      mainwindow.setBottomComponent(new EspConfig(false,node));
    } else if (nodeInfo == "ESP Remote Access") {
      mainwindow.setBottomComponent(new EspAccConfig(false,node));
    } else if (nodeInfo == "GRE VPN Tunnels") {
      mainwindow.setBottomComponent(new GreConfig());
    } else if (nodeInfo == "Modem Config") {
      mainwindow.setBottomComponent(new ModemConfig());
    } else if (node == faxnode) {
      mainwindow.setBottomComponent(new FaxConfig());
    } else if ((node == modemrules) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new AddFwUser());
    } else if ((node.getParent() == modemrules) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new RuleConfig(node,false));
    } else if ((node.isNodeAncestor(modemrules)) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new RuleConfig(node,true));
    } else if (node.getParent().toString() == "GRE VPN Tunnels") {
      mainwindow.setBottomComponent(new AddGreRoute(false));
    } else if (node.getParent().toString() == "ESP VPN Tunnels") {
      mainwindow.setBottomComponent(new EspConfig(true,node));
    } else if (node.getParent().toString() == "ESP Remote Access") {
      mainwindow.setBottomComponent(new EspAccConfig(true,node));
    } else if ((Depth == 3) && (node.isNodeAncestor(intNode)) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new AddFwUser());
    } else if ((Depth == 4) && (node.isNodeAncestor(intNode)) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new RuleConfig(node,false));
    } else if ((Depth == 5) && (node.isNodeAncestor(intNode)) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new RuleConfig(node,true));
    } else if ((Depth == 4) && (node.isNodeAncestor(grenode)) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new RuleConfig(node,false));
    } else if ((Depth == 5) && (node.isNodeAncestor(grenode)) && (systype.equals("full"))) {
      mainwindow.setBottomComponent(new RuleConfig(node,true));
/*
    } else if (node == protoconf) {
      mainwindow.setBottomComponent(new ProtoEdit(false));
    } else if (node.isNodeAncestor(protoconf)) {
      mainwindow.setBottomComponent(new ProtoEdit(true));
*/
    } else if (node == lbnode) {
      mainwindow.setBottomComponent(new AdslLB(node,false));
    } else if (nodeInfo == "Additional ADSL Links") {
      mainwindow.setBottomComponent(new AdslLink(node,false));
    } else if (node.isNodeAncestor(adsllink)) {
      mainwindow.setBottomComponent(new AdslLink(node,true));
    } else if (node == adslacc) {
      mainwindow.setBottomComponent(new AdslUsers(node,false));
    } else if (node.isNodeAncestor(adslacc)) {
      mainwindow.setBottomComponent(new AdslUsers(node,true));
    } else if (node.isNodeAncestor(lbnode)) {
      mainwindow.setBottomComponent(new AdslLB(node,true));
    } else if (node == sslcert){
      mainwindow.setBottomComponent(new SecurityWin());
    } else if (node.isNodeAncestor(voipnode)) {
      if (node == voipsip) {
        mainwindow.setBottomComponent(new VoipConfig(false,node,false));
      } else if (node == voipiax) {
        mainwindow.setBottomComponent(new VoipConfig(false,node,true));
      } else if (node.isNodeAncestor(voipsip)) {
        mainwindow.setBottomComponent(new VoipConfig(true,node,false));
      } else if (node.isNodeAncestor(voipiax)) {
        mainwindow.setBottomComponent(new VoipConfig(true,node,true));
      } else {
        mainwindow.setBottomComponent(new VoipRouting());
      }
    } else {
      mainwindow.setBottomComponent(null);
    }
    mainwindow.setDividerLocation(0.3);
/*
    } else if (node.isNodeAncestor(grenode)) {
      mainwindow.setBottomComponent(new AddGreRoute(true));
*/
  }
  private void addLabel(JLabel label,JTextField textfield,GridBagLayout gridbag,GridBagConstraints layout){
    layout.gridwidth=1;
    gridbag.setConstraints(label,layout);
    add(label);
    layout.gridwidth=GridBagConstraints.REMAINDER;
    gridbag.setConstraints(textfield,layout);
    add(textfield);
  }

  public Vector getIntList(boolean ltype){
    Vector intlist;
    IntDef iface;
    DefaultMutableTreeNode inode;
    intlist=new Vector();

    for (Enumeration e = intNode.children() ; e.hasMoreElements() ;) {
        inode=(DefaultMutableTreeNode)e.nextElement();
        iface=(IntDef)inode.getUserObject();
        if ((iface.IntName.indexOf(":") < 0) || (ltype)) {
          intlist.addElement(inode);
        }
    }
    return(intlist);
  }
/*
  public Vector getProtoList(){
    Vector protolist;
    DefaultMutableTreeNode inode;
    protolist=new Vector();

    protolist.addElement(null);

    for (Enumeration e = protoconf.children() ; e.hasMoreElements() ;) {
        inode=(DefaultMutableTreeNode)e.nextElement();
        protolist.addElement(inode);
    }
    return(protolist);
  }
*/
  class AdslLB extends Container implements ActionListener {
    ManageNode sortpanel;
    JTextField tosdescrip,destip,destport,srcport; 
    JComboBox tosval,protocol,ingressmark;
    JLabel textlabel;
    JButton addint;
    boolean isEdit;
    DefaultMutableTreeNode node;
    TOSConfig LBConfig;

    public AdslLB(DefaultMutableTreeNode actnode,boolean edit) {
      node=actnode;
      isEdit=edit;
      String rproto[]={"TCP","UDP","GRE","ESP","AH","OSPF","ALL"};
      String ingress[]={"High","Med","Low"};
      String tosv[]={"Normal-Service","Minimize-Cost","Maximize-Reliability","Maximize-Throughput","Minimize-Delay"};

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      layout.anchor=GridBagConstraints.NORTH;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select Rule To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New Rule");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Rule");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      tosdescrip=new JTextField("",10);
      addLabel(new JLabel("Rule Description"),tosdescrip,gridbag,layout);

      destip=new JTextField("0/0",10);
      addLabel(new JLabel("Destination IP Address"),destip,gridbag,layout);

      layout.gridwidth=1;
      JLabel tlabel=new JLabel("Protocol");
      gridbag.setConstraints(tlabel,layout);
      add(tlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      protocol=new JComboBox(rproto);
      gridbag.setConstraints(protocol,layout);
      add(protocol);

      destport=new JTextField("",10);
      addLabel(new JLabel("Destination Port"),destport,gridbag,layout);

      srcport=new JTextField("1024:65535",10);
      addLabel(new JLabel("Source Port"),srcport,gridbag,layout);

      layout.gridwidth=1;
      JLabel tvlabel=new JLabel("TOS Setting");
      gridbag.setConstraints(tvlabel,layout);
      add(tvlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      tosval=new JComboBox(tosv);
      gridbag.setConstraints(tosval,layout);
      add(tosval);

      layout.gridwidth=1;
      JLabel iglabel=new JLabel("Priority");
      gridbag.setConstraints(iglabel,layout);
      add(iglabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      ingressmark=new JComboBox(ingress);
      gridbag.setConstraints(ingressmark,layout);
      add(ingressmark);

      if (isEdit) {
        LBConfig=(TOSConfig)node.getUserObject();
        tosdescrip.setText(LBConfig.Description);
        destip.setText(LBConfig.Address);
        destport.setText(LBConfig.Dest);
        srcport.setText(LBConfig.Src);
        protocol.setSelectedItem(LBConfig.Protocol);
        tosval.setSelectedItem(LBConfig.TOS);
        ingressmark.setSelectedItem(LBConfig.Ingress);
        addint=new JButton("Save Rule");
      } else {
        addint=new JButton("Add Rule");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add Rule");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if ((destip.getText().length() > 0 ) && (tosdescrip.getText().length() >0 )) {
        if (destport.getText().length() == 0) {
          destport.setText("0:65534");
        }
        if (srcport.getText().length() == 0) {
          srcport.setText("0:65534");
        }
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addAdslLB(tosdescrip.getText(),destip.getText(),destport.getText(),srcport.getText(),
                                                     protocol.getSelectedItem().toString(),tosval.getSelectedItem().toString(),
                                                     ingressmark.getSelectedItem().toString());
          if (childnode != null) {
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          }
          tosdescrip.setText("");
          destip.setText("");
          destport.setText("");
          srcport.setText("1024:65535");
          protocol.setSelectedIndex(0);
          tosval.setSelectedIndex(0);
          ingressmark.setSelectedIndex(0);
        } else {
          LBConfig.Description=tosdescrip.getText();
          LBConfig.Address=destip.getText();
          LBConfig.Dest=destport.getText();
          LBConfig.Src=srcport.getText();
          LBConfig.Protocol=protocol.getSelectedItem().toString();
          LBConfig.TOS=tosval.getSelectedItem().toString();
          LBConfig.Ingress=ingressmark.getSelectedItem().toString();
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node));
        }
      }
    }
  }

  public DefaultMutableTreeNode addAdslLB(String tosdescrip,String destip,String destport,String srcport,String protocol,
                                            String tosval,String igress) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new TOSConfig(tosdescrip,destip,destport,srcport,protocol,tosval,igress));
    treeModel.insertNodeInto(childnode,lbnode,lbnode.getChildCount());
    return childnode;
  }


  class AdslUsers extends Container implements ActionListener {
    ManageNode sortpanel;
    JTextField username,status; 
    JPasswordField password1,password2;
    JLabel textlabel;
    JButton addint;
    boolean isEdit;
    DefaultMutableTreeNode node;
    AdslAccount EditAccount;

    public AdslUsers(DefaultMutableTreeNode actnode,boolean edit) {
      node=actnode;
      isEdit=edit;

      if (isEdit) {
        EditAccount=(AdslAccount)node.getUserObject();
      } else {
        EditAccount=new AdslAccount();
      }

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      layout.anchor=GridBagConstraints.NORTH;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select ADSL Account To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter New ADSL Account");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit ADSL Account (" +EditAccount.Username+" "+EditAccount.userstatuslist[EditAccount.Status]+")");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      username=new JTextField("",10);
      if (! isEdit) {
        addLabel(new JLabel("Username"),username,gridbag,layout);
      }

      password1=new JPasswordField("",10);
      addPwLabel(new JLabel("Password"),password1,gridbag,layout);

      password2=new JPasswordField("",10);
      addPwLabel(new JLabel("Confirm Password"),password2,gridbag,layout);

      if (isEdit) {
        username.setText(EditAccount.Username);
        password1.setText(EditAccount.Password);
        password2.setText(EditAccount.Password);
        addint=new JButton("Save Account");
      } else {
        addint=new JButton("Add Account");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add Rule");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

    }
    private void addLabel(JLabel label,JTextField textfield,GridBagLayout gridbag,GridBagConstraints layout){
      layout.gridwidth=1;
      gridbag.setConstraints(label,layout);
      add(label);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(textfield,layout);
      add(textfield);
    }

    private void addPwLabel(JLabel label,JPasswordField textfield,GridBagLayout gridbag,GridBagConstraints layout){
      layout.gridwidth=1;
      gridbag.setConstraints(label,layout);
      add(label);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(textfield,layout);
      add(textfield);
    }

    public void actionPerformed(ActionEvent event) {
      if ((username.getText().length() > 0) && (password1.getText().length() > 0) &&
          (password1.getText().equals(password2.getText()))) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=null;
          if (getAdslACC(username.getText()) == null) {
            childnode=addAdslACC(username.getText(),password1.getText(),0);
          }
          if (childnode != null) {
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          }
          username.setText("");
          password1.setText("");
          password2.setText("");
        } else {
          setAdslLink(EditAccount.Username,password1.getText());
          EditAccount.Password=password1.getText();
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      } else if (isEdit) {
        password1.setText(EditAccount.Password);
        password2.setText(EditAccount.Password);
      }
    }
  }

  public DefaultMutableTreeNode addAdslACC(String username,String password,int status) { 
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new AdslAccount(username,password,status));
    treeModel.insertNodeInto(childnode,adslacc,adslacc.getChildCount());
    return childnode;
  }

  class AdslLink extends Container implements ActionListener {
    ManageNode sortpanel;
    JTextField intdescrip,auser,ingress,egress,mtos,port,service,virtip,remip; 
    JPasswordField pass1,pass2;
    JLabel textlabel;
    JButton addint;
    boolean isEdit;
    DefaultMutableTreeNode node;
    ExtraAdslLink EditLink;

    public AdslLink(DefaultMutableTreeNode actnode,boolean edit) {
      node=actnode;
      isEdit=edit;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      layout.anchor=GridBagConstraints.NORTH;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select Link To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New Link");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Link");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      intdescrip=new JTextField("",10);
      addLabel(new JLabel("Link Name"),intdescrip,gridbag,layout);

      port=new JTextField("",10);
      addLabel(new JLabel("Ethernet Card For Link"),port,gridbag,layout);

      service=new JTextField("",10);
      addLabel(new JLabel("PPPoE Service (AC/Service)"),service,gridbag,layout);

      auser=new JTextField("",10);
      addLabel(new JLabel("Username To Use For This Link"),auser,gridbag,layout);

      pass1=new JPasswordField("",10);
      addLabel(new JLabel("Password For Above Username"),pass1,gridbag,layout);

      pass2=new JPasswordField("",10);
      addLabel(new JLabel("Confirm Password"),pass2,gridbag,layout);

      ingress=new JTextField("",10);
      addLabel(new JLabel("Limit For Incomeing Traffic (Downloads)"),ingress,gridbag,layout);

      egress=new JTextField("",10);
      addLabel(new JLabel("Limit For Outgoing Traffic (Uploads)"),egress,gridbag,layout);

      mtos=new JTextField("",10);
      addLabel(new JLabel("TOS To Match (Comma Seperated)"),mtos,gridbag,layout);

      virtip=new JTextField("",10);
      addLabel(new JLabel("Virtual IP (Unique Non Routed IP)"),virtip,gridbag,layout);

      remip=new JTextField("",10);
      addLabel(new JLabel("Remote IP To Route To (Link To Virtual IP)"),remip,gridbag,layout);


      if (isEdit) {
        EditLink=(ExtraAdslLink)node.getUserObject();
        intdescrip.setText(EditLink.Description);
        auser.setText(EditLink.User);
        pass1.setText(EditLink.Pass);
        pass2.setText(EditLink.Pass);
        ingress.setText(EditLink.Ingress);
        egress.setText(EditLink.Egress);
        mtos.setText(EditLink.TOS);
        virtip.setText(EditLink.VIP);
        remip.setText(EditLink.RIP);
        service.setText(EditLink.Service);
        if (EditLink.Port.length() > 0) {
          port.setText(EditLink.Port);
        } else {
          port.setText(modemconf.ComPort);
        }
        addint=new JButton("Save Link");
      } else {
        port.setText(modemconf.ComPort);
        addint=new JButton("Add Link");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add Route");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if ((auser.getText().length() > 0 ) && (pass1.getText().length() > 0) &&
          (pass1.getText().equals(pass2.getText())) && 
          (intdescrip.getText().length() >0 ) && (ingress.getText().length() >0) &&
          (egress.getText().length() > 0) &&  (port.getText().length() > 0)) {
//          (service.getText().length() > 0)){
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addAdslLink(intdescrip.getText(),auser.getText(),pass1.getText(),ingress.getText(),
                                                       egress.getText(),mtos.getText(),
                                                       port.getText(),service.getText(),virtip.getText(),remip.getText());
          if (childnode != null) {
            if (getAdslACC(auser.getText()) == null) {
              addAdslACC(auser.getText(),pass1.getText(),1);
            } else {
              setAdslLink(auser.getText(),pass1.getText());
            }
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          }
          intdescrip.setText("");
          auser.setText("");
          pass1.setText("");
          pass2.setText("");
          ingress.setText("");
          egress.setText("");
          mtos.setText("");
          service.setText("");
          virtip.setText("");
          remip.setText("");
          port.setText(modemconf.ComPort);
        } else {
          setAdslACC(EditLink.User,EditLink.Pass,false);
          if (getAdslACC(auser.getText()) == null) {
            addAdslACC(auser.getText(),pass1.getText(),1);
          }
          setAdslLink(auser.getText(),pass1.getText());
          EditLink.Description=intdescrip.getText();
          EditLink.User=auser.getText();
          EditLink.Pass=pass1.getText();
          EditLink.Ingress=ingress.getText();
          EditLink.Egress=egress.getText();
          EditLink.TOS=mtos.getText();
          EditLink.VIP=virtip.getText();
          EditLink.RIP=remip.getText();
          EditLink.Port=port.getText();
          EditLink.Service=service.getText();
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node));
        }
      } else {
        pass1.setText(EditLink.Pass);
        pass2.setText(EditLink.Pass);
      }
    }
  }

  public DefaultMutableTreeNode addAdslLink(String intdescrip,String uname,String lpass,String ingress,String egress,
                                            String mtos,String port,String service,String virtip,String remip) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ExtraAdslLink(intdescrip,uname,lpass,ingress,egress,mtos,port,service,virtip,remip));
    if (adsllink.getChildCount() < 10) {
      treeModel.insertNodeInto(childnode,adsllink,adsllink.getChildCount());
      return childnode;
    } else {
      return null;
    }
  }


  class NetworkGlobal extends Container implements ActionListener {
    JComboBox lint,lent,lvpn;
//applenetphase;
    JTextField prwins,secwins,nexthop,lease,maxlease,ntpserver,pdns,sdns,ldaplogin,ldapserver,vpnrange,ovpnrange,l2tprange,external;
    JTextField ingress,egress,bridgeint;
    public NetworkGlobal() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Global TCP/IP Configuration");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      pdns=new JTextField(tcpconf.pdns,10);
      addLabel(new JLabel("Primary DNS Server"),pdns,gridbag,layout);

      sdns=new JTextField(tcpconf.sdns,10);
      addLabel(new JLabel("Secondary DNS Server"),sdns,gridbag,layout);

      prwins=new JTextField(tcpconf.pwins,10);
      addLabel(new JLabel("Primary Wins Server"),prwins,gridbag,layout);

      secwins=new JTextField(tcpconf.swins,10);
      addLabel(new JLabel("Secondary Wins Server"),secwins,gridbag,layout);

      nexthop=new JTextField(tcpconf.nexthop,10);
      addLabel(new JLabel("Gateway (Next Hop To Internet)"),nexthop,gridbag,layout);

      external=new JTextField(tcpconf.external,10);
      addLabel(new JLabel("External (Natted IP)"),external,gridbag,layout);

      vpnrange=new JTextField(tcpconf.vpnrange,10);
      addLabel(new JLabel("Network Used For IPSEC VPN Access"),vpnrange,gridbag,layout);

      ovpnrange=new JTextField(tcpconf.ovpnrange,10);
      addLabel(new JLabel("Network Used For Open VPN Access"),ovpnrange,gridbag,layout);

      l2tprange=new JTextField(tcpconf.l2tprange,10);
      addLabel(new JLabel("Network Used For L2TP Access"),l2tprange,gridbag,layout);

      lease=new JTextField(tcpconf.lease,10);
      addLabel(new JLabel("DHCP Lease Time"),lease,gridbag,layout);

      maxlease=new JTextField(tcpconf.maxlease,10);
      addLabel(new JLabel("DHCP Max. Lease Time"),maxlease,gridbag,layout);

      ntpserver=new JTextField(tcpconf.ntpserver,10);
      addLabel(new JLabel("Network Time Protocol Server"),ntpserver,gridbag,layout);

      ldapserver=new JTextField(tcpconf.ldapserver,10);
      addLabel(new JLabel("LDAP Server IP (User Database)"),ldapserver,gridbag,layout);

      ldaplogin=new JTextField(tcpconf.ldaplogin,10);
      addLabel(new JLabel("LDAP Server Bind DN (Username)"),ldaplogin,gridbag,layout);

      bridgeint=new JTextField(tcpconf.bridgeint,10);
      addLabel(new JLabel("Internal Bridge Interfaces"),bridgeint,gridbag,layout);

      layout.gridwidth=1;
      JLabel intilabel=new JLabel("Select Internal Interface");
      gridbag.setConstraints(intilabel,layout);
      add(intilabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      lint=new JComboBox(getIntList(false));
      lint.setSelectedItem(tcpconf.intint);
      gridbag.setConstraints(lint,layout);
      add(lint);

      layout.gridwidth=1;
      JLabel vpnilabel=new JLabel("Select External Open VPN Interface");
      gridbag.setConstraints(vpnilabel,layout);
      add(vpnilabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      lvpn=new JComboBox(getIntList(false));
      lvpn.addItem(dod);
      lvpn.setSelectedItem(tcpconf.vpnint);
      gridbag.setConstraints(lvpn,layout);
      add(lvpn);

      layout.gridwidth=1;
      JLabel extilabel=new JLabel("Select External/PPPoE Interface");
      gridbag.setConstraints(extilabel,layout);
      add(extilabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      lent=new JComboBox(getIntList(false));
      lent.addItem(dod);
      lent.setSelectedItem(tcpconf.extint);
      gridbag.setConstraints(lent,layout);
      add(lent);

/*
      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel atlabel=new JLabel("Apple Talk Configuration");
      gridbag.setConstraints(atlabel,layout);
      add(atlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      applenetstart=new JTextField(tcpconf.ANetStart,10);
      addLabel(new JLabel("Start Of Net Range"),applenetstart,gridbag,layout);

      applenetfin=new JTextField(tcpconf.ANetFin,10);
      addLabel(new JLabel("End Of Net Range (Optional)"),applenetfin,gridbag,layout);

      layout.gridwidth=1;
      JLabel phaselabel=new JLabel("Select Appletalk Phase");
      gridbag.setConstraints(phaselabel,layout);
      add(phaselabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      String atphaselist[]={"1","2"};
      applenetphase=new JComboBox(atphaselist);
      if (tcpconf.ANetPhase.equals("2")) {
        applenetphase.setSelectedIndex(1);
      }
      gridbag.setConstraints(applenetphase,layout);
      add(applenetphase);
*/
      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel tclabel=new JLabel("Gateway Traffic Limits");
      gridbag.setConstraints(tclabel,layout);
      add(tclabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;
      
      ingress=new JTextField(tcpconf.ingress,10);
      addLabel(new JLabel("Limit For Incomeing Traffic (Downloads)"),ingress,gridbag,layout);

      egress=new JTextField(tcpconf.egress,10);
      addLabel(new JLabel("Limit For Outgoing Traffic (Uploads)"),egress,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton savenet=new JButton("Save Settings");
      savenet.setActionCommand("Save Net");
      savenet.addActionListener(this);
      gridbag.setConstraints(savenet,layout);
      add(savenet);

    }
    private void addLabel(JLabel label,JTextField textfield,GridBagLayout gridbag,GridBagConstraints layout){
      layout.gridwidth=1;
      gridbag.setConstraints(label,layout);
      add(label);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(textfield,layout);
      add(textfield);
    }

    private void addPwLabel(JLabel label,JPasswordField textfield,GridBagLayout gridbag,GridBagConstraints layout){
      layout.gridwidth=1;
      gridbag.setConstraints(label,layout);
      add(label);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(textfield,layout);
      add(textfield);
    }
    public void actionPerformed(ActionEvent event) {

      tcpconf.pdns=pdns.getText();
      tcpconf.sdns=sdns.getText();
      tcpconf.pwins=prwins.getText();
      tcpconf.swins=secwins.getText();
      tcpconf.lease=lease.getText();
      tcpconf.maxlease=maxlease.getText();
      tcpconf.nexthop=nexthop.getText();
      tcpconf.external=external.getText();
      tcpconf.vpnrange=vpnrange.getText();
      tcpconf.ovpnrange=ovpnrange.getText();
      tcpconf.l2tprange=l2tprange.getText();
      tcpconf.ntpserver=ntpserver.getText();

      tcpconf.ldapserver=ldapserver.getText();
      tcpconf.ldaplogin=ldaplogin.getText();
      tcpconf.bridgeint=bridgeint.getText();

      tcpconf.intint=(DefaultMutableTreeNode)lint.getSelectedItem();
      tcpconf.extint=(DefaultMutableTreeNode)lent.getSelectedItem();
      tcpconf.vpnint=(DefaultMutableTreeNode)lvpn.getSelectedItem();
/*
      tcpconf.ANetStart=applenetstart.getText();
      tcpconf.ANetFin=applenetfin.getText();
      tcpconf.ANetPhase=applenetphase.getSelectedItem().toString();
*/
      tcpconf.ingress=ingress.getText();
      tcpconf.egress=egress.getText();
    }
  }

  class getSerial extends Container implements ActionListener {
    JTextField hname,dname,ldaplogin;

    public getSerial() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Serial Key To Enable All Features");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      hname=new JTextField(tcpconf.hname,10);
      addLabel(new JLabel("Server Hostname"),hname,gridbag,layout);

      dname=new JTextField(tcpconf.dname,10);
      addLabel(new JLabel("Domain"),dname,gridbag,layout);

      ldaplogin=new JTextField(tcpconf.ldaplogin,10);
      addLabel(new JLabel("LDAP Server Bind DN (Username)"),ldaplogin,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton savenet=new JButton("Save Settings");
      savenet.setActionCommand("Save Net");
      savenet.addActionListener(this);
      gridbag.setConstraints(savenet,layout);
      add(savenet);
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
      tcpconf.hname=hname.getText();
      tcpconf.dname=dname.getText();
    }
  }

  class IntConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)intwindow.getLastSelectedPathComponent();
    JTextField intdescrip,intip,intgw,intstart,intend,inteth,ingress,egress,intmac;
    JButton addint=new JButton("Add Interface");
    JButton editint=new JButton("Edit Interface");
    public IntConfig() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      if (systype.equals("full")) {
        layout.weighty=1;
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select Interface To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);
      }

      layout.weighty=0;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Enter Properties Of New Network Interface");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      intdescrip=new JTextField("",10);
      if (! systype.equals("full")) {
        intdescrip.setEnabled(false);
        intdescrip.setText("Internal");
      }
      addLabel(new JLabel("Description"),intdescrip,gridbag,layout);

      intip=new JTextField("",10);
      addLabel(new JLabel("Interface Ip Address (IP/SN)"),intip,gridbag,layout);

      intgw=new JTextField("",10);
      addLabel(new JLabel("Dhcp Gateway (If Not Server)"),intgw,gridbag,layout);

      if (systype.equals("full")) {
        intmac=new JTextField("",10);
        addLabel(new JLabel("Interface MAC Address (optional)"),intmac,gridbag,layout);

        intstart=new JTextField("",10);
        addLabel(new JLabel("Start Of Dynamic Ip Range"),intstart,gridbag,layout);

        intend=new JTextField("",10);
        addLabel(new JLabel("End Of Dynamic Ip Range"),intend,gridbag,layout);

        ingress=new JTextField("",10);
        addLabel(new JLabel("Available Incoming Bandwidth"),ingress,gridbag,layout);

        egress=new JTextField("",10);
        addLabel(new JLabel("Available Outgoing Bandwidth"),egress,gridbag,layout);
      }

      inteth=new JTextField("",10);
      if (! systype.equals("full")) {
        inteth.setEnabled(false);
        inteth.setText("eth0");
      }
      addLabel(new JLabel("Physical Interface (eth*)"),inteth,gridbag,layout);

      layout.weighty=1;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      if ((lnetwork.getChildCount() == 0) | (systype.equals("full"))){
        layout.gridwidth=GridBagConstraints.REMAINDER;
        addint.setActionCommand("Add Interface");
        addint.addActionListener(this);
        gridbag.setConstraints(addint,layout);
        add(addint);
        addint.setEnabled(servalid);
      }

      if ((! systype.equals("full")) && (lnetwork.getChildCount() > 0)) {
        layout.gridwidth=GridBagConstraints.REMAINDER;
        editint.setActionCommand("Edit Interface");
        editint.addActionListener(this);
        gridbag.setConstraints(editint,layout);
        add(editint);
      }

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
      DefaultMutableTreeNode childnode;
      IntDef intdata;
      String ipdata[];

      ipdata=intip.getText().split("/");
      if (event.getActionCommand() == "Add Interface") {
        if ((ipdata[0].length() > 0 ) && (ipdata[1].length() > 0) && 
            (inteth.getText().length() >0 ) && (intdescrip.getText().compareTo("Dialup") != 0)) {
          if (systype.equals("full")) {
            childnode=addInterface(intdescrip.getText(),ipdata[0],ipdata[1],intstart.getText(),
                                   intend.getText(),inteth.getText(),ingress.getText(),egress.getText(),
                                   intmac.getText(),intgw.getText());
          } else {
            childnode=addInterfacel(intdescrip.getText(),ipdata[0],ipdata[1],"","",inteth.getText(),"","","","");
          }
          if (childnode != null) {
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            if (systype.equals("full")) {
              sortpanel.listdata.addElement(childnode);
            }
          }
        }
      } else if (event.getActionCommand() == "Edit Interface") {
        if (systype.equals("full")) {
          childnode=(DefaultMutableTreeNode)treeModel.getChild(intNode,0);
        } else {
          childnode=(DefaultMutableTreeNode)treeModel.getChild(lnetwork,0);
        }
        intdata=(IntDef)childnode.getUserObject();
        intdata.IPAddress=ipdata[0];
        intdata.IPSubnet=ipdata[1];
        intdata.IPGateway=intgw.getText();
        intdata.MAC=intmac.getText();
        if (systype.equals("full")) {
          treeModel.reload(intNode);
        } else {
          treeModel.reload(lnetwork);
        }
      }

      intip.setText("");
      intgw.setText("");
      if (systype.equals("full")) {
        intdescrip.setText("");
        inteth.setText("");
        intstart.setText("");
        intend.setText("");
        ingress.setText("");
        egress.setText("");
        intmac.setText("");
      }

      if ((! systype.equals("full")) && (lnetwork.getChildCount() > 0)) {
        addint.setEnabled(false);
      }
    }
  }
  public DefaultMutableTreeNode addInterface(String descrip,String intip,String intnm,String intstart,
                                             String intend,String inteth,String ingress,String egress,
                                             String maddr,String gateway){
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new IntDef(descrip,
                                                                           intip,
                                                                           intnm,
                                                                           intstart,
                                                                           intend,
                                                                           inteth,
                                                                           ingress,
                                                                           egress,maddr,gateway));
    treeModel.insertNodeInto(childnode,intNode,intNode.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addInterfacel(String descrip,String intip,String intnm,String intstart,
                                              String intend,String inteth,String ingress,String egress,
                                              String maddr,String gateway){
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new IntDef(descrip,
                                                                           intip,
                                                                           intnm,
                                                                           intstart,
                                                                           intend,
                                                                           inteth,
                                                                           ingress,
                                                                           egress,maddr,gateway));
    treeModel.insertNodeInto(childnode,lnetwork,lnetwork.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode getInterface(String tofind) {
    IntDef intset=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = intNode.children() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      intset=(IntDef)intdata.getUserObject();
      if (intset.IntName.equals(tofind)) {
        outdata=intdata;
      }
    }
    return outdata;
  }


  class RouteConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    JTextField intdescrip,intip,intnm,intgw; 
    JLabel textlabel;
    JButton addint;
    boolean isEdit;
    DefaultMutableTreeNode node;
    GenralRoute EditRoute;

    public RouteConfig(DefaultMutableTreeNode actnode,boolean edit) {
      node=actnode;
      isEdit=edit;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      layout.anchor=GridBagConstraints.NORTH;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(genroute,treeModel,"Select Route To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New Route");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Route");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      intdescrip=new JTextField("",10);
      addLabel(new JLabel("Description"),intdescrip,gridbag,layout);

      intip=new JTextField("",10);
      addLabel(new JLabel("Destination Network Address"),intip,gridbag,layout);

      intnm=new JTextField("",10);
      addLabel(new JLabel("Destination Subnet Bits (8-32)"),intnm,gridbag,layout);

      intgw=new JTextField("",10);
      addLabel(new JLabel("Gateway"),intgw,gridbag,layout);


      if (isEdit) {
        EditRoute=(GenralRoute)node.getUserObject();
        intdescrip.setText(EditRoute.Description);
        intip.setText(EditRoute.IPAddress);
        intnm.setText(EditRoute.IPSubnet);
        intgw.setText(EditRoute.Gateway);
        addint=new JButton("Save Route");
      } else {
        addint=new JButton("Add Route");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add Route");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if ((intip.getText().length() > 0 ) && (intnm.getText().length() > 0) && 
          (intdescrip.getText().length() >0 ) && (intgw.getText().length() >0)) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addGenRoute(intdescrip.getText(),intip.getText(),intnm.getText(),intgw.getText());
          intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          intdescrip.setText("");
          intip.setText("");
          intnm.setText("");
          intgw.setText("");
        } else {
          EditRoute.Description=intdescrip.getText();
          EditRoute.IPAddress=intip.getText();
          EditRoute.IPSubnet=intnm.getText();
          EditRoute.Gateway=intgw.getText();
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node));
        }
      }
    }
  }

  class WirelessConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    JTextField key,channel,power,regdom;
    JLabel textlabel;
    JButton addint;
    JComboBox device,mode,atype,apmode;
    boolean isEdit;
    DefaultMutableTreeNode node;
    WiFiConfig EditWiFi;

    public WirelessConfig(DefaultMutableTreeNode actnode,boolean edit) {
      node=actnode;
      isEdit=edit;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      layout.anchor=GridBagConstraints.NORTH;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(iwconfig,treeModel,"Select Wireless NIC To Manage");
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New Wireless NIC");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
        EditWiFi = new WiFiConfig();
      } else {
        EditWiFi=(WiFiConfig)node.getUserObject();
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        if (systype.equals("full")) {
          textlabel=new JLabel("Edit Wireless Node "+EditWiFi.device);
        } else {
          textlabel=new JLabel("Edit Wireless Settings");
        }
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      if ((! isEdit) && (systype.equals("full"))) {
        layout.gridwidth=1;
        JLabel intilabel=new JLabel("Select Interface");
        gridbag.setConstraints(intilabel,layout);
        add(intilabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        device=new JComboBox(getIntList(false));
        device.setSelectedItem(tcpconf.intint);
        gridbag.setConstraints(device,layout);
        add(device);
      }

      layout.gridwidth=1;
      JLabel apmodelabel=new JLabel("WiFi Configuration");
      gridbag.setConstraints(apmodelabel,layout);
      add(apmodelabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String apmodelist[]={"AP","Client","Hotspot"};
      apmode=new JComboBox(apmodelist);
      gridbag.setConstraints(apmode,layout);
      add(apmode);

      layout.gridwidth=1;
      JLabel modelabel=new JLabel("802.11 Mode (AP/Hotspot)");
      gridbag.setConstraints(modelabel,layout);
      add(modelabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      mode=new JComboBox(EditWiFi.modelist);
      gridbag.setConstraints(mode,layout);
      add(mode);

      layout.gridwidth=1;
      JLabel atlabel=new JLabel("Preferred Auth Type");
      gridbag.setConstraints(atlabel,layout);
      add(atlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String atlist[]={"None","WPA","EAP"};
      atype=new JComboBox(atlist);
      gridbag.setConstraints(atype,layout);
      add(atype);

      channel=new JTextField("",10);
      addLabel(new JLabel("Channel"),channel,gridbag,layout);

      key=new JTextField("",10);
      addLabel(new JLabel("Key (WEP 40/104/128bit ASCII/HEX)"),key,gridbag,layout);

      power=new JTextField("",10);
      addLabel(new JLabel("TX Power (mW)"),power,gridbag,layout);

      regdom=new JTextField("",10);
      addLabel(new JLabel("Regulatory Domain (ISO Code)"),regdom,gridbag,layout);

      if (isEdit) {
        key.setText(EditWiFi.key);
        channel.setText(EditWiFi.channel);
        power.setText(EditWiFi.power);
        atype.setSelectedItem(EditWiFi.auth);
        apmode.setSelectedItem(EditWiFi.opmode);
        mode.setSelectedIndex(EditWiFi.mode);
        regdom.setText(EditWiFi.regdom);
        addint=new JButton("Save Config");
      } else {
        mode.setSelectedIndex(2);
        atype.setSelectedItem("WPA");
        apmode.setSelectedItem("AP");
        regdom.setText("ZA");
        addint=new JButton("Add Config");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add Route");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if (!isEdit && (device != null) && (channel.getText().length() > 0)) {
        DefaultMutableTreeNode childnode=addWiFi((DefaultMutableTreeNode)device.getSelectedItem(),mode.getSelectedIndex(),
                                        atype.getSelectedItem().toString(),apmode.getSelectedItem().toString(),regdom.getText(),
                                        channel.getText(),power.getText(),key.getText());
        intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);

        key.setText("");
        channel.setText("");
        power.setText("");
        regdom.setText("ZA");
        device.setSelectedItem(tcpconf.intint);
        mode.setSelectedIndex(2);
        atype.setSelectedItem("WPA");
        apmode.setSelectedItem("AP");
      } else if (isEdit && (channel.getText().length() > 0)) {
        EditWiFi.key=key.getText();
        EditWiFi.channel=channel.getText();
        EditWiFi.mode=mode.getSelectedIndex();
        EditWiFi.auth=atype.getSelectedItem().toString();
        EditWiFi.opmode=apmode.getSelectedItem().toString();
        EditWiFi.power=power.getText();
        EditWiFi.regdom=regdom.getText();
        treeModel.reload(node);
        intwindow.scrollPathToVisible(new TreePath(node.getPath()));
      } else {
        treeModel.reload(node);
        intwindow.scrollPathToVisible(new TreePath(node));
      }
    }
  }


  public DefaultMutableTreeNode addWiFi(DefaultMutableTreeNode device,int mode,String auth,
                                            String apmode,String regdom,String channel,String power,String key) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new WiFiConfig(device,mode,auth,apmode,regdom,channel,power,key));
    treeModel.insertNodeInto(childnode,iwconfig,iwconfig.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addGenRoute(String intdescrip,String intip,String intnm,String intgw) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new GenralRoute(intdescrip,intip,intnm,intgw));
    treeModel.insertNodeInto(childnode,genroute,genroute.getChildCount());
    return childnode;
  }

  class WanConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    JTextField intdescrip,intip,intnm,intstart,intend,intlgw,intrgw; 
    JLabel textlabel;
    boolean isEdit;
    WanDef EditWan;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)intwindow.getLastSelectedPathComponent();
    JButton addint;

    public WanConfig(boolean edit) {
      isEdit=edit;
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(wanNode,treeModel,"Select Wan Node To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New Wan Route");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Wan Route");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      intdescrip=new JTextField("",10);
      addLabel(new JLabel("Description"),intdescrip,gridbag,layout);

      intip=new JTextField("",10);
      addLabel(new JLabel("Network Address"),intip,gridbag,layout);

      intnm=new JTextField("",10);
      addLabel(new JLabel("Interface Ip Subnet Bits (8-32)"),intnm,gridbag,layout);

      intstart=new JTextField("",10);
      addLabel(new JLabel("Start Of Dynamic Ip Range"),intstart,gridbag,layout);

      intend=new JTextField("",10);
      addLabel(new JLabel("End Of Dynamic Ip Range"),intend,gridbag,layout);

      intlgw=new JTextField("",10);
      addLabel(new JLabel("Local Gateway"),intlgw,gridbag,layout);

      intrgw=new JTextField("",10);
      addLabel(new JLabel("Remote Gateway"),intrgw,gridbag,layout);

      if (isEdit) {
        EditWan=(WanDef)node.getUserObject();
        intdescrip.setText(EditWan.Description);
        intip.setText(EditWan.IPAddress);
        intnm.setText(EditWan.IPSubnet);
        intstart.setText(EditWan.IPStart);
        intend.setText(EditWan.IPEnd);
        intlgw.setText(EditWan.LGateway);
        intrgw.setText(EditWan.RGateway);
        addint=new JButton("Save Wan Node");
      } else {
        addint=new JButton("Add Wan Node");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add Interface");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if ((intip.getText().length() > 0 ) && (intnm.getText().length() > 0) && 
          (intdescrip.getText().length() >0 ) && (intlgw.getText().length() >0) && 
          (intrgw.getText().length() >0 )) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addIpRoute(intdescrip.getText(),intip.getText(),intnm.getText(),
                                                      intstart.getText(),intend.getText(),intlgw.getText(),intrgw.getText());
          intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);

          intdescrip.setText("");
          intip.setText("");
          intnm.setText("");
          intstart.setText("");
          intend.setText("");
          intlgw.setText("");
          intrgw.setText("");
        } else {
          EditWan.Description=intdescrip.getText();
          EditWan.IPAddress=intip.getText();
          EditWan.IPSubnet=intnm.getText();
          EditWan.IPStart=intstart.getText();
          EditWan.IPEnd=intend.getText();
          EditWan.LGateway=intlgw.getText();
          EditWan.RGateway=intrgw.getText();
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node.getPath()));        }
      } 
    }
  }

  public DefaultMutableTreeNode addIpRoute(String intdescrip,String intip,String intnm,
                                           String intstart,String intend,String intlgw,String intrgw) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new WanDef(intdescrip,intip,intnm,intstart,
                                                                               intend,intlgw,intrgw));
    treeModel.insertNodeInto(childnode,wanNode,wanNode.getChildCount());
    return childnode;
  }

  class GreConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    JTextField intip,remip,greintbind,linkmtu,crlurl; 
    JComboBox ipsec;

    public GreConfig() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(grenode,treeModel,"Select GRE Tunnel To Manage");      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Enter Properties Of New GRE Tunnel");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      greintbind=new JTextField("",10);
      addLabel(new JLabel("Local Interface To Bind To"),greintbind,gridbag,layout);

      intip=new JTextField("",10);
      addLabel(new JLabel("Local Link IP Address"),intip,gridbag,layout);

      remip=new JTextField("",10);
      addLabel(new JLabel("Remote Hostname"),remip,gridbag,layout);

      crlurl=new JTextField("",10);
      addLabel(new JLabel("URL Of CRL For Updates"),crlurl,gridbag,layout);

      int mtu=Integer.parseInt(modemconf.MTU)-64;
      linkmtu=new JTextField(Integer.toString(mtu),10);
      addLabel(new JLabel("Link MTU"),linkmtu,gridbag,layout);

      layout.gridwidth=1;
      JLabel tlabel=new JLabel("IPSEC Encryption");
      gridbag.setConstraints(tlabel,layout);
      add(tlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      ipsec=new JComboBox(GreDef.ipsectype);
      gridbag.setConstraints(ipsec,layout);
      add(ipsec);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton addint=new JButton("Add Gre Tunnel");
      addint.setActionCommand("Add GRE Tunnel");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if ((intip.getText().length() > 0 ) && (remip.getText().length() > 0) &&
          (greintbind.getText().length() > 0) && (linkmtu.getText().length() > 0 )) {
        DefaultMutableTreeNode childnode=addGreTunnel(intip.getText(),
                                remip.getText(),greintbind.getText(),
                                linkmtu.getText(),ipsec.getSelectedIndex(),crlurl.getText());
        intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);

        intip.setText("");
        remip.setText("");
        greintbind.setText("");
        linkmtu.setText("");
        ipsec.setSelectedIndex(0);
        crlurl.setText("");
      }
    }
  }

  public DefaultMutableTreeNode addGreTunnel(String intip,String remip,String lint,String mtu,int ipsec,String crlurl) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new GreDef(intip,remip,lint,mtu,ipsec,crlurl));
    treeModel.insertNodeInto(childnode,grenode,grenode.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode getGreTunnel(String tofind) {
    GreDef intset=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = grenode.children() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      intset=(GreDef)intdata.getUserObject();
      if (intset.LocalIP.equals(tofind)) {
        outdata=intdata;
      }
    }
    return outdata;
  }


  class AddGreRoute extends Container implements ActionListener {
    JTextField greroute,tunnelid,greintbind,intip,remip,linkmtu,crlurl;
    JTextField address,snmask,username;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)intwindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    JCheckBox staticr;
    ListItem EditRoute;
    GreDef EditGre;
    JComboBox ipsec;

    public AddGreRoute(boolean edit){
      isEdit=edit;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select Route To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New Source Network");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing GRE Route");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }


      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      username=new JTextField("",10);
      addLabel(new JLabel("Network Name"),username,gridbag,layout);

      address=new JTextField("",10);
      addLabel(new JLabel("Ip Address"),address,gridbag,layout);

      snmask=new JTextField("",10);
      addLabel(new JLabel("Subnet Mask"),snmask,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      staticr=new JCheckBox("Add Static Route For Network",false);
      gridbag.setConstraints(staticr,layout);
      add(staticr);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;

      if (isEdit) {
        EditRoute=(ListItem)node.getUserObject();
        greroute.setText(EditRoute.Entry);
        adduser=new JButton("Save Route");
      } else {
        adduser=new JButton("Add Source Network");
      }

      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Gre Network");
      adduser.addActionListener(this);
      gridbag.setConstraints(adduser,layout);
      add(adduser);


      if (! isEdit) {
        EditGre=(GreDef)node.getUserObject();

        layout.anchor=GridBagConstraints.NORTH;
        layout.weightx=0;
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel textlabele=new JLabel("Edit GRE Tunnel");
        gridbag.setConstraints(textlabele,layout);
        add(textlabele);

        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTHWEST;

        greintbind=new JTextField(EditGre.LocalINT,10);
        addLabel(new JLabel("Local Interface To Bind To"),greintbind,gridbag,layout);

        intip=new JTextField(EditGre.LocalIP,10);
        addLabel(new JLabel("Local Link IP Address"),intip,gridbag,layout);

        remip=new JTextField(EditGre.RemoteIP,10);
        addLabel(new JLabel("Remote Hostname"),remip,gridbag,layout);

        crlurl=new JTextField(EditGre.CRLURL,10);
        addLabel(new JLabel("URL Of CRL For Updates"),crlurl,gridbag,layout);

        linkmtu=new JTextField(EditGre.MTU,10);
        addLabel(new JLabel("Link MTU If Needed"),linkmtu,gridbag,layout);

        layout.gridwidth=1;
        JLabel tlabel=new JLabel("IPSEC Encryption");
        gridbag.setConstraints(tlabel,layout);
        add(tlabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        ipsec=new JComboBox(GreDef.ipsectype);
        gridbag.setConstraints(ipsec,layout);
        add(ipsec);
        ipsec.setSelectedIndex(EditGre.Ipsec);

        layout.weighty=1;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        layout.anchor=GridBagConstraints.NORTH;

        JButton editgre=new JButton("Edit Tunnel");

        editgre.setActionCommand("Edit Gre Route");
        editgre.addActionListener(this);
        gridbag.setConstraints(editgre,layout);
        add(editgre);
      }
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
      if (event.getActionCommand() == "Add Gre Network") {
        if ((username.getText().length() > 0) && (address.getText().length() > 0) && (snmask.getText().length() > 0)) {
          DefaultMutableTreeNode childnode=addSourceNetwork(node,username.getText(),address.getText(),snmask.getText(),
                                                            "","",staticr.isSelected());
          intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          username.setText("");
          address.setText("");
          snmask.setText("");
          staticr.setSelected(false);
        }
      } else {
        if ((intip.getText().length() > 0 ) && (remip.getText().length() > 0)) {
          EditGre.LocalIP=intip.getText();
          EditGre.RemoteIP=remip.getText();
          EditGre.LocalINT=greintbind.getText();
          EditGre.MTU=linkmtu.getText();
          EditGre.CRLURL=crlurl.getText();
          EditGre.Ipsec=ipsec.getSelectedIndex();
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  class VoipConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    JComboBox authtype;
    JPasswordField pass1,pass2;
    JTextField user,address,keyname,authcontext,extension;
    boolean isEdit,isIAX;
    VoipReg EditVoip;
    JButton addint;
    JLabel textlabel;
    DefaultMutableTreeNode node;
    public VoipConfig(boolean edit,DefaultMutableTreeNode enode,boolean isiax) {
      isEdit=edit;
      isIAX=isiax;
      node=enode;
      
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select VOIP Registration To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New VOIP Registration");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing VOIP Registration");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }
      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      user=new JTextField("",10);
      addLabel(new JLabel("Username"),user,gridbag,layout);

      pass1=new JPasswordField("",10);
      addLabel(new JLabel("Password"),pass1,gridbag,layout);
      pass2=new JPasswordField("",10);
      addLabel(new JLabel("Confirm Password"),pass2,gridbag,layout);

      address=new JTextField("",10);
      addLabel(new JLabel("Remote Address"),address,gridbag,layout);

      if (isIAX) {
        layout.gridwidth=1;
        JLabel atlabel=new JLabel("Authentication Type");
        gridbag.setConstraints(atlabel,layout);
        add(atlabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;

        String atylist[]={"plaintext","rsa","md5"};
        authtype=new JComboBox(atylist);
        gridbag.setConstraints(authtype,layout);
        add(authtype);

        keyname=new JTextField("",10);
        addLabel(new JLabel("Key Name (For RSA Auth)"),keyname,gridbag,layout);
      } else {
        extension=new JTextField("",10);
        addLabel(new JLabel("Extension Number If Not Username"),extension,gridbag,layout);
      }

      authcontext=new JTextField("",10);
      addLabel(new JLabel("Authentication Context"),authcontext,gridbag,layout);

      if (isEdit) {
        EditVoip=(VoipReg)node.getUserObject();
        user.setText(EditVoip.Username);
        pass1.setText(EditVoip.Password);
        pass2.setText(EditVoip.Password);
        address.setText(EditVoip.Address);
        authcontext.setText(EditVoip.AuthContext);
        if (isIAX) {
          authtype.setSelectedItem(EditVoip.Authtype);
          keyname.setText(EditVoip.Key);
        } else {
          extension.setText(EditVoip.Extension);
        }
        addint=new JButton("Save VOIP Registration");
      } else {
        addint=new JButton("Add VOIP Registration");
      }
      
      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add Voip Registration");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if ((address.getText().length() > 0) && (pass1.getText().equals(pass2.getText())) &&
          (user.getText().length() > 0) && (pass1.getText().length() > 0) &&
          (authcontext.getText().length() >0) ) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode; 
          if (isIAX) {
            childnode=addIaxReg(user.getText(),pass1.getText(),address.getText(),
                                authtype.getSelectedItem().toString(),keyname.getText(),
                                authcontext.getText());
            keyname.setText("");
            authtype.setSelectedIndex(0);
          } else {
            childnode=addSipReg(user.getText(),pass1.getText(),address.getText(),
                                authcontext.getText(),extension.getText());
            extension.setText("");
          }
          if (childnode != null ) {
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          }
          authcontext.setText("");
          user.setText("");
          pass1.setText("");
          pass2.setText("");
          address.setText("");
        } else {
          EditVoip.Username=user.getText(); 
          EditVoip.Password=pass1.getText(); 
          EditVoip.Address=address.getText();
          EditVoip.AuthContext=authcontext.getText();
          if (isIAX) {
            EditVoip.Authtype=authtype.getSelectedItem().toString();
            EditVoip.Key=keyname.getText(); 
          } else {
            EditVoip.Extension=extension.getText();
          }
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  public DefaultMutableTreeNode addIaxReg(String user,String pass,String address,String authtype,
                                          String keyname,String context) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new VoipReg(user,pass,address,authtype,keyname,context));
    treeModel.insertNodeInto(childnode,voipiax,voipiax.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addSipReg(String user,String pass,String address,
                                          String context,String exten) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new VoipReg(user,pass,address,context,exten));
    treeModel.insertNodeInto(childnode,voipsip,voipsip.getChildCount());
    return childnode;
  }

  class VoipRouting extends Container implements ActionListener {
    JTextField fwdun,iaxun,iaxnum,gosun,ftun,vbox,vboxip,vboxpre,h323gkid;
    JPasswordField fwdpw1,fwdpw2,iaxpw1,iaxpw2,gospw1,gospw2,ftpw1,ftpw2,vbpw1,vbpw2;
    JComboBox vbproto,vbdtmf;
    JCheckBox voipregs,voipfuser,voipvideo,voipsrtp;
 
    public VoipRouting() {
      String vproto[]={"IAX","SIP","H.323"};
      String vdtmf[]={"auto","rfc2833","info","inband"};
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=0;
      
      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel tlvbox=new JLabel("Parent VOIP Server");
      gridbag.setConstraints(tlvbox,layout);
      add(tlvbox);


      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      vbox=new JTextField(voipdefconf.vbox,10);
      addLabel(new JLabel("Username"),vbox,gridbag,layout);

      vboxip=new JTextField(voipdefconf.vboxip,10);
      addLabel(new JLabel("IP Address"),vboxip,gridbag,layout);

      vboxpre=new JTextField(voipdefconf.vboxpre,10);
      addLabel(new JLabel("Sent Prefix (H.323)"),vboxpre,gridbag,layout);

      h323gkid=new JTextField(voipdefconf.h323gkid,10);
      addLabel(new JLabel("H.323 Gatekeeper ID"),h323gkid,gridbag,layout);

      vbpw1=new JPasswordField(voipdefconf.vboxpass,10);
      addLabel(new JLabel("Password"),vbpw1,gridbag,layout);
      vbpw2=new JPasswordField(voipdefconf.vboxpass,10);
      addLabel(new JLabel("Confirm Password"),vbpw2,gridbag,layout);


      layout.gridwidth=1;
      JLabel vplabel=new JLabel("VOIP Protocol");
      gridbag.setConstraints(vplabel,layout);
      add(vplabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.HORIZONTAL;
      
      vbproto=new JComboBox(vproto);
      gridbag.setConstraints(vbproto,layout);
      add(vbproto);
      vbproto.setSelectedItem(voipdefconf.vboxp);

      layout.gridwidth=1;
      JLabel vbdtmflabel=new JLabel("DTMF Method");
      gridbag.setConstraints(vbdtmflabel,layout);
      add(vbdtmflabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.HORIZONTAL;
      
      vbdtmf=new JComboBox(vdtmf);
      gridbag.setConstraints(vbdtmf,layout);
      add(vbdtmf);
      vbdtmf.setSelectedItem(voipdefconf.vboxdtmf);

      voipregs=new JCheckBox("Register With Provider (IAX/SIP)",voipdefconf.vboxreg);
      gridbag.setConstraints(voipregs,layout);
      add(voipregs);

      voipfuser=new JCheckBox("Send From User (Prevents Setting CLI)",voipdefconf.vboxfuser);
      gridbag.setConstraints(voipfuser,layout);
      add(voipfuser);

      voipvideo=new JCheckBox("Disable Video Codecs",voipdefconf.vboxvideo);
      gridbag.setConstraints(voipvideo,layout);
      add(voipvideo);

      voipsrtp=new JCheckBox("Attempt SRTP",voipdefconf.vboxsrtp);
      gridbag.setConstraints(voipsrtp,layout);
      add(voipsrtp);

/*
      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel1=new JLabel("Free World Dialup");
      gridbag.setConstraints(textlabel1,layout);
      add(textlabel1);
      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      fwdun=new JTextField(voipdefconf.fwduser,10);
      addLabel(new JLabel("Username"),fwdun,gridbag,layout);

      fwdpw1=new JPasswordField(voipdefconf.fwdpass,10);
      addLabel(new JLabel("Password"),fwdpw1,gridbag,layout);
      fwdpw2=new JPasswordField(voipdefconf.fwdpass,10);
      addLabel(new JLabel("Confirm Password"),fwdpw2,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel3=new JLabel("Gossip Tel");
      gridbag.setConstraints(textlabel3,layout);
      add(textlabel3);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      gosun=new JTextField(voipdefconf.gosuser,10);
      addLabel(new JLabel("Username"),gosun,gridbag,layout);

      gospw1=new JPasswordField(voipdefconf.gospass,10);
      addLabel(new JLabel("Password"),gospw1,gridbag,layout);
      gospw2=new JPasswordField(voipdefconf.gospass,10);
      addLabel(new JLabel("Confirm Password"),gospw2,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel4=new JLabel("Virbiage Fresh Tel (Firefly)");
      gridbag.setConstraints(textlabel4,layout);
      add(textlabel4);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      ftun=new JTextField(voipdefconf.fteluser,10);
      addLabel(new JLabel("Username"),ftun,gridbag,layout);

      ftpw1=new JPasswordField(voipdefconf.ftelpass,10);
      addLabel(new JLabel("Password"),ftpw1,gridbag,layout);
      ftpw2=new JPasswordField(voipdefconf.ftelpass,10);
      addLabel(new JLabel("Confirm Password"),ftpw2,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel2=new JLabel("IAX Tel");
      gridbag.setConstraints(textlabel2,layout);
      add(textlabel2);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      iaxun=new JTextField(voipdefconf.iaxuser,10);
      addLabel(new JLabel("Username"),iaxun,gridbag,layout);

      iaxpw1=new JPasswordField(voipdefconf.iaxpass,10);
      addLabel(new JLabel("Password"),iaxpw1,gridbag,layout);
      iaxpw2=new JPasswordField(voipdefconf.iaxpass,10);
      addLabel(new JLabel("Confirm Password"),iaxpw2,gridbag,layout);

      iaxnum=new JTextField(voipdefconf.iaxnumber,10);
      addLabel(new JLabel("Assigned Number"),iaxnum,gridbag,layout);
*/

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton savenet=new JButton("Save Settings");
      savenet.setActionCommand("VOIP Config");
      savenet.addActionListener(this);
      gridbag.setConstraints(savenet,layout);
      add(savenet);

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
      voipdefconf.vbox=vbox.getText();
      voipdefconf.vboxip=vboxip.getText();
      voipdefconf.vboxpre=vboxpre.getText();
      voipdefconf.h323gkid=h323gkid.getText();
      voipdefconf.vboxp=vbproto.getSelectedItem().toString();
      voipdefconf.vboxdtmf=vbdtmf.getSelectedItem().toString();
      voipdefconf.vboxreg=voipregs.isSelected();
      voipdefconf.vboxfuser=voipfuser.isSelected();
      voipdefconf.vboxvideo=voipvideo.isSelected();
      voipdefconf.vboxsrtp=voipsrtp.isSelected();
      if (vbpw1.getText().equals(vbpw2.getText())) {
        voipdefconf.vboxpass=vbpw1.getText();
      } else {
        vbpw1.setText("");
        vbpw2.setText("");
      }

/*
      voipdefconf.iaxuser=iaxun.getText();
      if (iaxpw1.getText().equals(iaxpw2.getText())) {
        voipdefconf.iaxpass=iaxpw1.getText();
      } else {
        iaxpw1.setText("");
        iaxpw2.setText("");
      }
      voipdefconf.fwduser=fwdun.getText();
      if (fwdpw1.getText().equals(fwdpw2.getText())) {
        voipdefconf.fwdpass=fwdpw1.getText();
      } else {
        fwdpw1.setText("");
        fwdpw2.setText("");
      }
      voipdefconf.gosuser=gosun.getText();
      if (gospw1.getText().equals(gospw2.getText())) {
        voipdefconf.gospass=gospw1.getText();
      } else {
        gospw1.setText("");
        gospw2.setText("");
      }
      voipdefconf.fteluser=ftun.getText();
      if (ftpw1.getText().equals(ftpw2.getText())) {
        voipdefconf.ftelpass=ftpw1.getText();
      } else {
        ftpw1.setText("");
        ftpw2.setText("");
      }
*/
    }
  }

  class EspConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    JComboBox mode,idtype,pidtype,encrypt,hash,dhgroup,pfsgroup;
//initiate;
    JTextField idval,pidval,remgw,address,localsn,remotesn;
    boolean isEdit;
    EspDef EditTun;
    JButton addint;
    JLabel textlabel;
    DefaultMutableTreeNode node;
    public EspConfig(boolean edit,DefaultMutableTreeNode enode) {

      isEdit=edit; 
      node=enode;
      
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(espnode,treeModel,"Select ESP Tunnel To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Properties Of New ESP Tunnel");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing ESP Link");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }
      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      address=new JTextField("",10);
      addLabel(new JLabel("Remote IP Address"),address,gridbag,layout);

/*
      layout.gridwidth=1;
      JLabel itlabel=new JLabel("This Link Must .. Encryption");
      gridbag.setConstraints(itlabel,layout);
      add(itlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      String tylist[]={"Enforce","Attempt"};
      initiate=new JComboBox(tylist);
      gridbag.setConstraints(initiate,layout);
      add(initiate);

      layout.gridwidth=1;
      JLabel emlabel=new JLabel("Exchange Mode");
      gridbag.setConstraints(emlabel,layout);
      add(emlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      String emlist[]={"main","aggressive"};
      mode=new JComboBox(emlist);
      gridbag.setConstraints(mode,layout);
      add(mode);

      String idlist[]={"fqdn","user_fqdn","address","asn1dn"};

      layout.gridwidth=1;
      JLabel midlabel=new JLabel("ID Type To Send");
      gridbag.setConstraints(midlabel,layout);
      add(midlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      idtype=new JComboBox(idlist);
      gridbag.setConstraints(idtype,layout);
      add(idtype);
      
      idval=new JTextField("",10);
      addLabel(new JLabel("Sent ID"),idval,gridbag,layout);

      layout.gridwidth=1;
      JLabel pidlabel=new JLabel("ID To Recive");
      gridbag.setConstraints(pidlabel,layout);
      add(pidlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      pidtype=new JComboBox(idlist);
      gridbag.setConstraints(pidtype,layout);
      add(pidtype);

      pidval=new JTextField("",10);
      addLabel(new JLabel("Recived ID"),pidval,gridbag,layout);
*/

      String cipher[]={"aes","cast128","blowfish","3des","des"};
      layout.gridwidth=1;
      JLabel cilabel=new JLabel("Proposed Cipher");
      gridbag.setConstraints(cilabel,layout);
      add(cilabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      encrypt=new JComboBox(cipher);
      gridbag.setConstraints(encrypt,layout);
      add(encrypt);

      String hashal[]={"sha1","md5"};
      layout.gridwidth=1;
      JLabel halabel=new JLabel("Proposed Hash");
      gridbag.setConstraints(halabel,layout);
      add(halabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      hash=new JComboBox(hashal);
      gridbag.setConstraints(hash,layout);
      add(hash);

      String dhglist[]={"5","2","1"};      
      layout.gridwidth=1;
      JLabel dglabel=new JLabel("Proposed Diffie-Hellman Group Phase 1");
      gridbag.setConstraints(dglabel,layout);
      add(dglabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      dhgroup=new JComboBox(dhglist);
      gridbag.setConstraints(dhgroup,layout);
      add(dhgroup);

/*
      layout.gridwidth=1;
      JLabel pglabel=new JLabel("Proposed Diffie-Hellman Group Phase 2");
      gridbag.setConstraints(pglabel,layout);
      add(pglabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;

      pfsgroup=new JComboBox(dhglist);
      gridbag.setConstraints(pfsgroup,layout);
      add(pfsgroup);
*/
      localsn=new JTextField("",10);
      addLabel(new JLabel("Local Subnet To Be Accesable"),localsn,gridbag,layout);

      remotesn=new JTextField("",10);
      addLabel(new JLabel("Remote Subnet Or Virtual IP"),remotesn,gridbag,layout);

      remgw=new JTextField("",10);
      addLabel(new JLabel("Remote Gateway (ICMP Alive Testing)"),remgw,gridbag,layout);

      if (isEdit) {
        EditTun=(EspDef)enode.getUserObject();
        address.setText(EditTun.Address);
        localsn.setText(EditTun.Local);
        remotesn.setText(EditTun.Remote);
        remgw.setText(EditTun.Test);
/*
        if (EditTun.InitType.length() > 0) {
          initiate.setSelectedItem(EditTun.InitType);
        }
        mode.setSelectedItem(EditTun.Mode);
        idtype.setSelectedItem(EditTun.IDType);
        idval.setText(EditTun.IDVal);
        pidtype.setSelectedItem(EditTun.PIDType);
        pidval.setText(EditTun.PIDVal);
*/
        encrypt.setSelectedItem(EditTun.Cipher);
        hash.setSelectedItem(EditTun.Hash);
        dhgroup.setSelectedItem(EditTun.DHGroup);
/*
        pfsgroup.setSelectedItem(EditTun.PFSGroup);
*/
        addint=new JButton("Save ESP Tunnel");
      } else {
        addint=new JButton("Add ESP Tunnel");
      }
      
      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add ESP Tunnel");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      if ((address.getText().length() > 0) && (remgw.getText().length() > 0) &&
          (localsn.getText().length() > 0) && (remotesn.getText().length() > 0)) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode; 
/*
          childnode=addEspTunnel(address.getText(),mode.getSelectedItem().toString(),
                                 idtype.getSelectedItem().toString(),idval.getText(),
                                 pidtype.getSelectedItem().toString(),pidval.getText(),
				 encrypt.getSelectedItem().toString(),hash.getSelectedItem().toString(),
				 dhgroup.getSelectedItem().toString(),pfsgroup.getSelectedItem().toString(),
                                 localsn.getText(),remotesn.getText(),remgw.getText());
*/
          childnode=addEspTunnel(address.getText(),localsn.getText(),remotesn.getText(),remgw.getText(),
                                 encrypt.getSelectedItem().toString(),hash.getSelectedItem().toString(),
                                 dhgroup.getSelectedItem().toString());

          if (childnode != null ) {
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          }

          remgw.setText("");
          address.setText("");
          localsn.setText("");
          remotesn.setText("");
/*
          initiate.setSelectedIndex(0);
          idval.setText("");
          pidval.setText("");
          mode.setSelectedIndex(0);
          idtype.setSelectedIndex(0);
          pidtype.setSelectedIndex(0);
          pfsgroup.setSelectedIndex(0);
*/
          encrypt.setSelectedIndex(0);
          hash.setSelectedIndex(0);
          dhgroup.setSelectedIndex(0);
        } else {
          EditTun.Address=address.getText();
          EditTun.Local=localsn.getText(); 
          EditTun.Remote=remotesn.getText(); 
          EditTun.Test=remgw.getText();
/*
          EditTun.InitType=initiate.getSelectedItem().toString();
          EditTun.IDVal=idval.getText();
          EditTun.PIDVal=pidval.getText();
          EditTun.Mode=mode.getSelectedItem().toString();
          EditTun.IDType=idtype.getSelectedItem().toString();
          EditTun.PIDType=pidtype.getSelectedItem().toString();
          EditTun.PFSGroup=pfsgroup.getSelectedItem().toString();
*/	
          EditTun.Cipher=encrypt.getSelectedItem().toString();
          EditTun.Hash=hash.getSelectedItem().toString();
          EditTun.DHGroup=dhgroup.getSelectedItem().toString();
          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  class EspAccConfig extends Container implements ActionListener {
    ManageNode sortpanel;
    JComboBox lint;
    JTextField idval,pidval,localsn,remotesn;
    boolean isEdit;
    EspDef EditTun;
    JButton addint;
    JLabel textlabel;
    DefaultMutableTreeNode node;
    public EspAccConfig(boolean edit,DefaultMutableTreeNode enode) {

      isEdit=edit; 
      node=enode;
      
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(espaccnode,treeModel,"Select ESP Access To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Allow The Following Access Via ESP");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing ESP Access");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }
      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      localsn=new JTextField("",10);
      addLabel(new JLabel("Local Subnet To Be Accesable"),localsn,gridbag,layout);

      remotesn=new JTextField("",10);
      addLabel(new JLabel("Remote Subnet To Access"),remotesn,gridbag,layout);

      layout.gridwidth=1;
      JLabel portlabel=new JLabel("Local Subnet Linked To");
      gridbag.setConstraints(portlabel,layout);
      add(portlabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      lint=new JComboBox(getIntList(false));
      lint.setSelectedItem(tcpconf.intint);
      gridbag.setConstraints(lint,layout);
      add(lint);

      if (isEdit) {
        EditTun=(EspDef)enode.getUserObject();
        localsn.setText(EditTun.Local);
        remotesn.setText(EditTun.Remote);
	DefaultMutableTreeNode intnode=getInterface(EditTun.Interface);
	if (intnode != null) {
	  lint.setSelectedItem(intnode);
        } else {
          lint.setSelectedIndex(0);
        }
        addint=new JButton("Save ESP Access");
      } else {
        addint=new JButton("Add ESP Access");
      }
      
      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      addint.setActionCommand("Add ESP Tunnel");
      addint.addActionListener(this);
      gridbag.setConstraints(addint,layout);
      add(addint);

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
      IntDef intset;
      DefaultMutableTreeNode intnode;

      if ((localsn.getText().length() > 0) && (remotesn.getText().length() > 0)) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode; 

          intnode=(DefaultMutableTreeNode)lint.getSelectedItem();
          intset=(IntDef)intnode.getUserObject();
          childnode=addEspTunnel(localsn.getText(),remotesn.getText(),intset.IntName);

          if (childnode != null ) {
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          }

          localsn.setText("");
          remotesn.setText("");
          lint.setSelectedIndex(0);
        } else {
          EditTun.Local=localsn.getText(); 
          EditTun.Remote=remotesn.getText(); 

          intnode=(DefaultMutableTreeNode)lint.getSelectedItem();
          intset=(IntDef)intnode.getUserObject();
          EditTun.Interface=intset.IntName;

          treeModel.reload(node);
          intwindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }
  
  public DefaultMutableTreeNode addEspTunnel(String address,String local,String remote,String ltest,
                                             String crypt,String hash,String dhgroup) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new EspDef(address,local,remote,ltest,crypt,
                                                                           hash,dhgroup));
    treeModel.insertNodeInto(childnode,espnode,espnode.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addEspTunnel(String address,String local,String remote,
                                             String ltest,String ctype) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new EspDef(address,local,remote,ltest,
                                                                ctype));
    treeModel.insertNodeInto(childnode,espnode,espnode.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addEspTunnel(String local,String remote) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new EspDef(local,remote));
    treeModel.insertNodeInto(childnode,espaccnode,espaccnode.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addEspTunnel(String local,String remote,String iface) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new EspDef(local,remote,iface));
    treeModel.insertNodeInto(childnode,espaccnode,espaccnode.getChildCount());
    return childnode;
  }
 
  class ModemConfig extends Container implements ActionListener {
    JComboBox port,speed,flow,ctype;
    JCheckBox carrier,error,busy,dialtone,bsdcomp,defcomp;
    JTextField init1,init2,dial,number,username,password,destip,localip,idleto,holdoff,maxfail,condelay,mtumru;
/*
    linktest
*/

    public ModemConfig() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=0;
      
      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      layout.gridwidth=1;
      JLabel ctypelabel=new JLabel("Connection Type");
      gridbag.setConstraints(ctypelabel,layout);
      add(ctypelabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String typelist[]={"Dialup","Leased","ADSL","3G","3GIPW"};
      ctype=new JComboBox(typelist);
      ctype.setSelectedItem(modemconf.ConnType);
      gridbag.setConstraints(ctype,layout);
      ctype.addActionListener(this);
      ctype.setActionCommand("Change");
      add(ctype);

      layout.gridwidth=1;
      JLabel portlabel=new JLabel("Com Port (Set PPPoE Port In Global Settings)");
      gridbag.setConstraints(portlabel,layout);
      add(portlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String portlist[]={"Com1","Com2","Com3","Com4","USB0","USB1","USB2","USB3"};
      port=new JComboBox(portlist);
      port.setSelectedItem(modemconf.ComPort);
      gridbag.setConstraints(port,layout);
      add(port);
      holdoff=new JTextField(modemconf.HoldoffTime,10);
      addLabel(new JLabel("Holdoff Time (s)"),holdoff,gridbag,layout);

/*
      linktest=new JTextField(modemconf.LinkTest,10);
      addLabel(new JLabel("Addr. For DNS Link Tests"),linktest,gridbag,layout);
*/
      mtumru=new JTextField(modemconf.MTU,10);
      addLabel(new JLabel("MTU/MRU"),mtumru,gridbag,layout);

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=1;
      bsdcomp=new JCheckBox("Use BSD Compresion",modemconf.BSD);
      gridbag.setConstraints(bsdcomp,layout);
      add(bsdcomp);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      defcomp=new JCheckBox("Use Deflate Compresion",modemconf.Deflate);
      gridbag.setConstraints(defcomp,layout);
      add(defcomp);


      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel2=new JLabel("Dialup And Leased Line Options");
      gridbag.setConstraints(textlabel2,layout);
      add(textlabel2);
      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      layout.gridwidth=1;
      JLabel speedlabel=new JLabel("Port Speed");
      gridbag.setConstraints(speedlabel,layout);
      add(speedlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String speedlist[]={"115200","57600","38400","19200","9600"};
      speed=new JComboBox(speedlist);
      speed.setSelectedItem(modemconf.Speed);
      gridbag.setConstraints(speed,layout);
      add(speed);
      layout.gridwidth=1;
      JLabel flowlabel=new JLabel("Flow Control");
      gridbag.setConstraints(flowlabel,layout);
      add(flowlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String flowlist[]={"Hardware (RTS/CTS)","Software","Hardware (DTR/CTS)","None"};
      flow=new JComboBox(flowlist);
      flow.setSelectedItem(modemconf.FlowControl);
      gridbag.setConstraints(flow,layout);
      add(flow);
      localip=new JTextField(modemconf.LocalIP,10);
      addLabel(new JLabel("Local Address"),localip,gridbag,layout);
      destip=new JTextField(modemconf.DestIP,10);
      addLabel(new JLabel("Gateway Address"),destip,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel3=new JLabel("Authentication Options (3G/3GIPW/ADSL/Dialup)");
      gridbag.setConstraints(textlabel3,layout);
      add(textlabel3);
      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      number=new JTextField(modemconf.Number,10);
      addLabel(new JLabel("Dial In Number/Service ID/APN"),number,gridbag,layout);

      username=new JTextField(modemconf.UserName,10);
      addLabel(new JLabel("Dial In User Name"),username,gridbag,layout);

      password=new JTextField(modemconf.Password,10);
      addLabel(new JLabel("Dial In Password"),password,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel4=new JLabel("Connection Options (Dialup)");
      gridbag.setConstraints(textlabel4,layout);
      add(textlabel4);
      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      init1=new JTextField(modemconf.InitString1,10);
      addLabel(new JLabel("Init String 1"),init1,gridbag,layout);
      init2=new JTextField(modemconf.InitString2,10);
      addLabel(new JLabel("Init String 2"),init2,gridbag,layout);
      dial=new JTextField(modemconf.DialString,10);
      addLabel(new JLabel("Dial String"),dial,gridbag,layout);

      idleto=new JTextField(modemconf.IdleTimeout,10);
      addLabel(new JLabel("Idle Timeout (s)"),idleto,gridbag,layout);
      maxfail=new JTextField(modemconf.MaxFail,10);
      addLabel(new JLabel("Fail Limit"),maxfail,gridbag,layout);
 
      condelay=new JTextField(modemconf.ConnectDelay,10);
      addLabel(new JLabel("Connect Delay (ms)"),condelay,gridbag,layout);

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=1;
      carrier=new JCheckBox("Abort On NO CARRIER",modemconf.NoCarrier);
      gridbag.setConstraints(carrier,layout);
      add(carrier);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      dialtone=new JCheckBox("Abort On NO DIALTONE",modemconf.NoDialtone);
      gridbag.setConstraints(dialtone,layout);
      add(dialtone);

      layout.gridwidth=1;
      busy=new JCheckBox("Abort On BUSY",modemconf.Busy);
      gridbag.setConstraints(busy,layout);
      add(busy);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      error=new JCheckBox("Abort On Error",modemconf.Error);
      gridbag.setConstraints(error,layout);
      add(error);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton savenet=new JButton("Save Settings");
      savenet.setActionCommand("Modem Config");
      savenet.addActionListener(this);
      gridbag.setConstraints(savenet,layout);
      add(savenet);

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
      if (event.getActionCommand() == "Modem Config") {
        setAdslACC(modemconf.UserName,modemconf.Password,false);
        if (ctype.getSelectedItem().toString().equals("ADSL")) {
          if (getAdslACC(username.getText()) != null) {
            setAdslACC(username.getText(),password.getText(),true);
          } else {
            addAdslACC(username.getText(),password.getText(),1);
          }
        }
        modemconf.ComPort=port.getSelectedItem().toString();
        modemconf.Speed=speed.getSelectedItem().toString();
        modemconf.FlowControl=flow.getSelectedItem().toString();
        modemconf.ConnType=ctype.getSelectedItem().toString();
        modemconf.InitString1=init1.getText();
        modemconf.InitString2=init2.getText();
        modemconf.DialString=dial.getText();
        modemconf.Number=number.getText();
        modemconf.UserName=username.getText();
        modemconf.Password=password.getText();
        modemconf.MTU=mtumru.getText();
        modemconf.DestIP=destip.getText();
        modemconf.LocalIP=localip.getText();
        modemconf.IdleTimeout=idleto.getText();
        modemconf.ConnectDelay=condelay.getText();
        modemconf.HoldoffTime=holdoff.getText();
/*
        modemconf.LinkTest=linktest.getText();
*/
        modemconf.MaxFail=maxfail.getText();
        modemconf.NoCarrier=carrier.isSelected();
        modemconf.NoDialtone=dialtone.isSelected();
        modemconf.Busy=busy.isSelected();
        modemconf.Error=error.isSelected();
        modemconf.Deflate=defcomp.isSelected();
        modemconf.BSD=bsdcomp.isSelected();
      } else {
        if (ctype.getSelectedItem().toString().equals("3GIPW")) {
          init1.setText("");
          init2.setText("");
          dial.setText("");
          idleto.setText("");
          maxfail.setText("");
          number.setText("sentech.co.za");
          mtumru.setText("1500");
        } else if (ctype.getSelectedItem().toString().equals("3G")) {
          init1.setText("");
          init2.setText("");
          dial.setText("");
          idleto.setText("");
          maxfail.setText("");
          number.setText("internet");
          mtumru.setText("1500");
        } else if (ctype.getSelectedItem().toString().equals("ADSL")) {
          init1.setText("");
          init2.setText("");
          dial.setText("");
          idleto.setText("");
          maxfail.setText("");
          number.setText("TelkomSA");
          mtumru.setText("1450");
        } else {
          idleto.setText("120");
          maxfail.setText("5");
          init1.setText("AT&F");
          init2.setText("ATL1M1");
          dial.setText("ATDT");
          mtumru.setText("1500");
          number.setText("");
        }
      }
    }
  }

  class FaxConfig extends Container implements ActionListener {
    JTextField tagname,tagnum,ccode,acode,ldist,intpre,retry,number,ringdelay,rtimeout,cidno,cidna;

    public FaxConfig() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;
      layout.weighty=0;

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Fax Configuration");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      tagname=new JTextField(faxconf.Tagline,10);
      addLabel(new JLabel("Name Appearing In Tagline"),tagname,gridbag,layout);

      tagnum=new JTextField(faxconf.Tagnum,10);
      addLabel(new JLabel("Number Appearing In Tagline"),tagnum,gridbag,layout);

      cidno=new JTextField(faxconf.CIDNumber,10);
      addLabel(new JLabel("CID Number Filter"),cidno,gridbag,layout);

      cidna=new JTextField(faxconf.CIDName,10);
      addLabel(new JLabel("CID Name Filter"),cidna,gridbag,layout);

      ccode=new JTextField(faxconf.Country,10);
      addLabel(new JLabel("Country Code"),ccode,gridbag,layout);

      acode=new JTextField(faxconf.Area,10);
      addLabel(new JLabel("Area Code"),acode,gridbag,layout);

      ldist=new JTextField(faxconf.LongDist,10);
      addLabel(new JLabel("Long Distance Prefix"),ldist,gridbag,layout);

      intpre=new JTextField(faxconf.IntPre,10);
      addLabel(new JLabel("International Prefix"),intpre,gridbag,layout);

      retry=new JTextField(faxconf.Retry,10);
      addLabel(new JLabel("Number Of Retrys"),retry,gridbag,layout);

      rtimeout=new JTextField(faxconf.TimeOut,10);
      addLabel(new JLabel("Delay Between Retries"),rtimeout,gridbag,layout);

      ringdelay=new JTextField(faxconf.Rings,10);
      addLabel(new JLabel("Number Of Rings Before Awnser"),ringdelay,gridbag,layout);

      number=new JTextField(faxconf.Pages,10);
      addLabel(new JLabel("Max Pages (Sent/Recv.)"),number,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton savenet=new JButton("Save Settings");
      savenet.setActionCommand("Modem Config");
      savenet.addActionListener(this);
      gridbag.setConstraints(savenet,layout);
      add(savenet);

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
      faxconf.Tagline=tagname.getText();
      faxconf.Tagnum=tagnum.getText();
      faxconf.Country=ccode.getText();
      faxconf.Area=acode.getText();
      faxconf.LongDist=ldist.getText();
      faxconf.IntPre=intpre.getText();
      faxconf.Retry=retry.getText();
      faxconf.Pages=number.getText();
      faxconf.Rings=ringdelay.getText();
      faxconf.CIDNumber=cidno.getText();
      faxconf.CIDName=cidna.getText();
      faxconf.TimeOut=rtimeout.getText();
    }
  }

  public class AddFwUser extends Container implements ActionListener{
    JTextField username,address,intmac,snmask,intip,ingress,egress,intgw,intstart,intend,inteth,intingress,integress;
    JComboBox area;
    ManageNode sortpanel;
    IntDef intdata;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)intwindow.getLastSelectedPathComponent();

    public AddFwUser(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Source Network To Manage");
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Enter Properties Of New Source Network");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      username=new JTextField("",10);
      addLabel(new JLabel("Network Name"),username,gridbag,layout);

      address=new JTextField("",10);
      addLabel(new JLabel("Ip Address"),address,gridbag,layout);

      snmask=new JTextField("",10);
      addLabel(new JLabel("Subnet Mask"),snmask,gridbag,layout);

      if (! node.isNodeAncestor(modemrules)) {
        ingress=new JTextField("",10);
        addLabel(new JLabel("Bandwidth Limit (Incoming)"),ingress,gridbag,layout);

        egress=new JTextField("",10);
        addLabel(new JLabel("Bandwidth Limit (Outgoing)"),egress,gridbag,layout);
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Source Network");

      adduser.setActionCommand("Add Source Network");
      adduser.addActionListener(this);

      gridbag.setConstraints(adduser,layout);
      add(adduser);

      if (node != modemrules) {
        intdata=(IntDef)node.getUserObject();

        layout.weightx=1;
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel textlabele=new JLabel("Edit "+intdata.IntName);
        gridbag.setConstraints(textlabele,layout);
        add(textlabele);

        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTHWEST;


        inteth=new JTextField(intdata.Description,10);
        addLabel(new JLabel("Description"),inteth,gridbag,layout);

        intip=new JTextField(intdata.IPAddress+"/"+intdata.IPSubnet,10);
        addLabel(new JLabel("Interface Ip Address (IP/SN)"),intip,gridbag,layout);

        intgw=new JTextField(intdata.IPGateway,10);
        addLabel(new JLabel("Dhcp Gateway (If Not Server)"),intgw,gridbag,layout);

        intmac=new JTextField(intdata.MAC,10);
        addLabel(new JLabel("Interface MAC Address (optional)"),intmac,gridbag,layout);

        intstart=new JTextField(intdata.IPStart,10);
        addLabel(new JLabel("Start Of Dynamic Ip Range"),intstart,gridbag,layout);

        intend=new JTextField(intdata.IPEnd,10);
        addLabel(new JLabel("End Of Dynamic Ip Range"),intend,gridbag,layout);

        intingress=new JTextField(intdata.Ingress,10);
        addLabel(new JLabel("Available Incoming Bandwidth"),intingress,gridbag,layout);

        integress=new JTextField(intdata.Egress,10);
        addLabel(new JLabel("Available Outgoing Bandwidth"),integress,gridbag,layout);

        layout.weighty=1;
        layout.fill=GridBagConstraints.NONE;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;

        JButton addint=new JButton("Save "+intdata.IntName);

        addint.setActionCommand("Save Interface");
        addint.addActionListener(this);
        gridbag.setConstraints(addint,layout);
        add(addint);
      }
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
      String ipdata[];

      if (event.getActionCommand() == "Add Source Network") {
        if ((username.getText().length() > 0) && (address.getText().length() > 0) && (snmask.getText().length() > 0)) {
          if (! node.isNodeAncestor(modemrules)) {
            DefaultMutableTreeNode childnode=addSourceNetwork(node,username.getText(),address.getText(),snmask.getText(),
                                                              ingress.getText(),egress.getText());
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          } else {
            DefaultMutableTreeNode childnode=addSourceNetwork(node,username.getText(),address.getText(),snmask.getText(),
                                                              "","");
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
          }
          username.setText("");
          address.setText("");
          snmask.setText("");
          if (! node.isNodeAncestor(modemrules)) {
            ingress.setText("");
            egress.setText("");
          }
        }
      }

      if (event.getActionCommand() == "Save Interface") {
        ipdata=intip.getText().split("/");
        intdata.IPAddress=ipdata[0];
        intdata.IPSubnet=ipdata[1];
        intdata.IPGateway=intgw.getText();
        intdata.IPStart=intstart.getText();
        intdata.IPEnd=intend.getText();
        intdata.Description=inteth.getText();
        intdata.Ingress=intingress.getText();
        intdata.MAC=intmac.getText();
        intdata.Egress=integress.getText();
        treeModel.reload(node.getParent());
        intwindow.setSelectionPath(new TreePath(node.getPath()));
      }
    }
  }

  public DefaultMutableTreeNode addSourceNetwork(DefaultMutableTreeNode node,String username,String address,String snmask,
                                                 String ingress,String egress){
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FwUser(username,address,snmask,node.toString(),ingress,egress));
    treeModel.insertNodeInto(childnode,node,node.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addSourceNetwork(DefaultMutableTreeNode node,String username,String address,String snmask,
                                                 String ingress,String egress,boolean sroute){
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FwUser(username,address,snmask,
                                             node.toString(),ingress,egress,sroute));
    treeModel.insertNodeInto(childnode,node,node.getChildCount());
    return childnode;
  }


  public DefaultMutableTreeNode getSourceNetwork(DefaultMutableTreeNode node,String tofind) {
    FwUser intset=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    if (node == null) {
      return node;
    }
    for (Enumeration e = node.children() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      intset=(FwUser)intdata.getUserObject();
      if (intset.UserName.equals(tofind)) {
        outdata=intdata;
      }
    }
    return outdata;
  }

  public void setGreRoute(String tofind,String network,String snmask) {
    FwUser intset=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode node=null;
    DefaultMutableTreeNode outdata=null;

    node=getGreTunnel(tofind);
    if (node != null) {
      for (Enumeration e = node.children() ; e.hasMoreElements() ;) {
        intdata=(DefaultMutableTreeNode)e.nextElement();
        intset=(FwUser)intdata.getUserObject();
        if ((intset.IPSubnet.equals(snmask)) && (intset.IPAddress.equals(network))) {
          intset.Route=true;
        }
      }
    }
  }

  public DefaultMutableTreeNode getAdslACC(String username) { 
    AdslAccount eadslacc=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = adslacc.children() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      eadslacc=(AdslAccount)intdata.getUserObject();
      if (eadslacc.Username.equals(username)) {
        outdata=intdata;
      }
    }
    return outdata;
  }

  public void setAdslACC(String username,String password,boolean active) { 
    AdslAccount eadslacc=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = adslacc.children() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      eadslacc=(AdslAccount)intdata.getUserObject();
      if (eadslacc.Username.equals(username)) {
        eadslacc.Password=password;
        if (active) {
          if (eadslacc.Status == 0){
            eadslacc.Status=1;
          } else if (eadslacc.Status == 2) {
            eadslacc.Status=3;       
          }
        } else {
          if (eadslacc.Status == 1){
            eadslacc.Status=0;
          } else if (eadslacc.Status == 3) {
            eadslacc.Status=2;
          }
        }
        outdata=intdata;
      }
    }
  }

  public void setAdslLink(String username,String password) { 
    ExtraAdslLink eadsllink=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = adsllink.children() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      eadsllink=(ExtraAdslLink)intdata.getUserObject();
      if (eadsllink.User.equals(username)) {
        eadsllink.Pass=password;
        outdata=intdata;
      }
      setAdslACC(eadsllink.User,eadsllink.Pass,true);
    }

    if ((modemconf.ConnType.equals("ADSL")) && (modemconf.UserName.equals(username))) {
      modemconf.Password=password;
    }
  }

  private class FwUser {
    String UserName,IPAddress,IPSubnet,Area,Ingress,Egress;
    boolean Route;
    public FwUser(String User,String Addr,String Snet,String Location,String bwin,String bwout){
      UserName=User;
      IPAddress=Addr;
      IPSubnet=Snet;
      Area=Location;
      Ingress=bwin;
      Egress=bwout;
      Route=false;
    }
    public FwUser(String User,String Addr,String Snet,String Location,String bwin,String bwout,
                  boolean sroute){
      UserName=User;
      IPAddress=Addr;
      IPSubnet=Snet;
      Area=Location;
      Ingress=bwin;
      Egress=bwout;
      Route=sroute;
    }
    public String toString(){
      String Output=UserName+" ("+IPAddress+"/"+IPSubnet;
      if (Ingress.length() > 0) {
        Output=Output+" "+Ingress+"kbs in";
      }
      if (Egress.length() > 0) {
        Output=Output+" "+Egress+"kbs out";
      }

      if (Route) {
        Output=Output+" Staticly Routed";
      }

      Output=Output+")";
      return Output;
    }
  }

  private class FwRule {
    String Protocol,Type,Action,Destination,IPAddress,SourcePort,DestPort,Description,State,DirFlag,Pdir,TOS,Priority;
    public FwRule(String Proto,String Typ,String Act,String Dest,String Addr,String Sprt,String Dprt,
                  String Descrip,String Rstate,String pflow,String tosval,String prio){
      Pdir=pflow;
      State=Rstate;
      Protocol=Proto;
      Type=Typ;
      Action=Act;
      Destination=Dest;
      Description=Descrip;
      TOS=tosval;
      Priority=prio;
      if (Pdir.equals("In")) {
        DirFlag=" <-- ";
      } else {
        DirFlag=" --> ";
      }

      if (Addr.equals("0") | Addr.equals("0.0.0.0") | Addr.equals("0.0.0.0/0")) {
        IPAddress="0/0";
      } else {
        IPAddress=Addr;
      }
      if (Sprt.equals("")) {
        SourcePort="0:65535";
      } else {
        SourcePort=Sprt;
      }
      if (Dprt.equals("")) {
        DestPort="0:65535";
      } else {
        DestPort=Dprt;
      }
    }
    public String toString(){
      String Output=Description+" ("+Action+" "+State+" "+Type+" "+Protocol+" "+SourcePort+DirFlag+IPAddress+":"+DestPort+" Via "+Destination+")";
      return Output;
    }
    public void updaterule() {
      if (Pdir.equals("In")) {
        DirFlag=" <-- ";
      } else {
        DirFlag=" --> ";
      }

      if (IPAddress.equals("0") | IPAddress.equals("0.0.0.0") | IPAddress.equals("0.0.0.0/0")) {
        IPAddress="0/0";
      }
      if (SourcePort.equals("")) {
        SourcePort="0:65535";
      }
      if (DestPort.equals("")) {
        DestPort="0:65535";
      }
    }
  }


  private class FwProto {
    String Protocol,Type,Action,SourcePort,DestPort,Description,State,DirFlag,Pdir;
    public FwProto(String Proto,String Typ,String Act,String Sprt,String Dprt,String Descrip,String Rstate,String pflow){
      Pdir=pflow;
      State=Rstate;
      Protocol=Proto;
      Type=Typ;
      Action=Act;
      Description=Descrip;

      if (Pdir.equals("In")) {
        DirFlag=" <-- ";
      } else {
        DirFlag=" --> ";
      }
      if (Sprt.equals("")) {
        SourcePort="0:65535";
      } else {
        SourcePort=Sprt;
      }
      if (Dprt.equals("")) {
        DestPort="0:65535";
      } else {
        DestPort=Dprt;
      }
    }
    public String toString(){
      String Output=Description+" ("+Action+" "+State+" "+Type+" "+Protocol+" "+SourcePort+" "+DirFlag+" "+DestPort+")";
      return Output;
    }
    public void updaterule() {
      if (Pdir.equals("In")) {
        DirFlag=" <-- ";
      } else {
        DirFlag=" --> ";
      }
      if (SourcePort.equals("")) {
        SourcePort="0:65535";
      }
      if (DestPort.equals("")) {
        DestPort="0:65535";
      }
    }
    public String confOut() {
      String Output="";

      Output=Protocol+" "+Type+" "+Action.replaceAll(" ","_")+" "+SourcePort+" "+DestPort+" "+Description.replaceAll(" ","_")+" "+
             State+" "+Pdir;   

      return Output;
    }
  }


  class RuleConfig extends Container implements ActionListener{
    JComboBox protocol,statetype,ruletype,ruleaction,destination,pdir,plist,rtos,ingressmark;
    JTextField destip=new JTextField("",10);
    JTextField srcprt=new JTextField("",10);
    JTextField dstprt=new JTextField("",10);
    JTextField discrip=new JTextField("",10);
    JTextField address,snmask,ingressbw,egress;
    ManageNode sortpanel;
    FwUser sndata;
    JCheckBox staticr;
    DefaultMutableTreeNode node;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    FwRule EditRule;

    public RuleConfig(DefaultMutableTreeNode actnode, boolean edit){
      node=actnode;
      isEdit=edit;

      String newline = System.getProperty("line.separator");

      String rtype[]={"NAT","Forward","Proxy","Local"};
      String raction[]={"Accept","Deny And Log","Deny"};
      String rstate[]={"New","Related","Any"};
      String rdir[]={"Out","In"};
      String rproto[]={"TCP","UDP","GRE","ESP","AH","OSPF","ALL"};
      String tosv[]={"Normal-Service","Minimize-Cost","Maximize-Reliability","Maximize-Throughput","Minimize-Delay"};
      String ingress[]={"High","Med","Low"};

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;


      layout.anchor=GridBagConstraints.NORTH;

      if (! isEdit) {
        layout.weighty=1;
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select Rule To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        textlabel=new JLabel("Enter Properties Of New Rule");
        layout.gridwidth=GridBagConstraints.REMAINDER;
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        textlabel=new JLabel("Editing Rule");
        layout.gridwidth=GridBagConstraints.REMAINDER;
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }
       
     
      if (isEdit) {
        EditRule=(FwRule)node.getUserObject();
      }

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      addLabel(new JLabel("Description"),discrip,gridbag,layout);
      addLabel(new JLabel("Destination Address/Subnet"),destip,gridbag,layout);

      layout.fill=GridBagConstraints.NONE;
      if (node.isNodeAncestor(grenode)){
        layout.gridwidth=GridBagConstraints.REMAINDER;
      } else {
        layout.gridwidth=2;
      }
      JLabel dlabel=new JLabel("Trafic Passes Via ...");
      gridbag.setConstraints(dlabel,layout);
      add(dlabel);

      if (! node.isNodeAncestor(grenode)){
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel iglabel=new JLabel("Priority");
        gridbag.setConstraints(iglabel,layout);
        add(iglabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      if (node.isNodeAncestor(grenode)){ 
        layout.gridwidth=GridBagConstraints.REMAINDER;
      } else {
        layout.gridwidth=2;
      }

      destination=new JComboBox(getIntList(false));
      destination.addItem(dod);
      destination.addItem(tun);
      destination.addItem(ovpn);
      if (tcpconf.extint != null) {
        destination.setSelectedItem(tcpconf.extint);
      }
      gridbag.setConstraints(destination,layout);
      add(destination);

      ingressmark=new JComboBox(ingress);
      if (! node.isNodeAncestor(grenode)){ 
        layout.gridwidth=GridBagConstraints.REMAINDER;
        gridbag.setConstraints(ingressmark,layout);
        add(ingressmark);
      }
      
      layout.gridwidth=1;
      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      JLabel plabel=new JLabel("Protocol");
      gridbag.setConstraints(plabel,layout);
      add(plabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      JLabel pdlabel=new JLabel("Direction");
      gridbag.setConstraints(pdlabel,layout);
      add(pdlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel tlabel=new JLabel("Type");
      gridbag.setConstraints(tlabel,layout);
      add(tlabel);

      layout.gridwidth=1;
      layout.fill=GridBagConstraints.HORIZONTAL;
      
      protocol=new JComboBox(rproto);
      gridbag.setConstraints(protocol,layout);
      add(protocol);

      pdir=new JComboBox(rdir);
      gridbag.setConstraints(pdir,layout);
      add(pdir);

      ruletype=new JComboBox(rtype);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(ruletype,layout);
      add(ruletype);

      layout.gridwidth=1;
      layout.anchor=GridBagConstraints.NORTHWEST;
      
      JLabel slabel=new JLabel("State");
      gridbag.setConstraints(slabel,layout);
      add(slabel);
      
      JLabel alabel=new JLabel("Action");
      gridbag.setConstraints(alabel,layout);
      add(alabel);

      JLabel toslabel=new JLabel("TOS (Not Proxy)");
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(toslabel,layout);
      add(toslabel);

      layout.gridwidth=1;
      layout.fill=GridBagConstraints.HORIZONTAL;
      
      statetype=new JComboBox(rstate);
      gridbag.setConstraints(statetype,layout);
      add(statetype);

      ruleaction=new JComboBox(raction);
      gridbag.setConstraints(ruleaction,layout);
      add(ruleaction);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      rtos=new JComboBox(tosv);
      gridbag.setConstraints(rtos,layout);
      add(rtos);

      addLabel(new JLabel("Source Port"),srcprt,gridbag,layout);
      addLabel(new JLabel("Destination Port"),dstprt,gridbag,layout);


      if (! isEdit) {
        layout.weightx=0;
      } else {
        protocol.setSelectedItem(EditRule.Protocol);
        pdir.setSelectedItem(EditRule.Pdir);
        ruletype.setSelectedItem(EditRule.Type);
        statetype.setSelectedItem(EditRule.State);
        ruleaction.setSelectedItem(EditRule.Action);
        ingressmark.setSelectedItem(EditRule.Priority);
        rtos.setSelectedItem(EditRule.TOS);
        if (EditRule.Destination.equals("-")) {
          destination.setSelectedItem(dod);
        } else if (EditRule.Destination.equals("+")) {
          destination.setSelectedItem(tun);
        } else if (EditRule.Destination.equals("=")) {
          destination.setSelectedItem(ovpn);
        } else {
          DefaultMutableTreeNode passint=getInterface(EditRule.Destination);
          destination.setSelectedItem(passint);
        }
        destip.setText(EditRule.IPAddress);
        dstprt.setText(EditRule.DestPort);
        srcprt.setText(EditRule.SourcePort);
        discrip.setText(EditRule.Description);
      }
      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;

      if (isEdit) {
        adduser=new JButton("Save Rule");
      } else {
        adduser=new JButton("Save New Rule");
      }

      adduser.setActionCommand("Add New Rule");
      adduser.addActionListener(this);

      gridbag.setConstraints(adduser,layout);
      add(adduser);


      if (! isEdit) {
        sndata=(FwUser)node.getUserObject();

        layout.anchor=GridBagConstraints.NORTH;
        layout.weightx=0;
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel textlabele=new JLabel("Edit "+sndata.UserName);
        gridbag.setConstraints(textlabele,layout);
        add(textlabele);

        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTHWEST;

        address=new JTextField(sndata.IPAddress,10);
        addLabel(new JLabel("Ip Address"),address,gridbag,layout);

        snmask=new JTextField(sndata.IPSubnet,10);
        addLabel(new JLabel("Subnet Mask"),snmask,gridbag,layout);      

        if ((! node.isNodeAncestor(modemrules)) && (! node.isNodeAncestor(grenode))) {
          ingressbw=new JTextField(sndata.Ingress,10);
          addLabel(new JLabel("Bandwidth Limit (Incoming)"),ingressbw,gridbag,layout);      

          egress=new JTextField(sndata.Egress,10);
          addLabel(new JLabel("Bandwidth Limit (Outgoing)"),egress,gridbag,layout);      
        } else if (node.isNodeAncestor(grenode)) {
          layout.gridwidth=GridBagConstraints.REMAINDER;
          staticr=new JCheckBox("Add Static Route For Network",sndata.Route);
          gridbag.setConstraints(staticr,layout);
          add(staticr);
        }

        layout.weighty=1;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        layout.anchor=GridBagConstraints.NORTH;

        JButton edituser=new JButton("Save "+sndata.UserName);
        edituser.setActionCommand("Edit User");
        edituser.addActionListener(this);
        gridbag.setConstraints(edituser,layout);
        add(edituser);
      }

      }
      private void addLabel(JLabel label,JTextField textfield,GridBagLayout gridbag,GridBagConstraints layout){
        layout.gridwidth=2;
        layout.weightx=1;
        layout.fill=GridBagConstraints.NONE;
        gridbag.setConstraints(label,layout);
        add(label);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.weightx=1;
        layout.fill=GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(textfield,layout);
        add(textfield);
    }
    public void actionPerformed(ActionEvent event) {
      IntDef destint;
      DefaultMutableTreeNode iface;
      String intname;

      if (event.getActionCommand() == "Add New Rule") {
        if (ruletype.getSelectedItem().toString().equals("Local")) {
          destip.setText("-");
        }

        if ((destip.getText().length() > 0) && (discrip.getText().length() > 0)) {
          iface=(DefaultMutableTreeNode)destination.getSelectedItem();
  
          if ((! iface.toString().equals("Modem")) && (! iface.toString().equals("GRE Tunnel")) && (! iface.toString().equals("Open VPN"))) {
            destint=(IntDef)iface.getUserObject();
            intname=destint.IntName;
          } else if (iface.toString().equals("GRE Tunnel")) {
            intname="+";
          } else if (iface.toString().equals("Open VPN")) {
            intname="=";
          } else {
            intname="-";
          }

          if (! isEdit) {
            DefaultMutableTreeNode childnode=addFwRule(node,protocol.getSelectedItem().toString(),
                                                       ruletype.getSelectedItem().toString(),
                                                       ruleaction.getSelectedItem().toString(),
                                                       intname,
                                                       destip.getText(),
                                                       srcprt.getText(),
                                                       dstprt.getText(),
                                                       discrip.getText(),
                                                       statetype.getSelectedItem().toString(),
                                                       pdir.getSelectedItem().toString(),
                                                       rtos.getSelectedItem().toString(),
                                                       ingressmark.getSelectedItem().toString());
            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);

            destip.setText("");
            srcprt.setText("");
            dstprt.setText("");
            discrip.setText("");
            protocol.setSelectedIndex(0);
            ruletype.setSelectedIndex(0);
            statetype.setSelectedIndex(0);
            pdir.setSelectedIndex(0);
            ruleaction.setSelectedIndex(0);
            rtos.setSelectedIndex(0);
            ingressmark.setSelectedIndex(0);
            if (tcpconf.extint != null) {
              destination.setSelectedItem(tcpconf.extint);
            } else {
              destination.setSelectedIndex(0);
            }
          } else {
            EditRule.Protocol=protocol.getSelectedItem().toString();
            EditRule.Pdir=pdir.getSelectedItem().toString();
            EditRule.Type=ruletype.getSelectedItem().toString();
            EditRule.State=statetype.getSelectedItem().toString();
            EditRule.Action=ruleaction.getSelectedItem().toString();
            EditRule.TOS=rtos.getSelectedItem().toString();
            EditRule.Destination=intname;
            EditRule.Priority=ingressmark.getSelectedItem().toString();

            EditRule.Description=discrip.getText();
            EditRule.DestPort=dstprt.getText();
            EditRule.SourcePort=srcprt.getText();
            EditRule.IPAddress=destip.getText();

            EditRule.updaterule();
            treeModel.reload(node);
            intwindow.scrollPathToVisible(new TreePath(node.getPath()));
          }
        }
      } else if (event.getActionCommand() == "Edit User") {
        sndata.IPAddress=address.getText();
        sndata.IPSubnet=snmask.getText();
        if ((! node.isNodeAncestor(modemrules)) && (! node.isNodeAncestor(grenode))) {
          sndata.Ingress=ingressbw.getText();        
          sndata.Egress=egress.getText();        
        } else if (node.isNodeAncestor(grenode)) {
          sndata.Route=staticr.isSelected();
        }
        treeModel.reload(node.getParent());
        intwindow.setSelectionPath(new TreePath(node.getPath()));
      } else if (event.getActionCommand() == "List Selected") {
        FwProto prnode;
        DefaultMutableTreeNode inode;

        inode=(DefaultMutableTreeNode)plist.getSelectedItem();
        if (inode != null) {
          prnode=(FwProto)inode.getUserObject();
          protocol.setSelectedItem(prnode.Protocol);
          pdir.setSelectedItem(prnode.Pdir);
          ruletype.setSelectedItem(prnode.Type);
          statetype.setSelectedItem(prnode.State);
          ruleaction.setSelectedItem(prnode.Action);
          dstprt.setText(prnode.DestPort);
          srcprt.setText(prnode.SourcePort);
          plist.setSelectedIndex(0);
        }
      }
    }
  }


/*
  class ProtoEdit extends Container implements ActionListener{
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)intwindow.getLastSelectedPathComponent();

    JComboBox protocol,statetype,ruletype,ruleaction,pdir;
    JTextField srcprt=new JTextField("",10);
    JTextField dstprt=new JTextField("",10);
    JTextField discrip=new JTextField("",10);

    ManageNode sortpanel;
    FwUser sndata;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    FwProto EditProto;

    public ProtoEdit(boolean edit){
      isEdit=edit;

      String newline = System.getProperty("line.separator");

      String rtype[]={"NAT","Forward","Proxy","Local"};
      String rproto[]={"TCP","UDP","GRE","ESP","AH","OSPF","ALL"};
      String raction[]={"Accept","Deny And Log","Deny"};
      String rstate[]={"New","Related","Any"};
      String rdir[]={"Out","In"};

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.weightx=1;


      layout.anchor=GridBagConstraints.NORTH;

      if (! isEdit) {
        layout.weighty=1;
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(protoconf,treeModel,"Select Protocol To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        textlabel=new JLabel("Enter Properties Of New Protocol");
        layout.gridwidth=GridBagConstraints.REMAINDER;
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        textlabel=new JLabel("Editing Protocol");
        layout.gridwidth=GridBagConstraints.REMAINDER;
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }
       
     
      if (isEdit) {
        EditProto=(FwProto)node.getUserObject();
      }


      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=1;


      JLabel plabel=new JLabel("Protocol");
      gridbag.setConstraints(plabel,layout);
      add(plabel);

      JLabel pdlabel=new JLabel("Direction");
      gridbag.setConstraints(pdlabel,layout);
      add(pdlabel);

      JLabel tlabel=new JLabel("Type");
      gridbag.setConstraints(tlabel,layout);
      add(tlabel);

      JLabel slabel=new JLabel("State");
      gridbag.setConstraints(slabel,layout);
      add(slabel);
      
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel alabel=new JLabel("Action");
      gridbag.setConstraints(alabel,layout);
      add(alabel);

      layout.gridwidth=1;
      
      protocol=new JComboBox(rproto);
      gridbag.setConstraints(protocol,layout);
      add(protocol);

      pdir=new JComboBox(rdir);
      gridbag.setConstraints(pdir,layout);
      add(pdir);

      ruletype=new JComboBox(rtype);
      gridbag.setConstraints(ruletype,layout);
      add(ruletype);

      statetype=new JComboBox(rstate);
      gridbag.setConstraints(statetype,layout);
      add(statetype);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      ruleaction=new JComboBox(raction);
      gridbag.setConstraints(ruleaction,layout);
      add(ruleaction);

      addLabel(new JLabel("Source Port"),srcprt,gridbag,layout);
      addLabel(new JLabel("Destination Port"),dstprt,gridbag,layout);
      addLabel(new JLabel("Description"),discrip,gridbag,layout);

      if (! isEdit) {
        layout.weightx=0;
      } else {
        protocol.setSelectedItem(EditProto.Protocol);
        pdir.setSelectedItem(EditProto.Pdir);
        ruletype.setSelectedItem(EditProto.Type);
        statetype.setSelectedItem(EditProto.State);
        ruleaction.setSelectedItem(EditProto.Action);
        dstprt.setText(EditProto.DestPort);
        srcprt.setText(EditProto.SourcePort);
        discrip.setText(EditProto.Description);
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;

      if (isEdit) {
        adduser=new JButton("Save Protocol");
      } else {
        adduser=new JButton("Save New Protocol");
      }

      adduser.setActionCommand("Add New Rule");
      adduser.addActionListener(this);

      gridbag.setConstraints(adduser,layout);
      add(adduser);

      }
      private void addLabel(JLabel label,JTextField textfield,GridBagLayout gridbag,GridBagConstraints layout){
        layout.gridwidth=2;
        layout.weightx=1;
        layout.fill=GridBagConstraints.NONE;
        gridbag.setConstraints(label,layout);
        add(label);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.weightx=1;
        layout.fill=GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(textfield,layout);
        add(textfield);
    }

    public void actionPerformed(ActionEvent event) {
      IntDef destint;
      DefaultMutableTreeNode iface;
      String intname="-";

      if (event.getActionCommand() == "Add New Rule") {
        if (discrip.getText().length() > 0) {
          if (! isEdit) {
            DefaultMutableTreeNode childnode=addFwProto(protocol.getSelectedItem().toString(),
                                                       ruletype.getSelectedItem().toString(),
                                                       ruleaction.getSelectedItem().toString(),
                                                       srcprt.getText(),
                                                       dstprt.getText(),
                                                       discrip.getText(),
                                                       statetype.getSelectedItem().toString(),
                                                       pdir.getSelectedItem().toString());                                      

            intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);

            srcprt.setText("");
            dstprt.setText("");
            discrip.setText("");
            protocol.setSelectedIndex(0);
            ruletype.setSelectedIndex(0);
            statetype.setSelectedIndex(0);
            pdir.setSelectedIndex(0);
            ruleaction.setSelectedIndex(0);
          } else {
            EditProto.Protocol=protocol.getSelectedItem().toString();
            EditProto.Pdir=pdir.getSelectedItem().toString();
            EditProto.Type=ruletype.getSelectedItem().toString();
            EditProto.State=statetype.getSelectedItem().toString();
            EditProto.Action=ruleaction.getSelectedItem().toString();
            EditProto.DestPort=dstprt.getText();
            EditProto.SourcePort=srcprt.getText();
            EditProto.Description=discrip.getText();
            EditProto.updaterule();
            treeModel.reload(node);
            intwindow.scrollPathToVisible(new TreePath(node.getPath()));
          }
        }
      }
    }
  }

  public DefaultMutableTreeNode addFwProto(String protocol,String ruletype,String ruleaction,
                                          String srcprt,String dstprt,String discrip,String rstate,String pdirec){

      DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FwProto(protocol,ruletype,ruleaction,
                                                                             srcprt,dstprt,discrip,rstate,pdirec));
      treeModel.insertNodeInto(childnode,protoconf,protoconf.getChildCount());
      return childnode;
  }
*/
  public DefaultMutableTreeNode addFwRule(DefaultMutableTreeNode node,String protocol,String ruletype,String ruleaction,String destint,
                                          String destip,String srcprt,String dstprt,String discrip,String rstate,String pdirec,
                                          String tosval,String prio){

      DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FwRule(protocol,ruletype,ruleaction,destint,destip,
                                                                             srcprt,dstprt,discrip,rstate,pdirec,tosval,prio));
      treeModel.insertNodeInto(childnode,node,node.getChildCount());
      return childnode;
  }

  public String getConfig(){
    DefaultMutableTreeNode intdata,tdata,trdata;
    String newline = System.getProperty("line.separator");
    String Alias="";
    IntDef intset;
    WanDef wanset;
    GenralRoute genrouted;
    GreDef gretun;
    EspDef esptun;
    VoipReg sipreg,iaxreg;
    FwProto protorule;
    Output="";
    ExtraAdslLink adsldata;
    TOSConfig lbdata;
    FwUser gresrc;


    if (systype.equals("full")) {
      if ((servalid) && (! systype.equals("full"))) {
        intset=(IntDef)tcpconf.intint.getUserObject();
        if (intset.Description.length() == 0) {
          intset.Description=intset.IntName;
        }
        if ((intset.IPStart.length() > 0) && (intset.IPEnd.length() >0 )) {
          Output=AddConfL("IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+
                          intset.IntName+" "+intset.IPStart+" "+intset.IPEnd+" "+intset.Ingress+" "+intset.Egress+" "+
                          intset.MAC+" "+intset.IPGateway);
        } else {
          Output=AddConfL("IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+intset.IntName+" - - "+
                          intset.Ingress+" "+intset.Egress+" "+intset.MAC+" "+intset.IPGateway);
        }
      } else {
        for (Enumeration e = intNode.children() ; e.hasMoreElements() ;) {
          intdata=(DefaultMutableTreeNode)e.nextElement();
          intset=(IntDef)intdata.getUserObject();
          if (intset.Description.length() == 0) {
            intset.Description=intset.IntName;
          }
      
          if (intset.IntName.indexOf(":") < 0) {
            if ((intset.IPStart.length() > 0) && (intset.IPEnd.length() >0 )) {
              Output=AddConfL("IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+
                              intset.IntName+" "+intset.IPStart+" "+intset.IPEnd+" "+intset.Ingress+" "+intset.Egress+" "+intset.MAC+" "+intset.IPGateway);
            } else {
              Output=AddConfL("IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+intset.IntName+" - - "+
                              intset.Ingress+" "+intset.Egress+" "+intset.MAC+" "+intset.IPGateway);
            }
          } else {
            if ((intset.IPStart.length() > 0) && (intset.IPEnd.length() >0 )) {
              Alias=Alias+"IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+
                    intset.IntName+" "+intset.IPStart+" "+intset.IPEnd+" "+intset.Ingress+" "+intset.Egress+" "+intset.MAC+" "+intset.IPGateway+newline;
            } else {
              Alias=Alias+"IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+
                    intset.IntName+" - - "+intset.Ingress+" "+intset.Egress+" "+intset.MAC+" "+intset.IPGateway+newline;
            }
          }
        }
      }
      if (Alias.length() > 0) {
        Output=Output+Alias;
      }

      for (Enumeration e = wanNode.children() ; e.hasMoreElements() ;) {
        intdata=(DefaultMutableTreeNode)e.nextElement();
        wanset=(WanDef)intdata.getUserObject();
 
        if ((wanset.IPStart.length() > 0) && (wanset.IPEnd.length() >0 )) {
          Output=AddConfL("IP Route "+wanset.Description.replaceAll(" ","_")+" "+wanset.IPAddress+" "+wanset.IPSubnet+" "+wanset.LGateway+" "+
                          wanset.RGateway+" "+wanset.IPStart+" "+wanset.IPEnd);
        } else {
          Output=AddConfL("IP Route "+wanset.Description.replaceAll(" ","_")+" "+wanset.IPAddress+" "+wanset.IPSubnet+" "+wanset.LGateway+" "+
                          wanset.RGateway);
        }
      }

      for (Enumeration e = genroute.children() ; e.hasMoreElements() ;) {
        intdata=(DefaultMutableTreeNode)e.nextElement();
        genrouted=(GenralRoute)intdata.getUserObject();
        Output=AddConfL("IP GenRoute "+genrouted.Description.replaceAll(" ","_")+" "+genrouted.IPAddress+" "+
                        genrouted.IPSubnet+" "+genrouted.Gateway);
      }

      WiFiConfig iwconf;
      for (Enumeration e = iwconfig.children() ; e.hasMoreElements() ;) {
        intdata=(DefaultMutableTreeNode)e.nextElement();
        iwconf=(WiFiConfig)intdata.getUserObject();
        Output=AddConfL(iwconf.confout());
      }

/*
      for (Enumeration e = protoconf.children() ; e.hasMoreElements() ;) {
        intdata=(DefaultMutableTreeNode)e.nextElement();
        protorule=(FwProto)intdata.getUserObject();
        Output=AddConfL("IP Proto "+protorule.confOut());
      }
*/
    } else {
      for (Enumeration e = lnetwork.children() ; e.hasMoreElements() ;) {
        intdata=(DefaultMutableTreeNode)e.nextElement();
        intset=(IntDef)intdata.getUserObject();
        if (intset.Description.length() == 0) {
          intset.Description=intset.IntName;
        }
        if ((intset.IPStart.length() > 0) && (intset.IPEnd.length() >0 )) {
          Output=AddConfL("IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+
                          intset.IntName+" "+intset.IPStart+" "+intset.IPEnd);
        } else {
          Output=AddConfL("IP Interface "+intset.Description.replaceAll(" ","_")+" "+intset.IPAddress+" "+intset.IPSubnet+" "+intset.IntName);
        }
      }
    }

    if (systype.equals("full")) {
      Output=AddConfL("IP SysConf Nexthop "+tcpconf.nexthop);
      Output=AddConfL("IP SysConf NattedIP "+tcpconf.external);

      if (tcpconf.vpnrange.length() > 0) {
        Output=AddConfL("IP SysConf VPNNet "+tcpconf.vpnrange);
      }

      if (tcpconf.ovpnrange.length() > 0) {
        Output=AddConfL("IP SysConf OVPNNet "+tcpconf.ovpnrange);
      }

      if (tcpconf.l2tprange.length() > 0) {
        Output=AddConfL("IP SysConf L2TPNet "+tcpconf.l2tprange);
      }

      if (tcpconf.intint != null) {
        intset=(IntDef)tcpconf.intint.getUserObject();
        Output=AddConfL("IP SysConf Internal "+intset.IntName);
      }

      if (tcpconf.extint != null) {
        if (tcpconf.extint.toString() !="Modem") {
          intset=(IntDef)tcpconf.extint.getUserObject();
          Output=AddConfL("IP SysConf External "+intset.IntName);
        } else {
          Output=AddConfL("IP SysConf External Dialup");
        }
      }

      if (tcpconf.vpnint != null) {
        if (tcpconf.vpnint.toString() !="Modem") {
          intset=(IntDef)tcpconf.vpnint.getUserObject();
          Output=AddConfL("IP SysConf OVPNInt "+intset.IntName);
        } else {
          Output=AddConfL("IP SysConf OVPNInt Dialup");
        }
      }

      for (Enumeration e = grenode.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        gretun=(GreDef)tdata.getUserObject();
        Output=AddConfL("IP GRE Tunnel "+gretun.LocalIP+" "+gretun.RemoteIP+" "+gretun.LocalINT+" "+gretun.Ipsec+" "+gretun.MTU+" "+gretun.CRLURL);
      }

      for (Enumeration e = espnode.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        esptun=(EspDef)tdata.getUserObject();
        Output=AddConfL("IP ESP Tunnel "+esptun.getConf());
      }

      for (Enumeration e = espaccnode.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        esptun=(EspDef)tdata.getUserObject();
        Output=AddConfL("IP ESP Access "+esptun.getConf());
      }

      if (voipdefconf.getconfig().length() > 0) {
        Output=AddConfL(voipdefconf.getconfig());
      }

      for (Enumeration e = voipiax.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        iaxreg=(VoipReg)tdata.getUserObject();
        Output=AddConfL(iaxreg.getConf());
      }

      for (Enumeration e = voipsip.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        sipreg=(VoipReg)tdata.getUserObject();
        Output=AddConfL(sipreg.getConf());
      }

      if (tcpconf.pdns.length() > 0) {
        Output=AddConfL("IP SysConf PrimaryDns "+tcpconf.pdns);
      }
      if (tcpconf.sdns.length() > 0) {
        Output=AddConfL("IP SysConf SecondaryDns "+tcpconf.sdns);
      }

      if (tcpconf.pwins.length() > 0) {
        Output=AddConfL("IP SysConf PrimaryWins "+tcpconf.pwins);
      }
      if (tcpconf.swins.length() > 0) {
        Output=AddConfL("IP SysConf SecondaryWins "+tcpconf.swins);
      }
      if (tcpconf.lease.length() > 0) {
        Output=AddConfL("IP SysConf DHCPLease "+tcpconf.lease);
      }

      if (tcpconf.maxlease.length() > 0) {
        Output=AddConfL("IP SysConf DHCPMaxLease "+tcpconf.maxlease);
      }

      if (tcpconf.ntpserver.length() > 0) {
        Output=AddConfL("IP SysConf NTPServer "+tcpconf.ntpserver);
      }

/*      if (tcpconf.ANetStart.length() > 0) {
        Output=AddConfL("IP SysConf ATalkNStart "+tcpconf.ANetStart);
      }

      if (tcpconf.ANetFin.length() > 0) {
        Output=AddConfL("IP SysConf ATalkNFin "+tcpconf.ANetFin);
      }

      if (tcpconf.ANetPhase.length() > 0) {
        Output=AddConfL("IP SysConf ATalkPhase "+tcpconf.ANetPhase);
      }
*/
      if (tcpconf.bridgeint.length() > 0) {
        Output=AddConfL("IP SysConf Bridge "+tcpconf.bridgeint);
      }

      if (tcpconf.ldapserver.length() > 0) {
        Output=AddConfL("IP LDAP Server "+tcpconf.ldapserver);
      }

      if (tcpconf.ldaplogin.length() > 0) {
        Output=AddConfL("IP LDAP Login "+tcpconf.ldaplogin);
      }

      if (tcpconf.ingress.length() > 0) {
        Output=AddConfL("IP TC Ingress "+tcpconf.ingress);
      }
      if (tcpconf.egress.length() > 0) {
        Output=AddConfL("IP TC Egress "+tcpconf.egress);
      }

      if (caconf.Country.length() > 0) {
        Output=AddConfL("X509 Config Country "+caconf.Country);
      }

      if (caconf.State.length() > 0) {
        Output=AddConfL("X509 Config State "+caconf.State);
      }

      if (caconf.City.length() > 0) {
        Output=AddConfL("X509 Config City "+caconf.City);
      }

      if (caconf.Company.length() > 0) {
        Output=AddConfL("X509 Config Company "+caconf.Company);
      }

      if (caconf.Division.length() > 0) {
        Output=AddConfL("X509 Config Division "+caconf.Division);
      }

      if (caconf.Name.length() > 0) {
        Output=AddConfL("X509 Config Name "+caconf.Name);
      }

      if (caconf.Email.length() > 0) {
        Output=AddConfL("X509 Config Email "+caconf.Email);
      }

      Output=AddConfL("X509 Config Locked "+caconf.Changeable);
  
    } else {
      if (tcpconf.hname.length() > 0) {
        Output=AddConfL("DNS Hostname "+tcpconf.hname);
      }
      if (tcpconf.dname.length() > 0) {
        Output=AddConfL("DNS Domain "+tcpconf.dname);
      }

      if (tcpconf.ldaplogin.length() > 0) {
        Output=AddConfL("IP LDAP Login "+tcpconf.ldaplogin);
      }

      if (tcpconf.skey.length() > 0) {
        Output=AddConfL("Serial "+tcpconf.skey);
      }
      Output=AddConfL("IP SysConf Internal eth0");
      Output=AddConfL("IP SysConf External Dialup");
    }


    if (modemconf.ComPort.length() > 0) {
      Output=AddConfL("IP Modem ComPort "+modemconf.ComPort);
    }

    if (modemconf.Speed.length() > 0) {
      Output=AddConfL("IP Modem Speed "+modemconf.Speed);
    }

    if (modemconf.FlowControl.length() > 0) {
      if (modemconf.FlowControl == "Hardware (RTS/CTS)") {
        Output=AddConfL("IP Modem FlowControl crtscts");
      } else if (modemconf.FlowControl == "Hardware (DTR/CTS)") {
        Output=AddConfL("IP Modem FlowControl cdtrcts");
      } else if (modemconf.FlowControl == "Software") {
        Output=AddConfL("IP Modem FlowControl xonxoff");
      }
    }

    if (modemconf.FlowControl.length() > 0) {
      Output=AddConfL("IP Modem Connection "+modemconf.ConnType);
    }

    if (modemconf.InitString1.length() > 0) {
      Output=AddConfL("IP Modem Init1 "+modemconf.InitString1);
    }

    if (modemconf.InitString2.length() > 0) {
      Output=AddConfL("IP Modem Init2 "+modemconf.InitString2);
    }

    if (modemconf.DialString.length() > 0) {
      Output=AddConfL("IP Modem DialString "+modemconf.DialString);
    }

    if (modemconf.Number.length() > 0) {
      Output=AddConfL("IP Modem Number "+modemconf.Number);
    }

    if (modemconf.UserName.length() > 0) {
      Output=AddConfL("IP Modem Username "+modemconf.UserName);
    }

    if (modemconf.Password.length() > 0) {
      Output=AddConfL("IP Modem Password "+modemconf.Password);
    }

    if (modemconf.MTU.length() > 0) {
      Output=AddConfL("IP Modem MTU "+modemconf.MTU);
    } else {
      Output=AddConfL("IP Modem MTU 1500");
    }

    if (modemconf.LocalIP.length() > 0) {
      Output=AddConfL("IP Modem Address "+modemconf.LocalIP);
    }

    if (modemconf.DestIP.length() > 0) {
      Output=AddConfL("IP Modem Gateway "+modemconf.DestIP);
    }

    if (modemconf.IdleTimeout.length() > 0) {
      Output=AddConfL("IP Modem IdleTimeout "+modemconf.IdleTimeout);
    }

    if (modemconf.HoldoffTime.length() > 0) {
      Output=AddConfL("IP Modem Holdoff "+modemconf.HoldoffTime);
    }

    if (modemconf.LinkTest.length() > 0) {
      Output=AddConfL("IP Modem LinkTest "+modemconf.LinkTest);
    }

    if (modemconf.MaxFail.length() > 0) {
      Output=AddConfL("IP Modem Maxfail "+modemconf.MaxFail);
    }

    if (modemconf.ConnectDelay.length() > 0) {
      Output=AddConfL("IP Modem ConnectDelay "+modemconf.ConnectDelay);
    }

    if (modemconf.NoCarrier) {
      Output=AddConfL("IP Modem NoCarrier");
    }

    if (modemconf.NoDialtone) {
      Output=AddConfL("IP Modem NoDialtone");
    }

    if (modemconf.Deflate) {
      Output=AddConfL("IP Modem Deflate");
    }

    if (modemconf.BSD) {
      Output=AddConfL("IP Modem BSD");
    }

    if (modemconf.Error) {
      Output=AddConfL("IP Modem Error");
    }

    if (modemconf.Busy) {
      Output=AddConfL("IP Modem Busy");
    }

    if (systype.equals("full")) {
      for (Enumeration e = intNode.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        intset=(IntDef)tdata.getUserObject();
        if (intset.Description.length() == 0) {
          intset.Description=intset.IntName;
        }
        getFwRuleConf(tdata,intset.IntName);
      }
      getFwRuleConf(modemrules,"Modem");

      for (Enumeration e = grenode.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        gretun=(GreDef)tdata.getUserObject();
        getFwRuleConf(tdata,gretun.LocalIP);
      }

      for (Enumeration e = grenode.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        gretun=(GreDef)tdata.getUserObject();
        for (Enumeration e1 = tdata.children() ; e1.hasMoreElements() ;) {
          trdata=(DefaultMutableTreeNode)e1.nextElement();
          gresrc=(FwUser)trdata.getUserObject();
          if (gresrc.Route) {
            Output=AddConfL("IP GRE Route "+gretun.LocalIP+" "+gresrc.IPAddress+" "+gresrc.IPSubnet);
          }
        }
      }

      AdslAccount adsluser;
      for (Enumeration e = adslacc.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        adsluser=(AdslAccount)tdata.getUserObject();
        Output=AddConfL("IP ADSL_USER "+adsluser.Username+" "+adsluser.Password+" "+adsluser.Status+" "+
                        adsluser.Type);
      }

      Output=Output+faxconf.toString();

      for (Enumeration e = adsllink.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        adsldata=(ExtraAdslLink)tdata.getUserObject();
        Output=AddConfL("IP ADSL "+adsldata.Description.replaceAll(" ","_")+" "+adsldata.User+" "+adsldata.Pass+" "+
                        adsldata.Ingress+" "+adsldata.Egress+" "+adsldata.TOS+" "+adsldata.Port+" "+adsldata.Service.replaceAll(" ","_")+" "+adsldata.VIP+" "+adsldata.RIP);
      }
      for (Enumeration e = lbnode.children() ; e.hasMoreElements() ;) {
        tdata=(DefaultMutableTreeNode)e.nextElement();
        lbdata=(TOSConfig)tdata.getUserObject();
        Output=AddConfL("IP TOS "+lbdata.Description.replaceAll(" ","_")+" "+lbdata.Address+" "+lbdata.Dest+" "+
                        lbdata.Src+" "+lbdata.Protocol+" "+lbdata.TOS+" "+lbdata.Ingress);
      }
    }

    return Output;
  }

  public void getFwRuleConf(DefaultMutableTreeNode rulenode,String intname) {
    DefaultMutableTreeNode intdata,tdata,trdata;
    FwUser snet;
    FwRule rule;

    for (Enumeration e1 = rulenode.children() ; e1.hasMoreElements() ;) {
      trdata=(DefaultMutableTreeNode)e1.nextElement();
      snet=(FwUser)trdata.getUserObject();
      Output=AddConfL("IP FW SourceNetwork "+snet.UserName.replaceAll(" ","_")+" "+snet.IPAddress+" "+snet.IPSubnet+" "+intname+" "+snet.Ingress+" "+snet.Egress);
      for (Enumeration e2 = trdata.children() ; e2.hasMoreElements() ;) {
        intdata=(DefaultMutableTreeNode)e2.nextElement();
        rule=(FwRule)intdata.getUserObject();
        if (rule.SourcePort.length() == 0) {
          rule.SourcePort="0:65535";
        }
        if (rule.DestPort.length() == 0) {
          rule.DestPort="0:65535";
        }
        Output=AddConfL("IP FW Rule "+rule.Description.replaceAll(" ","_")+" "+rule.IPAddress+" "+rule.SourcePort+" "+
                        rule.DestPort+" "+rule.Protocol+" "+rule.Type+" "+
                        rule.Action.replaceAll(" ","_")+" "+snet.UserName.replaceAll(" ","_")+" "+
                        rule.Destination+" "+intname+" "+rule.State+" "+rule.Pdir+" "+rule.TOS+" "+rule.Priority);
      }
    }
  }

  public String AddConfL(String newconf){
    if (newconf != null) {
      String newline = System.getProperty("line.separator");
      String confout=Output+newconf+newline;
      return confout;
    } else {
      return Output;
    }
  }
  public void delConfig() {
    tcpconf.delConfig();
    modemconf.delConfig();
    faxconf.delConfig();
    intNode.removeAllChildren();
    treeModel.reload(intNode);

    iwconfig.removeAllChildren();
    treeModel.reload(iwconfig);
//    lwireless.removeAllChildren();
//    treeModel.reload(lwireless);

    wanNode.removeAllChildren();
    treeModel.reload(wanNode);
    genroute.removeAllChildren();
    treeModel.reload(genroute);
    grenode.removeAllChildren();
    treeModel.reload(grenode);
    espnode.removeAllChildren();
    treeModel.reload(espnode);
    espaccnode.removeAllChildren();
    treeModel.reload(espaccnode);
    voipsip.removeAllChildren();
    treeModel.reload(voipsip);
    voipiax.removeAllChildren();
    treeModel.reload(voipiax);
    modemrules.removeAllChildren();
    adsllink.removeAllChildren();
    adslacc.removeAllChildren();
    lbnode.removeAllChildren();
    treeModel.reload(modemrules);
    lnetwork.removeAllChildren();
    treeModel.reload(lnetwork);
    caconf.delConfig();
//    protoconf.removeAllChildren();
//    treeModel.reload(protoconf);

  }

  public void addDefaultAdslLB() {
    if (systype.equals("full")) {
      addAdslLB("DNS UDP","0/0","53","1024:65535","UDP","Minimize-Delay","High");
      addAdslLB("DNS TCP","0/0","53","1024:65535","TCP","Minimize-Delay","High");
      addAdslLB("IDENT","0/0","113","1024:65535","TCP","Minimize-Delay","High");
      addAdslLB("NTP","0/0","123","123","UDP","Minimize-Delay","High");
      addAdslLB("SSH","0/0","22","1024:65535","TCP","Minimize-Delay","High");
      addAdslLB("SMTP","0/0","25","1024:65535","TCP","Maximize-Reliability","Low");
      addAdslLB("IMAP","0/0","143","1024:65535","TCP","Maximize-Reliability","Med");
      addAdslLB("POP3","0/0","110","1024:65535","TCP","Maximize-Reliability","Med");
      addAdslLB("IMAP SSL","0/0","993","1024:65535","TCP","Maximize-Reliability","Med");
      addAdslLB("POP3 SSL","0/0","995","1024:65535","TCP","Maximize-Reliability","Med");
      addAdslLB("LDAP","0/0","389","1024:65535","TCP","Maximize-Reliability","High");
      addAdslLB("LDAP SSL","0/0","636","1024:65535","TCP","Maximize-Reliability","High");
      addAdslLB("HTTP","0/0","80","1024:65535","TCP","Maximize-Throughput","High");
      addAdslLB("HTTPS","0/0","443","1024:65535","TCP","Maximize-Throughput","High");
      addAdslLB("HTTPS Management","0/0","666","1024:65535","TCP","Maximize-Throughput","High");
      addAdslLB("FTP","0/0","21","1024:65535","TCP","Maximize-Throughput","Low");
      addAdslLB("FTPS","0/0","989","1024:65535","TCP","Maximize-Throughput","Low");
    }
  }

  public void setDefault() {
    DefaultMutableTreeNode childnode,usernode;

    delConfig();

    childnode=new DefaultMutableTreeNode(new IntDef("Internal","192.168.0.1","24","","","eth0","","","",""));
    treeModel.insertNodeInto(childnode,intNode,intNode.getChildCount());
    treeModel.insertNodeInto(childnode,lnetwork,lnetwork.getChildCount());

    if (systype.equals("full")) {
      tcpconf.pwins="";
      tcpconf.lease="43200";
      tcpconf.maxlease="86400";
      usernode=addSourceNetwork(childnode,"Local Lan","192.168.0.0","24","","");
      addFwRule(usernode,"TCP","Local","Accept","-","-","","22","Allow SSH Access","New","Out","Minimize-Delay","High");
      addDefaultAdslLB();
      intwindow.scrollPathToVisible(new TreePath(childnode.getPath()));
      tcpconf.intint=childnode;
      tcpconf.extint=dod;
      tcpconf.vpnint=dod;
      caconf.Country="ZA";
      caconf.State="Gauteng";
      caconf.City="Johanesburg";
      caconf.Company="Company";
      caconf.Division="IT Security";
      caconf.Name="VPN And SSL Root Certificate"; 
      caconf.Email="root@company.co.za"; 
    }


    modemconf.ComPort="Com1";
    modemconf.Speed="38400";
    modemconf.FlowControl="Hardware (RTS/CTS)";
    modemconf.ConnType="Dialup";
    modemconf.InitString1="AT&F";
    modemconf.InitString2="ATL1M1";
    modemconf.DialString="ATDT";
    modemconf.MTU="1500";
    modemconf.LocalIP="10.0.0.1";
    modemconf.DestIP="10.0.0.2";
    modemconf.IdleTimeout="120";
    modemconf.HoldoffTime="10";
    modemconf.LinkTest="";
    modemconf.MaxFail="5";
    modemconf.NoCarrier=true;
    modemconf.NoDialtone=true;
    modemconf.Busy=true;
    modemconf.Error=true;
    faxconf.defConfig();

    DrawWindow();
  }
  class SecurityWin extends Container implements ActionListener{
    JCheckBox lockcaconf;
    JTextField country,state,city,company,division,name,email;
 
   public SecurityWin() {

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.weightx=1;
      layout.weighty=0;
 
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("CA Configuration");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      country=new JTextField(caconf.Country,10);
      addLabel(new JLabel("Country Code"),country,gridbag,layout);

      state=new JTextField(caconf.State,10);
      addLabel(new JLabel("State"),state,gridbag,layout);

      city=new JTextField(caconf.City,10);
      addLabel(new JLabel("City"),city,gridbag,layout);

      company=new JTextField(caconf.Company,10);
      addLabel(new JLabel("Company"),company,gridbag,layout);

      division=new JTextField(caconf.Division,10);
      addLabel(new JLabel("Division"),division,gridbag,layout);

      name=new JTextField(caconf.Name,10);
      addLabel(new JLabel("Name Apearing On Certificate"),name,gridbag,layout);

      if (caconf.Changeable) {
        layout.weighty=1;
      }

      email=new JTextField(caconf.Email,10);
      addLabel(new JLabel("Email Address"),email,gridbag,layout);

      setEditable(! caconf.Changeable);

      if (! caconf.Changeable) {
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        lockcaconf=new JCheckBox("Lock Config (Only When Unlocked Will A CA Certificate Be Created/Recreated)",caconf.Changeable);
        gridbag.setConstraints(lockcaconf,layout);
        add(lockcaconf);

        layout.weighty=1;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        layout.anchor=GridBagConstraints.NORTH;
        JButton saveemail=new JButton("Save Settings");
        saveemail.setActionCommand("Save CA");
        saveemail.addActionListener(this);
        gridbag.setConstraints(saveemail,layout);
        add(saveemail);
      }

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
      caconf.Country=country.getText();
      caconf.State=state.getText();
      caconf.City=city.getText();
      caconf.Company=company.getText();
      caconf.Division=division.getText();
      caconf.Name=name.getText();
      caconf.Email=email.getText();
      caconf.Changeable=lockcaconf.isSelected();
      setEditable(! caconf.Changeable);
    }
  public void setEditable(boolean Editable) {
    country.setEditable(Editable);
    state.setEditable(Editable);
    city.setEditable(Editable);
    company.setEditable(Editable);
    division.setEditable(Editable);
    name.setEditable(Editable);
    email.setEditable(Editable);
  }
}

class TcpConf {
    String pdns,sdns,pwins,swins,nexthop,external,lease,maxlease,ntpserver,skey,dname,hname;
    String ldapserver,ldaplogin,bridgeint,ingress,egress,vpnrange,ovpnrange,l2tprange;
    DefaultMutableTreeNode intint,extint,vpnint;
    
    public TcpConf() {
        delConfig();
    }
    
    public void delConfig() {
        skey="";
        hname="";
        dname="";
        pdns="";
        sdns="";
        pwins="";
        swins="";
        nexthop="";
        external="";
        lease="43200";
        maxlease="86400";
        ntpserver="";
        intint=null;
        extint=null;
        vpnint=null;
/*
        ANetStart="0";
        ANetFin="65534";
        ANetPhase="2";
*/
        ldapserver="127.0.0.1";
        ldaplogin="uid=admin,ou=users";
        bridgeint="";
        ingress="";
        egress="";
        vpnrange="";
        ovpnrange="";
        l2tprange="";
    }    
}

class FaxConf {
    String ComPort;
    String Rings;
    String Speed;
    String Tagline;
    String Tagnum;
    String Country;
    String Area;
    String LongDist;
    String IntPre;
    String Retry;
    String Pages;
    String FlowCmd;
    String DtrCmd;
    String DcdCmd;
    String TimeOut;    
    String CIDName;
    String CIDNumber;  
    boolean Stream;
    
    boolean CTimeout;
    
    boolean Volume;
    
    boolean ConectWait;
    
    int FlowControl;
    
    int Trigger;
    
    public FaxConf() {
        defConfig();
    }
    
    public void defConfig() {
        ComPort="Disabled";
        Rings="3";
        Speed="38400";
        FlowControl=1;
        Trigger=0;
        Tagline="Netsentry Firewall";
        Tagnum="UNSET";
        Country="27";
        Area="11";
        LongDist="0";
        IntPre="09";
        Retry="3";
        Pages="25";
        FlowCmd="AT&K3";
        DtrCmd="AT&D2";
        DcdCmd="AT&C1";
        Volume=true;
        ConectWait=true;
        Volume=true;
        ConectWait=true;
        Stream=true;
        CTimeout=true;
        TimeOut="60";
        CIDName="NAME=";
        CIDNumber="NMBR=";
    }
    
    public void delConfig() {
        ComPort="";
        Rings="";
        Speed="";
        Tagline="";
        Tagnum="";
        Country="";
        Area="";
        LongDist="";
        IntPre="";
        Retry="";
        Pages="";
        FlowCmd="";
        Volume=false;
        ConectWait=false;
        Stream=false;
        CTimeout=false;
        Trigger=0;
        FlowControl=0;
        TimeOut="";
        CIDName="";
        CIDNumber="";
    }
    
    public String toString() {
        String newline = System.getProperty("line.separator");
        String Output = "";
        
        Output=Output+"IP FAX Port "+ComPort+newline;
        Output=Output+"IP FAX RingDelay "+Rings+newline;
        Output=Output+"IP FAX Speed "+Speed+newline;
        Output=Output+"IP FAX FlowControl "+FlowControl+newline;
        Output=Output+"IP FAX Trigger "+Trigger+newline;
        Output=Output+"IP FAX TagName "+Tagline+newline;
        Output=Output+"IP FAX TagNum "+Tagnum+newline;
        Output=Output+"IP FAX Country "+Country+newline;
        Output=Output+"IP FAX AreaCode "+Area+newline;
        Output=Output+"IP FAX LongDistPrefix "+LongDist+newline;
        Output=Output+"IP FAX InatPrefix "+IntPre+newline;
        Output=Output+"IP FAX Retry "+Retry+newline;
        Output=Output+"IP FAX MaxPages "+Pages+newline;
        Output=Output+"IP FAX FlowCmd "+FlowCmd+newline;
        Output=Output+"IP FAX DTRCmd "+DtrCmd+newline;
        Output=Output+"IP FAX DCDCmd "+DcdCmd+newline;
        Output=Output+"IP FAX TimeOut "+TimeOut+newline;
        
        Output=Output+"IP FAX CIDNumber "+CIDNumber+newline;
        Output=Output+"IP FAX CIDName "+CIDName+newline;
        
        if (Volume) {
            Output=Output+"IP FAX SpkOn"+newline;
        }
        
        if (ConectWait) {
            Output=Output+"IP FAX CONNECT"+newline;
        }
        
        if (Stream) {
            Output=Output+"IP FAX Stream"+newline;
        }
        
        if (CTimeout) {
            Output=Output+"IP FAX CTimeout"+newline;
        }
        
        return Output;
    }
    
    public void setVal(String item, String valin) {
        if (item.equals("Port")) {
            ComPort=valin;
        } else if (item.equals("RingDelay")) {
            Rings=valin;
        } else if (item.equals("Speed")) {
            Speed=valin;
        } else if ((item.equals("FlowControl")) && (valin.equals("0"))) {
            FlowControl=0;
        } else if ((item.equals("FlowControl")) && (valin.equals("1"))) {
            FlowControl=1;
        } else if ((item.equals("Trigger")) && (valin.equals("0"))) {
            Trigger=0;
        } else if ((item.equals("Trigger")) && (valin.equals("1"))) {
            Trigger=1;
        } else if (item.equals("TagName")) {
            Tagline=valin;
        } else if (item.equals("TagNum")) {
            Tagnum=valin;
        } else if (item.equals("Country")) {
            Country=valin;
        } else if (item.equals("AreaCode")) {
            Area=valin;
        } else if (item.equals("LongDistPrefix")) {
            LongDist=valin;
        } else if (item.equals("InatPrefix")) {
            IntPre=valin;
        } else if (item.equals("Retry")) {
            Retry=valin;
        } else if (item.equals("MaxPages")) {
            Pages=valin;
        } else if (item.equals("FlowCmd")) {
            FlowCmd=valin;
        } else if (item.equals("DTRCmd")) {
            DtrCmd=valin;
        } else if (item.equals("DCDCmd")) {
            DcdCmd=valin;
        } else if (item.equals("TimeOut")) {
            TimeOut=valin;
        } else if (item.equals("CIDNumber")) {
            CIDNumber=valin;
        } else if (item.equals("CIDName")) {
            CIDName=valin;
        } else if (item.equals("SpkOn"))  {
            Volume=true;
        } else if (item.equals("CONNECT")) {
            ConectWait=true;
        } else if (item.equals("Stream")) {
            Stream=true;
        } else if (item.equals("CTimeout")) {
            CTimeout=true;
        }
    }  
  }

  public void setSerValid(boolean sertype) {
    DefaultMutableTreeNode inode;
    IntDef iface;
    servalid=sertype;

    if ((servalid) && (! systype.equals("full"))){
      for (Enumeration e = intNode.children() ; e.hasMoreElements() ;) {
          inode=(DefaultMutableTreeNode)e.nextElement();
          iface=(IntDef)inode.getUserObject();
          if ((IntDef)tcpconf.intint.getUserObject() != iface) {
            treeModel.removeNodeFromParent(inode);
          }
      }
    }
  }
}

class voipdefreg {
  String fwduser,fwdpass,iaxuser,iaxpass,iaxnumber,gosuser,gospass,fteluser,ftelpass,vbox,vboxpass,vboxp,vboxip,vboxpre;
  String h323gkid,vboxdtmf;
  Boolean vboxreg,vboxfuser,vboxvideo,vboxsrtp;
  public voipdefreg() {
    delConfig();
  }
  public void delConfig() {
    fwduser="";
    fwdpass="";
    iaxuser="";
    iaxnumber="";
    gosuser="";
    gospass="";
    fteluser="";
    ftelpass="";
    vbox="";
    vboxp="IAX";
    vboxdtmf="auto";
    vboxpass="";
    h323gkid="";
    vboxpre="";
    vboxip="";
    vboxreg=true;
    vboxfuser=true;
    vboxvideo=false;
    vboxsrtp=true;
  }

  public String getconfig() {
    String Output="";
    String newline = System.getProperty("line.separator");
    if (vboxip.length() > 0) {
      Output="IP VOIP VBOX "+vbox+" "+vboxpass+" "+vboxip+" "+vboxp+" "+vboxpre+" "+h323gkid+" "+vboxreg.toString()+" "+vboxdtmf+" "+vboxfuser+" "+vboxvideo+" "+vboxsrtp;
    }
/*
    if ((fwduser.length() > 0) && (fwdpass.length() > 0)) {
      Output=Output+newline+"IP VOIP FWD "+fwduser+" "+fwdpass;
    }
    if ((gosuser.length() > 0) && (gospass.length() > 0)) {
      Output=Output+newline+"IP VOIP GOSSIP "+gosuser+" "+gospass;
    }
    if ((fteluser.length() > 0) && (ftelpass.length() > 0)) {
      Output=Output+newline+"IP VOIP FRESHTEL "+fteluser+" "+ftelpass;
    }
    if ((iaxuser.length() > 0) && (iaxpass.length() > 0)) {
      Output=Output+newline+"IP VOIP IAXTEL "+iaxuser+" "+iaxpass+" "+iaxnumber;  
    }
*/
    return Output;
  }
}

class ModemConf {
    String ComPort;
    String InitString1;
    String InitString2;
    String DialString;
    
    String Speed;
    
    String Number;
    
    String ConnType;
    String UserName;    
    String Password;
    
    String DestIP;
    
    String LocalIP;
    
    String FlowControl;
    
    
    String IdleTimeout;
    
    String HoldoffTime;
    String LinkTest;
    
    String MaxFail;
    
    String ConnectDelay;
    
    String MTU;
    
    boolean NoCarrier;
    
    boolean Busy;
    
    boolean NoDialtone;
    
    boolean Error;
    
    boolean BSD;
    
    boolean Deflate;
    
    public ModemConf() {
        delConfig();
    }
    
    public void delConfig() {
        ComPort="";
        InitString1="";
        InitString2="";
        DialString="";
        Speed="";
        Number="";
        UserName="";
        Password="";
        DestIP="";
        LocalIP="";
        FlowControl="";
        ConnType="";
        IdleTimeout="";
        HoldoffTime="";
        LinkTest="";
        MaxFail="";
        NoCarrier=false;
        Busy=false;
        NoDialtone=false;
        Error=false;
        BSD=false;
        Deflate=false;
        ConnectDelay="";
        MTU="";
    }
}



class CaConf {
  String Country,State,City,Company,Division,Name,Email;
  boolean Changeable;
  public CaConf(){
    delConfig();
  }
  public void delConfig(){
    Country="ZA";
    State="Gauteng";
    City="Johanesburg";
    Company="Company";
    Division="IT Security";
    Name="VPN And SSL Root Certificate";
    Email="root@company.co.za";
    Changeable=false;
  }
}

class AdslAccount {
  String Username,Password;
  int Status,Type;
//  String usertypelist[]={"Unknown","2Gb","3Gb","4Gb","6Gb","9Gb"};
  String userstatuslist[]={"Unused","Active","Capped","Capped/Active"};
  public AdslAccount(String uname,String pw,int status){
    Username=uname;
    Password=pw;
    Status=status;
//    Type=type;
  }
  public AdslAccount(){
    Username="";
    Password="";
    Status=0;
//    Type=0;
  }
  public String toString(){
//      return Username+" ("+usertypelist[Type]+" "+userstatuslist[Status]+")";
      return Username+" ("+userstatuslist[Status]+")";
  }
}
