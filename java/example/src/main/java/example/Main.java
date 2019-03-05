package example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class Main {
  public static void main(String[] args) throws Exception {
    Server server = new Server(8080);
    ServletHandler handler = new ServletHandler();
    server.setHandler(handler);
    handler.addFilterWithMapping(EchoFilter.class,"/*", 0);
    server.start();
    System.out.println("Listening on http://localhost:8080/...");
    server.join();
  }
}
