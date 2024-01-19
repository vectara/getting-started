using CommandLine;
using Newtonsoft.Json.Linq;
using System.Text.Json;
using VectaraExampleCommon;

namespace VectaraExampleRest
{
    /// <summary>
    /// A class containing examples about how to use Vectara API using REST and API Key.
    /// </summary>
    class RestApiKeyQueries
    {
        static void Main(string[] args)
        {
            _ = Parser.Default.ParseArguments<Args>(args)
                .WithParsed<Args>((args) =>
                {
                    try 
                    {
                        Query(args.CustomerId, args.CorpusId, "Test Query.", args.ApiKey);
                    }
                    catch (Exception ex)
                    {
                        Console.Error.WriteLine(ex.Message);
                        return;
                    }
                })
                .WithNotParsed<Args>((errs) =>
                {
                    foreach (Error err in errs)
                    {
                        Console.Error.WriteLine(err.ToString());
                    }
                });
        }


        /// <summary>
        /// Queries a Vectara corpus.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusId"> The corpus that needs to be queried. </param>
        /// <param name="query"> The query text. </param>
        /// <param name="apiKey"> A valid API Key. </param>
        /// <throws> Exception if no results are found. </throws>
        private static void Query(long customerId, long corpusId, string query, string apiKey)
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

                    request.Headers.Add("x-api-key", apiKey);
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

                    Console.WriteLine(result);
                }
                catch (Exception)
                {
                    throw;
                }
            }
        }
    }
}