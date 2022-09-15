using Ai.Zir;
using Ai.Zir.Admin;
using Ai.Zir.Indexing;
using Ai.Zir.Serving;
using CommandLine;
using Grpc.Core;
using Grpc.Net.Client;
using System.Text.Json;
using VectaraExampleCommon;

namespace VectaraExampleGrpc
{
    /// <summary>
    /// A class containing examples about how to use Vectara API using gRPC and OAuth2.
    /// </summary>
    class GrpcBasicExample
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
        /// Returns a new GrpcChannel with the required authentication headers and metadata.
        /// </summary>
        private static GrpcChannel AuthenticatedChannel(string address,
                                                        string jwtToken,
                                                        long customerId,
                                                        long? corpusId)
        {
            var credentials = CallCredentials.FromInterceptor((context, metadata) =>
            {
                if (!string.IsNullOrEmpty(jwtToken))
                {
                    metadata.Add("Authorization", $"Bearer {jwtToken}");
                    metadata.Add("customer-id", customerId.ToString());
                    if (corpusId.HasValue)
                    {
                        metadata.Add("corpus-id", ((long)corpusId).ToString());
                    }
                }
                return Task.CompletedTask;
            });

            var channel = GrpcChannel.ForAddress(address, new GrpcChannelOptions
            {
                Credentials = ChannelCredentials.Create(ChannelCredentials.SecureSsl, credentials)
            });
            return channel;
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
            GrpcChannel channel = null;
            try
            {
                String address = "https://" + indexingEndpoint + ":443";
                channel = AuthenticatedChannel(address, jwtToken, customerId, corpusId);

                var indexingClient = new IndexService.IndexServiceClient(channel);
                var request = new IndexDocumentRequest();
                request.CustomerId = customerId;
                request.CorpusId = corpusId;
                request.Document = new Document
                {
                    DocumentId = Guid.NewGuid().ToString(),  // Generating a random document id.
                    Title = "Dummy Title",
                    Description = "Dummy description",
                    MetadataJson = JsonSerializer.Serialize(new Dictionary<string, string>
                                                            {
                                                                {"author", "Vectara"},
                                                                {"date_created", "July 1st, 2022"}
                                                            })
                };
                request.Document.Section.Add(new Section
                {
                    Id = 1,
                    Title = "Section Title",
                    Text = "Section Text"
                });

                var result = indexingClient.Index(request);
                Console.WriteLine(result.ToString());
            }
            catch (RpcException ex)
            {
                Console.Error.WriteLine(ex.Message);
            }
            finally
            {
                if (channel != null)
                {
                    channel.ShutdownAsync();
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
            GrpcChannel channel = null;
            try
            {
                String address = "https://" + servingEndpoint + ":443";
                channel = AuthenticatedChannel(address, jwtToken, customerId, corpusId);

                var servingClient = new QueryService.QueryServiceClient(channel);
                var batchRequest = new BatchQueryRequest();
                var queryRequest = new QueryRequest
                {
                    Query = query,
                    NumResults = 10
                };
                queryRequest.CorpusKey.Add(new CorpusKey
                {
                    CorpusId = (uint)corpusId,
                    CustomerId = (uint)customerId
                });
                batchRequest.Query.Add(queryRequest);

                var result = servingClient.Query(batchRequest);
                Console.WriteLine(result.ToString());
            }
            catch (RpcException ex)
            {
                Console.Error.WriteLine(ex.Message);
            }
            finally
            {
                if (channel != null)
                {
                    channel.ShutdownAsync();
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
            GrpcChannel channel = null;
            try
            {
                String address = "https://" + adminEndpoint + ":443";
                channel = AuthenticatedChannel(address, jwtToken, customerId, null);

                var adminClient = new AdminService.AdminServiceClient(channel);
                var request = new CreateCorpusRequest
                {
                    Corpus = new Corpus
                    {
                        Name = corpusName,
                        Description = "Corpus created from CSharp example."
                    }
                };

                var result = adminClient.CreateCorpus(request);
                Console.WriteLine(result.ToString());
            }
            catch (RpcException ex)
            {
                Console.Error.WriteLine(ex.Message);
            }
            finally
            {
                if (channel != null)
                {
                    channel.ShutdownAsync();
                }
            }
        }
    }
}