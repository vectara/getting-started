namespace VectaraExampleCommon;

using System.Text.Json;

/// <summary>
/// A class that makes an HTTP POST call to obtain a JWT Token based on authentication URL,
/// client ID and client secret.
/// </summary>
public class JWTFetcher
{
    public String authDomain { get; set; }
    public String clientId { get; set; }
    public String clientSecret { get; set; }

    private string Base64Encode(String clientId, String clientSecret)
    {
        String text = clientId + ":" + clientSecret;
        var plainTextBytes = System.Text.Encoding.UTF8.GetBytes(text);
        return System.Convert.ToBase64String(plainTextBytes);
    }

    /// <summary>
    /// Fetches a client_credentials JWT Token based on authentication URL, client ID and 
    /// client secret.
    /// </summary>
    public String? FetchClientCredentials()
    {
        if (String.IsNullOrEmpty(authDomain) ||
            String.IsNullOrEmpty(clientId) ||
            String.IsNullOrEmpty(clientSecret))
        {
            return null;
        }

        if (!authDomain.EndsWith("/oauth2/token"))
        {
            if (!authDomain.EndsWith("/"))
            {
                authDomain += "/";
            }
            authDomain += "oauth2/token";
        }

        using (var client = new HttpClient())
        {
            var request = new HttpRequestMessage
            {
                RequestUri = new Uri(authDomain),
                Method = HttpMethod.Post,
                Content = new FormUrlEncodedContent(
                            new Dictionary<string, string> {
                                    {"grant_type", "client_credentials"},
                                    {"client_id", clientId}
                            })
            };
            request.Headers.Add("Authorization", "Basic " + Base64Encode(clientId, clientSecret));
            request.Content.Headers.Remove("Content-Type");
            request.Content.Headers.Add("Content-Type", "application/x-www-form-urlencoded");
            try
            {
                HttpResponseMessage res = client.Send(request);
                String result = res.Content.ReadAsStringAsync().Result;
                var values = JsonSerializer.Deserialize<Dictionary<string, Object>>(result);
                if (!values.ContainsKey("access_token"))
                {
                    Console.WriteLine("Could not retrieve JWT Token.");
                    return null;
                }
                return values["access_token"].ToString();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                return null;
            }
        }
    }
}
