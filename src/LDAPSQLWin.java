import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;


public class LDAPSQLWin extends Container {
  JSplitPane mainwindow=new JSplitPane();
  JTree userswindow;
  DefaultTreeModel treeModel;
  String Output;
  String sqlpass="admin";
  String hordepass="admin";
  String ulogdpass="admin";
  String radiuspass="radius";
  String radiusserver="localhost";
  String sqlcontpass="control";
  String sqlforumpass="forum";
  String pgadmin="pgadmin";
  String pgexchange="exchange";
  String cubitpass="i56kfm";
  String asteriskpass="asterisk";
  String asteriskserver="localhost";
  String asteriskmpass="asterisk";
  String asteriskmserver="localhost";
  String repdn="";
  String sqlserv="localhost";
  String voipsecret="asterisk";
  String voiphostname="";
  boolean ldapbu=false;
  boolean sqlbu=false;
  boolean voipbu=false;
  boolean guestaccess=true;
  JTextField sql,horde,opsecret,ophostname,ulogd,sqlcont,sqlcrm,sqlcubit,sqlastmserv,sqlmasterisk,sqlastserv,radserv,sqlasterisk,sqlchat,moddn,pgadmintb,exchangetb,sqlserver,radpass;
  JCheckBox gaccess,ldapbup,mysqlbup,voipbup;
  DefaultMutableTreeNode topbranch,abooks,replica;
  LDAPReplicate EditRep=new LDAPReplicate();

  public LDAPSQLWin() {
    setLayout(new BorderLayout());

    topbranch = new DefaultMutableTreeNode("LDAP/SQL Config");
    abooks = new DefaultMutableTreeNode("Address Books");
    replica = new DefaultMutableTreeNode("Replication");

    topbranch.add(abooks);
    topbranch.add(replica);

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
    } else if (node == abooks) {
      mainwindow.setBottomComponent(new AddBook(false));
    } else if (node.isNodeAncestor(abooks)) {
      mainwindow.setBottomComponent(new AddBook(true));
    } else if (node == replica) {
      mainwindow.setBottomComponent(new AddReplica(true));
    } else if (node.getParent() == replica){
      mainwindow.setBottomComponent(new AddReplica(true));
    } else {
      mainwindow.setBottomComponent(null);
    }
    mainwindow.setDividerLocation(0.3);

