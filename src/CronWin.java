import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class CronWin extends Container {
  JSplitPane mainwindow=new JSplitPane();
  JTree userswindow;
  DefaultTreeModel treeModel;
  DefaultMutableTreeNode topbranch;
  String Output;
  Vector UserList;
  boolean addhost=false;

  public CronWin() {
    setLayout(new BorderLayout());

    topbranch=new DefaultMutableTreeNode("Preset Tasks");
    treeModel = new DefaultTreeModel(topbranch);
    userswindow=new JTree(treeModel);
    userswindow.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    userswindow.setShowsRootHandles(true);

    mainwindow.setLeftComponent(new JScrollPane(userswindow));    
//    userswindow.setSelectionPath(new TreePath(topbranch.getPath()));
    mainwindow.setBottomComponent(null);
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
    addhost=false;

    if (nodeInfo.toString() == "Preset Tasks" ){
      mainwindow.setBottomComponent(new AddCronJob(false));
    } else {
      mainwindow.setBottomComponent(new AddCronJob(true));
    }
    mainwindow.setDividerLocation(0.3);
  }

  class AddCronJob extends Container implements ActionListener {
    JComboBox task,period,fromh,toh,days;
    ManageNode sortpanel;
    boolean isEdit;
    JLabel textlabel;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode)userswindow.getLastSelectedPathComponent();
    CronJob EditJob;
    JButton adduser;

    public AddCronJob(boolean edit){

      GridBagLayout gridbag = new GridBagLayout();
      GridBagConstraints layout = new GridBagConstraints();
      isEdit=edit;
      setLayout(gridbag);

      layout.anchor=GridBagConstraints.NORTH;
      layout.weightx=1;
      layout.weighty=1;

      if (! isEdit ) {
        layout.fill=GridBagConstraints.HORIZONTAL;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        sortpanel=new ManageNode(topbranch,treeModel,"Select Task To Manage",false);      
        gridbag.setConstraints(sortpanel,layout);
        add(sortpanel);

        layout.weighty=0;

        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("New Task To Add");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      } else {
        layout.weighty=0;
        layout.fill=GridBagConstraints.NONE;
        layout.gridwidth=GridBagConstraints.REMAINDER;
        textlabel=new JLabel("Editing Task");
        gridbag.setConstraints(textlabel,layout);
        add(textlabel);
      }

      layout.fill=GridBagConstraints.HORIZONTAL;
      layout.anchor=GridBagConstraints.NORTHWEST;

      layout.gridwidth=1;
      JLabel tlabel=new JLabel("Task To Perform");
      gridbag.setConstraints(tlabel,layout);
      add(tlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String tasklist[]={"Send Queued Mail","Fetch POP3 Mail","System Update"};
      task=new JComboBox(tasklist);
      gridbag.setConstraints(task,layout);
      add(task);

      layout.gridwidth=1;
      JLabel plabel=new JLabel("Frequency (Minutes)");
      gridbag.setConstraints(plabel,layout);
      add(plabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      period=new JComboBox();
      period.addItem(new Integer(1));
      period.addItem(new Integer(2));
      period.addItem(new Integer(5));
      for (int pint=6;pint >= 1;pint--) {
        period.addItem(new Integer(60/pint));
      }
      period.setSelectedIndex(5);
      gridbag.setConstraints(period,layout);
      add(period);

      layout.gridwidth=1;
      JLabel slabel=new JLabel("From (Hour)");
      gridbag.setConstraints(slabel,layout);
      add(slabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      fromh=new JComboBox();
      for (int fint=1;fint < 24;fint++) {
        fromh.addItem(new Integer(fint));
      }
      gridbag.setConstraints(fromh,layout);
      add(fromh);

      layout.gridwidth=1;
      JLabel elabel=new JLabel("To (Hour)");
      gridbag.setConstraints(elabel,layout);
      add(elabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      toh=new JComboBox();
      for (int tint=23;tint >= 1;tint--) {
        toh.addItem(new Integer(tint));
      }
      gridbag.setConstraints(toh,layout);
      add(toh);

      layout.gridwidth=1;
      JLabel dlabel=new JLabel("Days To Perform Task");
      gridbag.setConstraints(dlabel,layout);
      add(dlabel);
      layout.gridwidth=GridBagConstraints.REMAINDER;
      String daylist[]={"Monday To Friday","Monday To Saturday","Everyday","Saturday","Sunday"};
      days=new JComboBox(daylist);
      gridbag.setConstraints(days,layout);
      add(days);

      if (isEdit) {
	EditJob=(CronJob)node.getUserObject();
        task.setSelectedItem(EditJob.Task);
        period.setSelectedItem(new Integer(EditJob.Period));
        fromh.setSelectedItem(new Integer(EditJob.StartHour));
        toh.setSelectedItem(new Integer(EditJob.EndHour));
        days.setSelectedItem(EditJob.Days);
        adduser=new JButton("Save Task");
      } else {
        adduser=new JButton("Add Task");
      }

      layout.weighty=1;
      layout.anchor=GridBagConstraints.NORTH;
      layout.gridwidth=GridBagConstraints.REMAINDER;
      layout.fill=GridBagConstraints.NONE;
      adduser.setActionCommand("Add Cron Job");
      adduser.addActionListener(this);
      gridbag.setConstraints(adduser,layout);
      add(adduser);
    }

    public void actionPerformed(ActionEvent event) {
      if (! isEdit) {
        DefaultMutableTreeNode childnode=new DefaultMutableTreeNode(new CronJob(task.getSelectedItem().toString(),
                                                                                period.getSelectedItem().toString(),
                                                                                fromh.getSelectedItem().toString(),
                                                                                toh.getSelectedItem().toString(),
                                                                                days.getSelectedItem().toString()));
        treeModel.insertNodeInto(childnode,topbranch,topbranch.getChildCount());
        userswindow.scrollPathToVisible(new TreePath(childnode.getPath()));
        sortpanel.listdata.addElement(childnode);

        task.setSelectedIndex(0);
        period.setSelectedIndex(5);
        fromh.setSelectedIndex(0);
        toh.setSelectedIndex(0);
        days.setSelectedIndex(0);
      } else {
        EditJob.Task=task.getSelectedItem().toString();
        EditJob.Period=period.getSelectedItem().toString();
        EditJob.StartHour=fromh.getSelectedItem().toString();
        EditJob.EndHour=toh.getSelectedItem().toString();
        EditJob.Days=days.getSelectedItem().toString();
        treeModel.reload(node);
        userswindow.scrollPathToVisible(new TreePath(node.getPath()));
      }
    }
  }

  public String getConfig() {
    Output="";
    DefaultMutableTreeNode rdata;
    String jobtd;
    CronJob job;

    for (Enumeration e = topbranch.children() ; e.hasMoreElements() ;) {
      rdata=(DefaultMutableTreeNode)e.nextElement();
      job=(CronJob)rdata.getUserObject();
      if (job.Days.equals("Everyday")) {
        jobtd="*";
      } else if (job.Days.equals("Monday To Friday")) {
        jobtd="mon-fri";
      } else if (job.Days.equals("Monday To Saturday")) {
        jobtd="mon-sat";
      } else if (job.Days.equals("Saturday")) {
        jobtd="sat";
      } else if (job.Days.equals("Sunday")) {
        jobtd="sun";
      } else {
        jobtd=job.Days;
      }

      Output=AddConfL("Cron "+job.Task.replaceAll(" ","_")+" "+job.Period+" "+job.StartHour+" "+job.EndHour+" "+
                      jobtd);
    }
    return Output;
  }
  public String AddConfL(String newconf){
    String newline = System.getProperty("line.separator");
    String confout=Output+newconf+newline;
    return confout;
  }  
  public void delConfig() {
    topbranch.removeAllChildren();
    treeModel.reload(topbranch);
  }

  public void setDefault(String systype) {
    delConfig(); 

    if (systype.equals("full")) {
      treeModel.insertNodeInto(new DefaultMutableTreeNode(new CronJob("DNS Reconfigure","60","1","23","Everyday")),
                               topbranch,topbranch.getChildCount());
    }
    treeModel.insertNodeInto(new DefaultMutableTreeNode(new CronJob("Send Queued Mail","60","1","23","Everyday")),
                             topbranch,topbranch.getChildCount());
    treeModel.insertNodeInto(new DefaultMutableTreeNode(new CronJob("System Update","2","1","23","Everyday")),
                             topbranch,topbranch.getChildCount());

    userswindow.setSelectionPath(new TreePath(topbranch.getPath()));
    mainwindow.setBottomComponent(new AddCronJob(false));
    mainwindow.setDividerLocation(0.3);
  }

}
