using System.Text.Json;
using Newtonsoft.Json.Linq;

class RestQueryData
{
    /// <summary>
    /// Queries a Vectara corpus.
    /// </summary>
    /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
    /// <param name="corpusId"> The corpus that needs to be queried. </param>
    /// <param name="query"> The query text. </param>
    /// <param name="jwtToken"> A valid authentication token. </param>
    /// <throws> Exception if no results are found. </throws>
    public static void Query(long customerId, long corpusId, string query, string jwtToken)
    {
        using (var client = new HttpClient())
        {
            try
            {
                var request = new HttpRequestMessage
                {
                    RequestUri = new Uri($"https://{ServerEndpoints.commonEndpoint}/v1/query"),
                    Method = HttpMethod.Post,
                };
                Dictionary<string, object> queryData = new();
                List<object> queryList = new();
                List<object> corpusList = new()
                {
                    new Dictionary<string, object>()
                    {
                        {"customerId", customerId},
                        {"corpusId", corpusId}
                    }
                };
                queryList.Add(new Dictionary<string, object>()
                    {
                        {"query", query},
                        {"numResults", 10},
                        {"corpusKey", corpusList}
                    });

                queryData.Add("query", queryList);

                string jsonData = JsonSerializer.Serialize(queryData);

                request.Content = new StringContent(jsonData);
                request.Content.Headers.Remove("Content-Type");
                request.Content.Headers.Add("Content-Type", "application/json");

                request.Headers.Add("Authorization", $"Bearer {jwtToken}");
                request.Headers.Add("customer-id", customerId.ToString());

                HttpResponseMessage response = client.Send(request);
                string result = response.Content.ReadAsStringAsync().Result;
                JObject resultObj = JObject.Parse(result);
                JToken? responseSetArray = resultObj["responseSet"];
                if (responseSetArray == null)
                {
                    throw new Exception("No results found");
                }
                foreach (var responseSet in responseSetArray)
                {
                    JObject responseSetObj = JObject.Parse(responseSet.ToString());
                    JToken? documents = responseSetObj["document"];

                    foreach (JToken docSection in responseSetObj["response"])
                    {
                        string text = docSection["text"].ToString();
                        double score = double.Parse(docSection["score"].ToString());
                        // doc that this section belongs to
                        int documentIndex = int.Parse(docSection["documentIndex"].ToString()); 
                        JToken doc = documents.ElementAt(documentIndex);
                        string docId = doc["id"].ToString();
                        Console.WriteLine("[score:{0:N2}] [docId:{1}] [text:{2}]", score, docId, text);
                    }
                }
            }
            catch (Exception)
            {
                throw;
            }
        }
    }
}