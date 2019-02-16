package present.rpc;

import com.google.gson.JsonElement;

public class JsonRequest {

  /*
    Corresponding protocol buffer:

    // Request to invoke an RPC.
    message Request {
      // Application-specific headers
      optional bytes header = 3;

      // Argument to decode and pass to the RPC method
      required bytes argument = 4;
    }
  */

  public final JsonElement header;
  public final JsonElement argument;

  public JsonRequest(JsonElement header, JsonElement argument) {
    this.header = header;
    this.argument = argument;
  }

  private JsonRequest() {
    header = null;
    argument = null;
  }
}
