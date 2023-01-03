using CommandLine;
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
            Parser.Default.ParseArguments<Args>(args)
                .WithParsed<Args>((args) =>
                {
                    Query(args.CustomerId, args.CorpusId, "Test Query.", args.ServingEndpoint, args.ApiKey);
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
        /// <param name="servingEndpoint"> Serving API endpoint to which calls will be directed. </param>
        /// <param name="apiKey"> A valid API Key. </param>
        private static void Query(long customerId, long corpusId, String query, String servingEndpoint, String apiKey)
        {
            using (var client = new HttpClient())
            {
                try
                {
                    var request = new HttpRequestMessage
                    {
                        RequestUri = new Uri($"https://{servingEndpoint}/v1/query"),
                        Method = HttpMethod.Post,
                    };
                    Dictionary<String, Object> queryData = new();
                    List<Object> queryList = new();
                    List<Object> corpusList = new();
                    corpusList.Add(new Dictionary<String, Object>()
                    {
                        {"customerId", customerId},
                        {"corpusId", corpusId}
                    });
                    queryList.Add(new Dictionary<String, Object>()
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
}