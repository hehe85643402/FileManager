import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;

public class GUI {


    private JPanel rootpane;
    private JTable table1;
    private JScrollPane jsp;
    private JButton reButton;
    private JLabel pathLabel;
    private JPanel topPanel;
    private Vector<String> columnNname = new Vector();
    private Vector<Vector> data = new Vector();
    private JPopupMenu popupMenu = new JPopupMenu();
    private DefaultTableModel model;
    private Color focuscolor=new Color(0xC6E2FF);
    private JTextField jtf=new JTextField();
    public GUI() {
        //设置当前路径为根目录
        MFD.openPath("root");

        columnNname.add("名称");
        columnNname.add("类型");
        model = new DefaultTableModel(data, columnNname){
            @Override
            public boolean isCellEditable(int row, int column) {

                if(column==0)
                    return true;
                else
                    return false;

            }
        };
        table1.setModel(model);
        DefaultCellEditor dfc=new DefaultCellEditor(jtf);
        dfc.setClickCountToStart(1);
        table1.setDefaultEditor(table1.getColumnClass(0),dfc);
        /*
        table1=new JTable(model);
        table1.setPreferredSize(new Dimension(300,400));
        jsp.setViewportView(table1);
        jsp.setPreferredSize(new Dimension(300,400));
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jtf.setText("test");
        DefaultCellEditor dce = new DefaultCellEditor(jtf);
        dce.setClickCountToStart(1);

        table1.getColumn("名称").setCellEditor(dce);
        */
        //设置排序
        /*
        final TableRowSorter sorter = new TableRowSorter(model);
        table1.setRowSorter(sorter);*/
        //右键菜单新建文件夹
        JMenuItem createDir = new JMenuItem("新建文件夹");
        popupMenu.add(createDir);
        createDir.addActionListener(e -> {
            createDir();
        });

        //右键菜单重命名
        JMenuItem rename = new JMenuItem("重命名");
        rename.addActionListener(e -> {

        });
        //右键菜单删除
        JMenuItem delete = new JMenuItem("删除");
        delete.addActionListener(e -> {

        });
        //右键新建文件
        JMenuItem createFile = new JMenuItem("新建文件");
        createFile.addActionListener(e -> {
            create_File();
        });

        //右键菜单显示
        table1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);

            }
        });
        table1.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            //修改焦点所在行颜色
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                System.out.println(e.getPoint());
                int focusedRowIndex = table1.rowAtPoint(e.getPoint());
                //鼠标是否超出Jtable边界
                if(!table1.contains(e.getPoint()))
                    setOneRowBackgroundColor(table1,-1,focuscolor);
                else
                    {
                    if(focusedRowIndex!=-1)
                    {
                        //System.out.println("获取焦点"+focusedRowIndex);

                        setOneRowBackgroundColor(table1,focusedRowIndex,focuscolor);
                    }
                    else{
                        setOneRowBackgroundColor(table1,-1,focuscolor);
                    }
                }

            }
        });
        table1.addMouseListener(new MouseAdapter() {
            @Override

            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                int buttonid=e.getButton();
                int clickcount=e.getClickCount();

                if (buttonid == MouseEvent.BUTTON3)
                {
                    popupMenu.removeAll();
                    int focusedRowIndex = table1.rowAtPoint(e.getPoint());
                    if (focusedRowIndex != -1) {//是否选中文件或文件夹
                        if (MFD.path.size() == 1)//只在根目录显示
                            popupMenu.add(createDir);
                        popupMenu.add(rename);
                        popupMenu.add(delete);

                    } else {
                        if (MFD.path.size() == 1)//只在根目录显示
                            popupMenu.add(createDir);
                        else//在二级目录显示
                            popupMenu.add(createFile);
                    }
                    popupMenu.show(table1, e.getX(), e.getY());
                }
                else if(buttonid==MouseEvent.BUTTON1 && clickcount == 1){

                    int focusedRowIndex = table1.rowAtPoint(e.getPoint());
                    int focusedColumnIndex=table1.columnAtPoint(e.getPoint());
                    if(focusedRowIndex==-1)
                        table1.clearSelection();
                    else
                        jtf.selectAll();
                    /*鼠标单击实现选中
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

                    }*/
                }
                else if (buttonid == MouseEvent.BUTTON1 && clickcount == 2)
                {
                    int focusedRowIndex = table1.rowAtPoint(e.getPoint());
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
                    else
                        table1.clearSelection();
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

        reButton.setEnabled(false);
        refreshpath();
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
                    }
                    else
                    {
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
        String content="";
        UFD ufd=null;
        for (UFD u : MFD.ufdlist) {
            if (u.username.equals(MFD.path.get(1))) {
                content=u.openFile(name);
                ufd=u;
            }
            break;
        }

        fileEditWindow(name,content,ufd);
    }
    private void fileEditWindow(String name,String content,UFD ufd)
    {
        if (ufd==null)
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

                int options=JOptionPane.showConfirmDialog(frame,"是否在关闭之前保存文件","提示",JOptionPane.YES_NO_CANCEL_OPTION);
                if(options==JOptionPane.YES_OPTION)
                {
                    ufd.xiugaiFilewords(name,text);
                    frame.dispose();
                }
                else if(options==JOptionPane.NO_OPTION)
                {
                    frame.dispose();
                }
            }
        });

        frame.pack();

        frame.setLocationRelativeTo(rootpane);

        frame.setVisible(true);
    }

    private void refreshpath() {
        pathLabel.setText(MFD.getPath());
    }

    private void refreshList() {
        int rowcount=model.getRowCount();
        for(int i=rowcount-1;i>=0;i--)
            model.removeRow(i);

        if (MFD.path.size() == 1) {
            for (UFD u : MFD.ufdlist) {
                Vector Row = new Vector();
                Row.add(u.username);
                Row.add("文件夹");
                model.addRow(Row);
            }
        } else {
            for (UFD u : MFD.ufdlist) {
                if (u.username.equals(MFD.path.get(1))) {
                    for (FCB f : u.filelist) {
                        Vector Row = new Vector();
                        Row.add(f.filename);
                        Row.add("文件");
                        model.addRow(Row);
                    }
                    break;
                }
            }
        }
        table1.updateUI();
        refreshpath();

    }

    private void createDir() {
        String name = JOptionPane.showInputDialog(rootpane,"文件夹名称");
        if (name == null || name.isEmpty())
            return;
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
        table1.editCellAt(table1.getRowCount()-1,0);
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
