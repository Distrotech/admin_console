import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;


public class EmailWin extends Container {
  JSplitPane mainwindow=new JSplitPane();
  JTree userswindow;
  DefaultTreeModel treeModel;
  DefaultMutableTreeNode multidrop;
  EmailConf emailconf=new EmailConf();
  String Output;
  String systype="lite";
  DefaultMutableTreeNode topbranch = new DefaultMutableTreeNode("Email Config");
/*
  DefaultMutableTreeNode sysaliases = new DefaultMutableTreeNode("System Aliases/Lists");
  DefaultMutableTreeNode valiases = new DefaultMutableTreeNode("Virtual Aliases");
*/
  DefaultMutableTreeNode attfilter = new DefaultMutableTreeNode("File Attachment Filtering");

  public EmailWin() {
    setLayout(new BorderLayout());

    multidrop=new DefaultMutableTreeNode("Mail Collection");

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

  public void setSystype(String stypein) {
    DefaultMutableTreeNode adata,ddata;

    systype=stypein;

    topbranch.removeAllChildren();

    if (systype.equals("full")) {
/*
      topbranch.add(sysaliases);
      topbranch.add(valiases);
      topbranch.add(relaynode);
*/
      topbranch.add(attfilter);
      topbranch.add(multidrop);
    } else {
/*
      topbranch.add(sysaliases);
*/
      topbranch.add(multidrop);

/*
      for (Enumeration e = sysaliases.children() ; e.hasMoreElements() ;) {
        adata=(DefaultMutableTreeNode)e.nextElement();
        if (adata.getChildCount() > 0) {
          ddata=(DefaultMutableTreeNode)adata.getFirstLeaf();
          adata.removeAllChildren();
          adata.add(ddata);
        }
      }
      treeModel.reload(sysaliases);
*/
    }
    treeModel.reload(topbranch);
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
    } else if (nodeInfo.toString() == "Email Relaying" ){
      mainwindow.setBottomComponent(new AddRelay());
    } else if (nodeInfo.toString() == "Mail Collection" ){
      mainwindow.setBottomComponent(new AddMultiDrop(false));
    } else if (node.isNodeAncestor(multidrop) ){
      mainwindow.setBottomComponent(new AddMultiDrop(true));
/*
    } else if (nodeInfo.toString() == "System Aliases/Lists"){
      mainwindow.setBottomComponent(new AddAlias());
    } else if (nodeInfo.toString() == "Virtual Aliases") {
      mainwindow.setBottomComponent(new AddVDomain());
*/
    } else if (nodeInfo.toString() == "File Attachment Filtering") {
      mainwindow.setBottomComponent(new AttFilterAdmin(false));
    } else if (node.getParent() == attfilter) {
      mainwindow.setBottomComponent(new AttFilterAdmin(true));
/*
    } else if ((Depth == 3) & (systype.equals("full"))) { 
      if (node.getParent().toString() == "System Aliases/Lists") {
        mainwindow.setBottomComponent(new AddAddress());
      } else if (node.getParent().toString() == "Virtual Aliases") {
        mainwindow.setBottomComponent(new AddVAlias());
      } else {
        mainwindow.setBottomComponent(null);
      }
*/
    } else {
      mainwindow.setBottomComponent(null);
    }
    mainwindow.setDividerLocation(0.3);


  }

  class ConfigWin extends Container implements ActionListener{
    JTextField msgsize,smarthost,mx1,mx2,mredir,mdomain,mdns,scanchild,zipscan,maxscore,minscore,ldaplogin;
    JCheckBox useorbs,usequar,senderalert,allowiframe,allowobject,striphtml,arcmail,bupmail,msbupmail;
    JComboBox dtype,period,fromh,toh,days,scanperiod;
    JPasswordField lpass1,lpass2;
    int psel,fhsel,tohsel;

