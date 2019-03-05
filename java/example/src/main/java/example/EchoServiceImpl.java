package example;

public class EchoServiceImpl implements EchoService {
  @Override public EchoResponse echo(EchoRequest request) {
    return new EchoResponse(request.value);
  }
}
