<?php
/**
 * Queries a corpus corpus in Vectara platform using gRPC.
 *
 * @customer_id         Unique customer ID in Vectara platform.
 * @corpus_id           ID of corpus to be queried.
 * @serving_endpoint    The endpoint of Vectara Querying server.
 * @api_key             The API Key from Vectara platform.
 * @query               The query text.
 *
 * Returns              The result of query gRPC call.
 */

require __DIR__ . '/vendor/autoload.php';

$customer_id = $_POST['customer_id'];
$corpus_id = $_POST['corpus_id'];
$query = $_POST['query'];
$api_key = $_POST['api_key'];

$client = new Com\Vectara\QueryServiceClient($_POST['serving_endpoint'] . ':443', [
    'credentials' => \Grpc\ChannelCredentials::createSsl(),
    'update_metadata' => function ($metaData) use (
        $customer_id,
        $corpus_id,
        $api_key
    ) {
        $metaData['x-api-key'] = [$api_key];
        $metaData['customer-id-bin'] = [pack('J', $customer_id)];
        $metaData['corpus-id-bin'] = [pack('J', $corpus_id)];
        return $metaData;
    },
]);

$batchQueryRequest = new Com\Vectara\Serving\BatchQueryRequest();

$corpusKey = new Com\Vectara\Serving\CorpusKey();
$corpusKey->setCorpusId($corpus_id);

$queryRequest = new Com\Vectara\Serving\QueryRequest();
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
