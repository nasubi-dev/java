package oop1.section12;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.awt.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
  private JTree tree;
  private JTable table;
  private DefaultTableModel tableModel;
  private DefaultMutableTreeNode rootNode;
  private Document document;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      new MainFrame().setVisible(true);
    });
  }

  public MainFrame() {
    setTitle("学生名簿");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLocationRelativeTo(null);

    if (!loadXMLFile()) {
      JOptionPane.showMessageDialog(this, "university_data.xmlファイルが見つかりません。",
          "エラー", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    initializeComponents();
    buildTreeFromXML();
    layoutComponents();
  }

  private boolean loadXMLFile() {
    try {
      File xmlFile = new File("university_data.xml");
      if (!xmlFile.exists()) {
        return false;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(xmlFile);
      document.getDocumentElement().normalize();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private void initializeComponents() {
    rootNode = new DefaultMutableTreeNode("愛知工業大学");
    tree = new JTree(rootNode);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (selectedNode != null) {
          updateTable(selectedNode);
        }
      }
    });

    String[] columnNames = { "学生番号", "氏名", "学年" };
    tableModel = new DefaultTableModel(columnNames, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    table = new JTable(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  private void buildTreeFromXML() {
    try {
      Element root = document.getDocumentElement();

      NodeList faculties = root.getElementsByTagName("faculty");
      for (int i = 0; i < faculties.getLength(); i++) {
        Element faculty = (Element) faculties.item(i);
        String facultyName = faculty.getAttribute("name");
        DefaultMutableTreeNode facultyNode = new DefaultMutableTreeNode(facultyName);
        rootNode.add(facultyNode);

        NodeList departments = faculty.getElementsByTagName("department");
        for (int j = 0; j < departments.getLength(); j++) {
          Element department = (Element) departments.item(j);
          String departmentName = department.getAttribute("name");
          DefaultMutableTreeNode departmentNode = new DefaultMutableTreeNode(departmentName);
          facultyNode.add(departmentNode);

          NodeList students = department.getElementsByTagName("student");
          List<Student> studentList = new ArrayList<>();
          for (int k = 0; k < students.getLength(); k++) {
            Element student = (Element) students.item(k);
            String id = student.getAttribute("id");

            NodeList nameNodes = student.getElementsByTagName("name");
            NodeList gradeNodes = student.getElementsByTagName("grade");

            if (nameNodes.getLength() > 0 && gradeNodes.getLength() > 0) {
              String name = nameNodes.item(0).getTextContent();
              int grade = Integer.parseInt(gradeNodes.item(0).getTextContent());
              studentList.add(new Student(id, name, grade));
            }
          }
          departmentNode.setUserObject(new DepartmentData(departmentName, studentList));
        }
      }

      for (int i = 0; i < tree.getRowCount(); i++) {
        tree.expandRow(i);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void updateTable(DefaultMutableTreeNode node) {
    tableModel.setRowCount(0);

    Object userObject = node.getUserObject();
    if (userObject instanceof DepartmentData) {
      DepartmentData departmentData = (DepartmentData) userObject;
      for (Student student : departmentData.getStudents()) {
        Object[] row = { student.getId(), student.getName(), student.getGrade() };
        tableModel.addRow(row);
      }
    }
  }

  private void layoutComponents() {
    setLayout(new BorderLayout());

    JScrollPane treeScrollPane = new JScrollPane(tree);
    treeScrollPane.setPreferredSize(new Dimension(240, 0));
    treeScrollPane.setBorder(BorderFactory.createTitledBorder("大学構造"));

    JScrollPane tableScrollPane = new JScrollPane(table);
    tableScrollPane.setBorder(BorderFactory.createTitledBorder("学生一覧"));

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        treeScrollPane, tableScrollPane);
    splitPane.setDividerLocation(240);
    splitPane.setResizeWeight(0.3);
    splitPane.setContinuousLayout(true);

    add(splitPane, BorderLayout.CENTER);
  }

  private static class Student {
    private String id;
    private String name;
    private int grade;

    public Student(String id, String name, int grade) {
      this.id = id;
      this.name = name;
      this.grade = grade;
    }

    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getGrade() {
      return grade;
    }
  }

  private static class DepartmentData {
    private String name;
    private List<Student> students;

    public DepartmentData(String name, List<Student> students) {
      this.name = name;
      this.students = students;
    }

    public String getName() {
      return name;
    }

    public List<Student> getStudents() {
      return students;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
