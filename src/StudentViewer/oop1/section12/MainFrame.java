package section12;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainFrame extends JFrame {
    private JPanel panel1;
    private JTree tree1;
    private JTable table1;
    private JScrollPane tree;
    private JScrollPane table;
    
    private Document xmlDocument;
    
    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("学生ビューア");
        setSize(800, 600);

        initializeComponents();

        String[] columnNames = {"学籍番号", "氏名", "学年"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        table1.setModel(tableModel);

        tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree1.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree1.getLastSelectedPathComponent();
            if (selectedNode != null) {
                updateTable(selectedNode);
            }
        });

        loadXMLFile();
    }
    
    private void initializeComponents() {
        panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));

        tree1 = new JTree();
        table1 = new JTable();

        tree = new JScrollPane(tree1);
        table = new JScrollPane(table1);

        tree.setPreferredSize(new Dimension(300, 500));
        table.setPreferredSize(new Dimension(480, 500));

        panel1.add(tree);
        panel1.add(table);
        
        setContentPane(panel1);
    }
    
    private void loadXMLFile() {
        File xmlFile = new File("university_data.xml");
        if (!xmlFile.exists()) {
            JOptionPane.showMessageDialog(this, 
                "university_data.xmlファイルが見つかりません。", 
                "エラー", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(xmlFile);
            xmlDocument.getDocumentElement().normalize();
            
            buildTree();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "XMLファイルの読み込みに失敗しました: " + e.getMessage(), 
                "エラー", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void buildTree() {
        Element documentElement = xmlDocument.getDocumentElement();
        String universityName = documentElement.getAttribute("name");
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(universityName);
        
        NodeList faculties = documentElement.getElementsByTagName("faculty");
        
        for (int i = 0; i < faculties.getLength(); i++) {
            Element faculty = (Element) faculties.item(i);
            String facultyName = faculty.getAttribute("name");
            DefaultMutableTreeNode facultyNode = new DefaultMutableTreeNode(facultyName);
            
            NodeList departments = faculty.getElementsByTagName("department");
            for (int j = 0; j < departments.getLength(); j++) {
                Element department = (Element) departments.item(j);
                String departmentName = department.getAttribute("name");

                DefaultMutableTreeNode departmentNode = new DefaultMutableTreeNode(departmentName) {
                    private Element element = department;
                    
                    public Element getElement() {
                        return element;
                    }
                };
                
                facultyNode.add(departmentNode);
            }
            
            root.add(facultyNode);
        }
        
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        tree1.setModel(treeModel);

        for (int i = 0; i < tree1.getRowCount(); i++) {
            tree1.expandRow(i);
        }
    }
    
    private void updateTable(DefaultMutableTreeNode selectedNode) {
        DefaultTableModel tableModel = (DefaultTableModel) table1.getModel();
        tableModel.setRowCount(0);

        Element element = null;
        if (selectedNode.getClass().getName().contains("$")) {
            try {
                java.lang.reflect.Method getElementMethod = selectedNode.getClass().getMethod("getElement");
                element = (Element) getElementMethod.invoke(selectedNode);
            } catch (Exception e) {
                // リフレクションに失敗した場合は何もしない
            }
        }
        
        if (element != null) {
            NodeList students = element.getElementsByTagName("student");
            
            for (int i = 0; i < students.getLength(); i++) {
                Element student = (Element) students.item(i);
                String studentId = student.getAttribute("id");

                String name = "";
                String grade = "";
                
                NodeList nameNodes = student.getElementsByTagName("name");
                if (nameNodes.getLength() > 0) {
                    name = nameNodes.item(0).getTextContent();
                }
                
                NodeList gradeNodes = student.getElementsByTagName("grade");
                if (gradeNodes.getLength() > 0) {
                    grade = gradeNodes.item(0).getTextContent();
                }
                
                Object[] rowData = {studentId, name, grade};
                tableModel.addRow(rowData);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
