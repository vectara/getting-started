using System.Text.Json;

class RestDeleteDocuement
{
    /// <summary>
    /// Deletes a document from a corpus using delete-doc API.
    /// </summary>
    /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
    /// <param name="corpusId"> The corpus ID to which data will be indexed. </param>
    /// <param name="jwtToken"> A valid authentication token. </param>
    /// <param name="docId"> Id of the document that needs to be deleted.
    public static void DeleteDocument(long customerId, 
                                      long corpusId, 
                                      string jwtToken, 
                                      string docId)
    {
        using (var client = new HttpClient())
        {
            try
            {
                var request = new HttpRequestMessage
                {
                    RequestUri = new Uri($"https://{ServerEndpoints.commonEndpoint}/v1/delete-doc"),
                    Method = HttpMethod.Post,
                };

                Dictionary<string, object> deleteRequest = new()
                {
                    { "customerId", customerId },
                    { "corpusId", corpusId },
                    { "documentId", docId }
                };
                string jsonData = JsonSerializer.Serialize(deleteRequest);

                request.Content = new StringContent(jsonData);
                request.Content.Headers.Remove("Content-Type");
                request.Content.Headers.Add("Content-Type", "application/json");

                request.Headers.Add("customer-id", customerId.ToString());
                request.Headers.Add("Authorization", $"Bearer {jwtToken}");

                HttpResponseMessage response = client.Send(request);
                string result = response.Content.ReadAsStringAsync().Result;

                Console.WriteLine(result);
            }
            catch (Exception)
            {
                throw;
            }
        }
    }
}