import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class ManageNode extends Container implements ActionListener{
  DefaultListModel listdata;
  JList userlist; 
  DefaultMutableTreeNode parentnode;
  DefaultTreeModel treeModel;
  boolean DelEnode=false;

  public ManageNode(DefaultMutableTreeNode  selectednode,DefaultTreeModel TModel,String windowname){
    treeModel=TModel;
    parentnode=selectednode;
    SortPanel(windowname);
  }
  public ManageNode(DefaultMutableTreeNode  selectednode,DefaultTreeModel TModel,String windowname,boolean delempty){
    treeModel=TModel;
    parentnode=selectednode;
    DelEnode=delempty;
    SortPanel(windowname);
  }

  public void SortPanel(String SPLabel){
    String newline = System.getProperty("line.separator");

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints layout = new GridBagConstraints();

    setLayout(gridbag);

    layout.weightx=1;
    layout.weighty=0;

    layout.fill=GridBagConstraints.NONE;
    layout.anchor=GridBagConstraints.NORTH;
    JLabel textlabel=new JLabel(SPLabel);
    layout.gridwidth=GridBagConstraints.REMAINDER;
    gridbag.setConstraints(textlabel,layout);
    add(textlabel);

    layout.anchor=GridBagConstraints.NORTH;
    layout.gridwidth=GridBagConstraints.REMAINDER;
    layout.fill=GridBagConstraints.HORIZONTAL;
    listdata=new DefaultListModel();
    for (Enumeration e = parentnode.children() ; e.hasMoreElements() ;) {
        listdata.addElement((DefaultMutableTreeNode)e.nextElement());
    }
//    userlist.setVisibleRowCount(2);

    userlist=new JList(listdata);
    userlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    userlist.setFont(new Font("Monospaced", Font.PLAIN,10));
    userlist.setCellRenderer(new AbbrRender());
    JScrollPane sclist=new JScrollPane(userlist);
    gridbag.setConstraints(sclist,layout);
    add(sclist);


    JButton mup=new JButton("Move Up");      
    layout.gridwidth=1;
    gridbag.setConstraints(mup,layout);

    mup.setActionCommand("MoveUp");
    mup.addActionListener(this);

    add(mup);

    JButton sort=new JButton("Sort");      
    layout.gridwidth=1;
    gridbag.setConstraints(sort,layout);

    sort.setActionCommand("Sort");
    sort.addActionListener(this);

    add(sort);

    JButton rem=new JButton("Remove");      
    layout.gridwidth=1;
    gridbag.setConstraints(rem,layout);

    rem.setActionCommand("Remove");
    rem.addActionListener(this);

    add(rem);

    layout.weighty=1;
    layout.gridwidth=GridBagConstraints.REMAINDER;
    JButton mdn=new JButton("Move Down");
    gridbag.setConstraints(mdn,layout);

    mdn.setActionCommand("MoveDown");
    mdn.addActionListener(this);

    add(mdn);
      
  }
  public void actionPerformed(ActionEvent event) {
    DefaultMutableTreeNode tmpnode,tmpnode2,lownode;
    int idx=userlist.getSelectedIndex();
    DefaultListModel newlist;

    if (event.getActionCommand() == "MoveUp") {
      if (idx > 0) {
          tmpnode=(DefaultMutableTreeNode)listdata.getElementAt(idx);

          treeModel.removeNodeFromParent(tmpnode);
          treeModel.insertNodeInto(tmpnode,parentnode,idx-1);

          listdata.remove(idx);
          listdata.insertElementAt(tmpnode,idx-1);
          userlist.setSelectedIndex(idx-1);
        }
    }

    if (event.getActionCommand() == "Remove") {
      int pchildcnt=treeModel.getChildCount(parentnode)-1;
      if ((idx >= 0) & (idx <= pchildcnt)) {
          tmpnode=(DefaultMutableTreeNode)listdata.getElementAt(idx);
          treeModel.removeNodeFromParent(tmpnode);
          listdata.remove(idx);
          if ((pchildcnt == 0) & DelEnode) {
            treeModel.removeNodeFromParent(parentnode);
          }
        }
    }

    if (event.getActionCommand() == "MoveDown") {
      if ((idx >= 0) & (idx < treeModel.getChildCount(parentnode)-1)) {
          tmpnode=(DefaultMutableTreeNode)listdata.getElementAt(idx);

          treeModel.removeNodeFromParent(tmpnode);
          treeModel.insertNodeInto(tmpnode,parentnode,idx+1);

          listdata.remove(idx);
          listdata.insertElementAt(tmpnode,idx+1);
          userlist.setSelectedIndex(idx+1);
        }
    }

    if (event.getActionCommand() == "Sort") {
      int lsize=listdata.size();
      int lcnt=0;
      for(int icnt=0;icnt < lsize;icnt++) {
        tmpnode=(DefaultMutableTreeNode)listdata.getElementAt(icnt);
        lownode=tmpnode;
        for(int acnt=icnt+1;acnt < lsize;acnt++) {
          tmpnode2=(DefaultMutableTreeNode)listdata.getElementAt(acnt);
          if (lownode.toString().compareTo(tmpnode2.toString()) > 0) {
            lownode=tmpnode2;
            lcnt=acnt;
          }
        }
        if (lcnt > 0) {
          listdata.setElementAt(lownode,icnt);
          listdata.setElementAt(tmpnode,lcnt);
          treeModel.removeNodeFromParent(lownode);
          treeModel.insertNodeInto(lownode,parentnode,icnt);
          treeModel.removeNodeFromParent(tmpnode);
          treeModel.insertNodeInto(tmpnode,parentnode,lcnt);
        }
        lcnt=0;
      }
    }

  }

  class AbbrRender extends JLabel implements ListCellRenderer {
    public Component getListCellRendererComponent(JList list,Object value,
                                                  int index,boolean isSelected,
                                                  boolean cellHasFocus) {
      String s = value.toString();
      int maxlen=s.length();
      if (maxlen >= 68) {
        s=s.substring(0,68)+"...";
      }
      setText(s);
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setEnabled(list.isEnabled());
      setFont(list.getFont());
      setOpaque(true);
      return this;
    }
  }
}
