package oop1.kadai06;

public abstract class Employee {
  protected String employeeId;
  protected String name;
  protected double basePay;

  public Employee(String employeeId, String name, double basePay) {
    this.employeeId = employeeId;
    this.name = name;
    this.basePay = basePay;
  }

  public abstract double calculateGrossPay();

  public abstract double calculateTotalDeductions();

  public abstract String getEmployeeTypeName();

  public String getEmployeeId() {
    return employeeId;
  }

  public String getName() {
    return name;
  }

  public double getBasePay() {
    return basePay;
  }

  public double calculateNetPay() {
    return calculateGrossPay() - calculateTotalDeductions();
  }
}