    if (abooks.getChildCount() <= 0) {
      treeModel.insertNodeInto(new DefaultMutableTreeNode(new LDAPpath("Addressbook",true)),
                               abooks,abooks.getChildCount());
    }
  }

  class ConfigWin extends Container implements ActionListener{
    public ConfigWin() {

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.weightx=1;
      layout.weighty=0;
 
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Lightweight Directory Configuration");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      moddn=new JTextField(repdn,10);
      addLabel(new JLabel("List Of Slave RID's (Comma Seperated)"),moddn,gridbag,layout);

      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;
      gaccess=new JCheckBox("Allow All Users Default Read Access",guestaccess);
      gridbag.setConstraints(gaccess,layout);
      add(gaccess);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      ldapbup=new JCheckBox("Backup LDAP Database",ldapbu);
      gridbag.setConstraints(ldapbup,layout);
      add(ldapbup);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel sqllabel=new JLabel("MySQL Server Configuration");
      gridbag.setConstraints(sqllabel,layout);
      add(sqllabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      sql=new JTextField(sqlpass,10);
      addLabel(new JLabel("Adminsistrator Password"),sql,gridbag,layout);

      horde=new JTextField(hordepass,10);
      addLabel(new JLabel("Webmail Adminsistrator Password"),horde,gridbag,layout);

      ulogd=new JTextField(ulogdpass,10);
      addLabel(new JLabel("Intrusion Detection Database Password"),ulogd,gridbag,layout);

      sqlcont=new JTextField(sqlcontpass,10);
      addLabel(new JLabel("Control User Password For Web Admin"),sqlcont,gridbag,layout);

      sqlchat=new JTextField(sqlforumpass,10);
      addLabel(new JLabel("Password Used For Web Forum"),sqlchat,gridbag,layout);

      sqlserver=new JTextField(sqlserv,10);
      addLabel(new JLabel("SQL Server For Webmail"),sqlserver,gridbag,layout);


      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      mysqlbup=new JCheckBox("Backup SQL Database",sqlbu);
      gridbag.setConstraints(mysqlbup,layout);
      add(mysqlbup);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel pgsqllabel=new JLabel("PostgreSQL Server Configuration");
      gridbag.setConstraints(pgsqllabel,layout);
      add(pgsqllabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      pgadmintb=new JTextField(pgadmin,10);
      addLabel(new JLabel("PostgreSQL Admin Password"),pgadmintb,gridbag,layout);

      exchangetb=new JTextField(pgexchange,10);
      addLabel(new JLabel("Password For Exchange 4 Linux DB"),exchangetb,gridbag,layout);

      sqlcubit=new JTextField(cubitpass,10);
      addLabel(new JLabel("Password Used For Cubit Accounting"),sqlcubit,gridbag,layout);

      sqlastserv=new JTextField(asteriskserver,10);
      addLabel(new JLabel("SQL Server Used For Asterisk PBX"),sqlastserv,gridbag,layout);

      sqlasterisk=new JTextField(asteriskpass,10);
      addLabel(new JLabel("Password Used For Asterisk PBX"),sqlasterisk,gridbag,layout);

      sqlastmserv=new JTextField(asteriskmserver,10);
      addLabel(new JLabel("SQL Server Used For Master Asterisk PBX"),sqlastmserv,gridbag,layout);

      sqlmasterisk=new JTextField(asteriskmpass,10);
      addLabel(new JLabel("Password Used For Master Asterisk PBX"),sqlmasterisk,gridbag,layout);

      radserv=new JTextField(radiusserver,10);
      addLabel(new JLabel("SQL Server For Radius"),radserv,gridbag,layout);

      radpass=new JTextField(radiuspass,10);
      addLabel(new JLabel("Password For Radius Accounting"),radpass,gridbag,layout);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel voiplabel=new JLabel("Asterisk Operator Panel Config");
      gridbag.setConstraints(voiplabel,layout);
      add(voiplabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      opsecret=new JTextField(voipsecret,10);
      addLabel(new JLabel("Reception Pannel Secret"),opsecret,gridbag,layout);

      ophostname=new JTextField(voiphostname,10);
      addLabel(new JLabel("Reception Pannel Hostname"),ophostname,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      voipbup=new JCheckBox("Backup VOIP DB/Custom Sounds/Voicemail",voipbu);
      gridbag.setConstraints(voipbup,layout);
      add(voipbup);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton saveldap=new JButton("Save Settings");
      saveldap.setActionCommand("Save LDAP");
      saveldap.addActionListener(this);
      gridbag.setConstraints(saveldap,layout);
      add(saveldap);

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
      guestaccess=gaccess.isSelected();
      sqlpass=sql.getText();
      hordepass=horde.getText();
      ulogdpass=ulogd.getText();
      radiuspass=radpass.getText();
      radiusserver=radserv.getText();
      sqlserv=sqlserver.getText();
      sqlcontpass=sqlcont.getText();
      sqlforumpass=sqlchat.getText();
      cubitpass=sqlcubit.getText();
      asteriskpass=sqlasterisk.getText();
      asteriskserver=sqlastserv.getText();
      asteriskmpass=sqlmasterisk.getText();
      asteriskmserver=sqlastmserv.getText();
      repdn=moddn.getText();
      ldapbu=ldapbup.isSelected();
      sqlbu=mysqlbup.isSelected();
      pgadmin=pgadmintb.getText();
      pgexchange=exchangetb.getText();
      voipsecret=opsecret.getText();
      voiphostname=ophostname.getText();
      voipbu=voipbup.isSelected();	
    }
  }

  class AddBook extends Container implements ActionListener {
    JTextField book;
    JCheckBox anon;
    ManageNode sortpanel;
    boolean isEdit;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();    
    JLabel textlabel;
    LDAPpath EditBook;
    JButton adduser;

    public AddBook(boolean edit){
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
        sortpanel=new ManageNode(abooks,treeModel,"Select Address Book To Manage",false);
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Name Of New Address Book");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Address Book");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      book=new JTextField(null,10);
      addLabel(new JLabel("Address Book"),book,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      anon=new JCheckBox("Allow All Users Read Access To This Addressbook",true);
      gridbag.setConstraints(anon,layout);
      add(anon);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      if (isEdit) {
        EditBook=(LDAPpath)node.getUserObject();
        anon.setSelected(EditBook.Anonaccess);
        book.setText(EditBook.Pathname);
        adduser=new JButton("Save Address Book");
      } else {
        adduser=new JButton("Add Address Book");
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
      if ((book.getText().length() > 0) & (! book.getText().equals("addressbooks"))) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new LDAPpath(book.getText(),anon.isSelected()));
          treeModel.insertNodeInto(childnode,abooks,abooks.getChildCount());
          sortpanel.listdata.addElement(childnode);
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          book.setText("");
          anon.setSelected(true);
        } else {
          EditBook.Anonaccess=anon.isSelected();
          EditBook.Pathname=book.getText();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  class AddReplica extends Container implements ActionListener {
    JTextField host,bdn,rid;
    JPasswordField pass1,pass2;
    JCheckBox usessl;
    ManageNode sortpanel;
    JLabel textlabel;
    JButton adduser;
    boolean isEdit;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();

    public AddReplica(boolean Edit){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      isEdit=Edit;

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(replica,treeModel,"Select Replication Server To Manage",false);
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Adding Server To Replicate To");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Master Server Details");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      host=new JTextField(null,10);
      addLabel(new JLabel("Hostname"),host,gridbag,layout);

      rid=new JTextField(null,10);
      addLabel(new JLabel("Session ID"),rid,gridbag,layout);
/*
      bdn=new JTextField(null,10);
      addLabel(new JLabel("Bind DN (Admin Access)"),bdn,gridbag,layout);

      pass1=new JPasswordField(null,10);
      addLabel(new JLabel("Bind Password"),pass1,gridbag,layout);

      pass2=new JPasswordField(null,10);
      addLabel(new JLabel("Confirm Password"),pass2,gridbag,layout);
*/
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      usessl=new JCheckBox("Use SSL When Replicateing",true);
      gridbag.setConstraints(usessl,layout);
      add(usessl);

      if (isEdit) {
//        EditRep=(LDAPReplicate)node.getUserObject();
        host.setText(EditRep.Host);
        rid.setText(EditRep.RID);
/*
        bdn.setText(EditRep.BindDN);
        pass1.setText(EditRep.Password);
        pass2.setText(EditRep.Password);
*/
        usessl.setSelected(EditRep.UseSSL);

        adduser=new JButton("Save Replication");
      } else {
        adduser=new JButton("Add Replication");
      }

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.weighty=1;

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Replication");
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
//      if (host.getText().length() > 0) {
//          (pass1.getText().length() > 0) & (pass1.getText().equals(pass2.getText()))) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new LDAPReplicate(host.getText(),rid.getText(),
                                                                                        usessl.isSelected()));
          treeModel.insertNodeInto(childnode,replica,replica.getChildCount());
          sortpanel.listdata.addElement(childnode);
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          host.setText("");
          rid.setText("");
          pass1.setText("");
          pass2.setText("");
          usessl.setSelected(true);
        } else {
          EditRep.Host=host.getText();
          EditRep.RID=rid.getText();
          EditRep.UseSSL=usessl.isSelected();
/*
          EditRep.BindDN=bdn.getText();
          if (pass1.getText().equals(pass2.getText())) {
            EditRep.Password=pass1.getText();
          } else {
            pass1.setText(EditRep.Password);
            pass2.setText(EditRep.Password);
          }
*/

          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
/*
      } else if (isEdit) {
        host.setText(EditRep.Host);
        bdn.setText(EditRep.BindDN);
        pass1.setText(EditRep.Password);
        pass2.setText(EditRep.Password);
        usessl.setSelected(EditRep.UseSSL);
      }
*/
    }
  }

  public String getConfig() {
    DefaultMutableTreeNode adata;
    LDAPpath pdata;
    LDAPReplicate rdata;

    Output="";
    

    Output=AddConfL("LDAP AnonRead "+guestaccess);
    Output=AddConfL("LDAP Backup "+ldapbu);
    Output=AddConfL("LDAP ReplicateDN "+repdn);

    for (Enumeration e = abooks.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      pdata=(LDAPpath)adata.getUserObject();
      Output=AddConfL("LDAP Addressbook "+pdata.Pathname+" "+pdata.Anonaccess);
    }

    for (Enumeration e = replica.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      rdata=(LDAPReplicate)adata.getUserObject();
      Output=AddConfL("LDAP Replica "+rdata.Host+" "+rdata.RID+" "+rdata.UseSSL);
    }

    if (EditRep.Host.length() > 0) {
      Output=AddConfL("LDAP Replica "+EditRep.Host+" "+EditRep.RID+" "+EditRep.UseSSL);
    }

    if (sqlpass.length() > 0) {
      Output=AddConfL("SQL Password "+sqlpass);
    }
    if (hordepass.length() > 0) {
      Output=AddConfL("SQL WebmailPass "+hordepass);
    }
    if (ulogdpass.length() > 0) {
      Output=AddConfL("SQL IDPass "+ulogdpass);
    }
    if (radiuspass.length() > 0) {
      Output=AddConfL("SQL Radius "+radiuspass);
    }
    if (radiusserver.length() > 0) {
      Output=AddConfL("SQL RadiusServ "+radiusserver);
    }
    if (sqlserv.length() > 0) {
      Output=AddConfL("SQL Server "+sqlserv);
    }
    if (sqlcontpass.length() > 0) {
      Output=AddConfL("SQL Control "+sqlcontpass);
    }

    if (sqlforumpass.length() > 0) {
      Output=AddConfL("SQL Forum "+sqlforumpass);
    }

    if (cubitpass.length() > 0) {
      Output=AddConfL("SQL Cubit "+cubitpass);
    }

    if (asteriskpass.length() > 0) {
      Output=AddConfL("SQL Asterisk "+asteriskpass);
    }

    if (asteriskserver.length() > 0) {
      Output=AddConfL("SQL AsteriskServ "+asteriskserver);
    }

    if (asteriskmpass.length() > 0) {
      Output=AddConfL("SQL MAsterisk "+asteriskmpass);
    }

    if (asteriskmserver.length() > 0) {
      Output=AddConfL("SQL MAsteriskServ "+asteriskmserver);
    }

    if (pgadmin.length() > 0) {
      Output=AddConfL("SQL PGAdmin "+pgadmin);
    }
    
    if (pgexchange.length() > 0) {
      Output=AddConfL("SQL PGExchange "+pgexchange);
    }
    Output=AddConfL("SQL Backup "+sqlbu);

    if (voipsecret.length() > 0) {
      Output=AddConfL("SQL OpSecret "+voipsecret);
    }

    if (voiphostname.length() > 0) {
      Output=AddConfL("SQL OpHostname "+voiphostname);
    }

    Output=AddConfL("SQL VBackup "+voipbu);



    return Output;
  }

  public String AddConfL(String newconf){
    String newline = System.getProperty("line.separator");
    String confout=Output+newconf+newline;
    return confout;
  }

  public void delConfig() {
    sqlpass="admin";
    hordepass="admin";
    ulogdpass="admin";
    radiuspass="radius";
    radiusserver="localhost";
    sqlserv="localhost";
    sqlcontpass="control";
    sqlforumpass="forum";
    cubitpass="i56kfm";
    asteriskpass="asterisk";
    asteriskserver="localhost";
    asteriskmpass="asterisk";
    asteriskmserver="localhost";
    guestaccess=true;
    ldapbu=false;
    sqlbu=false;
    pgadmin="pgadmin";
    pgexchange="exchange";
    voipsecret="asterisk";
    voiphostname="";
    abooks.removeAllChildren();
    treeModel.reload(abooks);

    replica.removeAllChildren();
    treeModel.reload(replica);
  }

  public void setDefault() {
    delConfig();

    treeModel.insertNodeInto(new DefaultMutableTreeNode(new LDAPpath("Addressbook",true)),abooks,abooks.getChildCount());
    DrawWindow();
  }
  
}

class LDAPpath {
  String Pathname;
  boolean Anonaccess;
  public LDAPpath(String pname,boolean gaccess){
    Pathname=pname;
    Anonaccess=gaccess;
  }
  public String toString(){
    String Atype;
    if (Anonaccess) {
      Atype="Open";
    } else {
      Atype="Closed";
    }
    String Output=Pathname+" "+"("+Atype+")";
    return Output;
  }
}


class LDAPReplicate {
  String Host,BindDN,Password,RID;
  boolean UseSSL;
/*
  public LDAPReplicate(String host,String login,String pass,boolean sslaccess){
    Host=host;
    BindDN=login;
    Password=pass;
    UseSSL=sslaccess;
  }
*/
  public LDAPReplicate(String host,String rid,boolean sslaccess){
    Host=host;
    RID=rid;
    UseSSL=sslaccess;
  }
  public LDAPReplicate(){
    Host="";
    BindDN="";
    RID="";
    Password="";
    UseSSL=true;
  }
  public String toString(){
    String Atype;
    if (UseSSL) {
      Atype="With SSL";
    } else {
      Atype="No SSL";
    }
    String Output=Host+" "+"("+RID+" "+Atype+")";
    return Output;
  }
}

