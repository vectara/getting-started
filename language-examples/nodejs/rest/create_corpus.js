/**
 * This nodejs module has a createCorpus function which uses Vectara's 
 * create-corpus API via REST
 */

const axios = require('axios');

module.exports = {
    createCorpus: async function (customer_id, admin_endpoint, jwt_token) {
        const data = {
            'corpus': {
                'name': 'Test Corpus from NodeJS',
                'description': 'Dummy description'
            }
        };
        const config = {
            headers: {
                'Authorization': `Bearer ${jwt_token}`,
                'Content-Type': 'application/json',
                'customer-id': customer_id.toString()
            }
        };

        const result = await axios.post(`https://${admin_endpoint}/v1/create-corpus`, data, config);
        return result;
    }
};