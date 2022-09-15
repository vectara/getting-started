using Ai.Zir;
using Ai.Zir.Serving;
using CommandLine;
using Grpc.Core;
using Grpc.Net.Client;
using VectaraExampleCommon;

namespace VectaraExampleGrpc
{
    /// <summary>
    /// A class containing examples about how to use Vectara API using gRPC and API Key.
    /// </summary>
    class GrpcApiKeyQueries
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
        /// Returns a new GrpcChannel with the required authentication headers and metadata.
        /// </summary>
        private static GrpcChannel AuthenticatedChannel(string address,
                                                        string apiKey,
                                                        long customerId,
                                                        long? corpusId)
        {
            var credentials = CallCredentials.FromInterceptor((context, metadata) =>
            {
                metadata.Add("x-api-key", apiKey);
                metadata.Add("customer-id", customerId.ToString());
                if (corpusId.HasValue)
                {
                    metadata.Add("corpus-id", ((long)corpusId).ToString());
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
        /// Queries a Vectara corpus.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusId"> The corpus that needs to be queried. </param>
        /// <param name="query"> The query text. </param>
        /// <param name="servingEndpoint"> Serving API endpoint to which calls will be directed. </param>
        /// <param name="jwtToken"> A valid authentication token. </param>
        private static void Query(long customerId, long corpusId, String query, String servingEndpoint, String apiKey)
        {
            GrpcChannel channel = null;
            try
            {
                String address = "https://" + servingEndpoint + ":443";
                channel = AuthenticatedChannel(address, apiKey, customerId, corpusId);

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
    }
}