    public ConfigWin() {
      String dtypea[]={"Background","Queue","Deffered"};
      String spermin[]={"5","6","10","12","15","20","30","60"};

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.weightx=1;
      layout.weighty=0;
 
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Email Server Config");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      if (systype.equals("full")) {
        layout.gridwidth=1;
        JLabel dtlabel=new JLabel("Select Delivery Method");
        gridbag.setConstraints(dtlabel,layout);
        add(dtlabel);

        layout.gridwidth=GridBagConstraints.REMAINDER;
 
        dtype=new JComboBox(dtypea);
        dtype.setSelectedItem(emailconf.Dmethod);
        gridbag.setConstraints(dtype,layout);
        add(dtype);

        mx1=new JTextField(emailconf.MX1,10);
        addLabel(new JLabel("Primary Mail Server"),mx1,gridbag,layout);

        mx2=new JTextField(emailconf.MX2,10);
        addLabel(new JLabel("Secondary Mail Server"),mx2,gridbag,layout);

        smarthost=new JTextField(emailconf.SmartHost,10);
        addLabel(new JLabel("SMTP Gateway"),smarthost,gridbag,layout);

        ldaplogin=new JTextField(emailconf.LDAPUN,10);
        addLabel(new JLabel("LDAP Username (Restricted To Email)"),ldaplogin,gridbag,layout);

        lpass1=new JPasswordField(emailconf.LDAPPW,10);
        addLabel(new JLabel("LDAP Password"),lpass1,gridbag,layout);

        lpass2=new JPasswordField(emailconf.LDAPPW,10);
        addLabel(new JLabel("Confirm Password"),lpass2,gridbag,layout);

        msgsize=new JTextField(emailconf.MaxMsgSize,10);
        addLabel(new JLabel("Max. Message Size (Mb)"),msgsize,gridbag,layout);

        scanchild=new JTextField(emailconf.MSChild,10);
        addLabel(new JLabel("No. Of Scan Children (750-9000 msg/h each)"),scanchild,gridbag,layout);

        zipscan=new JTextField(emailconf.Ziplevel,10);
        addLabel(new JLabel("Depth To Scan Zip Files"),zipscan,gridbag,layout);

        layout.gridwidth=1;
        JLabel plabel=new JLabel("Process AV Mail Queue Every (Seconds)");
        gridbag.setConstraints(plabel,layout);
        add(plabel);

        layout.gridwidth=GridBagConstraints.REMAINDER;

        scanperiod=new JComboBox(spermin);
        scanperiod.setSelectedItem(emailconf.Rescan);
        gridbag.setConstraints(scanperiod,layout);
        add(scanperiod);

        minscore=new JTextField(emailconf.MinScore,10);
        addLabel(new JLabel("Min Score To Tag Spam"),minscore,gridbag,layout);

        maxscore=new JTextField(emailconf.MaxScore,10);
        addLabel(new JLabel("Max Score To Delete Spam"),maxscore,gridbag,layout);

        layout.gridwidth=1;
        layout.fill=GridBagConstraints.NONE;
        useorbs=new JCheckBox("Use RBL Spam Control",emailconf.Orbs);
        gridbag.setConstraints(useorbs,layout);
        add(useorbs);

        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        usequar=new JCheckBox("Quarintine Infections",emailconf.Quarantine);
        gridbag.setConstraints(usequar,layout);
        add(usequar);

        layout.gridwidth=1;
        layout.fill=GridBagConstraints.NONE;
        senderalert=new JCheckBox("Send Sender Alert",emailconf.AlertSender);
        gridbag.setConstraints(senderalert,layout);
        add(senderalert);

        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        allowiframe=new JCheckBox("Allow IFrame Tags",emailconf.IFrame);
        gridbag.setConstraints(allowiframe,layout);
        add(allowiframe);

        layout.gridwidth=1;
        layout.fill=GridBagConstraints.NONE;
        allowobject=new JCheckBox("Allow Object Embeding",emailconf.Object);
        gridbag.setConstraints(allowobject,layout);
        add(allowobject);

        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        striphtml=new JCheckBox("Strip Bad HTML",emailconf.HTML);
        gridbag.setConstraints(striphtml,layout);
        add(striphtml);

        layout.gridwidth=1;
        layout.fill=GridBagConstraints.NONE;
        arcmail=new JCheckBox("Archive Mail",emailconf.Archive);
        gridbag.setConstraints(arcmail,layout);
        add(arcmail);

        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        bupmail=new JCheckBox("Backup Users Mail Boxes",emailconf.Backup);
        gridbag.setConstraints(bupmail,layout);
        add(bupmail);

        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        msbupmail=new JCheckBox("Backup Mailscanner Messages",emailconf.MSBackup);
        gridbag.setConstraints(msbupmail,layout);
        add(msbupmail);
        
      } else {
        mredir=new JTextField(emailconf.Redirect,10);
        addLabel(new JLabel("Mail Server IP Address"),mredir,gridbag,layout);
        mdomain=new JTextField(emailconf.Domain,10);
        addLabel(new JLabel("Email Domain For Local Delivery"),mdomain,gridbag,layout);
        mdns=new JTextField(emailconf.DNS,10);
        addLabel(new JLabel("DNS Server"),mdns,gridbag,layout);

        layout.anchor=GridBagConstraints.NORTH;
        layout.fill=GridBagConstraints.NONE;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        JLabel p3col=new JLabel("Mail Collection Delivery Setup");
        gridbag.setConstraints(p3col,layout);
        add(p3col);

        layout.anchor=GridBagConstraints.NORTHWEST;

        layout.gridwidth=1;
        JLabel plabel=new JLabel("Frequency (Minutes)");
        gridbag.setConstraints(plabel,layout);
        add(plabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        period=new JComboBox();
        psel=2;

        for (int pint=1;pint <= 6;pint++) {
          Integer MinVal=new Integer(60/pint);
          period.addItem(MinVal);
          if (emailconf.FMPer.compareTo(MinVal) == 0) {
            psel=pint-1;
          }
        }
        period.setSelectedIndex(psel);
        gridbag.setConstraints(period,layout);
        add(period);

        layout.gridwidth=1;
        JLabel slabel=new JLabel("From (Hour)");
        gridbag.setConstraints(slabel,layout);
        add(slabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        fromh=new JComboBox();
        for (int fint=0;fint < 24;fint++) {
          Integer SHourVal=new Integer(fint);
          fromh.addItem(SHourVal);
          if (emailconf.FMSHour.compareTo(SHourVal) == 0) {
            fhsel=fint;
          }
        }
        fromh.setSelectedIndex(fhsel);
        gridbag.setConstraints(fromh,layout);
        add(fromh);

        layout.gridwidth=1;
        JLabel elabel=new JLabel("To (Hour)");
        gridbag.setConstraints(elabel,layout);
        add(elabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        toh=new JComboBox();
        for (int tint=23;tint >= 0;tint--) {
          Integer EHourVal=new Integer(tint);
          toh.addItem(EHourVal);
          if (emailconf.FMEHour.compareTo(EHourVal) == 0) {
            tohsel=23-tint;
          }
        }
        toh.setSelectedIndex(tohsel);
        gridbag.setConstraints(toh,layout);
        add(toh);

        layout.gridwidth=1;
        JLabel dlabel=new JLabel("Days To Collect Mail");
        gridbag.setConstraints(dlabel,layout);
        add(dlabel);
        layout.gridwidth=GridBagConstraints.REMAINDER;
        String daylist[]={"Monday To Friday","Monday To Saturday","Everyday"};
        days=new JComboBox(daylist);
        days.setSelectedIndex(emailconf.FMDay.intValue());
        gridbag.setConstraints(days,layout);
        add(days);
      }
  
      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton saveemail=new JButton("Save Settings");
      saveemail.setActionCommand("Save Email");
      saveemail.addActionListener(this);
      gridbag.setConstraints(saveemail,layout);
      add(saveemail);

    }
    private void addLabel(JLabel label,JTextField textfield,GridBagLayout gridbag,GridBagConstraints layout){
      layout.gridwidth=1;
      gridbag.setConstraints(label,layout);
      add(label);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(textfield,layout);
      add(textfield);
    }

    private void addLabel(JLabel label,JPasswordField textfield,GridBagLayout gridbag,GridBagConstraints layout){
      layout.gridwidth=1;
      gridbag.setConstraints(label,layout);
      add(label);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      gridbag.setConstraints(textfield,layout);
      add(textfield);
    }
    public void actionPerformed(ActionEvent event) {
      if (systype.equals("full")) {
        emailconf.Dmethod=dtype.getSelectedItem().toString();
        emailconf.SmartHost=smarthost.getText();

        emailconf.LDAPUN=ldaplogin.getText();
        if (lpass1.getText().equals(lpass2.getText())) {
          emailconf.LDAPPW=lpass1.getText();
        } else {
          lpass1.setText(emailconf.LDAPPW);
          lpass2.setText(emailconf.LDAPPW);
        }
        emailconf.MaxMsgSize=msgsize.getText();
        emailconf.MSChild=scanchild.getText();
        emailconf.Ziplevel=zipscan.getText();
        emailconf.Rescan=scanperiod.getSelectedItem().toString();
        emailconf.MaxScore=maxscore.getText();
        emailconf.MinScore=minscore.getText();

        if (emailconf.Dmethod.equals("Deffered")) {
          emailconf.Orbs=false;
        } else {
          emailconf.Orbs=useorbs.isSelected();
        }
        emailconf.MX1=mx1.getText();
        emailconf.MX2=mx2.getText();

        emailconf.Quarantine=usequar.isSelected();
        emailconf.AlertSender=senderalert.isSelected();
        emailconf.IFrame=allowiframe.isSelected();
        emailconf.Object=allowobject.isSelected();
        emailconf.HTML=striphtml.isSelected();
        emailconf.Archive=arcmail.isSelected();
        emailconf.Backup=bupmail.isSelected();
        emailconf.MSBackup=msbupmail.isSelected();

      } else {
        emailconf.Redirect=mredir.getText();
        emailconf.Domain=mdomain.getText();
        emailconf.DNS=mdns.getText();
        emailconf.FMPer=(Integer)period.getSelectedItem();
        emailconf.FMSHour=(Integer)fromh.getSelectedItem();
        emailconf.FMEHour=(Integer)toh.getSelectedItem();
        emailconf.FMDay=new Integer(days.getSelectedIndex());
      }
    }
  }

  class AddRelay extends Container implements ActionListener {
    JTextField domain;
    JCheckBox acceptd;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public AddRelay(){
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

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      acceptd=new JCheckBox("Allow Relaying");
      gridbag.setConstraints(acceptd,layout);
      acceptd.setSelected(true);
      add(acceptd);

      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JButton adduser=new JButton("Add Domain");
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
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new EmailRelay(domain.getText(),acceptd.isSelected()));
        treeModel.insertNodeInto(childnode,node,node.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);
        domain.setText("");
        acceptd.setSelected(true);
      }
    }
  }
  class AddMultiDrop extends Container implements ActionListener {
    JTextField server,username,envelope,localinfo,smtpserver;
    JCheckBox usessl,useimap; 
    JPasswordField pass1,pass2;
    JButton adduser;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    POP3Collect EditPOP3;
    JLabel textlabel;
    JComboBox ptype,localtype;
    boolean isEdit;

