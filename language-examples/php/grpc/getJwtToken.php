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

require __DIR__ . '/vendor/autoload.php';

function get_token($auth_url, $client_id, $client_secret)
{
    $provider = new \League\OAuth2\Client\Provider\GenericProvider([
        'clientId' => $client_id,
        'clientSecret' => $client_secret,
        'urlAuthorize' => $auth_url . '/oauth2/authorize',
        'urlAccessToken' => $auth_url . '/oauth2/token',
        'urlResourceOwnerDetails' => $auth_url . '/oauth2/resource',
    ]);

    try {
        // Try to get an access token using the client credentials grant.
        return $provider->getAccessToken('client_credentials');
    } catch (\League\OAuth2\Client\Provider\Exception\IdentityProviderException $e) {
        // Failed to get the access token
        return '';
    }
}
?>
