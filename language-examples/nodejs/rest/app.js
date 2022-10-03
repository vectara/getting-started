/**
 * This nodejs (express-based) server exposes endpoints that can be called via HTTP/POST. All of 
 * these endpoints call the Vectara APIs via HTTP/REST, and return the results.
 * 
 * The exposed endpoints are as follows:
 * 1. queryData             Queries a corpus using OAuth2 as authentication mechanism.
 * 2. queryDataWithApiKey   Queries a corpus using API Key as authentication mechanism.
 * 3. uploadFile            Uploads a file to a Vectara corpus.
 * 4. createCorpus          Creates a new corpus.
 * 5. resetCorpus           Resets a corpus.
 * 6. deleteCorpus          Deletes a corpus.
 */

const express = require('express');
const axios = require('axios');
const create_corpus = require('./create_corpus')
const delete_corpus = require('./delete_corpus')
const reset_corpus = require('./reset_corpus')
const index_document = require('./index_document');
const upload_file = require('./upload_file');
const query = require('./query')

const app = express();

app.use(express.json());
app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');
});

app.post('/queryData', (req, res) => {
    const { serving_endpoint, customer_id, corpus_id, auth_url, client_id, client_secret } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            query.query(customer_id, corpus_id, serving_endpoint, token)
                .then((result) => {
                    res.send(result.data);
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
    query.queryWithAPIKey (customer_id, corpus_id, serving_endpoint, api_key)
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
            create_corpus.createCorpus(customer_id, admin_endpoint, token)
                .then((result) => {
                    res.send(result.data);
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

app.post('/resetCorpus', (req, res) => {
    const { admin_endpoint, customer_id, auth_url, client_id, client_secret, corpus_id } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            reset_corpus.resetCorpus(customer_id, corpus_id, admin_endpoint, token)
                .then((result) => {
                    res.send(result.data);
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

app.post('/deleteCorpus', (req, res) => {
    const { admin_endpoint, customer_id, auth_url, client_id, client_secret, corpus_id } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            delete_corpus.deleteCorpus(customer_id, corpus_id, admin_endpoint, token)
                .then((result) => {
                    res.send(result.data);
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

app.post('/uploadFile', (req, res) => {
    const { indexing_endpoint, customer_id, corpus_id, auth_url, client_id, client_secret } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            upload_file.uploadFile(customer_id, corpus_id, indexing_endpoint, token)
            .then((result) => {
                res.send(result.data);
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

app.post('/indexDocument', (req, res) => {
    const { indexing_endpoint, customer_id, corpus_id, auth_url, client_id, client_secret } = req.body;
    getJwtToken(auth_url, client_id, client_secret)
        .then((token) => {
            index_document.indexDocument(customer_id, corpus_id, indexing_endpoint, token)
            .then((result) => {
                res.send(result.data);
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