    public AddMultiDrop(boolean edit){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      isEdit=edit;

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(node,treeModel,"Select Mail Collector To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New Mail Collector");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Mail Collector");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      server=new JTextField("",10);
      addLabel(new JLabel("Server Name"),server,gridbag,layout);
      username=new JTextField("",10);
      addLabel(new JLabel("POP3 Server User Name"),username,gridbag,layout);

      pass1=new JPasswordField("",10);
      addLabel(new JLabel("POP3 Server Password"),pass1,gridbag,layout);
      pass2=new JPasswordField("",10);
      addLabel(new JLabel("Confirm Password"),pass2,gridbag,layout);

      envelope=new JTextField("",10);
      addLabel(new JLabel("POP3 Envelope"),envelope,gridbag,layout);

      localinfo=new JTextField("",10);
      addLabel(new JLabel("Username/Domain(s) To Collect For"),localinfo,gridbag,layout);

      smtpserver=new JTextField("",10);
      addLabel(new JLabel("SMTP Server To Deliver To"),smtpserver,gridbag,layout);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      layout.gridwidth=1;
      JLabel dtypelabel=new JLabel("Type Of Collection");
      gridbag.setConstraints(dtypelabel,layout);
      add(dtypelabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      String dtypes[]={"Multidrop","Single Drop"};
      localtype=new JComboBox(dtypes);
      gridbag.setConstraints(localtype,layout);
      add(localtype);

      layout.gridwidth=1;
      JLabel ctypelabel=new JLabel("Protocol To Use");
      gridbag.setConstraints(ctypelabel,layout);
      add(ctypelabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String protocont[]={"pop3","imap","etrn","odmr"};
      ptype=new JComboBox(protocont);
      gridbag.setConstraints(ptype,layout);
      add(ptype);

      usessl=new JCheckBox("Use SSL Encryption",false);

      if (systype.equals("full")) {
        layout.gridwidth=GridBagConstraints.REMAINDER;
        layout.fill=GridBagConstraints.NONE;
        gridbag.setConstraints(usessl,layout);
        add(usessl);
      }
            
      if (isEdit) {
        EditPOP3=(POP3Collect)node.getUserObject();
        server.setText(EditPOP3.Server);
        username.setText(EditPOP3.UserName);
        pass1.setText(EditPOP3.Password);
        pass2.setText(EditPOP3.Password);
        envelope.setText(EditPOP3.Envelope);
        smtpserver.setText(EditPOP3.SMTP);
        localinfo.setText(EditPOP3.Domain);
        if (systype.equals("full")) {
          usessl.setSelected(EditPOP3.UseSSL);
        } else {
          usessl.setSelected(false);
        }
        ptype.setSelectedItem(EditPOP3.MProto);
        if (EditPOP3.Multidrop) {
          localtype.setSelectedIndex(0);
        } else {
          localtype.setSelectedIndex(1);
        }
        adduser=new JButton("Save Collector");
      } else {
        adduser=new JButton("Add Collector");
      }

      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
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
      boolean mdrop=localtype.getSelectedIndex() == 0;
      if ((server.getText().length() > 0) & (((username.getText().length() >0) &
          (pass1.getText().length() >0) & (pass1.getText().equals(pass2.getText()))) | 
          ((ptype.getSelectedItem().toString().equals("etrn")) & (localinfo.getText().length() >0)))) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new POP3Collect(server.getText(),
                                                                                      username.getText(),
                                                                                      pass1.getText(),
                                                                                      envelope.getText(),
                                                                                      localinfo.getText(),
                                                                                      mdrop,
                                                                                      usessl.isSelected(),
                                                                                      ptype.getSelectedItem().toString(),
		    								      smtpserver.getText()));
          treeModel.insertNodeInto(childnode,node,node.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          server.setText("");
          username.setText("");
          pass1.setText("");
          pass2.setText("");
          envelope.setText("");
          localinfo.setText("");
          smtpserver.setText("");
          ptype.setSelectedIndex(0);
          localtype.setSelectedIndex(0);
        } else {
          EditPOP3.Server=server.getText();
          EditPOP3.UserName=username.getText();
          if (pass1.getText().equals(pass2.getText())) {
            EditPOP3.Password=pass1.getText();
          } else {
            pass1.setText(EditPOP3.Password);
            pass2.setText(EditPOP3.Password);
          }
          EditPOP3.Envelope=envelope.getText();
          EditPOP3.SMTP=smtpserver.getText();
          EditPOP3.UseSSL=usessl.isSelected();
          EditPOP3.MProto=ptype.getSelectedItem().toString();
          EditPOP3.Domain=localinfo.getText();
          EditPOP3.Multidrop=mdrop;
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      } else if (isEdit) {
        server.setText(EditPOP3.Server);
        username.setText(EditPOP3.UserName);
        pass1.setText(EditPOP3.Password);
        pass2.setText(EditPOP3.Password);
        envelope.setText(EditPOP3.Envelope);
        smtpserver.setText(EditPOP3.SMTP);
        localinfo.setText(EditPOP3.Domain);
        localinfo.setText("");
        if (systype.equals("full")) {
          usessl.setSelected(EditPOP3.UseSSL);
        } else {
          usessl.setSelected(false);
        }
        ptype.setSelectedItem(EditPOP3.MProto);
        if (EditPOP3.Multidrop) {
          localtype.setSelectedIndex(0);
        } else {
          localtype.setSelectedIndex(1);
        }
      }
    }
  }

/*
  class AddAlias extends Container implements ActionListener {
    JTextField alias,address;
    ManageNode sortpanel;

    public AddAlias(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(sysaliases,treeModel,"Select Alias To Manage",true);      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);
      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Enter Details Of New Email Alias");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      alias=new JTextField(null,10);
      addLabel(new JLabel("Alias"),alias,gridbag,layout);

      address=new JTextField("",10);
      addLabel(new JLabel("Initial Entry"),address,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Alias");
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
      DefaultMutableTreeNode childnode;
      if ((alias.getText().length() > 0) & (address.getText().length() > 0)) {

        childnode=getAliasNode(sysaliases,alias.getText());
        if (childnode == null) {
          childnode=new DefaultMutableTreeNode(alias.getText());
          treeModel.insertNodeInto(childnode,sysaliases,sysaliases.getChildCount());
          sortpanel.listdata.addElement(childnode);
        } 

        DefaultMutableTreeNode aliasnode=new DefaultMutableTreeNode(address.getText());
        treeModel.insertNodeInto(aliasnode,childnode,childnode.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(aliasnode.getPath()));

        alias.setText("");
        address.setText("");
      }
    }
  }

  class AddVDomain extends Container implements ActionListener {
    JTextField domain;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public AddVDomain(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Virtual Domain To Manage",false);      
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

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Domain");

      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Virtual Domain User");
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
*/
  class AttFilterAdmin extends Container implements ActionListener {
    JTextField regex,logtext,usertext;
    JComboBox ruletype;
    ManageNode sortpanel;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    AttachRule EditRule;

