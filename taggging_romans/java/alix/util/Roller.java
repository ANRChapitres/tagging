package alix.util;

/**
 * Efficient Object to handle a sliding window, on different types, works like a
 * circular array.
 * 
 * @author glorieux-f
 *
 */
public abstract class Roller
{
  /** Size of left wing */
  protected final int left;
  /** Size of right wing */
  protected final int right;
  /** Size of the widow */
  protected final int size;
  /** Index of center cell */
  protected int center;

  /**
   * Constructor
   */
  public Roller(final int left, final int right) {
    if (left > 0)
      throw new IndexOutOfBoundsException("Roller, left context should be negative or zero.");
    this.left = left;
    if (right < 0)
      throw new IndexOutOfBoundsException("Roller, right context should be positive or zero.");
    this.right = right;
    size = -left + right + 1;
    center = left;
  }

  /**
   * Return the size of the roller
   * 
   * @return
   */
  public int size()
  {
    return size;
  }

  /**
   * Get pointer on the data array from a position. Will roll around array if out
   * the limits
   */
  protected int pointer(final int pos)
  {
    /*
     * if (pos < -left) throw(new ArrayIndexOutOfBoundsException(
     * pos+" < "+(-left)+", left context size.\n"+this.toString() )); else if (pos >
     * right) throw(new ArrayIndexOutOfBoundsException(
     * pos+" > "+(+right)+", right context size.\n"+this.toString() ));
     */
    return (((center + pos) % size) + size) % size;
  }

}
