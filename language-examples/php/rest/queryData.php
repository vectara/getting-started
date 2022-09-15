<?php
/**
 * Queries a corpus corpus in Vectara platform using HTTP/REST.
 *
 * @auth_url            Authentication URL for this customer.
 * @client_id           App client ID.
 * @client_secret       App client secret.
 * @customer_id         Unique customer ID in Vectara platform.
 * @corpus_id           ID of corpus to be queries.
 * @serving_endpoint    The endpoint of Vectara Querying server.
 * @query               The query text.
 *
 * Returns              The result of query rest call.
 */

include 'getJwtToken.php';

$url = 'https://h.' . $_POST['serving_endpoint'] . '/v1/query';
$customer_id = $_POST['customer_id'];
$corpus_id = $_POST['corpus_id'];
$query = $_POST['query'];

$jwt_token = get_token(
    $_POST['auth_url'],
    $_POST['client_id'],
    $_POST['client_secret']
);
if (empty($jwt_token)) {
    echo 'Could not obtain JWT token. Please check your credentials.';
    return;
}

$query_data = [
    'query' => $query,
    'numResults' => 10,
    'corpusKey' => [
        [
            'customerId' => $customer_id,
            'corpusId' => $corpus_id,
        ],
    ],
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type:application/json',
    'Authorization: Bearer ' . $jwt_token,
    'customer-id: ' . strval($customer_id),
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode(['query' => [$query_data]]));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$result = curl_exec($ch);
curl_close($ch);

echo $result;
?>