    public AttFilterAdmin(boolean edit){
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
        sortpanel=new ManageNode(attfilter,treeModel,"Select Filter Rule To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Attachment Filter Rule To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Edit Filter Rule");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      layout.gridwidth=1;
      JLabel ctypelabel=new JLabel("Type Of Filter To Add");
      gridbag.setConstraints(ctypelabel,layout);
      add(ctypelabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String filtercont[]={"allow","deny"};
      ruletype=new JComboBox(filtercont);
      gridbag.setConstraints(ruletype,layout);
      add(ruletype);

      regex=new JTextField("",10);
      addLabel(new JLabel("Regular Expresion (Rule)"),regex,gridbag,layout);

      usertext=new JTextField("",10);
      addLabel(new JLabel("Message To Appear Users Mailbox"),usertext,gridbag,layout);

      logtext=new JTextField("",10);
      addLabel(new JLabel("Message To Appear In Log File"),logtext,gridbag,layout);

      if (isEdit) {
        EditRule=(AttachRule)node.getUserObject();

        ruletype.setSelectedItem(EditRule.AFType); 
        regex.setText(EditRule.Regex);
        usertext.setText(EditRule.UserText);
        logtext.setText(EditRule.LogText);

        adduser=new JButton("Save Attachment Filter");
      } else {
        adduser=new JButton("Add Attachment Filter");
      }


      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;

      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Attachment Filter");
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
      String filterrule=ruletype.getSelectedItem().toString();
      if (filterrule.equals("allow")) {
        logtext.setText("-");
        usertext.setText("-");
      }
      if ((regex.getText().length() > 0) & (logtext.getText().length() > 0) & (usertext.getText().length() > 0)){
        if (! isEdit) {
          DefaultMutableTreeNode childnode=AddFilterRule(filterrule,regex.getText(),logtext.getText(),usertext.getText());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);
          regex.setText("");
          logtext.setText("");
          usertext.setText("");
          ruletype.setSelectedIndex(0);
        } else {
          EditRule.AFType=filterrule;
          EditRule.Regex=regex.getText(); 
          EditRule.UserText=usertext.getText(); 
          EditRule.LogText=logtext.getText(); 

          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  public DefaultMutableTreeNode AddFilterRule(String rtype,String regin,String ltext,String utext) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new AttachRule(rtype,
                                                                               regin,
                                                                               ltext,
                                                                               utext));
    treeModel.insertNodeInto(childnode,attfilter,attfilter.getChildCount());
    return childnode;
  }


  class AddVAlias extends Container implements ActionListener {
    JTextField alias,address;
    DefaultMutableTreeNode domain = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public AddVAlias(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(domain,treeModel,"Select Alias To Manage",false);      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);
      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Enter Details Of New Email Alias");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      alias=new JTextField(null,10);
      addLabel(new JLabel("Alias"),alias,gridbag,layout);

      address=new JTextField("",10);
      addLabel(new JLabel("Delivery Address"),address,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Alias");
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
      if (address.getText().length() > 0) {
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new VirtualAlias(alias.getText(),address.getText()));
        treeModel.insertNodeInto(childnode,domain,domain.getChildCount());
        sortpanel.listdata.addElement(childnode);
        alias.setText("");
        address.setText("");
      }
    }
  }

