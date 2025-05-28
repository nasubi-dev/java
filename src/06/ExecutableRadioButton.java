import javax.swing.JRadioButton;

public class ExecutableRadioButton extends JRadioButton {
  private final Executable executable;

  public ExecutableRadioButton(String text, Executable executable) {
    super(text);
    this.executable = executable;
  }

  public ExecutableRadioButton(String text, Executable executable, boolean selected) {
    super(text, selected);
    this.executable = executable;
  }

  public Executable getExecutable() {
    return executable;
  }
}