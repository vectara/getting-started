/**
 * This nodejs (express-based) server exposes endpoints that can be called via HTTP/POST. All of 
 * these endpoints call the Vectara APIs via HTTP/REST, and return the results.
 * 
 * The exposed endpoints are as follows:
 * 1. queryData             Queries a corpus using OAuth2 as authentication mechanism.
 * 2. queryDataWithApiKey   Queries a corpus using API Key as authentication mechanism.
 * 3. indexData             Indexes data to a Vectara corpus.
 * 4. createCorpus          Creates a new corpus.
 */

const express = require('express');
const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');

const app = express();

app.use(express.json());
app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');
});

app.post('/queryData', (req, res) => {
    const { serving_endpoint, customer_id, corpus_id, auth_url, client_id, client_secret } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            const data = generateQueryData("test", customer_id, corpus_id);
            const config = {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                    'customer-id': customer_id.toString()
                }
            };
            axios.post(`https://h.${serving_endpoint}/v1/query`, data, config)
                .then((result) => {
                    if (result.status != 200) {
                        res.send(result.status);
                    } else {
                        res.send(result.data);
                    }
                })
                .catch((err) => {
                    console.log(err);
                    res.send(JSON.stringify(err));
                })
        })
        .catch((err) => {
            error = {
                detail: "Could not obtain OAuth token.",
                message : err.message,
                code: err.code
            }
            res.send(JSON.stringify(error));
        });
});

app.post('/queryDataWithApiKey', (req, res) => {
    const { serving_endpoint, customer_id, corpus_id, api_key } = req.body;
    const data = generateQueryData("test", customer_id, corpus_id);
    const config = {
        headers: {
            'x-api-key': api_key,
            'Content-Type': 'application/json',
            'customer-id': customer_id.toString()
        }
    };

    axios.post(`https://h.${serving_endpoint}/v1/query`, data, config)
        .then((result) => {
            if (result.status != 200) {
                res.send(result.status);
            } else {
                res.send(result.data);
            }
        })
        .catch((err) => {
            console.log(err);
            res.send(JSON.stringify(err));
        })
});

app.post('/createCorpus', (req, res) => {
    const { admin_endpoint, customer_id, auth_url, client_id, client_secret } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            const data = {
                'corpus': {
                    'name': 'Test Corpus from NodeJS',
                    'description': 'Dummy description'
                }
            };
            const config = {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                    'customer-id': customer_id.toString()
                }
            };
            axios.post(`https://h.${admin_endpoint}/v1/create-corpus`, data, config)
                .then((result) => {
                    if (result.status != 200) {
                        res.send(result.status);
                    } else {
                        res.send(result.data);
                    }
                })
                .catch((err) => {
                    console.log(err);
                    res.send(JSON.stringify(err));
                })
        })
        .catch((err) => {
            error = {
                detail: "Could not obtain OAuth token.",
                message : err.message,
                code: err.code
            }
            res.send(JSON.stringify(error));
        });
});

app.post('/indexData', (req, res) => {
    const { indexing_endpoint, customer_id, corpus_id, auth_url, client_id, client_secret } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            const data = new FormData();
            data.append('c', customer_id);
            data.append('o', corpus_id);
            data.append('file', fs.createReadStream(__dirname + '/upload.pdf'));
            const config = {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'multipart/form-data'
                }
            };
            axios.post(`https://h.${indexing_endpoint}/upload`, data, config)
                .then((result) => {
                    if (result.status != 200) {
                        res.send(result.status);
                    } else {
                        res.send(result.data);
                    }
                })
                .catch((err) => {
                    console.log(`Error occured: ${err}`);
                    res.send(JSON.stringify(err));
                })
        })
        .catch((err) => {
            error = {
                detail: "Could not obtain OAuth token.",
                message : err.message,
                code: err.code
            }
            res.send(JSON.stringify(error));
        });
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`Listening on port ${port}...`));

function getJwtToken(auth_url, client_id, client_secret) {
    const url = `${auth_url}/oauth2/token`;
    const encoded = Buffer.from(`${client_id}:${client_secret}`).toString('base64');
    const config = {
        headers: {
            'Authorization': `Basic ${encoded}`
        }
    };

    return new Promise((resolve, reject) => {
        axios.post(url, new URLSearchParams({
            'grant_type': 'client_credentials',
            'client_id': client_id
        }), config)
            .then((result) => {
                resolve(result.data.access_token);
            })
            .catch((err) => {
                console.log(err);
                reject(err);
            });
    })
}

function generateQueryData(query, customer_id, corpus_id) {
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
}