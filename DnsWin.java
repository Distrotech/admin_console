import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class DnsWin extends Container {
  DefaultTreeModel treeModel;
  final JSplitPane mainwindow=new JSplitPane();
  final JTree userswindow;
  DnsConf dnsconf=new DnsConf();
  String Output="";
  DefaultMutableTreeNode hosts,hosted,topbranch;

  public DnsWin() {
    setLayout(new BorderLayout());

    topbranch = new DefaultMutableTreeNode("DNS Settings");
    hosts = new DefaultMutableTreeNode(new DnsType("Host Entries (Reverse)","Host"));
    hosted = new DefaultMutableTreeNode(new DnsType("Hosted Domains","Hosted"));

    treeModel = new DefaultTreeModel(topbranch);
    treeModel.insertNodeInto(hosts,topbranch,topbranch.getChildCount());
    treeModel.insertNodeInto(hosted,topbranch,topbranch.getChildCount());

    userswindow=new JTree(treeModel);
    userswindow.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    userswindow.setShowsRootHandles(true);

    mainwindow.setLeftComponent(new JScrollPane(userswindow));    
    userswindow.setSelectionPath(new TreePath(topbranch.getPath()));
    mainwindow.setBottomComponent(new DnsSetup());
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

    int Depth=userswindow.getSelectionPath().getPathCount();

    Object nodeInfo = node.getUserObject();
    if (nodeInfo.toString() == "Host Entries (Reverse)"){
      mainwindow.setBottomComponent(new AddHost(false));
    } else if (node.getParent() == hosts){
      mainwindow.setBottomComponent(new AddHost(true));
    } else if (Depth == 2){
      mainwindow.setBottomComponent(new AddHostedDomain());
    } else if ((Depth == 3) && (! node.getParent().toString().equals("Host Entries (Reverse)"))){
      mainwindow.setBottomComponent(new AddNSRecord(false));
    } else if (node.isNodeAncestor(hosted)){
      mainwindow.setBottomComponent(new AddNSRecord(true));
    } else if (Depth == 1) {
      mainwindow.setBottomComponent(new DnsSetup());
    } else {
      mainwindow.setBottomComponent(null);
    }
    mainwindow.setDividerLocation(0.3);
  }

  class DnsType {
    String Title,Confname;
    public DnsType(String disp,String condis){
      Title=disp;
      Confname=condis;
    }
    public String toString() {
      return Title;
    }
  }

  class AddHost extends Container implements ActionListener{
    ManageNode sortpanel;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    JTextField address,hname,macaddr;
    JLabel textlabel;
    JButton adduser;
    boolean isEdit;
    HostInf EditHost;

    public AddHost(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Host To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Host Details");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Host");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      address=new JTextField("",10);
      addLabel(new JLabel("IP Address"),address,gridbag,layout);

      hname=new JTextField("",10);
      addLabel(new JLabel("Hostname"),hname,gridbag,layout);

      macaddr=new JTextField("",10);
      addLabel(new JLabel("Mac Addr (Bootp)"),macaddr,gridbag,layout);

      if (isEdit) {
        adduser=new JButton("Save Host Details");
        EditHost=(HostInf)node.getUserObject();

        address.setText(EditHost.IPAddress);
        hname.setText(EditHost.HostName);
        macaddr.setText(EditHost.MACAddr);
      } else {
        adduser=new JButton("Add Record");
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
      if ((address.getText().length() > 0) & (hname.getText().length() > 0)){
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addIHost(address.getText(),hname.getText(),macaddr.getText());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          address.setText("");
          hname.setText("");
          macaddr.setText("");
        } else {
          EditHost.IPAddress=address.getText();
          EditHost.HostName=hname.getText();
          EditHost.MACAddr=macaddr.getText();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  public DefaultMutableTreeNode addIHost(String address,String hname,String macaddr) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new HostInf(address,hname,macaddr));
    treeModel.insertNodeInto(childnode,hosts,hosts.getChildCount());
    return childnode;
  }

  class AddRecord extends Container implements ActionListener{
    ManageNode sortpanel;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    JTextField address,value;
    JComboBox rtype;

    public AddRecord(boolean IsPtr,boolean Idns){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();
      String ftypes[]={"A","CNAME","MX","NS"};
      String fitypes[]={"A","CNAME"};
      String rtypes[]={"PTR","NS"};

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Record To Manage");      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Host Details");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      address=new JTextField("",10);
      addLabel(new JLabel("Record Name"),address,gridbag,layout);

      layout.gridwidth=1;
      JLabel rtlabel=new JLabel("Select Record Type");
      gridbag.setConstraints(rtlabel,layout);
      add(rtlabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;

      if (IsPtr) {
        rtype=new JComboBox(rtypes);
      }
      else {
        if (Idns) {
          rtype=new JComboBox(fitypes);
        } else {
          rtype=new JComboBox(ftypes);
        }
      }
      gridbag.setConstraints(rtype,layout);
      add(rtype);

      value=new JTextField("",10);
      addLabel(new JLabel("Entry To Add"),value,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Record");

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
      if (value.getText().length() > 0){
        DefaultMutableTreeNode childnode=addIRecord(node,address.getText(),rtype.getSelectedItem().toString(),
                                                    value.getText());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);
        address.setText("");
        rtype.setSelectedIndex(0);
        value.setText("");
      }
    }
  }

  class AddNSRecord extends Container implements ActionListener{
    ManageNode sortpanel;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    JTextField domain,nsaddress;
    JPasswordField tsigkey1,tsigkey2;
    JCheckBox internal;
    JLabel textlabel;
    JButton adduser;
    boolean isEdit;
    ListItem EditNS;
    ZoneInfo EditZone;

    public AddNSRecord(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Name Server To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
 
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Authoritive Name Server Details (To Allow Transfer To/From)");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
 
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Authoritive Name Server Details");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      nsaddress=new JTextField("",10);
      addLabel(new JLabel("Name Server IP Address"),nsaddress,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      if (isEdit) {
        EditNS=(ListItem)node.getUserObject();
        nsaddress.setText(EditNS.Entry);
        adduser=new JButton("Update");
      } else {
        adduser=new JButton("Add Name Server");
      }
      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Trust User");
      adduser.addActionListener(this);
      gridbag.setConstraints(adduser,layout);
      add(adduser);

      if (! isEdit) {
        EditZone=(ZoneInfo)node.getUserObject();
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel etextlabel=new JLabel("Edit "+EditZone.Zone+" Domain");
        gridbag.setConstraints(etextlabel,layout);
        add(etextlabel);

        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTHWEST;

        domain=new JTextField(EditZone.Zone,10);
        addLabel(new JLabel("Domain Name"),domain,gridbag,layout);

        tsigkey1=new JPasswordField(EditZone.Tsig,10);
        tsigkey2=new JPasswordField(EditZone.Tsig,10);
        if (EditZone.Tsig.length() > 0) {
          addLabel(new JLabel("TSIG Key (Remove To Change Entry To Slave)"),tsigkey1,gridbag,layout);
        } else {
          addLabel(new JLabel("Enter TSIG Key To Change Entry To Master"),tsigkey1,gridbag,layout);
        }
        addLabel(new JLabel("Confirm TSIG Key"),tsigkey2,gridbag,layout);

        layout.gridwidth=GridBagConstraints.REMAINDER;
        internal=new JCheckBox("This Domain Is A Private Internal Domain",EditZone.Internal);
        gridbag.setConstraints(internal,layout);
        add(internal);

        layout.weighty=1;

        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.anchor=GridBagConstraints.NORTH;
        layout.fill=GridBagConstraints.NONE;

        JButton editdomain=new JButton("Save Changes");
        editdomain.setActionCommand("editdomain");
        editdomain.addActionListener(this);
        gridbag.setConstraints(editdomain,layout);
        add(editdomain);
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
      if (event.getActionCommand() != "editdomain") {
        if (nsaddress.getText().length() > 0) {
          if (! isEdit) {
            DefaultMutableTreeNode childnode=addAuthNS(node,nsaddress.getText());
            userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
            nsaddress.setText("");
          } else {
            EditNS.Entry=nsaddress.getText();
            treeModel.reload(node);
            userswindow.scrollPathToVisible(new TreePath(node.getPath()));
          }
        }
      } else {
        if (tsigkey1.getText().equals(tsigkey2.getText())) {
          EditZone.Tsig=tsigkey1.getText();
        } else {
          tsigkey1.setText(EditZone.Tsig);
          tsigkey2.setText(EditZone.Tsig);
        }
        EditZone.Zone=domain.getText();
	EditZone.Internal=internal.isSelected();
        treeModel.reload(node);
        userswindow.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }
  }
 
  public DefaultMutableTreeNode addIRecord(DefaultMutableTreeNode node,String address,
                                           String rtype,String value){
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new RecordInf(address,rtype,value));
    treeModel.insertNodeInto(childnode,node,node.getChildCount());
    return childnode;
  }

  public DefaultMutableTreeNode addAuthNS(DefaultMutableTreeNode node,String address){
    if (node != null ){
      DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ListItem(address));
      treeModel.insertNodeInto(childnode,node,node.getChildCount());
      return childnode;
    } else {
      return null;
    }
  }

  class DnsSetup extends Container implements ActionListener {
    JTextField domainname=new JTextField(dnsconf.Domain,10);
    JTextField hostname=new JTextField(dnsconf.Hostname,10);
    JTextField searchdomain=new JTextField(dnsconf.Search,10);
    JTextField soaserial=new JTextField(dnsconf.Serial,10);
    JTextField soarefresh=new JTextField(dnsconf.Refresh,10);
    JTextField soaretry=new JTextField(dnsconf.Retry,10);
    JTextField soaexpire=new JTextField(dnsconf.Expire,10);
    JTextField defaultttl=new JTextField(dnsconf.DefaultTTL,10);
    JTextField dyndnsserv=new JTextField(dnsconf.DynDNSIP,10);
    JTextField dyndnsdomain=new JTextField(dnsconf.DynDNSDomain,10);

    JPasswordField dyndnssecret11=new JPasswordField(dnsconf.DynDNSSecret,10);
    JPasswordField dyndnssecret12=new JPasswordField(dnsconf.DynDNSSecret,10);

    JPasswordField dyndnssecret21=new JPasswordField(dnsconf.DynDNSSecret2,10);
    JPasswordField dyndnssecret22=new JPasswordField(dnsconf.DynDNSSecret2,10);

    JCheckBox dnsbackup=new JCheckBox("Backup DNS Directory",dnsconf.Backup);
    JCheckBox dnsauth=new JCheckBox("Authorotive (Internal)",dnsconf.Auth);
    JCheckBox dnsauthx=new JCheckBox("Authorotive (External)",dnsconf.AuthX);
    JCheckBox dnspeer=new JCheckBox("Ignore PPP DNS Servers",dnsconf.Usepeer);
    JCheckBox dnsextserv=new JCheckBox("Allow External Server",dnsconf.Recursion);
    JCheckBox dnsintfirst=new JCheckBox("Use Internal Server First",dnsconf.Intfirst);

    public DnsSetup(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);
      layout.weightx=1;
      layout.weighty=0;

      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Default DNS Settings");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;

      addLabel(new JLabel("Domain Name"),domainname,gridbag,layout);
      addLabel(new JLabel("Hostname"),hostname,gridbag,layout);
      addLabel(new JLabel("Search Domains"),searchdomain,gridbag,layout);
      addLabel(new JLabel("SOA Serial"),soaserial,gridbag,layout);
      addLabel(new JLabel("SOA Refresh"),soarefresh,gridbag,layout);
      addLabel(new JLabel("SOA Retry"),soaretry,gridbag,layout);
      addLabel(new JLabel("SOA Expire"),soaexpire,gridbag,layout);
      addLabel(new JLabel("Default TTL"),defaultttl,gridbag,layout);
      addLabel(new JLabel("IP Of Dynamic DNS Server"),dyndnsserv,gridbag,layout);
      addLabel(new JLabel("Dynamic Domain Name"),dyndnsdomain,gridbag,layout);
      addPwLabel(new JLabel("Shared Secret For DNS Updates"),dyndnssecret11,gridbag,layout);
      addPwLabel(new JLabel("Confirm Shared Secret"),dyndnssecret12,gridbag,layout);
      addPwLabel(new JLabel("Shared Secret For Dynamic DNS"),dyndnssecret21,gridbag,layout);
      addPwLabel(new JLabel("Confirm Shared Secret"),dyndnssecret22,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(dnsbackup,layout);
      add(dnsbackup);      

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(dnspeer,layout);
      add(dnspeer);      

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(dnsauth,layout);
      add(dnsauth);      

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(dnsauthx,layout);
      add(dnsauthx);      

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(dnsextserv,layout);
      add(dnsextserv);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(dnsintfirst,layout);
      add(dnsintfirst);
        
      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;

      JButton savedns=new JButton("Save Settings");
      savedns.setActionCommand("Save Dns");
      savedns.addActionListener(this);
      gridbag.setConstraints(savedns,layout);
      add(savedns);
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
      dnsconf.Domain=domainname.getText();
      dnsconf.Hostname=hostname.getText();
      dnsconf.Serial=soaserial.getText();
      dnsconf.Refresh=soarefresh.getText();
      dnsconf.Retry=soaretry.getText();
      dnsconf.Expire=soaexpire.getText();
      dnsconf.DefaultTTL=defaultttl.getText();
      dnsconf.DynDNSIP=dyndnsserv.getText();
      dnsconf.DynDNSDomain=dyndnsdomain.getText();
      dnsconf.Search=searchdomain.getText();
      if (dyndnssecret11.getText().equals(dyndnssecret12.getText())) {
        dnsconf.DynDNSSecret=dyndnssecret11.getText();
      } else {
        dyndnssecret11.setText(dnsconf.DynDNSSecret);
        dyndnssecret12.setText(dnsconf.DynDNSSecret);
      }
      if (dyndnssecret21.getText().equals(dyndnssecret22.getText())) {
        dnsconf.DynDNSSecret2=dyndnssecret21.getText();
      } else {
        dyndnssecret21.setText(dnsconf.DynDNSSecret2);
        dyndnssecret22.setText(dnsconf.DynDNSSecret2);
      }
      dnsconf.Backup=dnsbackup.isSelected();
      dnsconf.Auth=dnsauth.isSelected();
      dnsconf.Intfirst=dnsintfirst.isSelected();
      dnsconf.AuthX=dnsauthx.isSelected();
      dnsconf.Usepeer=dnspeer.isSelected();
      dnsconf.Recursion=dnsextserv.isSelected();
    }
  }

  class AddHostedDomain extends Container implements ActionListener {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    JTextField domain;
    JPasswordField tsigkey1,tsigkey2;
    JCheckBox internal;

    public AddHostedDomain(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Domain To Manage",false);      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("New Domain To Add");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      domain=new JTextField("",10);
      addLabel(new JLabel("Domain Name"),domain,gridbag,layout);

      tsigkey1=new JPasswordField("",10);
      addLabel(new JLabel("TSIG Key (Master Domain)"),tsigkey1,gridbag,layout);

      tsigkey2=new JPasswordField("",10);
      addLabel(new JLabel("Confirm TSIG Key"),tsigkey2,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      internal=new JCheckBox("This Domain Is A Private Internal Domain",false);
      gridbag.setConstraints(internal,layout);
      add(internal);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Domain");

      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Domain");
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
      String zonekey;
      if (domain.getText().length() > 0) {
        if ((tsigkey1.getText().length() > 0) & (tsigkey1.getText().equals(tsigkey2.getText()))){
          zonekey=tsigkey1.getText();
        } else {
          zonekey="";
        }
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ZoneInfo(domain.getText(),zonekey,internal.isSelected()));
        treeModel.insertNodeInto(childnode,node,node.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);
        domain.setText("");
        tsigkey1.setText("");
        tsigkey2.setText("");
	internal.setSelected(false);
      }
    }
  }

  public DefaultMutableTreeNode getDomain(DefaultMutableTreeNode node,String tofind) {
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;
    ZoneInfo lnode;

    for (Enumeration e = node.children() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      lnode=(ZoneInfo)intdata.getUserObject();
      if (lnode.Zone.equals(tofind)) {
        outdata=intdata;
      }
    }
    return outdata;
  }

  public String getConfig(){
    DefaultMutableTreeNode rdata,cdata,hdata;
    DnsType dtype;
    HostInf hostd;
    ZoneInfo zinfo; 
    RecordInf record;
    Output="";

    if (dnsconf.Domain.length() > 0) {
      Output=AddConfL("DNS Domain "+dnsconf.Domain);
    }

    if (dnsconf.Hostname.length() > 0) {
      Output=AddConfL("DNS Hostname "+dnsconf.Hostname);
    }

    if (dnsconf.Search.length() > 0) {
      Output=AddConfL("DNS Search "+dnsconf.Search);
    }

    if (dnsconf.Serial.length() > 0) {
      Output=AddConfL("DNS Serial "+dnsconf.Serial);
    }

    if (dnsconf.Refresh.length() > 0) {
      Output=AddConfL("DNS Refresh "+dnsconf.Refresh);
    }

    if (dnsconf.Retry.length() > 0) {
      Output=AddConfL("DNS Retry "+dnsconf.Retry);
    }

    if (dnsconf.Expire.length() > 0) {
      Output=AddConfL("DNS Expire "+dnsconf.Expire);
    }

    if (dnsconf.DefaultTTL.length() > 0) {
      Output=AddConfL("DNS DefaultTTL "+dnsconf.DefaultTTL);
    }

    if (dnsconf.DynDNSIP.length() > 0) {
      Output=AddConfL("DNS DynServ "+dnsconf.DynDNSIP);
    }

    if (dnsconf.DynDNSDomain.length() > 0) {
      Output=AddConfL("DNS DynZone "+dnsconf.DynDNSDomain);
    }

    if (dnsconf.DynDNSSecret.length() > 0) {
      Output=AddConfL("DNS DynKey "+dnsconf.DynDNSSecret);
    }

    if (dnsconf.DynDNSSecret2.length() > 0) {
      Output=AddConfL("DNS SmartKey "+dnsconf.DynDNSSecret2);
    }

    Output=AddConfL("DNS Backup "+dnsconf.Backup);
    Output=AddConfL("DNS Auth "+dnsconf.Auth);
    Output=AddConfL("DNS AuthX "+dnsconf.AuthX);
    Output=AddConfL("DNS Usepeer "+dnsconf.Usepeer);
    Output=AddConfL("DNS ExtServ "+dnsconf.Recursion);
    Output=AddConfL("DNS IntFirst "+dnsconf.Intfirst);
 

    for (Enumeration e = topbranch.children() ; e.hasMoreElements() ;) {
      rdata=(DefaultMutableTreeNode)e.nextElement();
      dtype=(DnsType)rdata.getUserObject();
      for (Enumeration e1 = rdata.children() ; e1.hasMoreElements() ;) {
        cdata=(DefaultMutableTreeNode)e1.nextElement();
        if (dtype.Confname == "Host") {
          hostd=(HostInf)cdata.getUserObject();
          Output=AddConfL("DNS Host "+hostd.HostName+" "+hostd.IPAddress+" "+hostd.MACAddr);
        } else if (dtype.Confname == "Hosted") {
          zinfo=(ZoneInfo)cdata.getUserObject();
          if (zinfo.Tsig.length() > 0) {
            Output=AddConfL("DNS Hosted "+zinfo.Zone+" "+zinfo.Tsig+" "+zinfo.Internal);
          } else {
            Output=AddConfL("DNS Hosted "+zinfo.Zone+" "+zinfo.Internal);
          }
          for (Enumeration e2 = cdata.children() ; e2.hasMoreElements() ;) {
            hdata=(DefaultMutableTreeNode)e2.nextElement();
            Output=AddConfL("DNS NameServer "+zinfo.Zone+" "+hdata.toString());
          }
        } else if (dtype.Confname == "Slave") {
          zinfo=(ZoneInfo)cdata.getUserObject();         
          Output=AddConfL("DNS "+dtype.Confname+"Domain "+zinfo.Zone);
        }
      }
    }
    return Output;
  }

  public String AddConfL(String newconf){
    String newline = System.getProperty("line.separator");
    String confout=Output+newconf+newline;
    return confout;
  }  

  public void delConfig() {
    DefaultMutableTreeNode delnode;

    dnsconf.delConfig();
    for (Enumeration e = topbranch.children() ; e.hasMoreElements() ;) {
      delnode=(DefaultMutableTreeNode)e.nextElement();
      delnode.removeAllChildren();
      treeModel.reload(delnode);
    }
  }

  public void setDefault() {
    delConfig();

    dnsconf.Domain="company.co.za";
    dnsconf.Hostname="netsentry";
    dnsconf.Serial="1";
    dnsconf.Refresh="3600";
    dnsconf.Retry="1800";
    dnsconf.Expire="604800";
    dnsconf.DefaultTTL="3600";
    dnsconf.DynDNSSecret="secret";
    dnsconf.DynDNSSecret2="secret";


    addIHost("192.168.0.20","manager","00:00:00:00:00");


    DrawWindow();
  }
}

class ZoneInfo {
    String Zone;
    String Tsig;
    boolean Internal;

    public ZoneInfo(String zone, String key,boolean intz) {
        Zone=zone;
        Tsig=key;
        Internal=intz;
    }
    
    public String toString() {
        String Output;
        if (Tsig.length() > 0) {
            Output=Zone+" (Master)";
        } else {
            Output=Zone+" (Slave)";
        }
        if (Internal) {
          Output=Zone+" [Private]";
        }
        return Output;
    }
    
}

