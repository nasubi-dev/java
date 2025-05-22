package oop1.kadai06;

public class FullTimeEmployee extends Employee implements CommuteAllowanceCalculable {
  public static final double STANDARD_MONTHLY_HOURS = 160.0;
  public static final double OVERTIME_RATE_MULTIPLIER = 1.25;
  public static final double SOCIAL_INSURANCE_RATE = 0.15;
  public static final double INCOME_TAX_RATE_FULLTIME = 0.10;
  private double overtimeHours;
  private double bonus;
  private double commuteAllowance;

  public FullTimeEmployee(String employeeId, String name, double basePay,
      double overtimeHours, double bonus, double commuteAllowance) {
    super(employeeId, name, basePay);
    this.overtimeHours = overtimeHours;
    this.bonus = bonus;
    this.commuteAllowance = commuteAllowance;
  }

  @Override
  public double calculateGrossPay() {
    return basePay + calculateOvertimePay() + bonus + commuteAllowance;
  }

  @Override
  public double calculateTotalDeductions() {
    return calculateSocialInsurance() + calculateIncomeTax();
  }
  @Override
  public String getEmployeeTypeName() {
    return "正社員";
  }

  @Override
  public double getCommuteAllowance() {
    return commuteAllowance;
  }

  public double calculateOvertimePay() {
    return (basePay / STANDARD_MONTHLY_HOURS) * OVERTIME_RATE_MULTIPLIER * overtimeHours;
  }
  public double getBonus() {
    return bonus;
  }

  public double calculateSocialInsurance() {
    return basePay * SOCIAL_INSURANCE_RATE;
  }

  public double calculateIncomeTax() {
    return calculateGrossPay() * INCOME_TAX_RATE_FULLTIME;
  }

  public double getOvertimeHours() {
    return overtimeHours;
  }
}
