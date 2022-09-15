<?php
/**
 * Returns an Authentication token based on the parameters passed.
 *
 * @auth_url         Authentication URL for this customer.
 * @client_id        App client ID.
 * @client_secret    App client secret.
 *
 * Returns           A valid app token in case of success or empty in case of failure.
 */
function get_token($auth_url, $client_id, $client_secret)
{
    $url = $auth_url . '/oauth2/token';
    $encoded = base64_encode($client_id . ':' . $client_secret);

    $fields = [
        'grant_type' => 'client_credentials',
        'client_id' => $client_id,
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Authorization: Basic ' . $encoded]);
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($fields));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec($ch);
    curl_close($ch);

    $data = json_decode($result);
    return $data->access_token;
}
?>
