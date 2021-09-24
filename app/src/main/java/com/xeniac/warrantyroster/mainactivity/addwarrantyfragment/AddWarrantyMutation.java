package com.xeniac.warrantyroster.mainactivity.addwarrantyfragment;// AUTO-GENERATED FILE. DO NOT MODIFY.
//
// This class was automatically generated by Apollo GraphQL plugin from the GraphQL queries it found.
// It should not be modified by hand.
//

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.OperationName;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.ResponseField;
import com.apollographql.apollo.api.ScalarTypeAdapters;
import com.apollographql.apollo.api.internal.InputFieldMarshaller;
import com.apollographql.apollo.api.internal.InputFieldWriter;
import com.apollographql.apollo.api.internal.OperationRequestBodyComposer;
import com.apollographql.apollo.api.internal.QueryDocumentMinifier;
import com.apollographql.apollo.api.internal.ResponseFieldMapper;
import com.apollographql.apollo.api.internal.ResponseFieldMarshaller;
import com.apollographql.apollo.api.internal.ResponseReader;
import com.apollographql.apollo.api.internal.ResponseWriter;
import com.apollographql.apollo.api.internal.SimpleOperationResponseParser;
import com.apollographql.apollo.api.internal.UnmodifiableMapBuilder;
import com.apollographql.apollo.api.internal.Utils;

import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;

import org.jetbrains.annotations.NotNull;

public final class AddWarrantyMutation implements Mutation<AddWarrantyMutation.Data, AddWarrantyMutation.Data, AddWarrantyMutation.Variables> {
    public static final String OPERATION_ID = "1acf5ce2aec7a26b2301daffb2a5e28ee2a377e2145324872f068349d015d503";

    public static final String QUERY_DOCUMENT = QueryDocumentMinifier.minify(
            "mutation addWarranty($warranty: AddWarrantyInput!) {\n"
                    + "  addWarranty(input: $warranty) {\n"
                    + "    __typename\n"
                    + "    title\n"
                    + "  }\n"
                    + "}"
    );

    public static final OperationName OPERATION_NAME = new OperationName() {
        @Override
        public String name() {
            return "addWarranty";
        }
    };

    private final AddWarrantyMutation.Variables variables;

    public AddWarrantyMutation(@NotNull AddWarrantyInput warranty) {
        Utils.checkNotNull(warranty, "warranty == null");
        variables = new AddWarrantyMutation.Variables(warranty);
    }

    @Override
    public String operationId() {
        return OPERATION_ID;
    }

    @Override
    public String queryDocument() {
        return QUERY_DOCUMENT;
    }

    @Override
    public AddWarrantyMutation.Data wrapData(AddWarrantyMutation.Data data) {
        return data;
    }

    @Override
    public AddWarrantyMutation.Variables variables() {
        return variables;
    }

