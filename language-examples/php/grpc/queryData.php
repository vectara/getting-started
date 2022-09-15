<?php
/**
 * Queries a corpus corpus in Vectara platform using gRPC.
 *
 * @auth_url            Authentication URL for this customer.
 * @client_id           App client ID.
 * @client_secret       App client secret.
 * @customer_id         Unique customer ID in Vectara platform.
 * @corpus_id           ID of corpus to be queries.
 * @serving_endpoint    The endpoint of Vectara Querying server.
 * @query               The query text.
 *
 * Returns              The result of query gRPC call.
 */

include 'getJwtToken.php';

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

$client = new Ai\Zir\QueryServiceClient($_POST['serving_endpoint'] . ':443', [
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

$batchQueryRequest = new Ai\Zir\Serving\BatchQueryRequest();

$corpusKey = new Ai\Zir\Serving\CorpusKey();
$corpusKey->setCorpusId($corpus_id);

$queryRequest = new Ai\Zir\Serving\QueryRequest();
$queryRequest->setQuery($query);
$queryRequest->setNumResults(10);
$queryRequest->setCorpusKey([$corpusKey]);

$batchQueryRequest->setQuery([$queryRequest]);

list($result, $status) = $client->query($batchQueryRequest)->wait();

if ($status->code !== Grpc\STATUS_OK) {
    echo 'ERROR: ' . $status->code . ', ' . $status->details . PHP_EOL;
    exit(1);
}

$rs = $result->getResponseSet()[0];
for ($i = 0; $i < count($rs->getResponse()); $i++) {
    $x = $rs->getResponse()[$i];
    echo sprintf("%d. [%0.3f] %s\n", $i + 1, $x->getScore(), $x->getText());
}
?>
