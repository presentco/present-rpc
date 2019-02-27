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
    RpcFilter rpcFilter = invocation -> {
      invocation.setHeader(new Header());
      return invocation.proceed();
    };
    EchoService echo = RpcClient.create("http://localhost:" + port,
        Header.class, EchoService.class, rpcFilter);
    EchoResponse response = echo.echo(new EchoRequest(42));
    assertEquals(42, (int) response.value);
    server.stop();
  }

  private Server startServer(int port) throws Exception {
    Server server = new Server(port);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addServletWithMapping(EchoServlet.class, "/*");
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
      return new EchoResponse(request.value);
    }
  }

  public static class EchoServlet extends RpcServlet {{
    service(Header.class, EchoService.class, new EchoServiceImpl(), RpcFilter.NOOP);
  }}
}
