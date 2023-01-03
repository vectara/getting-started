/**
 * This nodejs module has an index function which uses Vectara's 
 * index API via REST
 */

 const axios = require('axios');

 module.exports = {
     indexDocument: async function (customer_id, corpus_id, indexing_endpoint, jwt_token) {
         const data = {
            'customer_id': customer_id,
            'corpus_id': corpus_id,
            'document': {
                'document_id': 'doc-id-1',
                'title': 'An example title',
                'metadata_json': JSON.stringify({
                    "book-name": "An example title",
                    "collection": "Chemistry",
                    "author": "Example Author"
                }),
                'section': [
                    {
                        'text': 'This is a test document'
                    }
                ]
            }
         };
         /**
          * Note that both documents and sections can contain titles and
          * metadata_json.  These are optional for both levels.
          */
         const config = {
             headers: {
                 'Authorization': `Bearer ${jwt_token}`,
                 'Content-Type': 'application/json',
                 'customer-id': customer_id.toString()
             }
         };
 
         const result = await axios.post(`https://${indexing_endpoint}/v1/index`, data, config);
         return result;
     }
 };