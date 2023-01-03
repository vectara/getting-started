<?php
/**
 * Indexes a file to a specified corpus in Vectara platform using HTTP/REST.
 *
 * @auth_url            Authentication URL for this customer.
 * @client_id           App client ID.
 * @client_secret       App client secret.
 * @customer_id         Unique customer ID in Vectara platform.
 * @corpus_id           ID of corpus to which data will be indexed.
 * @indexing_endpoint   The endpoint of Vectara Indexing server.
 *
 * Returns              The result of index rest call.
 */

include 'getJwtToken.php';

$url = 'https://' . $_POST['indexing_endpoint'] . '/v1/upload';
$customer_id = $_POST['customer_id'];
$corpus_id = $_POST['corpus_id'];

$jwt_token = get_token(
    $_POST['auth_url'],
    $_POST['client_id'],
    $_POST['client_secret']
);
if (empty($jwt_token)) {
    echo 'Could not obtain JWT token. Please check your credentials.';
    return;
}

function makeCurlFile($file)
{
    $mime = mime_content_type($file);
    $info = pathinfo($file);
    $name = $info['basename'];
    return new CURLFile($file, $mime, $name);
}

$ch = curl_init();

$upload_data = [
    'file' => makeCurlFile('upload.pdf'),
    'c' => $customer_id,
    'o' => $corpus_id,
];

curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Content-Type:multipart/form-data',
    'Authorization: Bearer ' . $jwt_token,
]);
curl_setopt($ch, CURLOPT_POSTFIELDS, $upload_data);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$result = curl_exec($ch);
curl_close($ch);

echo $result;
?>
