package present.rpc;

/**
 * Indicates a problem with the client, as opposed to an unexpected problem with the server.
 */
public class ClientException extends RuntimeException {

  public ClientException(String message) {
    super(message);
  }
}
