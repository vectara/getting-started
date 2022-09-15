<?php
/**
 * Creates a corpus in Vectara platform using gRPC.
 *
 * @auth_url        Authentication URL for this customer.
 * @client_id       App client ID.
 * @client_secret   App client secret.
 * @customer_id     Unique customer ID in Vectara platform.
 * @admin_endpoint  The endpoint of Vectara Admin server.
 *
 * Returns          The status result of createCorpus gRPC call.
 */

include 'getJwtToken.php';

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

$client = new Ai\Zir\AdminServiceClient($_POST['admin_endpoint'] . ':443', [
    'credentials' => \Grpc\ChannelCredentials::createSsl(),
    'update_metadata' => function ($metaData) use ($jwt_token, $customer_id) {
        $metaData['Authorization'] = ['Bearer ' . $jwt_token];
        $metaData['customer-id-bin'] = [pack('J', $customer_id)];
        return $metaData;
    },
]);

$createCorpusRequest = new Ai\Zir\Admin\CreateCorpusRequest();
$corpus = new Ai\Zir\Admin\Corpus();
$corpus->setName('Test From PHP gRPC');
$corpus->setDescription('Test Description');
$createCorpusRequest->setCorpus($corpus);

list($result, $status) = $client->createCorpus($createCorpusRequest)->wait();

if ($status->code !== Grpc\STATUS_OK) {
    echo 'ERROR: ' . $status->code . ', ' . $status->details . PHP_EOL;
    exit(1);
}

if ($result->getStatus()->getCode() === 0) {
    echo 'SUCCESS: Corpus created with ID: ' . $result->getCorpusId();
} else {
    echo 'ERROR: Code: ' .
        $result->getStatus()->getCode() .
        ' Detail: ' .
        $result->getStatus()->getStatusDetail();
}
?>
