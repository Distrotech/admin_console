import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class RadiusWin extends Container {
  DefaultMutableTreeNode radclient,radrealm,radlink;
  DefaultTreeModel treeModel;
  final JSplitPane mainwindow=new JSplitPane();
  final JTree userswindow;
  String Output="";
  String systype="lite";
  String ssecret="RadSecret";
  String radserver="127.0.0.1";
  String radauport="1645";
  String radacport="1646";
  String wirerange="";
  Boolean wrangenat=true;
  String pppoeint="";
  String ingress="";
  String egress="";

  DefaultMutableTreeNode topbranch = new DefaultMutableTreeNode("Local Configuration");

  public RadiusWin() {
    setLayout(new BorderLayout());

    radclient = new DefaultMutableTreeNode("Radius Clients");
    radrealm = new DefaultMutableTreeNode("Radius Realms");
    radlink = new DefaultMutableTreeNode("Local RAS Links");

    treeModel = new DefaultTreeModel(topbranch);


    topbranch.add(radclient);
    topbranch.add(radrealm);
    topbranch.add(radlink);

    userswindow=new JTree(treeModel);
    userswindow.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    userswindow.setShowsRootHandles(true);

    mainwindow.setLeftComponent(new JScrollPane(userswindow));    
    mainwindow.setBottomComponent(null);
    mainwindow.setDividerLocation(0.3);

    add(mainwindow);
    userswindow.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DrawWindow();
      }
    });
  }

  public void DrawWindow() {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    if (node == null) {
      userswindow.setSelectionPath(new TreePath(topbranch.getPath()));
      node=topbranch;
    }
    Object nodeInfo = node.getUserObject();
    if (node == radclient){
      mainwindow.setBottomComponent(new AddClient());
    } else if (node == radrealm){
      mainwindow.setBottomComponent(new AddRealm(false));
    } else if (node == radlink){
      mainwindow.setBottomComponent(new AddLink(false));
    } else if (node.getParent() == radclient) {
      mainwindow.setBottomComponent(new EditRadClient());
    } else if (node.getParent() == radrealm){
      mainwindow.setBottomComponent(new AddRealm(true));
    } else if (node.getParent() == radlink){
      mainwindow.setBottomComponent(new AddLink(true));
    } else {
      mainwindow.setBottomComponent(new RadSecret());
    }
    mainwindow.setDividerLocation(0.3);
  }

  class RadSecret extends Container implements ActionListener {
    JTextField server,authport,accport,wrange,wint,ingresstf,egresstf;
    JCheckBox wirerangenat;
    JPasswordField secret1,secret2;

    public RadSecret(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Local Radius Configuration");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      server=new JTextField(radserver,10);
      addLabel(new JLabel("Radius Server"),server,gridbag,layout);

      authport=new JTextField(radauport,10);
      addLabel(new JLabel("Radius Server Auth Port"),authport,gridbag,layout);

      accport=new JTextField(radacport,10);
      addLabel(new JLabel("Radius Server Accounting Port"),accport,gridbag,layout);

      secret1=new JPasswordField(ssecret,10);
      addLabel(new JLabel("Shared Secret"),secret1,gridbag,layout);

      secret2=new JPasswordField(ssecret,10);
      addLabel(new JLabel("Confirm Secret"),secret2,gridbag,layout);

      wrange=new JTextField(wirerange,10);
      addLabel(new JLabel("Subnet Of Addressess For PPPoE"),wrange,gridbag,layout);

/*
      hrange=new JTextField(hspotrange,10);
      addLabel(new JLabel("Subnet Of Addressess For Hotspot"),hrange,gridbag,layout);
*/

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      wirerangenat=new JCheckBox("Treat This Range As Internal (Use Nat)",wrangenat);
      gridbag.setConstraints(wirerangenat,layout);
      add(wirerangenat);

      wint=new JTextField(pppoeint,10);
      addLabel(new JLabel("Interface To Listen For PPPoE"),wint,gridbag,layout);

/*
      hint=new JTextField(hspotint,10);
      addLabel(new JLabel("Interface To Listen For Hotspot"),hint,gridbag,layout);
*/

      ingresstf=new JTextField(ingress,10);
      addLabel(new JLabel("Limit For Incomeing PPPoE Traffic"),ingresstf,gridbag,layout);

      egresstf=new JTextField(egress,10);
      addLabel(new JLabel("Limit For Outgoing PPPoETraffic"),egresstf,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Save Settings");
      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Save Secret");
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
      if ((secret1.getPassword().length > 0) & (Arrays.equals(secret1.getPassword(),secret2.getPassword()))) { 
        ssecret=secret1.getPassword().toString();
      } else {
        secret1.setText(ssecret);
        secret2.setText(ssecret);
      }
      if (server.getText().length() > 0){ 
        radserver=server.getText();
      }
      if (authport.getText().length() > 0){ 
        radauport=authport.getText();
      }
      if (accport.getText().length() > 0){ 
        radacport=accport.getText();
      }
      if (wrange.getText().length() > 0){ 
        wirerange=wrange.getText();
      }
/*
      if (hrange.getText().length() > 0){ 
        hspotrange=hrange.getText();
      }
*/
      wrangenat=wirerangenat.isSelected();
      if (wint.getText().length() > 0){ 
        pppoeint=wint.getText();
      }
/*
      if (hint.getText().length() > 0){ 
        hspotint=hint.getText();
      }
*/
      if (ingresstf.getText().length() > 0){ 
        ingress=ingresstf.getText();
      }
      if (egresstf.getText().length() > 0){ 
        egress=egresstf.getText();
      }
    }
  }
  
  
  class AddClient extends Container implements ActionListener {
    JTextField host,gname,alias;
    JPasswordField secret1,secret2;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public AddClient(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Client To Manage");      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Enter Details Of New Client");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      host=new JTextField("",10);
      addLabel(new JLabel("Hostname/IP Address"),host,gridbag,layout);

      secret1=new JPasswordField("",10);
      addLabel(new JLabel("Shared Secret"),secret1,gridbag,layout);

      secret2=new JPasswordField("",10);
      addLabel(new JLabel("Confirm Secret"),secret2,gridbag,layout);

      alias=new JTextField("",10);
      addLabel(new JLabel("Alias/Short Name"),alias,gridbag,layout);      

      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Client");

      adduser.setActionCommand("Add Radius Client");
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
      if ((host.getText().length() > 0) & (secret1.getPassword().length > 0) &
          (alias.getText().length() > 0) & (Arrays.equals(secret1.getPassword(),secret2.getPassword()))) {
        DefaultMutableTreeNode childnode=addRadClient(host.getText(),secret1.getPassword().toString(),alias.getText());

        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);

        host.setText("");
        secret1.setText("");
        secret2.setText("");
        alias.setText(""); 
      } else {
        secret1.setText("");
        secret2.setText("");
      }
    }
  }


  class EditRadClient extends Container implements ActionListener {
    JTextField host,alias;
    JPasswordField secret1,secret2;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    RadClient raddata;

    public EditRadClient(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=0;

      raddata=(RadClient)node.getUserObject();

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Editing Client");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      host=new JTextField(raddata.Host,10);
      addLabel(new JLabel("Hostname/IP Address"),host,gridbag,layout);

      secret1=new JPasswordField(raddata.Secret,10);
      addLabel(new JLabel("Shared Secret"),secret1,gridbag,layout);      

      secret2=new JPasswordField(raddata.Secret,10);
      addLabel(new JLabel("Confirm Secret"),secret2,gridbag,layout);      

      alias=new JTextField(raddata.Alias,10);
      addLabel(new JLabel("Alias/Short Name"),alias,gridbag,layout);      

      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Save Client");

      adduser.setActionCommand("Save Client");
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
      if ((host.getText().length() > 0) & (Arrays.equals(secret1.getPassword(),secret2.getPassword())) &
          (secret1.getPassword().length > 0) & (alias.getText().length() > 0)) {
        raddata.Host=host.getText();
        raddata.Secret=secret1.getPassword().toString();
        raddata.Alias=alias.getText();

        treeModel.reload(node);
        userswindow.setSelectionPath(new TreePath(node.getPath()));
      } else {
        secret1.setText(raddata.Secret);
        secret2.setText(raddata.Secret);
      }
    }
  }

  public DefaultMutableTreeNode addRadClient(String nas,String secret,String alias) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new RadClient(nas,secret,alias));
    treeModel.insertNodeInto(childnode,radclient,radclient.getChildCount());
    return childnode;
  }

  class AddRealm extends Container implements ActionListener {
    JTextField realm,auth,acct;
    JPasswordField secret1,secret2;
    JCheckBox rrobin,strip;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    RadRealm EditRealm;

    public AddRealm(boolean edit){
      isEdit=edit;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select Realm To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Details Of New Realm");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Realm");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      realm=new JTextField("",10);
      addLabel(new JLabel("Realm Name"),realm,gridbag,layout);

      secret1=new JPasswordField("",10);
      addLabel(new JLabel("Shared Secret"),secret1,gridbag,layout);

      secret2=new JPasswordField("",10);
      addLabel(new JLabel("Confirm Secret"),secret2,gridbag,layout);

      auth=new JTextField("",10);
      addLabel(new JLabel("Authentication Host [<HOST>:<PORT>]"),auth,gridbag,layout);

      acct=new JTextField("",10);
      addLabel(new JLabel("Accounting Host [<HOST>:<PORT>]"),acct,gridbag,layout);


      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;

      rrobin=new JCheckBox("Use Round Robin Not Fail Over",false);
      gridbag.setConstraints(rrobin,layout);
      add(rrobin);

      layout.gridwidth=GridBagConstraints.REMAINDER;

      strip=new JCheckBox("Do Not Strip Realm",false);
      gridbag.setConstraints(strip,layout);
      add(strip);

      if (isEdit) {
        EditRealm=(RadRealm)node.getUserObject();
        realm.setText(EditRealm.Realm);
        secret1.setText(EditRealm.Secret);
        secret2.setText(EditRealm.Secret);
        auth.setText(EditRealm.Auth);
        acct.setText(EditRealm.Acct);

        rrobin.setSelected(EditRealm.RoundRobin);
        strip.setSelected(EditRealm.NoStrip);

        adduser=new JButton("Save Realm");
      } else {
        adduser=new JButton("Add Realm");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;

      layout.fill=GridBagConstraints.NONE;
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
      if ((realm.getText().length() > 0) & (auth.getText().length() > 0) & 
          (acct.getText().length() > 0) & (Arrays.equals(secret1.getPassword(),secret2.getPassword()))) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addRadRealm(realm.getText(),secret1.getPassword().toString(),auth.getText(),acct.getText(),
                                                       rrobin.isSelected(),strip.isSelected());
          sortpanel.listdata.addElement(childnode);
          realm.setText("");
          secret1.setText("");
          secret2.setText("");
          auth.setText("");
          acct.setText("");

          rrobin.setSelected(false);
          strip.setSelected(false);

          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        } else {
          EditRealm.Realm=realm.getText();
          EditRealm.Secret=secret1.getPassword().toString();
          EditRealm.Auth=auth.getText();
          EditRealm.Acct=acct.getText();
 
          EditRealm.RoundRobin=rrobin.isSelected();
          EditRealm.NoStrip=strip.isSelected();

          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      } else if (isEdit) {
        secret1.setText(EditRealm.Secret);
        secret2.setText(EditRealm.Secret);
      } else {
        secret1.setText("");
        secret2.setText("");
      }
    }
  }

  class AddLink extends Container implements ActionListener {
    JTextField ttyn,remip,locip,mtu;
    JComboBox ctype,speed,ptype;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    RadLink EditLink;

    public AddLink(boolean edit){
      isEdit=edit;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select RAS Link To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Details Of RAS Link");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing RAS Link");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      ttyn=new JTextField("",10);
      addLabel(new JLabel("Port Name"),ttyn,gridbag,layout);

      remip=new JTextField("",10);
      addLabel(new JLabel("Remote IP Address"),remip,gridbag,layout);

      locip=new JTextField("",10);
      addLabel(new JLabel("Local IP Address"),locip,gridbag,layout);

      mtu=new JTextField("",10);
      addLabel(new JLabel("MTU"),mtu,gridbag,layout);

      layout.gridwidth=1;
      JLabel ctypelabel=new JLabel("Connection Type");
      gridbag.setConstraints(ctypelabel,layout);
      add(ctypelabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String typelist[]={"Dialup","Leased"};
      ctype=new JComboBox(typelist);
      gridbag.setConstraints(ctype,layout);
      add(ctype);
      
      layout.gridwidth=1;
      JLabel speedlabel=new JLabel("Port Speed");
      gridbag.setConstraints(speedlabel,layout);
      add(speedlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String speedlist[]={"9600","19200","38400","57600","115200"};
      speed=new JComboBox(speedlist);
      speed.setSelectedIndex(2);
      gridbag.setConstraints(speed,layout);
      add(speed);

      layout.gridwidth=1;
      JLabel ptypelabel=new JLabel("Port Type");
      gridbag.setConstraints(ptypelabel,layout);
      add(ptypelabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String ptypelist[]={"Async","ISDN","ISDN-V110","ISDN-V120","xDSL"};
      ptype=new JComboBox(ptypelist);
      gridbag.setConstraints(ptype,layout);
      add(ptype);

      if (isEdit) {
        EditLink=(RadLink)node.getUserObject();

        ttyn.setText(EditLink.TTY);
        remip.setText(EditLink.Remote);
        locip.setText(EditLink.Local);
        mtu.setText(EditLink.MTU);
        ctype.setSelectedItem(EditLink.CType);
        speed.setSelectedItem(EditLink.Speed);
        ptype.setSelectedItem(EditLink.PType);
        
        adduser=new JButton("Save RAS Link");
      } else {
        adduser=new JButton("Add RAS Link");
      }

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;

      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Radius Link");
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
      if ((ttyn.getText().length() > 0) & (remip.getText().length() > 0) & 
          (locip.getText().length() > 0) & (mtu.getText().length() > 0)){
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addRadLink(ttyn.getText(),remip.getText(),locip.getText(),
                                                       ctype.getSelectedItem().toString(),speed.getSelectedItem().toString(),
                                                       ptype.getSelectedItem().toString(),mtu.getText());
          sortpanel.listdata.addElement(childnode);
          ttyn.setText("");
          remip.setText("");
          locip.setText("");
          mtu.setText("");

          ctype.setSelectedIndex(0);
          speed.setSelectedIndex(3);
          ptype.setSelectedIndex(0);
          
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        } else {
          EditLink.TTY=ttyn.getText();
          EditLink.Remote=remip.getText();
          EditLink.Local=locip.getText();
          EditLink.MTU=mtu.getText();
          
          EditLink.CType=ctype.getSelectedItem().toString();
          EditLink.PType=ptype.getSelectedItem().toString();
          EditLink.Speed=speed.getSelectedItem().toString();
 
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }
 
  public DefaultMutableTreeNode addRadRealm(String domain,String secret,String auth,String acct,boolean rrobin,boolean strip) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new RadRealm(domain,secret,auth,acct,rrobin,strip));
    treeModel.insertNodeInto(childnode,radrealm,radrealm.getChildCount());
    return childnode;
  }
  
  public DefaultMutableTreeNode addRadLink(String tty,String remip,String locip,
                                           String ctype,String speed,String ptype,String mtu) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new RadLink
                           (tty,remip,locip,ctype,speed,ptype,mtu));
    treeModel.insertNodeInto(childnode,radlink,radlink.getChildCount());
    return childnode;
  }

  
  public Vector getUserList(){
    Vector ulist;

    ulist=new Vector();
    for (Enumeration e = radclient.children() ; e.hasMoreElements() ;) {
        ulist.addElement((DefaultMutableTreeNode)e.nextElement());
    }
    return(ulist);
  }
  public String getConfig() {
    DefaultMutableTreeNode udata;
    String newline = System.getProperty("line.separator");
    String Alias="";
    RadClient rclient;
    RadRealm rrealm;
    RadLink rlink;
    Output="";

    for (Enumeration e = radclient.children() ; e.hasMoreElements() ;) {
      udata=(DefaultMutableTreeNode)e.nextElement();
      rclient=(RadClient)udata.getUserObject();
      Output=AddConfL("Radius Client "+rclient.Host+" "+rclient.Secret+" "+rclient.Alias);
    }

    for (Enumeration e = radrealm.children() ; e.hasMoreElements() ;) {
      udata=(DefaultMutableTreeNode)e.nextElement();
      rrealm=(RadRealm)udata.getUserObject();
      Output=AddConfL("Radius Realm "+rrealm.Realm+" "+rrealm.Auth+" "+rrealm.Acct+" "+rrealm.RoundRobin+" "+
                      rrealm.NoStrip+" "+rrealm.Secret);
    }

    for (Enumeration e = radlink.children() ; e.hasMoreElements() ;) {
      udata=(DefaultMutableTreeNode)e.nextElement();
      rlink=(RadLink)udata.getUserObject();
      Output=AddConfL("Radius RAS "+rlink.TTY+" "+rlink.Remote+" "+rlink.Local+" "+rlink.CType+" "+
                      rlink.Speed+" "+rlink.PType+" "+rlink.MTU);
    }

    Output=AddConfL("Radius Server "+radserver);
    Output=AddConfL("Radius AuthPort "+radauport);
    Output=AddConfL("Radius AccPort "+radacport);
    Output=AddConfL("Radius Secret "+ssecret);
    Output=AddConfL("Radius PPPoE "+wirerange+" "+wrangenat);
    Output=AddConfL("Radius PPPoEIF "+pppoeint);
//    Output=AddConfL("Radius Hotspot "+hspotrange+" "+hspotint);
    Output=AddConfL("Radius Ingress "+ingress);
    Output=AddConfL("Radius Egress "+egress);

    return Output;
  }

  public String AddConfL(String newconf){
    String newline = System.getProperty("line.separator");
    String confout=Output+newconf+newline;
    return confout;
  }

  public void delConfig() {
    radclient.removeAllChildren();
    treeModel.reload(radclient);

    radrealm.removeAllChildren();
    treeModel.reload(radrealm);

    radlink.removeAllChildren();
    treeModel.reload(radlink);
    
    ssecret="RadSecret";
    radserver="127.0.0.1";
    radauport="1645";
    radacport="1646";
    wirerange="";
    wrangenat=true;
    pppoeint="";
    ingress="";
    egress="";
  }

  public void setDefault() {
    delConfig();
    DrawWindow();
  }

}
