/**
 * This nodejs module has several query functions which use Vectara's 
 * query API via REST
 */

const axios = require('axios');

function generateQueryData (query, customer_id, corpus_id) {
    const data = {
        'query': [
            {
                'query': query,
                'numResults': 10,
                'corpusKey': [
                    {
                        'customerId': customer_id,
                        'corpusId': corpus_id
                    }
                ]
            }
        ]
    };
    return data;
};

module.exports = {
    query: async function (customer_id, corpus_id, serving_endpoint, jwt_token) {
        const data = generateQueryData("test", customer_id, corpus_id);
        const config = {
            headers: {
                'Authorization': `Bearer ${jwt_token}`,
                'Content-Type': 'application/json',
                'customer-id': customer_id.toString()
            }
        };
        const result = await axios.post(`https://h.${serving_endpoint}/v1/query`, data, config);
        return result;
    },

    queryWithAPIKey: async function (customer_id, corpus_id, serving_endpoint, api_key) {
        const data = generateQueryData("test", customer_id, corpus_id);
        const config = {
            headers: {
                'x-api-key': api_key,
                'Content-Type': 'application/json',
                'customer-id': customer_id.toString()
            }
        };
        const result = await axios.post(`https://h.${serving_endpoint}/v1/query`, data, config);
        return result;
    }
};