  class AddAddress extends Container implements ActionListener {
    JTextField address;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;

    public AddAddress(){
      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      sortpanel=new ManageNode(node,treeModel,"Select Address To Manage",true);      
      gridbag.setConstraints(sortpanel,layout);
      add(sortpanel);

      layout.weighty=0;

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("Enter New Address To Add To Alias");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      address=new JTextField("",10);
      addLabel(new JLabel("Additional Entry"),address,gridbag,layout);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
      JButton adduser=new JButton("Add Address");

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
      if (address.getText().length() > 0){
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(address.getText());
        treeModel.insertNodeInto(childnode,node,node.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);
        address.setText("");
      }
    }
  }
  public String getConfig() {
    Output="";
    DefaultMutableTreeNode rdata;
    EmailRelay relay; 
    POP3Collect pop3;
    AttachRule afilter;
    String jobtd="*";

    if (systype.equals("full")) {
      if (emailconf.Dmethod.length() > 0) {
        Output=AddConfL("Email Delivery "+emailconf.Dmethod);
      }
      if (emailconf.SmartHost.length() > 0) {
        Output=AddConfL("Email Smarthost "+emailconf.SmartHost);
      }
      if (emailconf.MX1.length() > 0) {
        Output=AddConfL("Email MailExchange1 "+emailconf.MX1);
      }
      if (emailconf.MX2.length() > 0) {
        Output=AddConfL("Email MailExchange2 "+emailconf.MX2);
      }
      if (emailconf.MaxMsgSize.length() > 0) {
        Output=AddConfL("Email MaxSize "+emailconf.MaxMsgSize);
      }
      if (emailconf.MSChild.length() > 0) {
        Output=AddConfL("Email ScanChildren "+emailconf.MSChild);
      }

      if (emailconf.Ziplevel.length() > 0) {
        Output=AddConfL("Email ZipLevel "+emailconf.Ziplevel);
      }

      if (emailconf.Rescan.length() > 0) {
        Output=AddConfL("Email Rescan "+emailconf.Rescan);
      }

      if (emailconf.MaxScore.length() > 0) {
        Output=AddConfL("Email MaxScore "+emailconf.MaxScore);
      } else {
        Output=AddConfL("Email MaxScore 5");
      }

      if (emailconf.MinScore.length() > 0) {
        Output=AddConfL("Email MinScore "+emailconf.MinScore);
      } else {
        Output=AddConfL("Email MinScore 2");
      }

      if (emailconf.LDAPUN.length() > 0) {
        Output=AddConfL("Email LDAP Login "+emailconf.LDAPUN);
      }

      if (emailconf.LDAPPW.length() > 0) {
        Output=AddConfL("Email LDAP Password "+emailconf.LDAPPW);
      }

      Output=AddConfL("Email AntiSpam "+emailconf.Orbs);
      Output=AddConfL("Email Quarantine "+emailconf.Quarantine);
      Output=AddConfL("Email AlertSender "+emailconf.AlertSender);
      Output=AddConfL("Email IFrame "+emailconf.IFrame);
      Output=AddConfL("Email Object "+emailconf.Object);
      Output=AddConfL("Email HTML "+emailconf.HTML);
      Output=AddConfL("Email Archive "+emailconf.Archive);
      Output=AddConfL("Email Backup "+emailconf.Backup);
      Output=AddConfL("Email MSBackup "+emailconf.MSBackup);

      //        usequar,senderalert,allowiframe,allowobject,striphtml;
//        emailconf.Quarantine emailconf.AlertSender emailconf.IFrame emailconf.Object emailconf.HTML

    } else {
      if (emailconf.Redirect.length() > 0) {
        Output=AddConfL("Email Redirect "+emailconf.Redirect);
      }
      if (emailconf.Domain.length() > 0) {
        Output=AddConfL("Email Domain "+emailconf.Domain);
      }

      if (emailconf.DNS.length() > 0) {
        Output=AddConfL("Email DNS "+emailconf.DNS);
      }

      if (emailconf.FMPer.intValue() > 0) {
        Output=AddConfL("Email FMPer "+emailconf.FMPer.toString());
      }

      if (emailconf.FMSHour.intValue() > 0) {
        Output=AddConfL("Email FMSHour "+emailconf.FMSHour.toString());
      }

      if (emailconf.FMEHour.intValue() > 0) {
        Output=AddConfL("Email FMEHour "+emailconf.FMEHour.toString());
      }

      if (emailconf.FMDay != null) {
        Output=AddConfL("Email FMDay "+emailconf.FMDay.toString());
      }

      if (emailconf.FMDay.intValue() == 0) {
        jobtd="mon-fri";
      } else if (emailconf.FMDay.intValue() == 1) {
        jobtd="mon-sat";
      } else if (emailconf.FMDay.intValue() == 2) {
        jobtd="*";
      }

      Output=AddConfL("Cron Fetch_POP3_Mail "+emailconf.FMPer.toString()+" "+emailconf.FMSHour.toString()+" "+
                                             emailconf.FMEHour.toString()+" "+jobtd);
    }

    for (Enumeration e = multidrop.children() ; e.hasMoreElements() ;) {
      rdata=(DefaultMutableTreeNode)e.nextElement();
      pop3=(POP3Collect)rdata.getUserObject();
      if (pop3.Envelope.equals("")) {
        pop3.Envelope="-";
      }
      Output=AddConfL("Email POP3 "+pop3.Server+" "+pop3.UserName+" "+pop3.Password+" "+
                      pop3.Domain.replaceAll(" ",",")+" "+pop3.Multidrop+" "+pop3.Envelope+
                      " "+pop3.UseSSL+" "+pop3.MProto+" "+pop3.SMTP);
    }

    DefaultMutableTreeNode adata,ddata;
    VirtualAlias valias;

/*
    for (Enumeration e = sysaliases.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      Output=AddConfL("Alias System "+adata.toString());
    }

    for (Enumeration e = sysaliases.children() ; e.hasMoreElements() ;) {
      adata=(DefaultMutableTreeNode)e.nextElement();
      if (systype.equals("full")) {
        for (Enumeration e1 = adata.children() ; e1.hasMoreElements() ;) {
          ddata=(DefaultMutableTreeNode)e1.nextElement();
          Output=AddConfL("Alias System "+adata.toString()+" "+ddata.toString());
        }
      } else if (adata.getChildCount() > 0) {
        ddata=(DefaultMutableTreeNode)adata.getFirstLeaf();
        Output=AddConfL("Alias System "+adata.toString()+" "+ddata.toString());
      }
    }

*/
    if (systype.equals("full")) {
/*
      for (Enumeration e = valiases.children() ; e.hasMoreElements() ;) {
        adata=(DefaultMutableTreeNode)e.nextElement();
        Output=AddConfL("Alias Vdomain "+adata.toString());
        for (Enumeration e1 = adata.children() ; e1.hasMoreElements() ;) {
          ddata=(DefaultMutableTreeNode)e1.nextElement();
          valias=(VirtualAlias)ddata.getUserObject();
          Output=AddConfL("Alias Virtual "+adata.toString()+" "+valias.Address+" "+valias.Alias+" ");
        }
      }

*/
      for (Enumeration e = attfilter.children() ; e.hasMoreElements() ;) {
        adata=(DefaultMutableTreeNode)e.nextElement();
        afilter=(AttachRule)adata.getUserObject();
        Output=AddConfL("Email Filter "+afilter.AFType+" "+afilter.Regex.replaceAll(" ","_")+" "+
                         afilter.UserText.replaceAll(" ","_")+" "+afilter.LogText.replaceAll(" ","_"));
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

/*
    sysaliases.removeAllChildren();
    treeModel.reload(sysaliases);

    valiases.removeAllChildren();
    treeModel.reload(valiases);
*/
    attfilter.removeAllChildren();
    treeModel.reload(attfilter);

    emailconf.delConfig();

    multidrop.removeAllChildren();
    treeModel.reload(multidrop);

/*
    if (systype.equals("full")) {
      relaynode.removeAllChildren();
      treeModel.reload(relaynode);
    }
*/
  }

  public void setAFDefault() {
    AddFilterRule("allow","\\.jpg$","-","-");
    AddFilterRule("allow","\\.gif$","-","-");
    AddFilterRule("allow","\\.url$","-","-");
    AddFilterRule("allow","\\.vcf$","-","-");
    AddFilterRule("allow","\\.txt$","-","-");
    AddFilterRule("allow","\\.zip$","-","-");
    AddFilterRule("allow","\\.t?gz$","-","-");
    AddFilterRule("allow","\\.bz2$","-","-");
    AddFilterRule("allow","\\.Z$","-","-");
    AddFilterRule("allow","\\.rpm$","-","-");
    AddFilterRule("allow","\\.gpg$","-","-");
    AddFilterRule("allow","\\.pgp$","-","-");
    AddFilterRule("allow","\\.sit$","-","-");
    AddFilterRule("allow","\\.asc$","-","-");
    AddFilterRule("allow","\\.hqx$","-","-");
    AddFilterRule("allow","\\.sit.bin$","-","-");
    AddFilterRule("allow","\\.sea$","-","-");
    AddFilterRule("deny","pretty\\s+park\\.exe$","\"Pretty Park\" virus","\"Pretty Park\" virus");
    AddFilterRule("deny","happy99.exe$","\"Happy\" virus","\"Happy\" virus");
    AddFilterRule("deny",".{150,}","Very long filename, possible OE attack","Very long filenames are good signs of attacks against Microsoft e-mail packages");
    AddFilterRule("deny","\\.ceo$","WinEvar virus attachment","Often used by the WinEvar virus");
    AddFilterRule("deny","\\.com$","Windows/DOS Executable","Executable DOS/Windows programs are dangerous in email");
    AddFilterRule("deny","\\.exe$","Windows/DOS Executable","Executable DOS/Windows programs are dangerous in email");
    AddFilterRule("deny","\\.mhtml$","Possible Eudora meta-refresh attack","MHTML files can be used in an attack against Eudora");
    AddFilterRule("deny","\\.reg$","Possible Windows registry attack","Windows registry entries are very dangerous in email");
    AddFilterRule("deny","\\.chm$","Possible compiled Help file-based virus","Compiled help files are very dangerous in email");
    AddFilterRule("deny","\\.cnf$","Possible SpeedDial attack","SpeedDials are very dangerous in email");
    AddFilterRule("deny","\\.hta$","Possible Microsoft HTML archive attack","HTML archives are very dangerous in email");
    AddFilterRule("deny","\\.ins$","Possible Microsoft Internet Comm. Settings attack","Windows Internet Settings are dangerous in email");
    AddFilterRule("deny","\\.jse?$","Possible Microsoft JScript attack","JScript Scripts are dangerous in email");
    AddFilterRule("deny","\\.lnk$","Possible Eudora *.lnk security hole attack","Eudora *.lnk security hole attack");
    AddFilterRule("deny","\\.ma[dfgmqrstvw]$","Possible Microsoft Access Shortcut attack","Microsoft Access Shortcuts are dangerous in email");
    AddFilterRule("deny","\\.pif$","Possible MS-Dos program shortcut attack","Shortcuts to MS-Dos programs are very dangerous in email");
    AddFilterRule("deny","\\.scf$","Possible Windows Explorer Command attack","Windows Explorer Commands are dangerous in email");
    AddFilterRule("deny","\\.sct$","Possible Microsoft Windows Script Component attack","Windows Script Components are dangerous in email");
    AddFilterRule("deny","\\.shb$","Possible document shortcut attack","Shortcuts Into Documents are very dangerous in email");
    AddFilterRule("deny","\\.shs$","Possible Shell Scrap Object attack","Shell Scrap Objects are very dangerous in email");
    AddFilterRule("deny","\\.vb[es]$","Possible Microsoft Visual Basic script attack","Visual Basic Scripts are dangerous in email");
    AddFilterRule("deny","\\.ws[cfh]$","Possible Microsoft Windows Script Host attack","Windows Script Host files are dangerous in email");
    AddFilterRule("deny","\\.xnk$","Possible Microsoft Exchange Shortcut attack","Microsoft Exchange Shortcuts are dangerous in email");
    AddFilterRule("deny","\\.scr$","Possible virus hidden in a screensaver","Windows Screensavers often hide viruses in email");
    AddFilterRule("deny","\\.bat$","Possible malicious batch file script","Batch files are often mailicious");
    AddFilterRule("deny","\\.cmd$","Possible malicious batch file script","Batch files are often mailicious");
    AddFilterRule("deny","\\.cpl$","Possible malicious control panel item","Control panel items often hide viruses in email");
    AddFilterRule("deny","\\{[a-hA-H0-9-]{25,}\\}$","Filename trying to hide it's real extension","Files ending in CLSID's are trying to hide their real extension");
    AddFilterRule("deny","\\s{10,}","Filename contains lots of white space","A long gap in a name is often used to hide part of it");
    AddFilterRule("allow","(\\.[a-z0-9]{3})\\1$","-","-");
    AddFilterRule("deny","\\.[a-z][a-z0-9]{2,3}\\.[a-z0-9]{3}$","Found possible filename hiding","Attempt to hide real filename extension");
  }

  public void setDefault() {
    delConfig();

    setAFDefault();
  
    if (systype.equals("full")) {
      emailconf.Dmethod="Deffered";
      emailconf.MaxMsgSize="10";
      emailconf.Orbs=true;
    }

/*
    addDefAlias("bin","root");
    addDefAlias("daemon","root");
    addDefAlias("MAILER-DAEMON","root");
    addDefAlias("manager","root");
    addDefAlias("nobody","root");
    addDefAlias("operator","root");
    addDefAlias("postmaster","root");
    addDefAlias("system","root");
    addDefAlias("toor","root");
*/

    DrawWindow();
  }




/*
  private void addDefAlias(String Alias,String Entry) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(Alias);
    treeModel.insertNodeInto(childnode,sysaliases,sysaliases.getChildCount());
    DefaultMutableTreeNode aliasnode=new DefaultMutableTreeNode(Entry);
    treeModel.insertNodeInto(aliasnode,childnode,childnode.getChildCount());
  }

  public DefaultMutableTreeNode getAliasNode(DefaultMutableTreeNode atype,String tofind){
    DefaultMutableTreeNode indata=null;
    DefaultMutableTreeNode outdata=null;

    for (Enumeration e = atype.children() ; e.hasMoreElements() ;) {
      indata=(DefaultMutableTreeNode)e.nextElement();
      if (indata.toString().equals(tofind)) {
        outdata=indata;
      }
    }
    return outdata;
  }
*/
}

class AttachRule {
  String AFType,Regex,LogText,UserText;
  public AttachRule(String rtype,String regin,String ltext,String utext) {
    AFType=rtype;
    Regex=regin;
    LogText=ltext;
    UserText=utext;
  }
  public String toString(){
    String Output="";
    if (AFType.equals("allow")) {
      Output=AFType+" "+Regex;
    } else {
      Output=AFType+" "+Regex+" "+UserText+" ["+LogText+"]";
    }
    return Output;
  }
}
