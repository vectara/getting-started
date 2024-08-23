# README #

> :warning: **DEPRECATION NOTE:**  This repository is deprecated and not maintained. Please checkout our newly
> released [APIv2](https://docs.vectara.com/docs/rest-api/vectara-rest-api-v-2).
> The API Playground for APIv2 includes code in several programming languages.
> This repository will soon be cleaned up.

## Cloning the Repository

This repository links a git submodule that contains the Vectara proto definitions
needed for `grpc` calls. In order to properly clone this repository, use the
`--recurse-submodules` flag in the clone command like following:

`git clone --recurse-submodules https://<RepoPath>`

Doing this will ensure that the submodule containing proto definitions is fetched as well.

If you have already cloned the repository, without using above flag, then you can run following
two commands to fetch the submodule:

1. `git submodule init`
2. `git submodule update`

## Authentication 

Vectara APIs require authentication of some form to work. Following are the two authentication 
methods that are supported.

1. OAuth2
2. API Keys

### OAuth2
An API User's first step is to obtain a JWT Token. To obtain JWT Token, following steps need to be
done.

1. Create an App Client in Vectara Console.
2. Assign appropriate rights to this App client over a corpus. These rights can be `QRY`, `IDX`,
   `ADM` or a combination of these three.
3. Note down the following three things from App Client page in Console.
    1. App Client ID
    2. App Client Secret
    3. Auth URL (This is available near the top of the page)
4. Also note down your Vectara customer ID and the Corpus ID (of the corpus you want to index data to, or query.)

Once you have all the above data, you can use it to obtain a valid JWT Auth token.
With this token, you can make API calls to index data, query data and/or perform
certain admin tasks such as create corpus, delete corpus, etc.

All of this is demonstrated in the code. For further clarification, please see the code.

> Please note that your App Client needs to have sufficient privileges to perform indexing, querying or admin tasks. These privileges can be assigned in Vectara console.


### API Keys
The API Key authentication method is supported with Serving/Querying only. 

Use the following link to generate an API Key via Vectara platform. 
[Api Keys](https://docs.vectara.com/docs/api-keys)

> While generating an API Key, make sure that you have assigned the correct corpus to it.
