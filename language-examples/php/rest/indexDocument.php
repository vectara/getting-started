<?php
/**
 * Indexes a document in Vectara platform using HTTP/REST.
 *
 * @auth_url           Authentication URL for this customer.
 * @client_id          App client ID.
 * @client_secret      App client secret.
 * @corpus_id          Corpus ID of the corpus to index to.
 * @customer_id        Unique customer ID in Vectara platform.
 * @indexing_endpoint  The endpoint of Vectara Admin server.
 *
 * Returns             The result of index REST call.
 */
include 'getJwtToken.php';

$url = 'https://h.' . $_POST['indexing_endpoint'] . '/v1/index';
$customer_id = $_POST['customer_id'];

$jwt_token = get_token(
    $_POST['auth_url'],
    $_POST['client_id'],
    $_POST['client_secret']
);
if (empty($jwt_token)) {
    echo 'Could not obtain JWT token. Please check your credentials.';
    return;
}

$index_data = [
    'customer_id' => $customer_id,
    'corpus_id' => $_POST['corpus_id'],
    'document' => [
        'document_id' => 'doc-id-1',
        'title' => 'My document title',
        'metadata_json' => json_encode([
            'book-name' => 'Example title',
            'collection' => 'Mathematics',
            'author' => 'Example Author'
        ]),
        'section' => array([
            'text' => 'This is a test document'
        ])
    ],
];
/**
 * Note that both documents and sections can contain titles and
 * metadata_json.  These are optional for both levels.
 */

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type:application/json',
    'Authorization: Bearer ' . $jwt_token,
    'customer-id: ' . strval($customer_id),
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($index_data));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

//execute post
$result = curl_exec($ch);
curl_close($ch);

echo $result;
?>
