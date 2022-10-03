using System.Text.Json;

class RestCreateCorpus
{
    /// <summary>
    /// Calls Vectara platform to create a corpus.
    /// </summary>
    /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
    /// <param name="corpusName"> The name of the corpus to be created. </param>
    /// <param name="adminEndpoint"> Admin API endpoint to which calls will be directed. </param>
    /// <param name="jwtToken"> A valid authentication token. </param>
    public static void CreateCorpus(long customerId, string corpusName, string adminEndpoint, string jwtToken)
    {
        using (var client = new HttpClient())
        {
            try
            {
                var request = new HttpRequestMessage
                {
                    RequestUri = new Uri($"https://h.{adminEndpoint}/v1/create-corpus"),
                    Method = HttpMethod.Post,
                };
                Dictionary<String, Object> corpusData = new();
                corpusData.Add("corpus", new Dictionary<String, Object>()
                    {
                        {"name", corpusName},
                        {"description", "Dummy description"}
                    });

                string jsonData = JsonSerializer.Serialize(corpusData);

                request.Content = new StringContent(jsonData);
                request.Content.Headers.Remove("Content-Type");
                request.Content.Headers.Add("Content-Type", "application/json");

                request.Headers.Add("Authorization", $"Bearer {jwtToken}");
                request.Headers.Add("customer-id", customerId.ToString());

                HttpResponseMessage response = client.Send(request);
                String result = response.Content.ReadAsStringAsync().Result;

                Console.WriteLine(result);
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine(ex.Message);
                return;
            }
        }
    }
}