import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class SambaWin extends Container {
  JSplitPane mainwindow=new JSplitPane();
  JTree userswindow;
  DefaultTreeModel treeModel;
  DefaultMutableTreeNode topbranch,shares,maps;
  SambaConf sambaconf=new SambaConf();
  String Output;
  boolean addshare=false;

  public SambaWin() {
    setLayout(new BorderLayout());

    DefaultMutableTreeNode topbranch = new DefaultMutableTreeNode("File Server Config");
    shares=new DefaultMutableTreeNode("Shared Folders");
    maps=new DefaultMutableTreeNode("Mapped Drives");
    topbranch.add(shares);
    topbranch.add(maps);

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

  public void DrawWindow(){
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    if (node == null) return;
    Object nodeInfo = node.getUserObject();
    int Depth=userswindow.getSelectionPath().getPathCount();
    addshare=false;
    if (Depth == 1){
      mainwindow.setBottomComponent(new ConfigWin());
    } else if (nodeInfo.toString() == "Shared Folders" ){
      addshare=true;
      mainwindow.setBottomComponent(new AddFileShare(false));
    } else if (node.isNodeAncestor(shares) ){
      addshare=true;
      mainwindow.setBottomComponent(new AddFileShare(true));
    } else if (nodeInfo.toString() == "Mapped Drives" ){
      addshare=true;
      mainwindow.setBottomComponent(new AddMapDrive(false));
    } else if (node.isNodeAncestor(maps) ){
      addshare=true;
      mainwindow.setBottomComponent(new AddMapDrive(true));
    } else {
      mainwindow.setBottomComponent(null);
    }
    mainwindow.setDividerLocation(0.3);
  }

  class ConfigWin extends Container implements ActionListener{
    JTextField descrip,workgroup,sname,adsserver,adsrealm,oslevel,rbrowse,avmaxsize,avthread;
    JCheckBox domaincont,domainmast,localmast,pmaster,avshare,avhome,uprofile,buhome,bushare,buwww,buftp;
    JComboBox authtype,homemount,sharemount,maptype;
    
    public ConfigWin() {
      String authtypea[]={"USER","DOMAIN","ADS"};
      String maptypea[]={"None","Both","Groups Only","Users Only"};

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();

      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.weightx=1;
      layout.weighty=0;
 
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel textlabel=new JLabel("File Server Config");
      gridbag.setConstraints(textlabel,layout);
      add(textlabel);

      layout.anchor=GridBagConstraints.NORTHWEST;
      layout.fill=GridBagConstraints.HORIZONTAL;

      workgroup=new JTextField(sambaconf.WorkGroup,10);
      addLabel(new JLabel("Workgroup"),workgroup,gridbag,layout);

      sname=new JTextField(sambaconf.ServerName,10);
      addLabel(new JLabel("Server Aliases"),sname,gridbag,layout);

      descrip=new JTextField(sambaconf.Description,10);
      addLabel(new JLabel("Description"),descrip,gridbag,layout);

      oslevel=new JTextField(sambaconf.OSLevel,10);
      addLabel(new JLabel("OS Level"),oslevel,gridbag,layout);

      rbrowse=new JTextField(sambaconf.RemoteBrowse,10);
      addLabel(new JLabel("Browse Sync"),rbrowse,gridbag,layout);

      layout.gridwidth=1;
      JLabel atlabel=new JLabel("Select Security Level");
      gridbag.setConstraints(atlabel,layout);
      add(atlabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      authtype=new JComboBox(authtypea);
      authtype.setSelectedItem(sambaconf.Authentication);
      gridbag.setConstraints(authtype,layout);
      add(authtype);

      layout.gridwidth=1;
      JLabel ablabel=new JLabel("Mapping Of Domain Users/Groups");
      gridbag.setConstraints(ablabel,layout);
      add(ablabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      maptype=new JComboBox(maptypea);
      maptype.setSelectedItem(sambaconf.Winbind);
      gridbag.setConstraints(maptype,layout);
      add(maptype);

      homemount=new JComboBox();
      sharemount=new JComboBox();
      for(char dletter='G';dletter <= 'W';dletter++) {
        sharemount.addItem(new Character(dletter));
        homemount.addItem(new Character(dletter));
      }

      layout.gridwidth=1;
      JLabel hslabel=new JLabel("Home Directory Mapped To ..");
      gridbag.setConstraints(hslabel,layout);
      add(hslabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      if (sambaconf.HomeDrive != null) {
        homemount.setSelectedItem(sambaconf.HomeDrive);
      } else {
        homemount.setSelectedIndex(1);
      }
      gridbag.setConstraints(homemount,layout);
      add(homemount);

      layout.gridwidth=1;
      JLabel sslabel=new JLabel("Shared Directory Mapped To ..");
      gridbag.setConstraints(sslabel,layout);
      add(sslabel);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      if (sambaconf.ShareDrive != null) {
        sharemount.setSelectedItem(sambaconf.ShareDrive);
      } else {
        sharemount.setSelectedIndex(12);
      }
      gridbag.setConstraints(sharemount,layout);
      add(sharemount);

      adsserver=new JTextField(sambaconf.ADSServer,10);
      addLabel(new JLabel("ADS Server"),adsserver,gridbag,layout);

      adsrealm=new JTextField(sambaconf.ADSRealm,10);
      addLabel(new JLabel("ADS Realm"),adsrealm,gridbag,layout);

      avmaxsize=new JTextField(sambaconf.AVMaxSize,10);
      addLabel(new JLabel("Maximum File Size To Virus Scan (K/M)"),avmaxsize,gridbag,layout);

      avthread=new JTextField(sambaconf.AVMaxThread,10);
      addLabel(new JLabel("Max. No. Of AV Children"),avthread,gridbag,layout);

      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;

      domaincont=new JCheckBox("Domain Controler",sambaconf.DControl);
      gridbag.setConstraints(domaincont,layout);
      add(domaincont);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      domainmast=new JCheckBox("Domain Browse Master",sambaconf.DMaster);
      gridbag.setConstraints(domainmast,layout);
      add(domainmast);

      layout.gridwidth=1;
      localmast=new JCheckBox("Local Browse Master",sambaconf.LMaster);
      gridbag.setConstraints(localmast,layout);
      add(localmast);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      pmaster=new JCheckBox("Prefferd Browse Master",sambaconf.PMaster);
      gridbag.setConstraints(pmaster,layout);
      add(pmaster);

      layout.gridwidth=1;
      avshare=new JCheckBox("Virus Protect Shared Folder",sambaconf.AVShare);
      gridbag.setConstraints(avshare,layout);
      add(avshare);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      avhome=new JCheckBox("Virus Protect Users Folders",sambaconf.AVHome);
      gridbag.setConstraints(avhome,layout);
      add(avhome);

      layout.gridwidth=1;
      bushare=new JCheckBox("Backup Shared Folder",sambaconf.BUShare);
      gridbag.setConstraints(bushare,layout);
      add(bushare);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      buhome=new JCheckBox("Backup Users Folders",sambaconf.BUHome);
      gridbag.setConstraints(buhome,layout);
      add(buhome);

      layout.gridwidth=1;
      buwww=new JCheckBox("Backup Frontpage WWW Sites",sambaconf.BUWWW);
      gridbag.setConstraints(buwww,layout);
      add(buwww);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      buftp=new JCheckBox("Backup Anon. FTP Data",sambaconf.BUFTP);
      gridbag.setConstraints(buftp,layout);
      add(buftp);

      
      layout.gridwidth=GridBagConstraints.REMAINDER;
      uprofile=new JCheckBox("Allow User Profiles",sambaconf.UProfile);
      gridbag.setConstraints(uprofile,layout);
      add(uprofile);

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      layout.anchor=GridBagConstraints.NORTH;
      JButton saveemail=new JButton("Save Settings");
      saveemail.setActionCommand("Save Samba");
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
    public void actionPerformed(ActionEvent event) {
      sambaconf.Authentication=authtype.getSelectedItem().toString();
      sambaconf.Winbind=maptype.getSelectedItem().toString();
      sambaconf.HomeDrive=(Character)homemount.getSelectedItem();
      sambaconf.ShareDrive=(Character)sharemount.getSelectedItem();
      sambaconf.WorkGroup=workgroup.getText();
      sambaconf.ServerName=sname.getText();
      sambaconf.Description=descrip.getText();
      sambaconf.ADSServer=adsserver.getText();
      sambaconf.ADSRealm=adsrealm.getText();
      sambaconf.OSLevel=oslevel.getText();
      sambaconf.RemoteBrowse=rbrowse.getText();
      sambaconf.AVMaxSize=avmaxsize.getText();
      sambaconf.AVMaxThread=avthread.getText();
      sambaconf.DControl=domaincont.isSelected();
      sambaconf.DMaster=domainmast.isSelected();
      sambaconf.LMaster=localmast.isSelected();
      sambaconf.PMaster=pmaster.isSelected();
      sambaconf.AVHome=avhome.isSelected();
      sambaconf.AVShare=avshare.isSelected();
      sambaconf.BUHome=buhome.isSelected();
      sambaconf.BUShare=bushare.isSelected();
      sambaconf.BUWWW=bushare.isSelected();
      sambaconf.BUFTP=bushare.isSelected();
      sambaconf.UProfile=uprofile.isSelected();
    }
  }
  class AddFileShare extends Container implements ActionListener {
    JCheckBox editors,guestread,dazuko,bupfl;
    JTextField descrip,folder,sgroups,mapdrive;
    ManageNode sortpanel;
    boolean isEdit; 
    JLabel textlabel;
    JButton adduser;
    FileShare EditShare;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();

    public AddFileShare(boolean edit){
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
        sortpanel=new ManageNode(shares,treeModel,"Select Share To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);
 
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New Share To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing File Share");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      descrip=new JTextField("",10);
      addLabel(new JLabel("Description Of Share"),descrip,gridbag,layout);

      folder=new JTextField("",10);
      addLabel(new JLabel("Folder Name"),folder,gridbag,layout);

      sgroups=new JTextField("",10);
      addLabel(new JLabel("Access Group"),sgroups,gridbag,layout);

      mapdrive=new JTextField("",10);
      addLabel(new JLabel("Map Drive As (Optional)"),mapdrive,gridbag,layout);


      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=1;
      editors=new JCheckBox("Allow Group Overwrite Access");
      gridbag.setConstraints(editors,layout);
      add(editors);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      guestread=new JCheckBox("Allow Non Group Read Access");
      gridbag.setConstraints(guestread,layout);
      add(guestread);

      layout.gridwidth=1;
      bupfl=new JCheckBox("Backup This Folder");
      gridbag.setConstraints(bupfl,layout);
      add(bupfl);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      dazuko=new JCheckBox("Use Clam Anti Virus On Access");
      gridbag.setConstraints(dazuko,layout);
      add(dazuko);


      if (isEdit) {
        EditShare=(FileShare)node.getUserObject();
        descrip.setText(EditShare.Description);        
        folder.setText(EditShare.Folder);
        sgroups.setText(EditShare.Group);
        mapdrive.setText(EditShare.MapDrive);
        editors.setSelected(EditShare.Overwrite);
        guestread.setSelected(EditShare.Read);
        dazuko.setSelected(EditShare.Avirus);
        bupfl.setSelected(EditShare.Backup);
        adduser=new JButton("Save Share");
      } else {
        adduser=new JButton("Add Share");
      }

      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add File Share");
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
      if ((descrip.getText().length() > 0) & (sgroups.getText().length() > 0) & (folder.getText().length() > 0)) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new FileShare(descrip.getText(),
                                                                                    folder.getText(),
                                                                                    sgroups.getText(),
                                                                                    editors.isSelected(),
                                                                                    guestread.isSelected(),
                                                                                    dazuko.isSelected(),
                                                                                    bupfl.isSelected(),
                                                                                    mapdrive.getText()));
          treeModel.insertNodeInto(childnode,shares,shares.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);

          descrip.setText("");
          folder.setText("");
          sgroups.setText("");
          mapdrive.setText("");
          editors.setSelected(false);
          dazuko.setSelected(false);
          bupfl.setSelected(false);
          guestread.setSelected(false);
        } else {
          EditShare.Description=descrip.getText();
          EditShare.Folder=folder.getText();
          EditShare.Group=sgroups.getText();
          EditShare.MapDrive=mapdrive.getText();
          EditShare.Overwrite=editors.isSelected();
          EditShare.Read=guestread.isSelected();
          EditShare.Avirus=dazuko.isSelected();
          EditShare.Backup=bupfl.isSelected();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  class AddMapDrive extends Container implements ActionListener {
    JTextField server,share,mapdrive;
    ManageNode sortpanel;
    boolean isEdit; 
    JLabel textlabel;
    JButton adduser;
    MappedDrive EditMap;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();

    public AddMapDrive(boolean edit){
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
        sortpanel=new ManageNode(maps,treeModel,"Select Maped Drive To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);
 
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New Map Drive To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Maped Drive");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      server=new JTextField("",10);
      addLabel(new JLabel("Server Share Is Located On"),server,gridbag,layout);

      share=new JTextField("",10);
      addLabel(new JLabel("Folder Name"),share,gridbag,layout);

      mapdrive=new JTextField("",10);
      addLabel(new JLabel("Virtual Drive"),mapdrive,gridbag,layout);

      if (isEdit) {
        EditMap=(MappedDrive)node.getUserObject();
        server.setText(EditMap.Server);        
        share.setText(EditMap.Share);
        mapdrive.setText(EditMap.MapDrive);
        adduser=new JButton("Save Mapping");
      } else {
        adduser=new JButton("Add Mapping");
      }

      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Mapped Drive");
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
      if ((server.getText().length() > 0) & (share.getText().length() > 0) & (mapdrive.getText().length() > 0)) {
        if (! isEdit) {
	  DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new MappedDrive(server.getText(),
                                                                                    share.getText(),
                                                                                    mapdrive.getText()));

          treeModel.insertNodeInto(childnode,maps,maps.getChildCount());
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);

          server.setText("");
          share.setText("");
          mapdrive.setText("");
        } else {
          EditMap.Server=server.getText();
          EditMap.Share=share.getText();
          EditMap.MapDrive=mapdrive.getText();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  public String getConfig(){
    Output="";
    DefaultMutableTreeNode sdata;
    FileShare share;
    MappedDrive map;

    if (sambaconf.WorkGroup.length() > 0) {
      Output=AddConfL("FileServer Option Domain "+sambaconf.WorkGroup.replaceAll(" ",""));
    }

    if (sambaconf.ServerName.length() > 0) {
      Output=AddConfL("FileServer Config netbios name = "+sambaconf.ServerName);
    }

    if (sambaconf.Description.length() > 0) {
      Output=AddConfL("FileServer Config server string = "+sambaconf.Description);
    }

    if (sambaconf.Authentication.length() > 0) {
      Output=AddConfL("FileServer Option Security "+sambaconf.Authentication);
    }

    if (sambaconf.Authentication.length() > 0) {
      Output=AddConfL("FileServer Option Winbind "+sambaconf.Winbind);
    }

    if (sambaconf.ADSServer.length() > 0) {
      Output=AddConfL("FileServer Option ADSServer "+sambaconf.ADSServer);
    }

    if (sambaconf.ADSRealm.length() > 0) {
      Output=AddConfL("FileServer Option ADSRealm "+sambaconf.ADSRealm);
    }

    if (sambaconf.RemoteBrowse.length() > 0) {
      Output=AddConfL("FileServer Option RemoteSync "+sambaconf.RemoteBrowse);
    }

    if (sambaconf.OSLevel.length() > 0) {
      Output=AddConfL("FileServer Config os level = "+sambaconf.OSLevel);
    }

    if (sambaconf.PMaster) {
      Output=AddConfL("FileServer Config preferred master = Yes");
    } else {
      Output=AddConfL("FileServer Config preferred master = No");
    }

    if (sambaconf.LMaster) {
      Output=AddConfL("FileServer Config local master = Yes");
    } else {
      Output=AddConfL("FileServer Config local master = No");
    }

    if (sambaconf.DMaster) {
      Output=AddConfL("FileServer Config domain master = Yes");
    } else {
      Output=AddConfL("FileServer Config domain master = No");
    }

    if (sambaconf.AVMaxSize.length() > 0) {
      Output=AddConfL("FileServer AVMaxSize "+sambaconf.AVMaxSize);
    }

    if (sambaconf.AVMaxThread.length() > 0) {
      Output=AddConfL("FileServer AVMaxThread "+sambaconf.AVMaxThread);
    }

    if (sambaconf.DControl) {
      Output=AddConfL("FileServer Controler "+sambaconf.HomeDrive+" "+sambaconf.ShareDrive);
    }

    if (sambaconf.AVHome) {
      Output=AddConfL("FileServer AVHome");
    }

    if (sambaconf.AVShare) {
      Output=AddConfL("FileServer AVShare");
    }

    if (sambaconf.BUHome) {
      Output=AddConfL("FileServer BUHome");
    }

    if (sambaconf.BUShare) {
      Output=AddConfL("FileServer BUShare");
    }

    if (sambaconf.BUFTP) {
      Output=AddConfL("FileServer BUFTP");
    }

    if (sambaconf.BUWWW) {
      Output=AddConfL("FileServer BUWWW");
    }

    if (sambaconf.UProfile) {
      Output=AddConfL("FileServer UProfile");
    }

    for (Enumeration e1 = shares.children() ; e1.hasMoreElements() ;) {
      sdata=(DefaultMutableTreeNode)e1.nextElement();
      share=(FileShare)sdata.getUserObject();
      String group=share.Group;
      if (group == "All Users") {
        group="users";
        share.Read=true;
      } else if (group == "Web Server Admin") {
        group="www";
      } else if (group == "File Server Admin") {
        group="smbadm";
      }

      Output=AddConfL("FileServer Share "+share.Description.replaceAll(" ","_")+" "+
                      share.Folder.replaceAll(" ","_")+" "+share.Overwrite+" "+group+" "+
                      share.Read+" "+share.Avirus+" "+share.Backup+" "+share.MapDrive);
    }

    for (Enumeration e1 = maps.children() ; e1.hasMoreElements() ;) {
      sdata=(DefaultMutableTreeNode)e1.nextElement();
      map=(MappedDrive)sdata.getUserObject();
      Output=AddConfL("FileServer Mapping "+map.Server.replaceAll(" ","_")+" "+
                      map.Share.replaceAll(" ","_")+" "+map.MapDrive);
    }

    return Output;
  }

  public String AddConfL(String newconf){
    String newline = System.getProperty("line.separator");
    String confout=Output+newconf+newline;
    return confout;
  }  
  public void refreshShareWin() {
    mainwindow.setBottomComponent(new AddFileShare(false));
  }

  public void delConfig() {

    sambaconf.delConfig();

    shares.removeAllChildren();
    treeModel.reload(shares);

    maps.removeAllChildren();
    treeModel.reload(maps);
  }

  public void setDefault() {
    delConfig(); 
    sambaconf.Authentication="USER";
    sambaconf.Winbind="Both";
    sambaconf.HomeDrive=new Character('U');
    sambaconf.ShareDrive=new Character('S');
    sambaconf.WorkGroup="WORKGROUP";
    sambaconf.ServerName="SENTRY";
    sambaconf.Description="NETSENTRY INTERNET SERVER";
    sambaconf.OSLevel="65";
    sambaconf.RemoteBrowse="";
    sambaconf.AVMaxSize="2M";
    sambaconf.AVMaxThread="100";
    sambaconf.DControl=false;
    sambaconf.DMaster=true;
    sambaconf.LMaster=true;
    sambaconf.PMaster=true;
    sambaconf.AVHome=false;
    sambaconf.AVShare=false;
    sambaconf.UProfile=false;
    DrawWindow();
  }

}
