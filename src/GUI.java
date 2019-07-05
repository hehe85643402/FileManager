import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

public class GUI {


    private JPanel rootpane;
    private JTable table1;
    private JScrollPane jsp;
    private JButton reButton;
    private JLabel pathLabel;
    private JPanel topPanel;
    private JPanel listjp;
    private JTree tree1;
    private JScrollPane jsp2;
    private Vector<String> columnNname = new Vector<>();
    private Vector<Vector> data = new Vector<>();
    private JPopupMenu popupMenu = new JPopupMenu();
    private DefaultTableModel model;
    private Color focuscolor = new Color(0xC6E2FF);
    private JTextField jtf = new JTextField();
    private DefaultMutableTreeNode root;//文件树的根目录
    private DefaultTreeModel treemodel;

    public GUI() {
        //设置当前路径为根目录
        MFD.openPath("root");

        columnNname.add("名称");
        columnNname.add("类型");

        model = new DefaultTableModel(data, columnNname) {
            @Override
            public boolean isCellEditable(int row, int column) {

                return column == 0;
            }
        };

        table1.setModel(model);
        //当输入框获取焦点时全选
        jtf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                jtf.selectAll();
            }
        });
        //table1.getColumn("")
        //设置table1的编辑器为输入框
        DefaultCellEditor dfc = new DefaultCellEditor(jtf);
        //设置table1单击开始编辑
        dfc.setClickCountToStart(1);
        table1.setDefaultEditor(table1.getColumnClass(0), dfc);
        //失去焦点时保存正在编辑的内容
        table1.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        //右键菜单新建文件夹
        JMenuItem createDir = new JMenuItem("新建文件夹");
        popupMenu.add(createDir);
        createDir.addActionListener(e -> createDir());

        //右键菜单重命名
        JMenuItem rename = new JMenuItem("重命名");
        rename.addActionListener(e ->
            table1.editCellAt(table1.getSelectedRow(), 0)
        );
        //右键菜单删除
        JMenuItem delete = new JMenuItem("删除");
        delete.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                String name = table1.getValueAt(row, 0).toString();
                if (table1.getValueAt(row, 1).toString().equals("文件夹")) {
                    UFD u = MFD.findUFDByname(name);
                    if (u != null)
                        MFD.ufdlist.remove(u);
                } else {
                    UFD u = MFD.findUFDByname(MFD.path.get(1));
                    if (u != null)
                        u.deleteFile(name);
                }
                refreshList();
            }
        });
        //右键新建文件
        JMenuItem createFile = new JMenuItem("新建文件");
        createFile.addActionListener(e ->
                create_File()
        );
        //右键打开文件
        JMenuItem openFile = new JMenuItem("打开");
        openFile.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                String name = table1.getValueAt(row, 0).toString();
                if (MFD.path.size() == 1)//双击打开文件夹
                {
                    System.out.println(name);
                    open(name);
                } else {//双击打开文件
                    openFile(name);
                }
            }

        });

        JMenuItem fileproperty = new JMenuItem("属性");

        fileproperty.addActionListener(e -> {
            int row = table1.getSelectedRow();
            if (row != -1) {
                String name = table1.getValueAt(row, 0).toString();
                if (MFD.path.size() == 1) {
                    UFD u = MFD.findUFDByname(name);
                    new FileProperty(rootpane, name, u, null, MFD.getPath() + "/" + name, u.property);
                } else {
                    UFD u = MFD.findUFDByname(MFD.path.get(1));
                    FCB fcb = null;
                    for (FCB f : u.filelist)
                        if (f.filename.equals(name)) {
                            fcb = f;
                            break;
                        }
                    new FileProperty(rootpane, name, u, fcb, MFD.getPath() + "/" + name, fcb.shuxing);
                }


            }

        });


        table1.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            //修改焦点所在行颜色
            public void mouseMoved(MouseEvent e) {
                if (table1.isEditing())
                    return;
                super.mouseMoved(e);
                //System.out.println(e.getPoint());
                int focusedRowIndex = table1.rowAtPoint(e.getPoint());
                //鼠标是否超出Jtable边界
                if (!table1.contains(e.getPoint()))
                    setOneRowBackgroundColor(table1, -1, focuscolor);
                else {
                    if (focusedRowIndex != -1) {
                        setOneRowBackgroundColor(table1, focusedRowIndex, focuscolor);
                    } else {
                        setOneRowBackgroundColor(table1, -1, focuscolor);
                    }
                }

            }
        });


        //重命名
        //将表格的修改内容更新到文件系统
        model.addTableModelListener(e -> {
            int type = e.getType();
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (type == TableModelEvent.UPDATE) {
                //System.out.println("行"+row+"列："+col);
                //System.out.println(table1.getValueAt(row,col));
                //System.out.println(table1.getValueAt(row,col));
                if (MFD.path.size() == 1) {
                    if (!MFD.rename(MFD.ufdlist.get(row), model.getValueAt(row, col).toString()))
                        JOptionPane.showMessageDialog(null, "存在相同名字的文件,重命名失败", "提示", JOptionPane.ERROR_MESSAGE);

                } else {
                    UFD ufd = MFD.findUFDByname(MFD.path.get(1));
                    if (ufd != null) {
                        //System.out.println(row);
                        FCB f = ufd.filelist.get(row);
                        if (ufd.renameFile(f.filename, model.getValueAt(row, col).toString()) == -1) {
                            JOptionPane.showMessageDialog(null, "存在相同名字的文件,重命名失败", "提示", JOptionPane.ERROR_MESSAGE);

                        }
                    }


                }
                refreshList();
            }
        });
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //点击其他位置停止编辑状态
                if(table1.isEditing())
                    table1.getCellEditor().stopCellEditing();
                super.mousePressed(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {

                super.mouseClicked(e);


                int buttonid = e.getButton();
                int clickcount = e.getClickCount();
                Point p = e.getPoint();
                int focusedRowIndex = table1.rowAtPoint(p);
                int focusedColumnIndex = table1.columnAtPoint(p);

                if (focusedRowIndex == -1)//鼠标未选中
                {
                    table1.clearSelection();
                }
                //右键菜单显示
                if (buttonid == MouseEvent.BUTTON3) {
                    popupMenu.removeAll();
                    if (focusedRowIndex != -1) {//选中文件或文件夹
                        table1.setRowSelectionInterval(focusedRowIndex, focusedRowIndex);
                        popupMenu.add(openFile);
                        popupMenu.add(rename);
                        popupMenu.add(delete);
                        popupMenu.add(fileproperty);

                    } else {//未选中文件或文件夹

                        if (MFD.path.size() == 1)//只在根目录显示
                            popupMenu.add(createDir);
                        else//在二级目录显示
                            popupMenu.add(createFile);
                    }
                    popupMenu.show(table1, e.getX(), e.getY());
                }
                /*else if(buttonid==MouseEvent.BUTTON1 && clickcount == 1){
                    if(focusedRowIndex==-1)
                    {
                    }
                    else
                    {
                        System.out.println(table1.getValueAt(table1.getSelectedRow(),0));
                    }
                    鼠标单击实现选中
                    else
                    {
                        if(focusedColumnIndex==0)
                        {
                            table1.editCellAt(focusedRowIndex,focusedColumnIndex);
                            JTextField j=(JTextField) table1.getCellEditor().getTableCellEditorComponent(table1,
                                    table1.getValueAt(focusedRowIndex,0),true,
                                    focusedRowIndex,0);
                            j.selectAll();
                        }

                    }
                }*/
                else if (buttonid == MouseEvent.BUTTON1 && clickcount == 2) {
                    if (focusedRowIndex != -1) {
                        String name = table1.getValueAt(focusedRowIndex, 0).toString();
                        if (MFD.path.size() == 1)//双击打开文件夹
                        {
                            System.out.println(name);
                            open(name);
                        } else {//双击打开文件
                            openFile(name);
                        }

                    }
                }
            }
        });


        table1.setRowSorter(new TableRowSorter<>(model));
        //返回按钮
        reButton.addActionListener(e -> {
            MFD.rePath();
            refreshList();
            reButton.setEnabled(false);
        });

        initJtree();
        reButton.setEnabled(false);
        refreshpath();
    }

    private void initJtree() {

        root = new DefaultMutableTreeNode("root");
        treemodel = new DefaultTreeModel(root);

        tree1.setModel(treemodel);
    }

    //设置列表某一行背景色
    public static void setOneRowBackgroundColor(JTable table, int rowIndex,
                                                Color color) {
        try {
            DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {

                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    if (row == rowIndex) {
                        setBackground(color);
                    } else {
                        setBackground(Color.white);
                    }
                    return super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                }
            };
            int columnCount = table.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                table.getColumn(table.getColumnName(i)).setCellRenderer(tcr);
            }
            table.updateUI();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void open(String name) {
        MFD.openPath(name);

        refreshList();

        reButton.setEnabled(true);
    }

    private void openFile(String name) {
        String content = "";
        UFD ufd = null;
        for (UFD u : MFD.ufdlist) {

            if (u.username.equals(MFD.path.get(1))) {
                try {
                    content = u.openFile(name);
                } catch (UnsupportedEncodingException ue) {
                    ue.printStackTrace();
                }

                ufd = u;
                break;
            }

        }
        if (ufd != null)
            fileEditWindow(name, content, ufd);
    }

    private void fileEditWindow(String name, String content, UFD ufd) {
        if (ufd == null)
            return;
        JFrame frame = new JFrame(name);
        CreatFile cf = new CreatFile(content);
        frame.setContentPane(cf.mpanel);
        //窗口关闭事件
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                String text = cf.getContent();
                System.out.println(text);

                int options = JOptionPane.showConfirmDialog(frame, "是否在关闭之前保存文件", "提示", JOptionPane.YES_NO_CANCEL_OPTION);
                if (options == JOptionPane.YES_OPTION) {
                    try {
                        ufd.xiugaiFilewords(name, text);
                    } catch (UnsupportedEncodingException ue) {
                        ue.printStackTrace();
                    }

                    frame.dispose();
                } else if (options == JOptionPane.NO_OPTION) {
                    frame.dispose();
                }
            }
        });

        frame.pack();

        frame.setLocationRelativeTo(rootpane);

        frame.setVisible(true);
    }

    private void refreshtree() {
        root.removeAllChildren();

        for (UFD u : MFD.ufdlist) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(u.username);
            root.add(node);
            for (FCB f : u.filelist) {
                DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(f.filename);
                node.add(node2);
            }
        }
        tree1.updateUI();
    }

    private void refreshpath() {
        pathLabel.setText(MFD.getPath());
    }

    private void refreshList() {
        int rowcount = model.getRowCount();
        for (int i = rowcount - 1; i >= 0; i--)
            model.removeRow(i);

        if (MFD.path.size() == 1) {
            for (UFD u : MFD.ufdlist) {
                Vector<String> Row = new Vector<>();
                Row.add(u.username);
                Row.add(u.type);
                model.addRow(Row);
            }
        } else {
            for (UFD u : MFD.ufdlist) {
                if (u.username.equals(MFD.path.get(1))) {
                    for (FCB f : u.filelist) {
                        Vector<String> Row = new Vector<>();
                        Row.add(f.filename);
                        Row.add("文件");
                        model.addRow(Row);
                    }
                    break;
                }
            }
        }
        table1.updateUI();
        refreshtree();
        refreshpath();

    }

    private void createDir() {
        String name;
        for (int i = 0; ; i++) {
            name = "新建文件夹";
            if (i != 0)
                name += "(" + i + ")";
            if (MFD.findUFDByname(name) == null)
                break;
        }
        MFD.ufdlist.add(new UFD(name));
        refreshList();
    }

    private void create_File() {
        for (UFD u : MFD.ufdlist) {
            if (u.username.equals(MFD.path.get(1))) {
                u.filelist.add(u.createFile(MFD.getPath()));
                break;
            }
        }
        refreshList();
        table1.editCellAt(table1.getRowCount() - 1, 0);
    }

    private void rename() {

    }

    public static void main(String[] args) {
        //设置UI风格为当前系统风格
        try {

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("文件管理");
        frame.setContentPane(new GUI().rootpane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