    @Override
    public ResponseFieldMapper<AddWarrantyMutation.Data> responseFieldMapper() {
        return new Data.Mapper();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public OperationName name() {
        return OPERATION_NAME;
    }

    @Override
    @NotNull
    public Response<AddWarrantyMutation.Data> parse(@NotNull final BufferedSource source,
                                                    @NotNull final ScalarTypeAdapters scalarTypeAdapters) throws IOException {
        return SimpleOperationResponseParser.parse(source, this, scalarTypeAdapters);
    }

    @Override
    @NotNull
    public Response<AddWarrantyMutation.Data> parse(@NotNull final ByteString byteString,
                                                    @NotNull final ScalarTypeAdapters scalarTypeAdapters) throws IOException {
        return parse(new Buffer().write(byteString), scalarTypeAdapters);
    }

    @Override
    @NotNull
    public Response<AddWarrantyMutation.Data> parse(@NotNull final BufferedSource source) throws
            IOException {
        return parse(source, ScalarTypeAdapters.DEFAULT);
    }

    @Override
    @NotNull
    public Response<AddWarrantyMutation.Data> parse(@NotNull final ByteString byteString) throws
            IOException {
        return parse(byteString, ScalarTypeAdapters.DEFAULT);
    }

    @Override
    @NotNull
    public ByteString composeRequestBody(@NotNull final ScalarTypeAdapters scalarTypeAdapters) {
        return OperationRequestBodyComposer.compose(this, false, true, scalarTypeAdapters);
    }

    @NotNull
    @Override
    public ByteString composeRequestBody() {
        return OperationRequestBodyComposer.compose(this, false, true, ScalarTypeAdapters.DEFAULT);
    }

    @Override
    @NotNull
    public ByteString composeRequestBody(final boolean autoPersistQueries,
                                         final boolean withQueryDocument, @NotNull final ScalarTypeAdapters scalarTypeAdapters) {
        return OperationRequestBodyComposer.compose(this, autoPersistQueries, withQueryDocument, scalarTypeAdapters);
    }

    public static final class Builder {
        private @NotNull
        AddWarrantyInput warranty;

        Builder() {
        }

        public Builder warranty(@NotNull AddWarrantyInput warranty) {
            this.warranty = warranty;
            return this;
        }

        public AddWarrantyMutation build() {
            Utils.checkNotNull(warranty, "warranty == null");
            return new AddWarrantyMutation(warranty);
        }
    }

    public static final class Variables extends Operation.Variables {
        private final @NotNull
        AddWarrantyInput warranty;

        private final transient Map<String, Object> valueMap = new LinkedHashMap<>();

        Variables(@NotNull AddWarrantyInput warranty) {
            this.warranty = warranty;
            this.valueMap.put("warranty", warranty);
        }

        public @NotNull
        AddWarrantyInput warranty() {
            return warranty;
        }

        @Override
        public Map<String, Object> valueMap() {
            return Collections.unmodifiableMap(valueMap);
        }

        @Override
        public InputFieldMarshaller marshaller() {
            return new InputFieldMarshaller() {
                @Override
                public void marshal(InputFieldWriter writer) throws IOException {
                    writer.writeObject("warranty", warranty.marshaller());
                }
            };
        }
    }

    /**
     * Data from the response after executing this GraphQL operation
     */
    public static class Data implements Operation.Data {
        static final ResponseField[] $responseFields = {
                ResponseField.forObject("addWarranty", "addWarranty", new UnmodifiableMapBuilder<String, Object>(1)
                        .put("input", new UnmodifiableMapBuilder<String, Object>(2)
                                .put("kind", "Variable")
                                .put("variableName", "warranty")
                                .build())
                        .build(), false, Collections.<ResponseField.Condition>emptyList())
        };

        final @NotNull
        AddWarranty addWarranty;

        private transient volatile String $toString;

        private transient volatile int $hashCode;

        private transient volatile boolean $hashCodeMemoized;

        public Data(@NotNull AddWarranty addWarranty) {
            this.addWarranty = Utils.checkNotNull(addWarranty, "addWarranty == null");
        }

        public @NotNull
        AddWarranty addWarranty() {
            return this.addWarranty;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public ResponseFieldMarshaller marshaller() {
            return new ResponseFieldMarshaller() {
                @Override
                public void marshal(ResponseWriter writer) {
                    writer.writeObject($responseFields[0], addWarranty.marshaller());
                }
            };
        }

        @Override
        public String toString() {
            if ($toString == null) {
                $toString = "Data{"
                        + "addWarranty=" + addWarranty
                        + "}";
            }
            return $toString;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Data) {
                Data that = (Data) o;
                return this.addWarranty.equals(that.addWarranty);
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (!$hashCodeMemoized) {
                int h = 1;
                h *= 1000003;
                h ^= addWarranty.hashCode();
                $hashCode = h;
                $hashCodeMemoized = true;
            }
            return $hashCode;
        }

        public static final class Mapper implements ResponseFieldMapper<Data> {
            final AddWarranty.Mapper addWarrantyFieldMapper = new AddWarranty.Mapper();

            @Override
            public Data map(ResponseReader reader) {
                final AddWarranty addWarranty = reader.readObject($responseFields[0], new ResponseReader.ObjectReader<AddWarranty>() {
                    @Override
                    public AddWarranty read(ResponseReader reader) {
                        return addWarrantyFieldMapper.map(reader);
                    }
                });
                return new Data(addWarranty);
            }
        }
    }

    public static class AddWarranty {
        static final ResponseField[] $responseFields = {
                ResponseField.forString("__typename", "__typename", null, false, Collections.<ResponseField.Condition>emptyList()),
                ResponseField.forString("title", "title", null, false, Collections.<ResponseField.Condition>emptyList())
        };

        final @NotNull
        String __typename;

        final @NotNull
        String title;

        private transient volatile String $toString;

        private transient volatile int $hashCode;

        private transient volatile boolean $hashCodeMemoized;

        public AddWarranty(@NotNull String __typename, @NotNull String title) {
            this.__typename = Utils.checkNotNull(__typename, "__typename == null");
            this.title = Utils.checkNotNull(title, "title == null");
        }

        public @NotNull
        String __typename() {
            return this.__typename;
        }

        public @NotNull
        String title() {
            return this.title;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public ResponseFieldMarshaller marshaller() {
            return new ResponseFieldMarshaller() {
                @Override
                public void marshal(ResponseWriter writer) {
                    writer.writeString($responseFields[0], __typename);
                    writer.writeString($responseFields[1], title);
                }
            };
        }

        @Override
        public String toString() {
            if ($toString == null) {
                $toString = "AddWarranty{"
                        + "__typename=" + __typename + ", "
                        + "title=" + title
                        + "}";
            }
            return $toString;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof AddWarranty) {
                AddWarranty that = (AddWarranty) o;
                return this.__typename.equals(that.__typename)
                        && this.title.equals(that.title);
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (!$hashCodeMemoized) {
                int h = 1;
                h *= 1000003;
                h ^= __typename.hashCode();
                h *= 1000003;
                h ^= title.hashCode();
                $hashCode = h;
                $hashCodeMemoized = true;
            }
            return $hashCode;
        }

        public static final class Mapper implements ResponseFieldMapper<AddWarranty> {
            @Override
            public AddWarranty map(ResponseReader reader) {
                final String __typename = reader.readString($responseFields[0]);
                final String title = reader.readString($responseFields[1]);
                return new AddWarranty(__typename, title);
            }
        }
    }
}