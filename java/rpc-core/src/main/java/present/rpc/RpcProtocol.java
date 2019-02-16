package present.rpc;

public enum RpcProtocol {
  JSON("application/json"),
  PROTO("application/x-protobuf");

  public final String contentType;

  RpcProtocol(String contentType) {
    this.contentType = contentType;
  }
}
