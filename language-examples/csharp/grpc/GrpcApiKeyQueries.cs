using Com.Vectara;
using Com.Vectara.Serving;
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
        /// Queries a Vectara corpus using an API Key.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusId"> The corpus that needs to be queried. </param>
        /// <param name="query"> The query text. </param>
        /// <param name="apiKey"> A valid API Key. </param>
        /// <throws> Exception if no results are found. </throws>
        private static void Query(long customerId, long corpusId, string query, string apiKey)
        {
            GrpcChannel? channel = null;
            try
            {
                string address = "https://" + ServerEndpoints.servingEndpoint + ":443";
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
                foreach (var status in result.Status)
                {
                    if (status.Code != Com.Vectara.StatusCode.Ok)
                    {
                        Console.Error.WriteLine(string.Format("Failure status on query: {0}", status.StatusDetail));
                    }
                }
                foreach (var responseSet in result.ResponseSet)
                {
                    foreach (var status in responseSet.Status)
                    {
                        if (status.Code != Com.Vectara.StatusCode.Ok)
                        {
                            Console.Error.WriteLine(string.Format("Failure querying corpus: {0}", status.StatusDetail));
                        }
                    }
                }

                Console.WriteLine(string.Format("Query response: {0}", result.ToString()));
            }
            catch (RpcException)
            {
                throw;
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