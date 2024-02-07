<?php
/**
 * Deletes a document from a corpus using gRPC.
 *
 * @auth_url            Authentication URL for this customer.
 * @client_id           App client ID.
 * @client_secret       App client secret.
 * @customer_id         Unique customer ID in Vectara platform.
 * @corpus_id           ID of corpus to which data will be indexed.
 * @indexing_endpoint   The endpoint of Vectara Indexing server.
 * @document_id         ID of the document to be deleted.
 *
 * Returns              The status result of index rest call.
 */

include 'getJwtToken.php';

$customer_id = $_POST['customer_id'];
$corpus_id = $_POST['corpus_id'];
$document_id = $_POST['document_id'];

$jwt_token = get_token(
    $_POST['auth_url'],
    $_POST['client_id'],
    $_POST['client_secret']
);
if (empty($jwt_token)) {
    echo 'Could not obtain JWT token. Please check your credentials.';
    return;
}

$client = new Com\Vectara\IndexServiceClient($_POST['indexing_endpoint'] . ':443', [
    'credentials' => \Grpc\ChannelCredentials::createSsl(),
    'update_metadata' => function ($metaData) use (
        $jwt_token,
        $customer_id,
        $corpus_id
    ) {
        $metaData['Authorization'] = ['Bearer ' . $jwt_token];
        $metaData['customer-id-bin'] = [pack('J', $customer_id)];
        $metaData['corpus-id-bin'] = [pack('J', $corpus_id)];
        return $metaData;
    },
]);

$deleteRequest = new Com\Vectara\DeleteDocumentRequest();
$deleteRequest->setCustomerId($customer_id);
$deleteRequest->setCorpusId($corpus_id);
$deleteRequest->setDocumentId($document_id);

list($result, $status) = $client->delete($deleteRequest)->wait();

if ($status->code !== Grpc\STATUS_OK) {
    echo 'ERROR: ' . $status->code . ', ' . $status->details . PHP_EOL;
    exit(1);
}

if ($status->code === 0) {
    echo 'SUCCESS: Document deleted';
} else {
    echo 'ERROR: Could not delete document, Status Code: '. $status->code;
}
?>
