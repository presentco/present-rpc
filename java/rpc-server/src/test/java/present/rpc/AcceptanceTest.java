package present.rpc;

import java.io.IOException;
import java.net.ServerSocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AcceptanceTest {

  @Test public void test() throws Exception {
    int port = randomPort();
    Server server = startServer(port);

    String url = "http://localhost:" + port;

    RpcInterceptor interceptor = invocation -> {
      invocation.headers().put("test-header", "test-value");
      return invocation.proceed();
    };

    test(RpcClient.create(url, EchoService.class, interceptor));
    test(RpcClient.create(RpcEncoding.JSON, url, EchoService.class, interceptor));

    server.stop();
  }

  private void test(EchoService echo) throws IOException {
    EchoResponse response = echo.echo(new EchoRequest(42));
    assertEquals(42, (int) response.value);
  }

  private static Server startServer(int port) throws Exception {
    Server server = new Server(port);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addFilterWithMapping(EchoFilter.class,"/*", 0);
    server.start();
    return server;
  }

  /** Finds an open port. */
  private static int randomPort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class EchoServiceImpl implements EchoService {
    @Override public EchoResponse echo(EchoRequest request) {
      assertEquals("test-value", RpcInvocation.current().headers().get("test-header"));
      return new EchoResponse(request.value);
    }
  }

  public static class EchoFilter extends RpcFilter {{
    service(EchoService.class, new EchoServiceImpl(), null);
  }}

  public static void main(String[] args) throws Exception {
    Server server = startServer(8080);
    server.join();
  }
}
