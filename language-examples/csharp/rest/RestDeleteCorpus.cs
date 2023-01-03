using System.Text.Json;

class RestDeleteCorpus
{
    /// <summary>
    /// Calls Vectara platform to delete a corpus.
    /// </summary>
    /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
    /// <param name="corpusId"> The unique ID of the corpus to be deleted. </param>
    /// <param name="adminEndpoint"> Admin API endpoint to which calls will be directed. </param>
    /// <param name="jwtToken"> A valid authentication token. </param>
    public static void DeleteCorpus(long customerId, long corpusId, string adminEndpoint, string jwtToken)
    {
        using (var client = new HttpClient())
        {
            try
            {
                var request = new HttpRequestMessage
                {
                    RequestUri = new Uri($"https://{adminEndpoint}/v1/delete-corpus"),
                    Method = HttpMethod.Post,
                };

                Dictionary<String, Object> data = new();
                data.Add("customer_id", customerId);
                data.Add("corpus_id", corpusId);

                string jsonData = JsonSerializer.Serialize(data);

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