package example;

public class EchoServiceImpl implements EchoService {
  @Override public EchoMessage echo(EchoMessage request) {
    return new EchoMessage(request.value);
  }
}
