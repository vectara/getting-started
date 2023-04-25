/**
 * This nodejs (express-based) server exposes endpoints that can be called via HTTP/POST. All of
 * these endpoints call the Vectara APIs via gRPC, and return the results.
 *
 * The exposed endpoints are as follows:
 * 1. queryData             Queries a corpus using OAuth2 as authentication mechanism.
 * 2. queryDataWithApiKey   Queries a corpus using API Key as authentication mechanism.
 * 3. indexData             Indexes data to a Vectara corpus.
 * 4. createCorpus          Creates a new corpus.
 */

const express = require("express");
const grpc = require("@grpc/grpc-js");
const protoLoader = require("@grpc/proto-loader");
const axios = require("axios");
const path = require("path");

let PROTO_PATHS = [
  path.join(__dirname, "../../../protos/admin.proto"),
  path.join(__dirname, "../../../protos/common.proto"),
  path.join(__dirname, "../../../protos/custom_dim.proto"),
  path.join(__dirname, "../../../protos/indexing.proto"),
  path.join(__dirname, "../../../protos/services.proto"),
  path.join(__dirname, "../../../protos/serving.proto"),
  path.join(__dirname, "../../../protos/status.proto"),
];

let packageDefinition = protoLoader.loadSync(PROTO_PATHS, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});
let vectara = grpc.loadPackageDefinition(packageDefinition).com.vectara;

const app = express();
app.use(express.json());

app.get("/", (_, res) => {
  res.sendFile(__dirname + "/index.html");
});

app.post("/queryData", (req, res) => {
  const {
    serving_endpoint,
    customer_id,
    corpus_id,
    auth_url,
    client_id,
    client_secret,
  } = req.body;
  getJwtToken(auth_url, client_id, client_secret)
    .then((token) => {
      const query_data = generateQueryData("test", customer_id, corpus_id);
      try {
        let queryService = new vectara.QueryService(
          `${serving_endpoint}:443`,
          getCredentials(token, customer_id, corpus_id)
        );

        queryService.Query(query_data, function (_, status) {
          res.send(status);
        });
      } catch (err) {
        console.log(err);
        res.send(JSON.stringify(err));
      }
    })
    .catch((err) => {
      error = {
        detail: "Could not obtain OAuth token.",
        message: err.message,
        code: err.code,
      };
      res.send(JSON.stringify(error));
    });
});

app.post("/queryDataWithApiKey", (req, res) => {
  const { serving_endpoint, customer_id, corpus_id, api_key } = req.body;
  const query_data = generateQueryData("test", customer_id, corpus_id);
  try {
    let queryService = new vectara.QueryService(
      `${serving_endpoint}:443`,
      getCredentials(api_key, customer_id, corpus_id, true)
    );

    queryService.Query(query_data, function (result, status) {
      res.send(status);
    });
  } catch (err) {
    console.log(err);
    res.send(JSON.stringify(err));
  }
});

app.post("/createCorpus", (req, res) => {
  const { admin_endpoint, customer_id, auth_url, client_id, client_secret } =
    req.body;
  getJwtToken(auth_url, client_id, client_secret)
    .then((token) => {
      const corpus_data = {
        corpus: {
          name: "Test Corpus from NodeJS",
          description: "Dummy description",
        },
      };

      try {
        let adminService = new vectara.AdminService(
          `${admin_endpoint}:443`,
          getCredentials(token, customer_id, null)
        );

        adminService.CreateCorpus(corpus_data, function (_, status) {
          res.send(status);
        });
      } catch (err) {
        console.log(err);
        res.send(JSON.stringify(err));
      }
    })
    .catch((err) => {
      error = {
        detail: "Could not obtain OAuth token.",
        message: err.message,
        code: err.code,
      };
      res.send(JSON.stringify(error));
    });
});

app.post("/indexData", (req, res) => {
  const {
    indexing_endpoint,
    customer_id,
    corpus_id,
    auth_url,
    client_id,
    client_secret,
  } = req.body;
  getJwtToken(auth_url, client_id, client_secret)
    .then((token) => {
      try {
        let indexingService = new vectara.IndexService(
          `${indexing_endpoint}:443`,
          getCredentials(token, customer_id, corpus_id)
        );

        indexingService.Index(
          generateIndexData(customer_id, corpus_id),
          function (_, status) {
            res.send(status);
          }
        );
      } catch (err) {
        console.log(err);
        res.send(JSON.stringify(err));
      }
    })
    .catch((err) => {
      error = {
        detail: "Could not obtain OAuth token.",
        message: err.message,
        code: err.code,
      };
      res.send(JSON.stringify(error));
    });
});

app.post("/deleteDocument", (req, res) => {
  const {
    indexing_endpoint,
    customer_id,
    corpus_id,
    auth_url,
    client_id,
    client_secret,
    document_id,
  } = req.body;
  getJwtToken(auth_url, client_id, client_secret)
    .then((token) => {
      const delete_request = {
        customer_id: customer_id,
        corpus_id: corpus_id,
        document_id: document_id,
      };

      try {
        let indexingService = new vectara.IndexService(
          `${indexing_endpoint}:443`,
          getCredentials(token, customer_id, corpus_id)
        );

        indexingService.Delete(delete_request, function (_, status) {
          res.send(status);
        });
      } catch (err) {
        console.log(err);
        res.send(JSON.stringify(err));
      }
    })
    .catch((err) => {
      error = {
        detail: "Could not obtain OAuth token.",
        message: err.message,
        code: err.code,
      };
      res.send(JSON.stringify(error));
    });
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`Listening on port ${port}...`));

function getJwtToken(auth_url, client_id, client_secret) {
  const url = `${auth_url}/oauth2/token`;
  const encoded = Buffer.from(`${client_id}:${client_secret}`).toString(
    "base64"
  );
  const config = {
    headers: {
      Authorization: `Basic ${encoded}`,
    },
  };

  return new Promise((resolve, reject) => {
    axios
      .post(
        url,
        new URLSearchParams({
          grant_type: "client_credentials",
          client_id: client_id,
        }),
        config
      )
      .then((result) => {
        resolve(result.data.access_token);
      })
      .catch((err) => {
        console.log(err);
        reject(err);
      });
  });
}

function generateQueryData(query, customer_id, corpus_id) {
  return {
    query: [
      {
        query: query,
        num_results: 10,
        corpus_key: [
          {
            customer_id: customer_id,
            corpus_id: corpus_id,
          },
        ],
      },
    ],
  };
}

function getCredentials(token, customer_id, corpus_id, is_api_key = false) {
  return grpc.credentials.combineCallCredentials(
    grpc.credentials.createSsl(),
    grpc.credentials.createFromMetadataGenerator(function (_, callback) {
      let md = new grpc.Metadata();
      if (is_api_key) {
        md.set("x-api-key", token);
      } else {
        md.set("Authorization", `Bearer ${token}`);
      }
      md.set("customer-id", customer_id.toString());
      if (corpus_id !== null) {
        md.set("corpus-id", corpus_id.toString());
      }
      return callback(null, md);
    })
  );
}

function generateIndexData(customer_id, corpus_id) {
  const metadata = {
    author: "William Shakespeare.",
  };
  const data = {
    customer_id: customer_id,
    corpus_id: corpus_id,
    document: {
      document_id: "doc-id-1", // Random string
      title: "Hello Title.",
      text: "Some text in the document.",
      metadata_json: JSON.stringify(metadata),
      section: [
        {
          id: 1,
          title: "Section Title",
          text: "Section Text.",
        },
      ],
    },
  };
  return data;
}
