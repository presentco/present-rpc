package present.rpc;

/**
 * @author Bob Lee (bob@present.co)
 */
public class RpcException extends RuntimeException {

  private final Error.Type type;

  public RpcException(Error.Type type, String message) {
    super(message);
    this.type = type;
  }

  public RpcException(Error.Type type, Exception e) {
    super(e);
    this.type = type;
  }

  public Error.Type getType() {
    return type;
  }
}
