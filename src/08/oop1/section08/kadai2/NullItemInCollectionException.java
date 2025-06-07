package oop1.section08.kadai2;

public class NullItemInCollectionException extends InvalidCollectionDataException {

  private final int index;

  public NullItemInCollectionException(String message, int index) {
    super(message);
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
}
