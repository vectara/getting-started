/**
 * This nodejs module has a deleteDocument function which uses Vectara's
 * delete-doc API via REST
 */

const axios = require("axios");

module.exports = {
  deleteDocument: async function (
    customer_id,
    corpus_id,
    indexing_endpoint,
    jwt_token,
    doc_id
  ) {
    const data = {
      customer_id: customer_id,
      corpus_id: corpus_id,
      document_id: doc_id,
    };
    const config = {
      headers: {
        Authorization: `Bearer ${jwt_token}`,
        "Content-Type": "application/json",
        "customer-id": customer_id.toString(),
      },
    };

    const result = await axios.post(
      `https://${indexing_endpoint}/v1/delete-doc`,
      data,
      config
    );
    return result;
  },
};
