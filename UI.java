package FileSystem;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.*;

import FileSystem.FileSystem;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hongjiayong on 16/6/7.
 */
public class UI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTree tree;
    private JScrollPane treePane;
    private JScrollPane tablePane;
    private tableModel model = new tableModel();
    private JTable fileTable;
    private JFileChooser chooser;

    private File rootFile;
    private File readMe;

    private Block block1;
    private Block block2;
    private Block block3;
    private Block block4;
    private Map<String, Block> blocks = new HashMap<String, Block>();

    private JLabel blockName = new JLabel("Block Name:");
    private JLabel nameField = new JLabel();
    private JLabel haveUsed = new JLabel("Used:");
    private JLabel usedField = new JLabel();
    private JLabel freeYet = new JLabel("Free:");
    private JLabel freeField = new JLabel();
    private JLabel fileNum = new JLabel("Block's File Number:");
    private JLabel fileNumField = new JLabel();
    
    
    private JTextField txtFilePath = new JTextField();
    private int fileTreeNodeSize = 20;
    private DefaultMutableTreeNode[] fileTreeNode = new DefaultMutableTreeNode[fileTreeNodeSize]; 
    private int fp_top = 0, fp_end = 0, fp = 0;
    
    private JTextField searchLine = new JTextField();

    private static String helpMessage =
            "<html>" +
                    "<body>" +
                    "<h1>文件系统模拟</h1>" +
                    "<h2>技术细节</h2>" +
                    "<h3>显式链接(FAT)</h3>" +
                    "<ul> <li>用一个专用文件记录分配和未分配的内存块</li> <li>使用链接的方式,不存在内存碎片</li> </ul>" +
                    "<h3>空闲空间管理 —— 位图</h3>" +
                    "<ul> <li>用二进制0和1分别代表未分配的内存块和已经分配的内存块</li> <li>在该项目中位图和FAT进行了合并</li> </ul>" +
                    "<h3>目录结构 —— 多级目录结构</h3>" +
                    "<h3>FCB</h3>" +
                    "<ul> <li>文件类型</li> <li>文件名</li> <li>文件大小</li> <li>文件最近更新时间</li> </ul>" +
                    "<h2>操作说明</h2>" +
                    "<ul> <li>必须先选择一个计算机上的文件夹作为模拟工作目录</li> <li>左侧树状结构即文件目录</li> <li>双击或点击文件夹左侧小图标可以打开文件目录</li> " +
                    "<li>右侧空白处为表格区域,将现实相关文件信息</li> <li>双击表格中的项可以直接打开相关文件</li>" +
                    "<li>下方绿色为盘信息面板,将现实相应盘的相应信息</li>" +
                    "<li>最下方空白处将显示内存块当前状况</li> </ul>" +
                    "<li>在树状结构中选中某一节点,右键即可选择相应的文件操作</li>" +
                    "<li>创建新的文件会要求输入文件名和文件大小(KB)</li>" +
                    "<h2>特别说明</h2>" +
                    "<ul> <li>本程序重在模拟,并不是真正地为文件开了这么大的空间</li> <li>仅支持生成txt,文本文件中直接现实FCB,不支持修改内容</li>" +
                    "<li>对于非法输入都会直接导致文件生成失败</li> <li>如果存档文件recover.txt被破坏,将无法打开文件</li></ul>" +
                    "</body>" +
                    "</html>";

    
    private void insertFileTreeNode(DefaultMutableTreeNode fNode){
    	fileTreeNode[fp] = fNode;
    	if(fp == fp_end){
    		if(fp == fileTreeNodeSize - 1){
    			fp = fp_end = 0;
    		}
    		else{
    			fp++;
    			fp_end++;
    		}
    		if(fp_top == fp_end){
				fp_top++;
			}
    	}
    	else{
    		if(fp == fileTreeNodeSize - 1){
    			fp = 0;
    		}else{
    			fp++;
    		}  	
    	}
    }
    
    
    // Search a file
    public boolean searchFile(String fileName, File parent){
    	boolean find = false;
        File [] files = parent.listFiles();
        for (File myFile:files){
            if (myFile.getName().equals(fileName)){
            	find = true;
				model.addRow(new MyFile(myFile, "0", Block.getSpace(myFile)));		//暂时放在0盘
            }
            if (myFile.isDirectory() && myFile.canRead()){
            	if(!find){
            		find = searchFile(fileName, myFile);
            	}
            	else{
            		searchFile(fileName, myFile);
            	}
            }
        }
        return find;
    }
    

    // Update block's information
     public void upDateBlock(Block currentBlock){
        fileNumField.setText(String.valueOf(currentBlock.getFileNum()));
        usedField.setText(String.valueOf(currentBlock.getSpace()) + " KB");
        freeField.setText(String.valueOf(1024 - currentBlock.getSpace()) + "KB");
    }
     
    
    private void upDateTree(DefaultMutableTreeNode parent, boolean insertFile){
    	if(parent == null) return;
    	
    	String blokName = ((MyFile)parent.getUserObject()).getBlockName();
        Block currentBlock = blocks.get(blokName);

        nameField.setText(String.valueOf(blokName));
        upDateBlock(currentBlock);
        
        String rootPath = ((MyFile)parent.getUserObject()).getFilePath();
        File rootFile = new File(rootPath);
        if (parent.getChildCount() > 0) {	//is a dir
        	model.removeRows(0, model.getRowCount());
            File[] childFiles = rootFile.listFiles();

            for (File file : childFiles) {
                model.addRow(new MyFile(file, blokName, Block.getSpace(file)));
            }
            fileTable.updateUI();
            
            if(insertFile){
            	insertFileTreeNode(parent);
            }
            txtFilePath.setText(rootPath);
        }
    }
    
    
    private void initJFileChooser(String rootPath){
    	String path = File.listRoots()[0].getPath();
        chooser = new JFileChooser(path);
        chooser.setDialogTitle("Choose a dir for this demo");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setPreferredSize(new Dimension(800, 600));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION){
            rootPath = chooser.getSelectedFile().getPath();
        }
    }
    
    
    private void initJOptionPane(String message, String title){
    	JLabel label = new JLabel(message);
        label.setFont(new Font("微软雅黑", Font.CENTER_BASELINE, 20));
        JScrollPane jScrollPane = new JScrollPane(label);
        jScrollPane.setPreferredSize(new Dimension(500, 600));
        JOptionPane.showMessageDialog(null,
                jScrollPane,
                title,
                JOptionPane.DEFAULT_OPTION);
    }
  
    
    private void initSearchLine(){
    	JPanel searchPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	
    	ImageIcon back_icon = new ImageIcon("E:/Java/MyFileSystem/src/back_icon.jpg");
    	JButton back = new JButton(back_icon);
    	back.setPreferredSize(new Dimension(back_icon.getImage().getWidth(null), back_icon.getImage().getHeight(null)));
    	back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(fp == fp_top) return;
            	
            	DefaultMutableTreeNode parent = null;
            	if(fp == 0){
            		fp = fileTreeNodeSize-1;
            		parent = fileTreeNode[fp];
            	}else{
            		parent = fileTreeNode[--fp];
            	}
            	upDateTree(parent, false);
            }
        });
    	searchPane.add(back);
    	
    	ImageIcon front_icon = new ImageIcon("E:/Java/MyFileSystem/src/front_icon.jpg");
    	JButton front = new JButton(front_icon);
    	front.setPreferredSize(new Dimension(front_icon.getImage().getWidth(null), front_icon.getImage().getHeight(null)));
    	front.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	if(fp == fp_end) return;
            	
            	DefaultMutableTreeNode parent = null;
            	if(fp == fileTreeNodeSize-1){
            		fp = 0;
            		parent = fileTreeNode[fp];
            	}else{
            		parent = fileTreeNode[++fp];
            	}
            	upDateTree(parent, false);
            }
        });
    	searchPane.add(front);
    	
    	searchPane.add(txtFilePath);
    	txtFilePath.setPreferredSize(new Dimension(500, 30));
    	
        final JLabel searchLabel = new JLabel("Search ");	//(eg. File:hehe.txt Dir:hehe):
        searchPane.add(searchLabel);
        searchLine.setPreferredSize(new Dimension(200, 30));
        searchPane.add(searchLine);
        JButton searchButton = new JButton("start");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = searchLine.getText();
                model.removeRows(0, model.getRowCount());
                if(!searchFile(fileName, rootFile)){
                    JOptionPane.showMessageDialog(null, "检索失败!", "Fail!", JOptionPane.WARNING_MESSAGE);
                }
                fileTable.updateUI();
                searchLine.setText("");
            }
        });
        searchPane.add(searchButton);
        getContentPane().add(searchPane, BorderLayout.NORTH);
    }


    // Ui
    public UI() throws IOException {
        setTitle("File System Demo by 1552718 韦曾妮");
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        

        String rootPath = new String();   
        initJFileChooser(rootPath);
        initJOptionPane(helpMessage, "文件系统模拟");   
        initSearchLine();

        // Create work space
        rootFile = new File(rootPath + File.separator + "myFileSystem");
        readMe = new File(rootPath + File.separator + "myFileSystem" + File.separator + "ReadMe.txt");
       

        boolean flag = true;

        // JTree init
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(new MyFile(rootFile, "0", 10240));
        
        insertFileTreeNode(root);
        txtFilePath.setText(((MyFile)root.getUserObject()).getFileName()); 
        
        if (!rootFile.exists()) {
            flag = false;
            try {
                rootFile.mkdir();
                readMe.createNewFile();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "The place is not support to create dir!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
            FileWriter writer = new FileWriter(readMe.getPath());
            writer.write("你好, 这是我的文件系统!!! \t\n");
            writer.write("空间: 10 * 1024K = 10M \t\n");
            writer.write("空闲空间管理:bitmap \t\n");
            writer.write("存储块管理:FAT \t\n");
            writer.flush();
            writer.close();
        }
        
        String block1Path = rootFile.getPath() + File.separator + "C盘";
        block1 = new Block("C盘", new File(block1Path), flag);
        blocks.put("C盘", block1);
        String block2Path = rootFile.getPath() + File.separator + "D盘";
        block2 = new Block("D盘", new File(block2Path), flag);
        blocks.put("D盘", block2);
        String block3Path = rootFile.getPath() + File.separator + "E盘";
        block3 = new Block("E盘", new File(block3Path), flag);
        blocks.put("E盘", block3);
        String block4Path = rootFile.getPath() + File.separator + "F盘";
        block4 = new Block("F盘", new File(block4Path), flag);
        blocks.put("F盘", block4);
        
        
        root.add(new DefaultMutableTreeNode(new MyFile(block1.getBlockFile(), block1.getBlockName(), 1024.0)));
        model.addRow(new MyFile(block1.getBlockFile(), block1.getBlockName(), 1024.0));
        ((DefaultMutableTreeNode)root.getChildAt(0)).add(new DefaultMutableTreeNode("temp"));

        root.add(new DefaultMutableTreeNode(new MyFile(block2.getBlockFile(), block2.getBlockName(), 1024.0)));
        model.addRow(new MyFile(block2.getBlockFile(), block2.getBlockName(), 1024.0));
        ((DefaultMutableTreeNode)root.getChildAt(1)).add(new DefaultMutableTreeNode("temp"));

        root.add(new DefaultMutableTreeNode(new MyFile(block3.getBlockFile(), block3.getBlockName(), 1024.0)));
        model.addRow(new MyFile(block3.getBlockFile(), block3.getBlockName(), 1024.0));
        ((DefaultMutableTreeNode)root.getChildAt(2)).add(new DefaultMutableTreeNode("temp"));

        root.add(new DefaultMutableTreeNode(new MyFile(block4.getBlockFile(), block4.getBlockName(), 1024.0)));
        model.addRow(new MyFile(block4.getBlockFile(), block4.getBlockName(), 1024.0));
        ((DefaultMutableTreeNode)root.getChildAt(3)).add(new DefaultMutableTreeNode("temp"));

        root.add(new DefaultMutableTreeNode(new MyFile(readMe, "0", 0)));
        model.addRow(new MyFile(readMe, "0", 0));

        

        // Table init
        fileTable = new JTable(model);
        fileTable.getTableHeader().setFont(new Font(Font.DIALOG,Font.CENTER_BASELINE,24));
        fileTable.setSelectionBackground(Color.ORANGE);

        fileTable.updateUI();

        final DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setEditable(false);
        tree.putClientProperty("Jtree.lineStyle",  "Horizontal");
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = e.getPath();
                if (parentPath == null){
                    parent = root;
                }else{
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }
                upDateTree(parent, true);
            }
        });      
        
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = event.getPath();
                if (parentPath == null){
                    parent = root;
                }else{
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }

                String blokName = ((MyFile)parent.getUserObject()).getBlockName();
                
                String rootPath = ((MyFile)parent.getUserObject()).getFilePath();
                insertFileTreeNode(parent);
                txtFilePath.setText(rootPath);

                File rootFile = new File(((MyFile)parent.getUserObject()).getFilePath());
                File [] childFiles = rootFile.listFiles();

                model.removeRows(0, model.getRowCount());
                for (File myFile : childFiles){
                    DefaultMutableTreeNode node = null;
                    node = new DefaultMutableTreeNode(new MyFile(myFile, blokName, Block.getSpace(myFile)));
                    if (myFile.isDirectory() && myFile.canRead()) {
                        node.add(new DefaultMutableTreeNode("temp"));
                    }

                    treeModel.insertNodeInto(node, parent, parent.getChildCount());
                    model.addRow(new MyFile(myFile, blokName, Block.getSpace(myFile)));
                }
                if (parent.getChildAt(0).toString().equals("temp") && parent.getChildCount() != 1)
                    treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(0));
                fileTable.updateUI();
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode parent = null;
                TreePath parentPath = event.getPath();
                if (parentPath == null){
                    parent = root;
                }else{
                    parent = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                }
                if (parent.getChildCount() > 0) {
                    int count = parent.getChildCount();
                    for (int i = count - 1; i >= 0; i--){
                        treeModel.removeNodeFromParent((MutableTreeNode) parent.getChildAt(i));
                    }
                    treeModel.insertNodeInto(new DefaultMutableTreeNode("temp"), parent, parent.getChildCount());
                }
                model.removeRows(0, model.getRowCount());
                fileTable.updateUI();
            }
        });
        
        
        treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(200, 400));
        getContentPane().add(treePane, BorderLayout.WEST);

        tablePane = new JScrollPane(fileTable);
        getContentPane().add(tablePane, BorderLayout.CENTER);


        
        // Mouse DoubleClick to open a file
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
                    String fileName = ((String) model.getValueAt(fileTable.getSelectedRow(), 0));
                    String filePath = ((String) model.getValueAt(fileTable.getSelectedRow(), 1));
                    
                    try {
                        if(Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(new File(filePath));
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "抱歉, 出错了!", "不能打开",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    
                }
            }
        });

        
        // Menu init
        final JPopupMenu myMenu = new JPopupMenu();
        myMenu.setPreferredSize(new Dimension(300, 200));

        
        // Create a file and update fileTable to show it
        JMenuItem createFileItem = new JMenuItem("新建文件");
        createFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                MyFile temp = (MyFile)node.getUserObject();
                String blokName = temp.getBlockName();
                Block currentBlock = blocks.get(blokName);

                String inputValue;
                double capacity;

                JOptionPane inputPane = new JOptionPane();
                inputPane.setPreferredSize(new Dimension(600, 600));
                inputPane.setInputValue(JOptionPane.showInputDialog("文件名:"));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                inputValue = inputPane.getInputValue().toString();
                inputPane.setInputValue(JOptionPane.showInputDialog("大小(KB):"));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                capacity = Double.parseDouble(inputPane.getInputValue().toString());

                File newFile = new File(temp.getFilePath() + File.separator + inputValue + ".txt");
                if (!newFile.exists() && !inputValue.equals(null)){
                    try {
                        if (currentBlock.createFile(newFile, capacity)) {
                            model.addRow(new MyFile(newFile, blokName, capacity));
                            fileTable.updateUI();
                            upDateBlock(currentBlock);
                            JOptionPane.showMessageDialog(null, "新建文件成功!", "Success", JOptionPane.DEFAULT_OPTION);
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, "新建失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        myMenu.add(createFileItem);

        
        // create a dir and update fileTable to show it
        JMenuItem createDirItem = new JMenuItem("新建文件夹");
        createDirItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                MyFile temp = (MyFile)node.getUserObject();
                String blokName = temp.getBlockName();
                Block currentBlock = blocks.get(blokName);
                String inputValue = JOptionPane.showInputDialog("文件夹名称:");
                if (inputValue == null) {
                    return;
                }
                File newDir = new File(temp.getFilePath() + File.separator + inputValue);
                if (newDir.exists())
                	FileSystem.deleteDirectory(newDir.getPath());
                try{
                    newDir.mkdir();
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new MyFile(newDir, blokName, 0));
                    newNode.add(new DefaultMutableTreeNode("temp")); 
                    model.addRow(new MyFile(newDir, blokName, 0));
                    fileTable.updateUI();
                    upDateBlock(currentBlock);
                    JOptionPane.showMessageDialog(null, "新建文件夹成功! ", "Success", JOptionPane.DEFAULT_OPTION);
                }catch (Exception E){
                    JOptionPane.showMessageDialog(null, "新建失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        myMenu.add(createDirItem);

        
        // Delete a file or a dir
        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                MyFile temp = (MyFile)node.getUserObject();
                String blokName = temp.getBlockName();
                Block currentBlock = blocks.get(blokName);
                int choose = JOptionPane.showConfirmDialog(null, "你确定要删除这个文件/文件夹?", "confirm", JOptionPane.YES_NO_OPTION);
                if (choose == 0){
                    if (currentBlock.deleteFile(temp.getMyFile(), temp.getCapacity())){
                        try {
                            currentBlock.rewriteBitMap();
                            currentBlock.rewriteRecoverWriter();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        upDateBlock(currentBlock);
                        treeModel.removeNodeFromParent(node);
                        JOptionPane.showMessageDialog(null, "删除成功！", "Success", JOptionPane.DEFAULT_OPTION);
                    }else{
                        JOptionPane.showMessageDialog(null, "删除失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        myMenu.add(deleteItem);

        
        // Format a dir
        JMenuItem formatItem = new JMenuItem("格式化");
        formatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                MyFile temp = (MyFile)node.getUserObject();
                String blokName = temp.getBlockName();
                Block currentBlock = blocks.get(blokName);
                int choose = JOptionPane.showConfirmDialog(null, "你确定要格式化这个文件夹?", "confirm", JOptionPane.YES_NO_OPTION);
                if (choose == 0){
                    try{
                    if (temp.getMyFile().isDirectory()) {
                        for (File myfile : temp.getMyFile().listFiles()) {
                            currentBlock.deleteFile(myfile, Block.getSpace(myfile));
                        }
                        upDateBlock(currentBlock);
                        JOptionPane.showMessageDialog(null, "格式化成功", "Success", JOptionPane.DEFAULT_OPTION);
                        currentBlock.rewriteBitMap();
                    }
                    }catch (Exception E1){
                        JOptionPane.showMessageDialog(null, "格式化失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        myMenu.add(formatItem);

        
        // Rename a file/dir
        JMenuItem renameItem = new JMenuItem("重命名");
        renameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                MyFile temp = (MyFile)node.getUserObject();
                String blokName = temp.getBlockName();
                Block currentBlock = blocks.get(blokName);

                String inputValue = null;
                JOptionPane inputPane = new JOptionPane();
                inputPane.setInputValue(JOptionPane.showInputDialog("新名字:"));
                if (inputPane.getInputValue() == null) {
                    return;
                }
                inputValue = inputPane.getInputValue().toString();
                try {
                    currentBlock.renameFile(temp.getMyFile(), inputValue, temp.getCapacity());
                    JOptionPane.showMessageDialog(null, "重命名成功", "Success", JOptionPane.DEFAULT_OPTION);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "重命名失败!!!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        myMenu.add(renameItem);
        
        
     // view a file/dir fcb
        JMenuItem viewItem = new JMenuItem("属性");
        viewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                MyFile file = (MyFile)node.getUserObject();
                
                String fileMessage ="<html>" +
                        "<body>" +
                        "<li>文件名：\t\t" + file.getFileName() + "</li>" +
                        "<li>文件类型：\t\ttxt</li>" +
                        "<li>文件地址：\t\t" + file.getFilePath() + "</li>" + 
                        " <li>文件大小：\t\t" + file.getCapacity() + "KB</li>" +
                        "</body>" +
                        "</html>";
                initJOptionPane(fileMessage, file.getFileName() + "的属性");
                
            }
        });
        myMenu.add(viewItem);
        
        
        // Listen to the tree
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON3){
                    myMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        
   

        // Information for the block
        JPanel panel = new JPanel();
        panel.setBackground(Color.green);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel tips = new JLabel("文件操作:选中左侧文件之后右键 打开文件:双击右侧表格内文件");
        panel.add(tips);
        panel.add(blockName);
        nameField.setForeground(Color.RED);
        panel.add(nameField);
        panel.add(new JLabel("  "));
        panel.add(haveUsed);
        usedField.setForeground(Color.RED);
        panel.add(usedField);
        panel.add(new JLabel("  "));
        panel.add(freeYet);
        freeField.setForeground(Color.RED);
        panel.add(freeField);
        panel.add(new JLabel("  "));
        panel.add(fileNum);
        fileNumField.setForeground(Color.RED);
        panel.add(fileNumField);
        getContentPane().add(panel, BorderLayout.SOUTH);
        
        
        setSize(1000, 580);
        setVisible(true);

        
    }

    public static void main(String[] args){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UI frame = new UI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
