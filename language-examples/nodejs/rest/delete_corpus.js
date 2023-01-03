/**
 * This nodejs module has a deleteCorpus function which uses Vectara's 
 * delete-corpus API via REST
 */

 const axios = require('axios');

 module.exports = {
     deleteCorpus: async function (customer_id, corpus_id, admin_endpoint, jwt_token) {
         const data = {
            'customer_id': customer_id,
            'corpus_id': corpus_id
         };
         const config = {
             headers: {
                 'Authorization': `Bearer ${jwt_token}`,
                 'Content-Type': 'application/json',
                 'customer-id': customer_id.toString()
             }
         };
 
         const result = await axios.post(`https://${admin_endpoint}/v1/delete-corpus`, data, config);
         return result;
     }
 };