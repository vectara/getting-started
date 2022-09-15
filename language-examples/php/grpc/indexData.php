<?php
/**
 * Indexes a document to a specified corpus in Vectara platform using gRPC.
 *
 * @auth_url            Authentication URL for this customer.
 * @client_id           App client ID.
 * @client_secret       App client secret.
 * @customer_id         Unique customer ID in Vectara platform.
 * @corpus_id           ID of corpus to which data will be indexed.
 * @indexing_endpoint   The endpoint of Vectara Indexing server.
 *
 * Returns              The status result of index rest call.
 */

include 'getJwtToken.php';

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

$client = new Ai\Zir\IndexServiceClient($_POST['indexing_endpoint'] . ':443', [
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

$indexRequest = new Ai\Zir\IndexDocumentRequest();

$section = new Ai\Zir\Indexing\Section();
$section->setId(1);
$section->setTitle('Section Title.');
$section->setText('Dummy Text');

# Generating a random guid to be used as document id.
$UUID = vsprintf(
    '%s%s-%s-%s-%s-%s%s%s',
    str_split(bin2hex(random_bytes(16)), 4)
);

$document = new Ai\Zir\Indexing\Document();
$document->setDocumentId($UUID);
$document->setTitle('Test Title.');
$docMetadata = json_encode([
    'author' => 'Vectara',
    'date_created' => 'July 1st, 2022',
]);
$document->setMetadataJson($docMetadata);
$document->setSection([$section]);

$indexRequest->setCustomerId($customer_id);
$indexRequest->setCorpusId($corpus_id);
$indexRequest->setDocument($document);

list($result, $status) = $client->index($indexRequest)->wait();

if ($status->code !== Grpc\STATUS_OK) {
    echo 'ERROR: ' . $status->code . ', ' . $status->details . PHP_EOL;
    exit(1);
}

if ($result->getStatus()->getCode() === 0) {
    echo 'SUCCESS: Document indexed';
} else {
    echo 'ERROR: Code: ' .
        $result->getStatus()->getCode() .
        ' Detail: ' .
        $result->getStatus()->getStatusDetail();
}
?>
