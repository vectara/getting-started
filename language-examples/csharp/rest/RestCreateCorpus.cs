using System.Text.Json;
using Newtonsoft.Json.Linq;

class RestCreateCorpus
{
    /// <summary>
    /// Calls Vectara platform to create a corpus.
    /// </summary>
    /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
    /// <param name="corpusName"> The name of the corpus to be created. </param>
    /// <param name="jwtToken"> A valid authentication token. </param>
    /// <returns> The corpus ID of the newly created corpus. </returns>
    /// <throws> Exception if corpus creation fails. </throws>
    public static uint CreateCorpus(long customerId, string corpusName, string jwtToken)
    {
        using (var client = new HttpClient())
        {
            try
            {
                var apiEndpoint = ServerEndpoints.commonEndpoint;
                var request = new HttpRequestMessage
                {
                    RequestUri = new Uri($"https://{apiEndpoint}/v1/create-corpus"),
                    Method = HttpMethod.Post,
                };
                Dictionary<string, object> corpusData = new()
                {
                    {
                        "corpus",
                        new Dictionary<string, object>()
                    {
                        {"name", corpusName},
                        {"description", "Dummy description"}
                    }
                    }
                };

                string jsonData = JsonSerializer.Serialize(corpusData);

                request.Content = new StringContent(jsonData);
                request.Content.Headers.Remove("Content-Type");
                request.Content.Headers.Add("Content-Type", "application/json");

                request.Headers.Add("Authorization", $"Bearer {jwtToken}");
                request.Headers.Add("customer-id", customerId.ToString());

                HttpResponseMessage response = client.Send(request);
                string result = response.Content.ReadAsStringAsync().Result;
                JObject resultObj = JObject.Parse(result);
                JToken? status = resultObj["status"];
                if (status == null)
                {
                    throw new Exception("Corpus creation failed");
                }
                JToken? code = status["code"];
                if (code == null || code.ToString() != "OK")
                {
                    throw new Exception(string.Format("Corpus creation failed: {0}", status["statusDetail"]));
                }
                return uint.Parse(resultObj["corpusId"].ToString());
            }
            catch (Exception)
            {
                throw;
            }
        }
    }
}