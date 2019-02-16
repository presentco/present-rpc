package present.rpc;

import static present.rpc.Error.Type.SERVER;

/**
 * @author Bob Lee (bob@present.co)
 */
public class ServerException extends RpcException {

  public ServerException(String message) {
    super(SERVER, message);
  }

  public ServerException(Exception e) {
    super(SERVER, e);
  }
}
