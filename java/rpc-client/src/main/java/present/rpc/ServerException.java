package present.rpc;

/**
 * Indicates an unexpected server error.
 *
 * @author Bob Lee
 */
public class ServerException extends RuntimeException {

  public ServerException(String message) {
    super(message);
  }
}
