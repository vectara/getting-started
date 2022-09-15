using CommandLine;
using Microsoft.Extensions.FileProviders;
using System.Net.Http.Headers;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using VectaraExampleCommon;

namespace VectaraExampleRest
{
    /// <summary>
    /// A class containing examples about how to use Vectara API using REST and OAuth2.
    /// </summary>
    class RestBasicExample
    {
        static void Main(string[] args)
        {
            Parser.Default.ParseArguments<Args>(args)
                .WithParsed<Args>((args) =>
                {
                    String? jwtToken = GetJwtToken(args.AuthUrl, args.AppclientId, args.AppclientSecret);
                    if (!String.IsNullOrEmpty(jwtToken))
                    {
                        CreateCorpus(args.CustomerId, "Test Corpus from Dotnet", args.AdminEndpoint, jwtToken);
                        Index(args.CustomerId, args.CorpusId, args.IndexingEndpoint, jwtToken);
                        // It takes some time to index data in Vectara platform. It is possible that query will
                        // return zero results immediately after indexing. Please wait 3-5 minutes and try again if
                        // that happens.
                        Query(args.CustomerId, args.CorpusId, "Test Query.", args.ServingEndpoint, jwtToken);
                    }
                    else
                    {
                        Console.Error.WriteLine("Could not obtain a JWT Token.");
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
        /// Fetches an authentication token based on authentication URL, client ID and client secret.
        /// </summary>
        private static String? GetJwtToken(String authUrl, String clientId, String clientSecret)
        {
            JWTFetcher jWTFetcher = new JWTFetcher
            {
                authDomain = authUrl,
                clientId = clientId,
                clientSecret = clientSecret
            };
            return jWTFetcher.FetchClientCredentials();
        }


        /// <summary>
        /// Generates a random key based on the size passed.
        /// </summary>
        private static string GetRandomKey(int size)
        {
            char[] chars =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".ToCharArray();

            byte[] data = new byte[4 * size];
            using (var crypto = RandomNumberGenerator.Create())
            {
                crypto.GetBytes(data);
            }
            StringBuilder result = new StringBuilder(size);
            for (int i = 0; i < size; i++)
            {
                var rnd = BitConverter.ToUInt32(data, i * 4);
                var idx = rnd % chars.Length;

                result.Append(chars[idx]);
            }
            return result.ToString();
        }

        private static Stream ReadFileFromResource(String dir, String fileName)
        {
            var embeddedProvider = new EmbeddedFileProvider(Assembly.GetExecutingAssembly());
            return embeddedProvider.GetFileInfo($"{dir}/{fileName}").CreateReadStream();
        }


        /// <summary>
        /// Indexes some data to a pre-created corpus in a customer account.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusId"> The corpus ID to which data will be indexed. </param>
        /// <param name="indexingEndpoint"> Indexing API endpoint to which calls will be directed. </param>
        /// <param name="jwtToken"> A valid authentication token. </param>
        private static void Index(long customerId, long corpusId, String indexingEndpoint, String jwtToken)
        {
            using (var client = new HttpClient())
            {
                try
                {
                    var request = new HttpRequestMessage
                    {
                        RequestUri = new Uri($"https://h.{indexingEndpoint}/upload"),
                        Method = HttpMethod.Post,
                    };
                    // Getting a randomly generated key that will be used as boundary in 
                    // multipart/form-data request.
                    String boundary = GetRandomKey(8);
                    var multipartContent = new MultipartFormDataContent(boundary);
                    multipartContent.Add(new StringContent(customerId.ToString()), name: "c");
                    multipartContent.Add(new StringContent(corpusId.ToString()), name: "o");

                    // File
                    String fileName = "upload.pdf";
                    var fileStreamContent = new StreamContent(ReadFileFromResource("pdf", fileName));
                    multipartContent.Add(fileStreamContent, name: "file", fileName: fileName);
                    fileStreamContent.Headers.ContentType = new MediaTypeHeaderValue("application/pdf");

                    request.Content = multipartContent;
                    request.Content.Headers.Remove("Content-Type");
                    request.Content.Headers.Add("Content-Type", "multipart/form-data;boundary=" + boundary);

                    request.Headers.Add("Authorization", $"Bearer {jwtToken}");

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

        /// <summary>
        /// Queries a Vectara corpus.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusId"> The corpus that needs to be queried. </param>
        /// <param name="query"> The query text. </param>
        /// <param name="servingEndpoint"> Serving API endpoint to which calls will be directed. </param>
        /// <param name="jwtToken"> A valid authentication token. </param>
        private static void Query(long customerId, long corpusId, String query, String servingEndpoint, String jwtToken)
        {
            using (var client = new HttpClient())
            {
                try
                {
                    var request = new HttpRequestMessage
                    {
                        RequestUri = new Uri($"https://h.{servingEndpoint}/v1/query"),
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

        /// <summary>
        /// Calls Vectara platform to create a corpus.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusName"> The name of the corpus to be created. </param>
        /// <param name="adminEndpoint"> Admin API endpoint to which calls will be directed. </param>
        /// <param name="jwtToken"> A valid authentication token. </param>
        private static void CreateCorpus(long customerId, string corpusName, string adminEndpoint, string jwtToken)
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
}