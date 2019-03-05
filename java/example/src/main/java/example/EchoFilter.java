package example;

import present.rpc.RpcFilter;

public class EchoFilter extends RpcFilter {{
  service(EchoService.class, new EchoServiceImpl(), null);
}}
