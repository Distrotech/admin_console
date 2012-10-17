import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class NFSWin extends Container {
  DefaultMutableTreeNode nfsmount,nfsshare,fsbackup;
  DefaultTreeModel treeModel;
  final JSplitPane mainwindow=new JSplitPane();
  final JTree userswindow;
  String Output="";
  String systype="lite";
  DefaultMutableTreeNode topbranch = new DefaultMutableTreeNode("Network Shares/Mounts");

  public NFSWin() {
    setLayout(new BorderLayout());

    nfsmount = new DefaultMutableTreeNode("Network Mounts (SMB+NFS)");
    nfsshare = new DefaultMutableTreeNode("NFS Shares");
    fsbackup = new DefaultMutableTreeNode("Default Folders To Backup");

    treeModel = new DefaultTreeModel(topbranch);


    topbranch.add(nfsmount);
    topbranch.add(nfsshare);
//    topbranch.add(fsbackup);
    
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
    if (node == nfsmount){
      mainwindow.setBottomComponent(new AddMount(false));
    } else if (node == nfsshare){
      mainwindow.setBottomComponent(new AddShare(false));
    } else if (node.getParent() == nfsmount) {
      mainwindow.setBottomComponent(new AddMount(true));
    } else if (node.getParent() == nfsshare){
      mainwindow.setBottomComponent(new AddShare(true));
    } else {
      mainwindow.setBottomComponent(null);
    }
    mainwindow.setDividerLocation(0.3);
  }

  class AddMount extends Container implements ActionListener {
    JTextField dscrip,folder,source,bind,smbuname,smbuid,smbgid;
    JCheckBox smbro,backup,vscan;
    JPasswordField smbpass1,smbpass2;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    NFSMount EMount;

    public AddMount(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Mount To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Details Of New Mount");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Mount");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      dscrip=new JTextField("",10);
      addLabel(new JLabel("Discription"),dscrip,gridbag,layout);

      folder=new JTextField("",10);
      addLabel(new JLabel("Folder (/mnt/autofs/<FOLDER>)"),folder,gridbag,layout);

      source=new JTextField("",10);
      addLabel(new JLabel("Source (<H>:<S> Or (//<H>/<S>)"),source,gridbag,layout);

      bind=new JTextField("",10);
      addLabel(new JLabel("Bind Path"),bind,gridbag,layout);      

      layout.gridwidth=1;
      backup=new JCheckBox("Backup This Folder",false);
      gridbag.setConstraints(backup,layout);
      add(backup);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      vscan=new JCheckBox("Virus Scan This Folder",false);
      gridbag.setConstraints(vscan,layout);
      add(vscan);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTH;
      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      JLabel smblabel=new JLabel("SMB Options");
      gridbag.setConstraints(smblabel,layout);
      add(smblabel);

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      smbuname=new JTextField("",10);
      addLabel(new JLabel("Username (Guest if blank)"),smbuname,gridbag,layout);

      smbpass1=new JPasswordField("",10);
      addLabel(new JLabel("Password"),smbpass1,gridbag,layout);

      smbpass2=new JPasswordField("",10);
      addLabel(new JLabel("Conifirm Password"),smbpass2,gridbag,layout);

      smbuid=new JTextField("",10);
      addLabel(new JLabel("UID (Sytem User Name)"),smbuid,gridbag,layout);

      smbgid=new JTextField("",10);
      addLabel(new JLabel("GID (Sytem Group Name)"),smbgid,gridbag,layout);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      smbro=new JCheckBox("Read Only Access",false);
      gridbag.setConstraints(smbro,layout);
      add(smbro);

      if (isEdit) {
        EMount=(NFSMount)node.getUserObject();

        dscrip.setText(EMount.Name);
        folder.setText(EMount.Folder);
        source.setText(EMount.Source);
        if (! EMount.Bind.equals("-")) {
          bind.setText(EMount.Bind);
        }
        
        if (! EMount.User.equals("-")) {
          smbuname.setText(EMount.User);
        }
        if (! EMount.Pass.equals("-")){
          smbpass1.setText(EMount.Pass);
          smbpass2.setText(EMount.Pass);
        }
        if (! EMount.UID.equals("-")) {
          smbuid.setText(EMount.UID);
        }
        if (! EMount.GID.equals("-")) {
          smbgid.setText(EMount.GID);
        }
        smbro.setSelected(EMount.Read);
	backup.setSelected(EMount.Backup);
      	vscan.setSelected(EMount.VScan);
        adduser=new JButton("Save Mount");
      } else {
        adduser=new JButton("Add Mount");
      }

      layout.gridwidth=1;
      layout.fill=GridBagConstraints.NONE;

      layout.weighty=1;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.anchor=GridBagConstraints.NORTH;
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
      if ((dscrip.getText().length() > 0) & (source.getText().length() > 0) &
          (folder.getText().length() > 0)) {
        if (! isEdit) {
          String smbpass="";
          if (Arrays.equals(smbpass1.getPassword(),smbpass2.getPassword())) {
            smbpass=smbpass1.getPassword().toString();
          }
          DefaultMutableTreeNode childnode=addNFSMount(dscrip.getText(),folder.getText(),source.getText(),
                                           bind.getText(),smbuname.getText(),smbpass,
                                           smbuid.getText(),smbgid.getText(),smbro.isSelected(),
                                           backup.isSelected(),vscan.isSelected());

          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
          sortpanel.listdata.addElement(childnode);

          dscrip.setText("");
          source.setText("");
          folder.setText(""); 
          bind.setText(""); 
          smbuname.setText(""); 
          smbpass1.setText(""); 
          smbpass2.setText(""); 
          smbuid.setText(""); 
          smbgid.setText(""); 
          smbro.setSelected(false); 
          backup.setSelected(false);
          vscan.setSelected(false);
        } else {
          EMount.Name=dscrip.getText();
          EMount.Folder=folder.getText();
          EMount.Source=source.getText();
          EMount.Bind=bind.getText();
          EMount.User=smbuname.getText();
          if (Arrays.equals(smbpass1.getPassword(),smbpass2.getPassword())) {
            EMount.Pass=smbpass1.getPassword().toString();
          } else {
            smbpass1.setText(EMount.Pass);
            smbpass2.setText(EMount.Pass);
          }
          EMount.UID=smbuid.getText();
          EMount.GID=smbgid.getText();
 
          EMount.Read=smbro.isSelected();
          EMount.Backup=backup.isSelected();
          EMount.VScan=vscan.isSelected();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }


  public DefaultMutableTreeNode addNFSMount(String dscrip,String folder,String source,
                                String bind,String smbuname,String smbpass,String smbuid,
                                String smbgid,boolean smbro,boolean fbup,boolean vscan) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new NFSMount(dscrip,folder,source,
                                         bind,smbuname,smbpass,smbuid,smbgid,smbro,fbup,vscan));
    treeModel.insertNodeInto(childnode,nfsmount,nfsmount.getChildCount());
    return childnode;
  }

  class AddShare extends Container implements ActionListener {
    JTextField server,path,suid,sgid;
    JCheckBox nfsro,nfssquash;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    JButton adduser;
    NFSShare EShare;

    public AddShare(boolean edit){
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
        sortpanel=new ManageNode(node,treeModel,"Select Share To Manage");      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Enter Details Of New Share");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Share");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      server=new JTextField("",10);
      addLabel(new JLabel("Allow Access From"),server,gridbag,layout);

      path=new JTextField("",10);
      addLabel(new JLabel("Path To Export"),path,gridbag,layout);

      suid=new JTextField("",10);
      addLabel(new JLabel("Squash To UID (If Set)"),suid,gridbag,layout);

      sgid=new JTextField("",10);
      addLabel(new JLabel("Squash To GID (If UID Set)"),sgid,gridbag,layout);

      layout.fill=GridBagConstraints.NONE;
      layout.gridwidth=1;
      nfssquash=new JCheckBox("Squash All Users (If UID Set)",false);
      gridbag.setConstraints(nfssquash,layout);
      add(nfssquash);

      layout.gridwidth=GridBagConstraints.REMAINDER;
      nfsro=new JCheckBox("Read Only Access",false);
      gridbag.setConstraints(nfsro,layout);
      add(nfsro);

      if (isEdit) {
        EShare=(NFSShare)node.getUserObject();
        server.setText(EShare.Server);
        path.setText(EShare.Path);

        if (! EShare.UID.equals("-")) {
          suid.setText(EShare.UID);
        }

        if (! EShare.GID.equals("-")) {
          sgid.setText(EShare.GID);
        }

        nfsro.setSelected(EShare.Read);
        nfssquash.setSelected(EShare.Squash);

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
      if ((server.getText().length() > 0) & (path.getText().length() > 0)) {
        if (! isEdit) {
          DefaultMutableTreeNode childnode=addNFSShare(server.getText(),path.getText(),suid.getText(),
                                                       sgid.getText(),nfsro.isSelected(),nfssquash.isSelected());
          sortpanel.listdata.addElement(childnode);
          server.setText("");
          path.setText("");
          suid.setText("");
          sgid.setText("");

          nfsro.setSelected(false);
          nfssquash.setSelected(false);
          
          userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        } else {
          EShare.Server=server.getText();
          EShare.Path=path.getText();
          EShare.UID=suid.getText();
          if (suid.getText().length() > 0) {
            EShare.GID=sgid.getText();
          } else {
            EShare.GID="";
            sgid.setText("");
          }
          EShare.Read=nfsro.isSelected();
          EShare.Squash=nfssquash.isSelected();
          treeModel.reload(node);
          userswindow.scrollPathToVisible(new TreePath(node.getPath()));
        }
      }
    }
  }

  public DefaultMutableTreeNode addNFSShare(String server,String path,String suid,String sgid,
                                            boolean nfsro,boolean nfssquash) {
    DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new NFSShare(server,path,suid,sgid,nfsro,nfssquash));
    treeModel.insertNodeInto(childnode,nfsshare,nfsshare.getChildCount());
    return childnode;
  }

  public Vector getUserList(){
    Vector ulist;

    ulist=new Vector();
    for (Enumeration e = nfsmount.children() ; e.hasMoreElements() ;) {
        ulist.addElement((DefaultMutableTreeNode)e.nextElement());
    }
    return(ulist);
  }
  public String getConfig() {
    DefaultMutableTreeNode udata;
    String newline = System.getProperty("line.separator");
    NFSMount nfsmnt;
    NFSShare nshare;
    Output="";

    for (Enumeration e = nfsmount.children() ; e.hasMoreElements() ;) {
      udata=(DefaultMutableTreeNode)e.nextElement();
      nfsmnt=(NFSMount)udata.getUserObject();
      Output=AddConfL("NFS Mount "+nfsmnt.confOut());
    }

    for (Enumeration e = nfsshare.children() ; e.hasMoreElements() ;) {
      udata=(DefaultMutableTreeNode)e.nextElement();
      nshare=(NFSShare)udata.getUserObject();
      if (nshare.UID.length() == 0) {
        nshare.UID="-";
      }
      if (nshare.GID.length() == 0) {
        nshare.GID="-";
      }
      Output=AddConfL("NFS Share "+nshare.Server+" "+nshare.Path+" "+nshare.UID+" "+
                      nshare.GID+" "+nshare.Read+" "+nshare.Squash);
    }

    return Output;
  }

  public String AddConfL(String newconf){
    String newline = System.getProperty("line.separator");
    String confout=Output+newconf+newline;
    return confout;
  }

  public void delConfig() {
    nfsmount.removeAllChildren();
    treeModel.reload(nfsmount);

    nfsshare.removeAllChildren();
    treeModel.reload(nfsshare);
  }

  public void setDefault() {
    delConfig();
    DrawWindow();
  }

}

