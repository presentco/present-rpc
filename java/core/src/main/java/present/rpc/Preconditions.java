package present.rpc;

class Preconditions {

  static <T> T checkNotNull(T o) {
    if (o == null) throw new NullPointerException();
    return o;
  }

  static void checkArgument(boolean expression) {
    if (!expression) throw new IllegalArgumentException();
  }
}
