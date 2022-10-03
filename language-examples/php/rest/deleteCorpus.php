<?php
/**
 * Deletes a corpus in Vectara platform using HTTP/REST.
 *
 * @auth_url        Authentication URL for this customer.
 * @client_id       App client ID.
 * @client_secret   App client secret.
 * @corpus_id       Corpus ID of the corpus to delete.
 * @customer_id     Unique customer ID in Vectara platform.
 * @admin_endpoint  The endpoint of Vectara Admin server.
 *
 * Returns          The result of deleteCorpus REST call.
 */
include 'getJwtToken.php';

$url = 'https://h.' . $_POST['admin_endpoint'] . '/v1/delete-corpus';
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

$corpus_data = [
    'customer_id' => $customer_id,
    'corpus_id' => $_POST['corpus_id'],
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type:application/json',
    'Authorization: Bearer ' . $jwt_token,
    'customer-id: ' . strval($customer_id),
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($corpus_data));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

//execute post
$result = curl_exec($ch);
curl_close($ch);

echo $result;
?>