class MappedDrive {
    
    String Server;
    
    String Share;
    
    String MapDrive;
    
    public MappedDrive(String serv, String shr, String mdrive) {
        Server=serv;
        Share=shr;
        MapDrive=mdrive;
    }
    
    public String toString() {
        String Output;
        Output="//"+Server+"/"+Share+" ---> "+MapDrive+":";
        return Output;
    }
    
}

class FileShare {
    String Description;
    String Folder;
    String Group;
    String MapDrive;
    boolean Overwrite;
    boolean Read;
    boolean Avirus,Backup;
    
    public FileShare(String descrip, String folder, String group, boolean gaccess,
                     boolean ngread, boolean dazuko,boolean bupfl,String mdrive) {
        Description=descrip;
        Folder=folder;
        Group=group;
        Overwrite=gaccess;
        Read=ngread;
        Avirus=dazuko;
        Backup=bupfl;
        MapDrive=mdrive;
    }
    
    public String toString() {
        String Output;
        Output=Description+" ("+Folder+" "+Group;
        if (Overwrite) {
            Output=Output+" Overwriteable";
        } else {
            Output=Output+" Readable";
        }
        if (Read) {
            Output=Output+" And Readable By Others";
        }
        if (Avirus) {
            Output=Output+" [Virus Scan On]";
        }
        
        if (MapDrive.length() > 0) {
            Output=Output+" [Map As "+MapDrive+"]";
        }
        
        Output=Output+")";
        return Output;
    }
    
}
