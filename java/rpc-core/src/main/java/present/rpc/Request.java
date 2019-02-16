// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: present/rpc.proto at 7:1
package present.rpc;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import okio.ByteString;

/**
 * Request to invoke an RPC.
 */
public final class Request extends Message<Request, Request.Builder> {
  public static final ProtoAdapter<Request> ADAPTER = new ProtoAdapter_Request();

  private static final long serialVersionUID = 0L;

  public static final ByteString DEFAULT_HEADER = ByteString.EMPTY;

  public static final ByteString DEFAULT_ARGUMENT = ByteString.EMPTY;

  /**
   * Application-specific headers
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#BYTES"
  )
  public final ByteString header;

  /**
   * Argument to decode and pass to the RPC method
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#BYTES",
      label = WireField.Label.REQUIRED
  )
  public final ByteString argument;

  public Request(ByteString header, ByteString argument) {
    this(header, argument, ByteString.EMPTY);
  }

  public Request(ByteString header, ByteString argument, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.header = header;
    this.argument = argument;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.header = header;
    builder.argument = argument;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof Request)) return false;
    Request o = (Request) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(header, o.header)
        && argument.equals(o.argument);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (header != null ? header.hashCode() : 0);
      result = result * 37 + argument.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (header != null) builder.append(", header=").append(header);
    builder.append(", argument=").append(argument);
    return builder.replace(0, 2, "Request{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<Request, Builder> {
    public ByteString header;

    public ByteString argument;

    public Builder() {
    }

    /**
     * Application-specific headers
     */
    public Builder header(ByteString header) {
      this.header = header;
      return this;
    }

    /**
     * Argument to decode and pass to the RPC method
     */
    public Builder argument(ByteString argument) {
      this.argument = argument;
      return this;
    }

    @Override
    public Request build() {
      if (argument == null) {
        throw Internal.missingRequiredFields(argument, "argument");
      }
      return new Request(header, argument, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_Request extends ProtoAdapter<Request> {
    ProtoAdapter_Request() {
      super(FieldEncoding.LENGTH_DELIMITED, Request.class);
    }

    @Override
    public int encodedSize(Request value) {
      return (value.header != null ? ProtoAdapter.BYTES.encodedSizeWithTag(3, value.header) : 0)
          + ProtoAdapter.BYTES.encodedSizeWithTag(4, value.argument)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, Request value) throws IOException {
      if (value.header != null) ProtoAdapter.BYTES.encodeWithTag(writer, 3, value.header);
      ProtoAdapter.BYTES.encodeWithTag(writer, 4, value.argument);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public Request decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 3: builder.header(ProtoAdapter.BYTES.decode(reader)); break;
          case 4: builder.argument(ProtoAdapter.BYTES.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public Request redact(Request value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
