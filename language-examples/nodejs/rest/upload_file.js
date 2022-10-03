/**
 * This nodejs module has an uploadFile function which uses Vectara's 
 * document upload API via REST
 */

const axios = require('axios');
const fs = require('fs');
const FormData = require('form-data');

module.exports = {
    uploadFile: async function (customer_id, corpus_id, indexing_endpoint, jwt_token) {
        const data = new FormData();
        data.append('c', customer_id);
        data.append('o', corpus_id);
        data.append('file', fs.createReadStream(__dirname + '/upload.pdf'));

        const config = {
            headers: {
                'Authorization': `Bearer ${jwt_token}`,
                'Content-Type': 'multipart/form-data'
            }
        };
        const result = await axios.post(`https://h.${indexing_endpoint}/upload`, data, config);
        return result;
    }
};