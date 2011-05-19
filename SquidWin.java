import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class SquidWin extends Container {
  JSplitPane mainwindow=new JSplitPane();
  JTree userswindow;
  DefaultTreeModel treeModel;
  DefaultMutableTreeNode filter,filtergrps,timezones,ipaccess,ipbypass,denykey,denyurl,allowkey,allowurl,flists,afilter,dialup,redirsite;
  String Output;
  SquidConf squidconf=new SquidConf();
  Vector IntList;
  DefaultMutableTreeNode topbranch = new DefaultMutableTreeNode("Proxy Config");


  public SquidWin() {
    setLayout(new BorderLayout());

    ipaccess=new DefaultMutableTreeNode("IP Access Control");
    topbranch.add(ipaccess);
    ipbypass=new DefaultMutableTreeNode("Bypass Control");
    topbranch.add(ipbypass);
    filter=new DefaultMutableTreeNode("Filter Lists");
    denyurl=new DefaultMutableTreeNode("Localy Denied URL");
    denykey=new DefaultMutableTreeNode("Localy Denied Keyword");
    allowurl=new DefaultMutableTreeNode("Localy Allowed URL");
    allowkey=new DefaultMutableTreeNode("Localy Allowed Keyword");
    flists=new DefaultMutableTreeNode("Known Lists");

    filter.add(denyurl);
    filter.add(denykey);
    filter.add(allowurl);
    filter.add(allowkey);
//    filter.add(flists);
    topbranch.add(filter);

    timezones=new DefaultMutableTreeNode("Time Controls");
    topbranch.add(timezones);
    filtergrps=new DefaultMutableTreeNode("Filter Groups");
    topbranch.add(filtergrps);
    afilter=new DefaultMutableTreeNode("Filter Access Control");
    topbranch.add(afilter);

    redirsite=new DefaultMutableTreeNode("Redirected Web Sites");
    topbranch.add(redirsite);    

    treeModel = new DefaultTreeModel(topbranch);
    userswindow=new JTree(treeModel);
    userswindow.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    userswindow.setShowsRootHandles(true);

    mainwindow.setLeftComponent(new JScrollPane(userswindow));    
    userswindow.setSelectionPath(new TreePath(topbranch.getPath()));
    mainwindow.setBottomComponent(new ConfigWin());
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
    int Depth=userswindow.getSelectionPath().getPathCount();

    if (Depth == 1){
     mainwindow.setBottomComponent(new ConfigWin());
    } else if (nodeInfo.toString() == "IP Access Control"){
     mainwindow.setBottomComponent(new ProxyAccess(false));
    } else if (node.isNodeAncestor(ipaccess)) {
     mainwindow.setBottomComponent(new ProxyAccess(true));
    } else if (nodeInfo.toString() == "Filter Access Control"){
     mainwindow.setBottomComponent(new FilterAccess());
    } else if (nodeInfo.toString() == "Bypass Control"){
     mainwindow.setBottomComponent(new ProxyBypassConfig(false));
    } else if (node.isNodeAncestor(ipbypass)) {
     mainwindow.setBottomComponent(new ProxyBypassConfig(true));
    } else if (nodeInfo.toString() == "Filter Groups"){
     mainwindow.setBottomComponent(new AddProxyGroup());
    } else if (nodeInfo.toString() == "Time Controls"){
     mainwindow.setBottomComponent(new FilterTime());
    } else if (nodeInfo.toString() == "Redirected Web Sites" ){
     mainwindow.setBottomComponent(new AddRedirectSite(false));
    } else if (node.isNodeAncestor(redirsite) ){
     mainwindow.setBottomComponent(new AddRedirectSite(true));
    } else if (Depth == 3) {
      if (node.getParent().toString() == "Time Controls"){
        mainwindow.setBottomComponent(new FilterTimePeriod(false));
      } else if (node.getParent().toString() == "Filter Groups"){
        mainwindow.setBottomComponent(new ProxyGroupMember(false));
      } else if (node.getParent().toString() == "Filter Access Control"){
        mainwindow.setBottomComponent(new AddListToControl(false));
      } else if (nodeInfo.toString() == "Localy Denied URL"){
        mainwindow.setBottomComponent(new AddLocalList(true,true,false));
      } else if (nodeInfo.toString() == "Localy Denied Keyword"){
        mainwindow.setBottomComponent(new AddLocalList(false,true,false));
      } else if (nodeInfo.toString() == "Localy Allowed URL"){
        mainwindow.setBottomComponent(new AddLocalList(true,false,false));
      } else if (nodeInfo.toString() == "Localy Allowed Keyword"){
        mainwindow.setBottomComponent(new AddLocalList(false,false,false));
      } else if (nodeInfo.toString() == "Known Lists"){
        mainwindow.setBottomComponent(new AddFilterList());
      } else {
        mainwindow.setBottomComponent(null);
      }
    } else if (Depth > 3) {
      if (node.isNodeAncestor(denyurl)) {
        mainwindow.setBottomComponent(new AddLocalList(true,true,true));
      } else if (node.isNodeAncestor(denykey)) {
        mainwindow.setBottomComponent(new AddLocalList(false,true,true));
      } else if (node.isNodeAncestor(allowurl)) {
        mainwindow.setBottomComponent(new AddLocalList(true,false,true));
      } else if (node.isNodeAncestor(allowkey)) {
        mainwindow.setBottomComponent(new AddLocalList(false,false,true));
      } else if (node.isNodeAncestor(filtergrps)) {
        mainwindow.setBottomComponent(new ProxyGroupMember(true));
      } else if (node.isNodeAncestor(afilter)) {
        mainwindow.setBottomComponent(new AddListToControl(true));
      } else if (node.isNodeAncestor(timezones)) {
        mainwindow.setBottomComponent(new FilterTimePeriod(true));
      } else {
        mainwindow.setBottomComponent(null);
      }
    } else {
      mainwindow.setBottomComponent(null);
    }
    mainwindow.setDividerLocation(0.3);

    if (flists.getChildCount() <= 0) {
      treeModel.insertNodeInto(new DefaultMutableTreeNode("ads"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("aggressive"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("audio-video"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("drugs"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("gambling"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("hacking"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("mail"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("porn"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("proxy"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("redirector"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("spyware"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("suspect"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("violence"),
                               flists,flists.getChildCount());
      treeModel.insertNodeInto(new DefaultMutableTreeNode("warez"),
                               flists,flists.getChildCount());
    }
  }

  class ConfigWin extends Container implements ActionListener {
    JTextField squidcs=new JTextField(squidconf.CacheSize,10);
    JTextField squidredir=new JTextField(squidconf.Redir,10);
    JTextField filterurl=new JTextField(squidconf.FilterRedirect,10);
    JTextField squidparent=new JTextField(squidconf.Parent,10);
    JTextField squidlogin=new JTextField(squidconf.Login,10);

    JPasswordField squidpass1=new JPasswordField(squidconf.Pass,10);
    JPasswordField squidpass2=new JPasswordField(squidconf.Pass,10);
    JCheckBox fqdnlog;

    public ConfigWin() {
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=0;
 
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Proxy Server Config");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      addLabel(new JLabel("Proxy Server Cache Size"),squidcs,gridbag,layout);
      addLabel(new JLabel("Proxy Server Parent (Local IP:PORT)"),squidparent,gridbag,layout);

      addLabel(new JLabel("Proxy Server Parent Login"),squidlogin,gridbag,layout);

      addLabel(new JLabel("Proxy Server Parent Password (If Required)"),squidpass1,gridbag,layout);
      addLabel(new JLabel("Confirm Password"),squidpass2,gridbag,layout);

      addLabel(new JLabel("Filter Redirect URL"),filterurl,gridbag,layout);
      addLabel(new JLabel("No. Redirector Children"),squidredir,gridbag,layout);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;

      fqdnlog=new JCheckBox("Log Fully Qualified Names",squidconf.fqdnlog);
      gridbag.setConstraints(fqdnlog,layout);
      add(fqdnlog);

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
      squidconf.CacheSize=squidcs.getText();
      squidconf.Parent=squidparent.getText();
      squidconf.Login=squidlogin.getText();
      if (squidpass1.getText().equals(squidpass2.getText())) {
        squidconf.Pass=squidpass1.getText();
      } else {
        squidpass1.setText("");
        squidpass2.setText("");
      }
      squidconf.FilterRedirect=filterurl.getText();
      squidconf.Redir=squidredir.getText();
      squidconf.fqdnlog=fqdnlog.isSelected();
    }
  }

  class ProxyAccess extends Container implements ActionListener {
    JTextField saddr,nmask;
    JCheckBox acceptd;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit; 
    JLabel textlabel;    
    JButton adduser;
    ProxyACL EditACL;

    public ProxyAccess(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Access Control To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New Source Address To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Access Control");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      saddr=new JTextField("",10);
      addLabel(new JLabel("Address"),saddr,gridbag,layout);

      nmask=new JTextField("255.255.255.255",10);
      addLabel(new JLabel("Netmask"),nmask,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      acceptd=new JCheckBox("Allow Access For Network");
      gridbag.setConstraints(acceptd,layout);
      acceptd.setSelected(true);
      add(acceptd);

      if (isEdit) {
        EditACL=(ProxyACL)node.getUserObject();
	saddr.setText(EditACL.IPAddr);
	nmask.setText(EditACL.Netmask);
        acceptd.setSelected(EditACL.Accept);
        adduser=new JButton("Save Access Control");
      } else {
        adduser=new JButton("Add Access Control");
      }

      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
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
      if ((saddr.getText().length() > 0) & (nmask.getText().length() > 0)){
        if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ProxyACL(saddr.getText(),
                                                                                   nmask.getText(),
                                                                                   acceptd.isSelected()));
          treeModel.insertNodeInto(childnode,node,node.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          saddr.setText("");
          nmask.setText("255.255.255.255");
          acceptd.setSelected(true);
        } else {
          EditACL.IPAddr=saddr.getText();
          EditACL.Netmask=nmask.getText();
          EditACL.Accept=acceptd.isSelected();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }
  class ProxyBypassConfig extends Container implements ActionListener {
    JTextField daddr,nmask;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    ProxyBypass EditBypass;

    public ProxyBypassConfig(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Destination To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
  
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New Bypass Destination To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
  
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Proxy Bypass");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      daddr=new JTextField("",10);
      addLabel(new JLabel("Address"),daddr,gridbag,layout);

      nmask=new JTextField("255.255.255.255",10);
      addLabel(new JLabel("Netmask"),nmask,gridbag,layout);

      if (isEdit) {
        EditBypass=(ProxyBypass)node.getUserObject();
        daddr.setText(EditBypass.IPAddr);
        nmask.setText(EditBypass.Netmask);
        adduser=new JButton("Save Address To Bypass");
      } else {
        adduser=new JButton("Add Address To Bypass");
      }

      layout.fill=GridBagConstraints.NONE;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
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
      if ((daddr.getText().length() > 0) & (nmask.getText().length() > 0)){
        if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ProxyBypass(daddr.getText(),
                                                                                   nmask.getText()));
          treeModel.insertNodeInto(childnode,node,node.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          daddr.setText("");
          nmask.setText("255.255.255.255");
        } else {
          EditBypass.IPAddr=daddr.getText();
          EditBypass.Netmask=nmask.getText();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  class ProxyGroupMember extends Container implements ActionListener {
    JTextField daddr,nmask,fgroup;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    JButton adduser;
    JLabel textlabel;
    boolean isEdit;
    ListItem EditGroup;
    FilterMember EditFilter;

    public ProxyGroupMember(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Member To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Add New Member Address Range");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Member Address Range");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      daddr=new JTextField("",10);
      addLabel(new JLabel("Address"),daddr,gridbag,layout);

      nmask=new JTextField("255.255.255.255",10);
      addLabel(new JLabel("Netmask"),nmask,gridbag,layout);

      layout.fill=GridBagConstraints.NONE;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      if (isEdit) {
        EditFilter=(FilterMember)node.getUserObject();
        daddr.setText(EditFilter.IPAddr);
        nmask.setText(EditFilter.Netmask);
        adduser=new JButton("Update Address");
      } else {
        adduser=new JButton("Add Address");
      }
      adduser.setActionCommand("Add Group Member");
      adduser.addActionListener(this);
      gridbag.setConstraints(adduser,layout);
      add(adduser);

      if (! isEdit) {
        EditGroup=(ListItem)node.getUserObject();
        layout.anchor=GridBagConstraints.NORTH;
        layout.weightx=0;
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel textlabele=new JLabel("Edit Group Name");
        gridbag.setConstraints(textlabele,layout);
        add(textlabele);

        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTHWEST;

        fgroup=new JTextField(EditGroup.Entry,10);
        addLabel(new JLabel("Group Name"),fgroup,gridbag,layout);

        layout.weighty=1;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        layout.anchor=GridBagConstraints.NORTH;

        JButton edituser=new JButton("Save");
        edituser.setActionCommand("Edit User");
        edituser.addActionListener(this);
        gridbag.setConstraints(edituser,layout);
        add(edituser);
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
      if (event.getActionCommand() == "Add Group Member") {
        if ((daddr.getText().length() > 0) & (nmask.getText().length() > 0)){
          if (! isEdit) {
            DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FilterMember(daddr.getText(),
                                                                                     nmask.getText()));
            treeModel.insertNodeInto(childnode,node,node.getChildCount());
            userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
            daddr.setText("");
            nmask.setText("255.255.255.255");
          } else {
            EditFilter.IPAddr=daddr.getText();
            EditFilter.Netmask=nmask.getText();
            treeModel.reload(node);
            userswindow.scrollPathToVisible(new TreePath(node.getPath()));
          }
        }
      } else {
        EditGroup.Entry=fgroup.getText();
        treeModel.reload(node);
        userswindow.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }
  }

  class AddProxyGroup extends Container implements ActionListener {
    JTextField fgroup;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public AddProxyGroup(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Group To Manage",false);      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("New Filter Group");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      fgroup=new JTextField("",10);
      addLabel(new JLabel("Group Name"),fgroup,gridbag,layout);


      layout.fill=GridBagConstraints.NONE;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JButton adduser=new JButton("Add Group");
      adduser.setActionCommand("Add Group");
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
      if (fgroup.getText().length() > 0){
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ListItem(fgroup.getText()));
        treeModel.insertNodeInto(childnode,node,node.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);
        fgroup.setText("");
      }
    }
  }

  class FilterTime extends Container implements ActionListener {
    JTextField timep;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public FilterTime(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Time Group To Manage",false);      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("New Filter Time Group");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      timep=new JTextField("",10);
      addLabel(new JLabel("Time Period Group Name"),timep,gridbag,layout);

      layout.fill=GridBagConstraints.NONE;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JButton adduser=new JButton("Add Group");
      adduser.setActionCommand("Add Group");
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
      if (timep.getText().length() > 0){
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ListItem(timep.getText()));
        treeModel.insertNodeInto(childnode,node,node.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);
        timep.setText("");
      }
    }
  }

  class FilterTimePeriod extends Container implements ActionListener {
    JTextField times,timee,zname;
    JList daylist;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    String weekdays[]={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
    ListItem EditZone;
    TimeSpace EditTime;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;

    public FilterTimePeriod(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Time Period To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
 
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New Filter Time Period");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
 
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Time Period");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;
      
      daylist=new JList(weekdays);
      JLabel dlabel=new JLabel("Select Days Of Week");
      layout.gridwidth=1;
      gridbag.setConstraints(dlabel,layout);
      add(dlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(daylist,layout);
      add(daylist);
      

      times=new JTextField("",10);
      addLabel(new JLabel("Start Time (HH:MM)"),times,gridbag,layout);

      timee=new JTextField("",10);
      addLabel(new JLabel("End Time (HH:MM)"),timee,gridbag,layout);

      layout.fill=GridBagConstraints.NONE;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;

      if (isEdit) {
        EditTime=(TimeSpace)node.getUserObject();
        times.setText(EditTime.TStart);
        timee.setText(EditTime.TEnd);
        String idata[];

        EditTime.Days=EditTime.Days.trim();
        idata=EditTime.Days.split(" ");
        int  sellist[]=new int[idata.length];
         
        for(int dcnt=0;dcnt < idata.length;dcnt++) {
          if (idata[dcnt].equals("mon")) {
            sellist[dcnt]=0;
          } else if (idata[dcnt].equals("tue")) {
            sellist[dcnt]=1;
          } else if (idata[dcnt].equals("wed")) {
            sellist[dcnt]=2;
          } else if (idata[dcnt].equals("thu")) {
            sellist[dcnt]=3;
          } else if (idata[dcnt].equals("fri")) {
            sellist[dcnt]=4;
          } else if (idata[dcnt].equals("sat")) {
            sellist[dcnt]=5;
          } else if (idata[dcnt].equals("sun")) {
            sellist[dcnt]=6;
          }
        }

        daylist.setSelectedIndices(sellist);
        adduser=new JButton("Update Time Period");
      } else {
        adduser=new JButton("Add Time Period");
      }

      adduser.setActionCommand("Add Time Period");
      adduser.addActionListener(this);
      gridbag.setConstraints(adduser,layout);
      add(adduser);

      if (! isEdit) {
        EditZone=(ListItem)node.getUserObject();
        layout.anchor=GridBagConstraints.NORTH;
        layout.weightx=0;
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel textlabele=new JLabel("Edit Time Period Name");
        gridbag.setConstraints(textlabele,layout);
        add(textlabele);

        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTHWEST;

        zname=new JTextField(EditZone.Entry,10);
        addLabel(new JLabel("Time Period Group Name"),zname,gridbag,layout);

        layout.weighty=1;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        layout.anchor=GridBagConstraints.NORTH;

        JButton edituser=new JButton("Save");
        edituser.setActionCommand("Edit User");
        edituser.addActionListener(this);
        gridbag.setConstraints(edituser,layout);
        add(edituser);
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
      String DaysSel="";
      if (event.getActionCommand() == "Add Time Period") {
        if ((daylist.getSelectedValues().length > 0) & (times.getText().length() > 0) &
            (timee.getText().length() > 0)){
          for(int rcnt=0;rcnt<daylist.getSelectedValues().length;rcnt++) {
            if (daylist.getSelectedValues()[rcnt].toString() == "Monday") {
              DaysSel="mon ";
            } else if (daylist.getSelectedValues()[rcnt].toString() == "Tuesday") {
              DaysSel=DaysSel+"tue ";
            } else if (daylist.getSelectedValues()[rcnt].toString() == "Wednesday") {
              DaysSel=DaysSel+"wed ";
            } else if (daylist.getSelectedValues()[rcnt].toString() == "Thursday") {
              DaysSel=DaysSel+"thu ";
            } else if (daylist.getSelectedValues()[rcnt].toString() == "Friday") {
              DaysSel=DaysSel+"fri ";
            } else if (daylist.getSelectedValues()[rcnt].toString() == "Saturday") {
              DaysSel=DaysSel+"sat ";
            } else if (daylist.getSelectedValues()[rcnt].toString() == "Sunday") {
              DaysSel=DaysSel+"sun ";
            }
          }
          if (! isEdit) {
            DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new TimeSpace(DaysSel,times.getText(),timee.getText()));
            treeModel.insertNodeInto(childnode,node,node.getChildCount());
            userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
            sortpanel.listdata.addElement(childnode);
            times.setText("");
            timee.setText("");
            daylist.setListData(weekdays);
          } else {
            EditTime.Days=DaysSel;
            EditTime.TStart=times.getText();
            EditTime.TEnd=timee.getText();
            treeModel.reload(node);
            userswindow.scrollPathToVisible(new TreePath(node.getPath()));
          }
        }
      } else {
          EditZone.Entry=zname.getText();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }
  }

  class FilterAccess extends Container implements ActionListener {
    JComboBox group,time,pass;
    JCheckBox tallow;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public FilterAccess(){
      String passlist[]={"Deny All Other Sites","Accept All Other Sites","Pass To Other Rules"};

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Access Control To Manage",false);      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("New Access Control");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      layout.gridwidth=1;
      JLabel glabel=new JLabel("Select Filter Group");
      gridbag.setConstraints(glabel,layout);
      add(glabel);

      Vector grplist=new Vector();
      for (Enumeration e = filtergrps.children() ; e.hasMoreElements() ;) {
        grplist.addElement((DefaultMutableTreeNode)e.nextElement());
      }

      layout.gridwidth=GridBagConstraints.REMAINDER;

      group=new JComboBox(grplist);
      gridbag.setConstraints(group,layout);
      add(group);

      layout.gridwidth=1;
      JLabel tlabel=new JLabel("Select Time Period");
      gridbag.setConstraints(tlabel,layout);
      add(tlabel);

      Vector timelist=new Vector();
      for (Enumeration e = timezones.children() ; e.hasMoreElements() ;) {
        timelist.addElement((DefaultMutableTreeNode)e.nextElement());
      }

      layout.gridwidth=GridBagConstraints.REMAINDER;

      time=new JComboBox(timelist);
      gridbag.setConstraints(time,layout);
      add(time);

      layout.gridwidth=1;
      JLabel dlabel=new JLabel("Default Action");
      gridbag.setConstraints(dlabel,layout);
      add(dlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      pass=new JComboBox(passlist);
      gridbag.setConstraints(pass,layout);
      add(pass);

      tallow=new JCheckBox("Apply Rules Dureing This Period (Alternativly Outside This Period)",true);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(tallow,layout);
      add(tallow);

      layout.fill=GridBagConstraints.NONE;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JButton adduser=new JButton("Add Group");
      adduser.setActionCommand("Add Access Control");
      adduser.addActionListener(this);
      gridbag.setConstraints(adduser,layout);
      add(adduser);
    }

    public void actionPerformed(ActionEvent event) {
      DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FilterACL(group.getSelectedItem().toString(),
                                                                               time.getSelectedItem().toString(),
                                                                               pass.getSelectedItem().toString(),
                                                                               tallow.isSelected()));
      treeModel.insertNodeInto(childnode,node,node.getChildCount());
      userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
      sortpanel.listdata.addElement(childnode);
      group.setSelectedIndex(0);
      time.setSelectedIndex(0);
      pass.setSelectedIndex(0);
      tallow.setSelected(true);
    }
  }

  class AddLocalList extends Container implements ActionListener {
    JTextField domain;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit;
    ListItem ListEnt;
    JButton adduser;

    public AddLocalList(boolean Url,boolean Deny,boolean edit){
      isEdit=edit;
      JLabel textlabel;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        if (Url) {
          sortpanel=new ManageNode(node,treeModel,"Select Url To Manage",false);      
        } else {
          sortpanel=new ManageNode(node,treeModel,"Select Keyword To Manage",false);      
        }

        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        if (Deny) {
          if (Url) {
            textlabel=new JLabel("New Domain To Deny");
          } else {
            textlabel=new JLabel("New Keyword To Deny");
          }
        } else {
          if (Url) {
            textlabel=new JLabel("New Domain To Allow");
          } else {
            textlabel=new JLabel("New Keyword To Allow");
          }
        }
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        if (Deny) {
          if (Url) {
            textlabel=new JLabel("Edit Denied Domain");
          } else {
            textlabel=new JLabel("Edit Denied Keyword");
          }
        } else {
          if (Url) {
            textlabel=new JLabel("Edit Allowed Domain");
          } else {
            textlabel=new JLabel("Edit Allowed Keyword");
          }
        }
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      domain=new JTextField("",10);
      addLabel(new JLabel("Entry"),domain,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;

      if (isEdit) {
	ListEnt=(ListItem)node.getUserObject();
	domain.setText(ListEnt.Entry);
        adduser=new JButton("Edit Entry");
      } else {
        adduser=new JButton("Save Entry");
      }

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
      if (domain.getText().length() > 0){
	if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new ListItem(domain.getText()));
          treeModel.insertNodeInto(childnode,node,node.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          domain.setText("");
        } else {
          ListEnt.Entry=domain.getText();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  class AddFilterList extends Container implements ActionListener {
    JTextField domain;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public AddFilterList(){
      JLabel textlabel;

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Manage Defined Lists",false);      

      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      textlabel=new JLabel("Add Defined List");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      domain=new JTextField("",10);
      addLabel(new JLabel("New List"),domain,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Entry");

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
      if (domain.getText().length() > 0){
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(domain.getText());
        treeModel.insertNodeInto(childnode,node,node.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);
        domain.setText("");
      }
    }
  }

  class AddListToControl extends Container implements ActionListener {
    JComboBox group,time,pass,vuser;
    JCheckBox tallow,callow;
    ManageNode sortpanel;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    DefaultMutableTreeNode cdata;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    FilterCTRL EditFilter;
    FilterACL EditACL;

    public AddListToControl(boolean edit){
      isEdit=edit;
      String passlist[]={"Deny All Other Sites","Accept All Other Sites","Pass To Other Rules"};

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
        sortpanel=new ManageNode(node,treeModel,"Select Permision To Manage",true);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Select List To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Updating Rule");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      layout.gridwidth=1;
      JLabel ulabel=new JLabel("Select List To Add To Control");
      gridbag.setConstraints(ulabel,layout);
      add(ulabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      vuser=new JComboBox();
      gridbag.setConstraints(vuser,layout);

      vuser.addItem("Local Denied");
      vuser.addItem("Local Allowed");

      for (Enumeration e = flists.children() ; e.hasMoreElements() ;) {
        cdata=(DefaultMutableTreeNode)e.nextElement();
        vuser.addItem(cdata.toString());
      }
      vuser.addItem("Non Resolved Sites");
      add(vuser);

      callow=new JCheckBox("Accept Access To This List",false);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      gridbag.setConstraints(callow,layout);
      add(callow);

      layout.weighty=1;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;

      if (isEdit) {
        EditFilter=(FilterCTRL)node.getUserObject();
        callow.setSelected(! EditFilter.Accept);
        vuser.setSelectedItem(EditFilter.List);
        adduser=new JButton("Update");
      } else {
        adduser=new JButton("Add Control");
      }
      adduser.setActionCommand("Add Filter Ctrl");
      adduser.addActionListener(this);

      gridbag.setConstraints(adduser,layout);
      add(adduser);

      if (! isEdit) {
        EditACL=(FilterACL)node.getUserObject();
        layout.anchor=GridBagConstraints.NORTH;
        layout.weightx=0;
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;

        JLabel textlabel=new JLabel("Edit Access Control");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);

        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.anchor=GridBagConstraints.NORTHWEST;

        layout.gridwidth=1;
        JLabel glabel=new JLabel("Select Filter Group");
        gridbag.setConstraints(glabel,layout);
        add(glabel);

        Vector grplist=new Vector();
        for (Enumeration e = filtergrps.children() ; e.hasMoreElements() ;) {
          grplist.addElement((DefaultMutableTreeNode)e.nextElement());
        }

        layout.gridwidth=GridBagConstraints.REMAINDER;

        group=new JComboBox(grplist);
        gridbag.setConstraints(group,layout);
        add(group);

        layout.gridwidth=1;
        JLabel tlabel=new JLabel("Select Time Period");
        gridbag.setConstraints(tlabel,layout);
        add(tlabel);

        Vector timelist=new Vector();
        for (Enumeration e = timezones.children() ; e.hasMoreElements() ;) {
          timelist.addElement((DefaultMutableTreeNode)e.nextElement());
        }

        layout.gridwidth=GridBagConstraints.REMAINDER;

        time=new JComboBox(timelist);
        gridbag.setConstraints(time,layout);
        add(time);

        layout.gridwidth=1;
        JLabel dlabel=new JLabel("Default Action");
        gridbag.setConstraints(dlabel,layout);
        add(dlabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        pass=new JComboBox(passlist);
        gridbag.setConstraints(pass,layout);
        add(pass);

        tallow=new JCheckBox("Apply Rules Dureing This Period (Alternativly Outside This Period)",true);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        gridbag.setConstraints(tallow,layout);
        add(tallow);

        layout.fill=GridBagConstraints.NONE;
        layout.weighty=1;
        layout.anchor=GridBagConstraints.NORTH;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JButton edituser=new JButton("Update");
        edituser.setActionCommand("Edit Access Control");
        edituser.addActionListener(this);
        gridbag.setConstraints(edituser,layout);
        add(edituser);

        group.setSelectedItem(EditACL.Group);
        time.setSelectedItem(EditACL.TimeSpace);
        pass.setSelectedItem(EditACL.Default);
        tallow.setSelected(EditACL.Accept);
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
      if (event.getActionCommand() == "Add Filter Ctrl") {
        if (! isEdit) {
          String list=(String)vuser.getSelectedItem();
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FilterCTRL(list,! callow.isSelected()));
          treeModel.insertNodeInto(childnode,node,node.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          vuser.setSelectedIndex(0);
          callow.setSelected(false);
          sortpanel.listdata.addElement(childnode);
        } else {
          EditFilter.List=(String)vuser.getSelectedItem();
          EditFilter.Accept=! callow.isSelected();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      } else {
        EditACL.Group=group.getSelectedItem().toString();
        EditACL.TimeSpace=time.getSelectedItem().toString();
        EditACL.Default=pass.getSelectedItem().toString();
        EditACL.Accept=tallow.isSelected();
        treeModel.reload(node);
        userswindow.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }
  }


  public DefaultMutableTreeNode getGroupNode(DefaultMutableTreeNode gtype,String tofind){
    DefaultMutableTreeNode indata=null;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = gtype.children() ; e.hasMoreElements() ;) {
      indata=(DefaultMutableTreeNode)e.nextElement();
      if (indata.toString().equals(tofind)) {
        outdata=indata;
      }
    }
    return outdata;
  }

  public DefaultMutableTreeNode getFilterACLNode(String group,String time,String Default,boolean inout){
    DefaultMutableTreeNode indata=null;
    DefaultMutableTreeNode outdata=null;
    FilterACL acl;

    for (Enumeration e = afilter.children() ; e.hasMoreElements() ;) {
      indata=(DefaultMutableTreeNode)e.nextElement();
      acl=(FilterACL)indata.getUserObject();
      if (acl.Group.equals(group) & acl.TimeSpace.equals(time) &
          acl.Default.equals(Default) & (acl.Accept & inout)) {
        outdata=indata;
      }
    }
    return outdata;
  }

  public String getConfig() {
    Output="";
    DefaultMutableTreeNode adata,gdata;
    ProxyACL access;
    ProxyBypass bypass;
    TimeSpace ptime;
    FilterMember fmem;
    FilterACL facl;
    String defaulta,fclist;
    FilterCTRL fctrl;
    RedirectedSite rsite;

    if (squidconf.CacheSize.length() > 0) {
      Output=AddConfL("Proxy CacheSize "+squidconf.CacheSize);
    }

    if (squidconf.Redir.length() > 0) {
      Output=AddConfL("Proxy Redir "+squidconf.Redir);
    }

    if (squidconf.Parent.length() > 0) {
      Output=AddConfL("Proxy Parent "+squidconf.Parent);
    }

    if (squidconf.Login.length() > 0) {
      Output=AddConfL("Proxy Login "+squidconf.Login);
    }

    if (squidconf.Pass.length() > 0) {
      Output=AddConfL("Proxy Pass "+squidconf.Pass);
    }

    if (squidconf.FilterRedirect.length() > 0) {
      Output=AddConfL("Proxy Redirect "+squidconf.FilterRedirect);
    }

    Output=AddConfL("Proxy LogFQDN "+squidconf.fqdnlog);

    for (Enumeration e = ipaccess.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      access=(ProxyACL)adata.getUserObject();
      Output=AddConfL("Proxy Access "+access.IPAddr+" "+access.Netmask+" "+access.Accept);
    }

    for (Enumeration e = ipbypass.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      bypass=(ProxyBypass)adata.getUserObject();
      Output=AddConfL("Proxy Bypass "+bypass.IPAddr+" "+bypass.Netmask);
    }

/*
    for (Enumeration e = flists.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Proxy FilterList "+adata.toString());
    }
*/
    for (Enumeration e = allowurl.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Proxy Allow URL "+adata.toString());
    }

    for (Enumeration e = allowkey.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Proxy Allow Keyword "+adata.toString());
    }

    for (Enumeration e = denyurl.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Proxy Deny URL "+adata.toString());
    }

    for (Enumeration e = denykey.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Proxy Deny Keyword "+adata.toString());
    }

    for (Enumeration e = timezones.children() ; e.hasMoreElements() ;) {
      gdata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Proxy TimeGroup "+gdata.toString().replaceAll(" ","_"));
      for (Enumeration e1 = gdata.children() ; e1.hasMoreElements() ;) {
        adata=(DefaultMutableTreeNode)e1.nextElement();
        ptime=(TimeSpace)adata.getUserObject();
        Output=AddConfL("Proxy TimeGroup "+gdata.toString().replaceAll(" ","_")+" "+ptime.TStart+" "+ptime.TEnd+" "+ptime.Days);
      }
    }

    for (Enumeration e = filtergrps.children() ; e.hasMoreElements() ;) { 
      gdata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Proxy SourceGroup "+gdata.toString().replaceAll(" ","_"));
      for (Enumeration e1 = gdata.children() ; e1.hasMoreElements() ;) {
        adata=(DefaultMutableTreeNode)e1.nextElement();
        fmem=(FilterMember)adata.getUserObject();
        Output=AddConfL("Proxy SourceGroup "+gdata.toString().replaceAll(" ","_")+" "+fmem.IPAddr+" "+fmem.Netmask); 
      }
    }


    for (Enumeration e = afilter.children() ; e.hasMoreElements() ;) {
      gdata=(DefaultMutableTreeNode)e.nextElement();
      facl=(FilterACL)gdata.getUserObject();

      if (facl.Default == "Deny All Other Sites") {
        defaulta="none";
      } else if (facl.Default == "Accept All Other Sites") {
        defaulta="all";
      } else {
        defaulta="";
      }

      for (Enumeration e1 = gdata.children() ; e1.hasMoreElements() ;) {
        adata=(DefaultMutableTreeNode)e1.nextElement();
        fctrl=(FilterCTRL)adata.getUserObject();

        if (fctrl.List == "Local Allowed") {
          fclist="local_allow";
        }else if (fctrl.List == "Local Denied") {
          fclist="local_deny";
        }else if (fctrl.List == "Non Resolved Sites") {
          fclist="in-addr";
        } else {
          fclist=fctrl.List;
        }
        Output=AddConfL("Proxy ACL "+facl.Group.replaceAll(" ","_")+" "+facl.TimeSpace.replaceAll(" ","_")+
                        " "+facl.Accept+" "+fclist.replaceAll(" ","_")+" "+fctrl.Accept+" "+defaulta);
      }
    }

    for (Enumeration e = redirsite.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      rsite=(RedirectedSite)adata.getUserObject();
      Output=AddConfL("WWW Redirect "+rsite.Domain+" "+rsite.IPAddr+" "+rsite.IName);
    }

    return Output;
  }

  public String AddConfL(String newconf){
    String newline = System.getProperty("line.separator");
    String confout=Output+newconf+newline;
    return confout;
  }
  public void delConfig() {

    squidconf.delConfig();

    timezones.removeAllChildren();
    treeModel.reload(timezones);

    ipaccess.removeAllChildren();
    treeModel.reload(ipaccess);

    ipbypass.removeAllChildren();
    treeModel.reload(ipbypass);

    afilter.removeAllChildren();
    treeModel.reload(afilter);

    filtergrps.removeAllChildren();
    treeModel.reload(filtergrps);

    flists.removeAllChildren();
    treeModel.reload(flists);

    allowurl.removeAllChildren();
    treeModel.reload(allowurl);

    allowkey.removeAllChildren();
    treeModel.reload(allowkey);

    denykey.removeAllChildren();
    treeModel.reload(denykey);

    denyurl.removeAllChildren();
    treeModel.reload(denyurl);

    redirsite.removeAllChildren();
    treeModel.reload(redirsite);
  }

  public void setDefault() {

    delConfig();
  
    treeModel.insertNodeInto(new DefaultMutableTreeNode(new ProxyBypass("0","0")),ipbypass,ipbypass.getChildCount());


    squidconf.CacheSize="2048";
    squidconf.Redir="15";
    squidconf.fqdnlog=true;
    squidconf.FilterRedirect="squidguard.php?clientaddr=%a&clientname=%n&clientident=%i&clientgroup=%s&destinationgroup=%t&url=%u";
 
    DrawWindow();
  }
  class AddRedirectSite extends Container implements ActionListener {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    JTextField domain,ipaddr;
    JComboBox nseg;
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    RedirectedSite EditReDir;
    JButton adduser;

    public AddRedirectSite(boolean edit){
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
        sortpanel=new ManageNode(redirsite,treeModel,"Select Site To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New WWW Redirect To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit WWW Redirect");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }


      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      domain=new JTextField("",10);
      addLabel(new JLabel("DNS Name Of Hosted Site"),domain,gridbag,layout);

      ipaddr=new JTextField("",10);
      addLabel(new JLabel("IP Address"),ipaddr,gridbag,layout);

      layout.gridwidth=1;
      JLabel ilabel=new JLabel("Select Interface Attached To Site");
      gridbag.setConstraints(ilabel,layout);
      add(ilabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      nseg=new JComboBox(IntList);
      if (IntList.lastIndexOf(dialup) < 0 ) {
        nseg.addItem(dialup);
      }
      gridbag.setConstraints(nseg,layout);
      add(nseg);

      if (isEdit) {
        EditReDir=(RedirectedSite)node.getUserObject();

        domain.setText(EditReDir.Domain);
        ipaddr.setText(EditReDir.IPAddr);
        if (EditReDir.IName.equals("-")) {
          nseg.setSelectedItem(dialup);
        } else {
          nseg.setSelectedItem(getInterface(EditReDir.IName));
        }

        adduser=new JButton("Save Redirect");
      } else {
        adduser=new JButton("Add Redirect");
      }

      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add WWW Redirect");
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
      String connseg;
      if ((domain.getText().length() > 0) & (ipaddr.getText().length() > 0) & (nseg.getSelectedItem() != null)) {
        DefaultMutableTreeNode ifnode=(DefaultMutableTreeNode)nseg.getSelectedItem();
        if (ifnode.toString().equals("Modem")) {
          connseg="-";
        } else {
          IntDef iface=(IntDef)ifnode.getUserObject();
          connseg=iface.IntName;
        }

        if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new RedirectedSite(domain.getText(),
                                                                                         ipaddr.getText(),
                                                                                         connseg));
          treeModel.insertNodeInto(childnode,redirsite,redirsite.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          domain.setText("");
          ipaddr.setText("");
          nseg.setSelectedIndex(0);
        } else {
          EditReDir.Domain=domain.getText();
          EditReDir.IPAddr=ipaddr.getText();
          EditReDir.IName=connseg;          
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
          treeModel.reload(node);
        }
      }
    }
  }

  private DefaultMutableTreeNode  getInterface(String tofind) {
    IntDef intset=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = IntList.elements() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      if (! intdata.toString().equals("Modem")) {
        intset=(IntDef)intdata.getUserObject();
        if (intset.IntName.equals(tofind)) {
          outdata=intdata;
        }
      }
    }
    return outdata;
  }

  private DefaultMutableTreeNode  getInterfaceIP(String tofind) {
    IntDef intset=null;
    DefaultMutableTreeNode intdata;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = IntList.elements() ; e.hasMoreElements() ;) {
      intdata=(DefaultMutableTreeNode)e.nextElement();
      if (! intdata.toString().equals("Modem")) {
        intset=(IntDef)intdata.getUserObject();
        if (intset.IPAddress.equals(tofind)) {
          outdata=intdata;
        }
      }
    }
    return outdata;
  }
  public void setIntList(Vector ul,DefaultMutableTreeNode dod){
    IntList=ul;
    dialup=dod;
  }
}


