using Com.Vectara;
using Com.Vectara.Admin;
using Com.Vectara.Indexing;
using Com.Vectara.Serving;
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
            _ = Parser.Default.ParseArguments<Args>(args)
                .WithParsed<Args>((args) =>
                {
                    string? jwtToken = GetJwtToken(args.AuthUrl, args.AppclientId, args.AppclientSecret);
                    if (!string.IsNullOrEmpty(jwtToken))
                    {
                        try
                        {
                            var corpusId = CreateCorpus(args.CustomerId, "CSharp Test", jwtToken);
                            string docId = Index(args.CustomerId, corpusId, jwtToken);
                            Query(args.CustomerId, corpusId, "Test Query.", jwtToken);
                            DeleteDocument(args.CustomerId, corpusId, jwtToken, docId);
                        }
                        catch (Exception ex)
                        {
                            Console.Error.WriteLine(ex.Message);
                            return;
                        }
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
        private static string? GetJwtToken(string authUrl, string clientId, string clientSecret)
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
        /// <param name="jwtToken"> A valid authentication token. </param>
        /// <returns> ID of the document that was indexed. </returns>
        /// <exception cref="Exception"> If document indexing fails. </exception>
        private static string Index(long customerId, long corpusId, string jwtToken)
        {
            GrpcChannel? channel = null;
            try
            {
                string address = "https://" + ServerEndpoints.indexingEndpoint + ":443";
                channel = AuthenticatedChannel(address, jwtToken, customerId, corpusId);

                var indexingClient = new IndexService.IndexServiceClient(channel);
                var request = new IndexDocumentRequest();
                string docId = Guid.NewGuid().ToString(); // Generating a random document id.
                request.CustomerId = customerId;
                request.CorpusId = corpusId;
                request.Document = new Document
                {
                    DocumentId = docId,
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
                if (result.Status.Code == Com.Vectara.StatusCode.Ok) {
                    Console.WriteLine("Document indexed successfully.");
                } else if (result.Status.Code == Com.Vectara.StatusCode.AlreadyExists) {
                    Console.WriteLine("Document was previously indexed: {0}", result.Status.StatusDetail);
                } else {
                    throw new Exception(string.Format("Could not index document: {0}", result.Status.StatusDetail));
                }
                return docId;
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

        /// <summary>
        /// Deletes a document from a corpus.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusId"> The corpus ID to which data will be indexed. </param>
        /// <param name="jwtToken"> A valid authentication token. </param>
        /// <param name="docId"> Document Id to be deleted. </param>
        /// <exception cref="Exception"> If document deletion fails. </exception>
        private static void DeleteDocument(long customerId,
                                           long corpusId,
                                           string jwtToken,
                                           string docId)
        {
            GrpcChannel? channel = null;
            try
            {
                string address = "https://" + ServerEndpoints.indexingEndpoint + ":443";
                channel = AuthenticatedChannel(address, jwtToken, customerId, corpusId);

                var indexingClient = new IndexService.IndexServiceClient(channel);
                var request = new DeleteDocumentRequest
                {
                    CustomerId = customerId,
                    CorpusId = corpusId,
                    DocumentId = docId
                };

                indexingClient.Delete(request);
                Console.WriteLine(string.Format("Document deletion request submitted for {0} in corpus {1}", docId, corpusId));
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

        /// <summary>
        /// Queries a Vectara corpus.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusId"> The corpus that needs to be queried. </param>
        /// <param name="query"> The query text. </param>
        /// <param name="jwtToken"> A valid authentication token. </param>
        /// <exception cref="Exception"> If query fails. </exception>
        private static void Query(long customerId, long corpusId, String query, String jwtToken)
        {
            GrpcChannel? channel = null;
            try
            {
                string address = "https://" + ServerEndpoints.servingEndpoint + ":443";
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

        /// <summary>
        /// Calls Vectara platform to create a corpus.
        /// </summary>
        /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
        /// <param name="corpusName"> The name of the corpus to be created. </param>
        /// <param name="jwtToken"> A valid authentication token. </param>
        /// <returns> ID of the corpus that was created. </returns>
        /// <exception cref="Exception"> If corpus creation fails. </exception>
        private static uint CreateCorpus(long customerId, string corpusName, string jwtToken)
        {
            GrpcChannel? channel = null;
            try
            {
                string address = "https://" + ServerEndpoints.adminEndpoint + ":443";
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
                if (result.Status.Code != Com.Vectara.StatusCode.Ok)
                {
                    throw new Exception(string.Format("Could not create corpus: {0}", result.Status.StatusDetail));
                }
                Console.WriteLine(string.Format("Corpus created successfully: {0}", result.CorpusId));
                return result.CorpusId;
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