using CommandLine;
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
                        RestCreateCorpus.CreateCorpus(args.CustomerId, "Test Corpus from Dotnet", args.AdminEndpoint, jwtToken);
                        // RestIndexData.IndexViaUpload(args.CustomerId, args.CorpusId, args.IndexingEndpoint, jwtToken);
                        RestIndexData.Index(args.CustomerId, args.CorpusId, args.IndexingEndpoint, jwtToken);
                        // It takes some time to index data in Vectara platform. It is possible that query will
                        // return zero results immediately after indexing. Please wait 3-5 minutes and try again if
                        // that happens.
                        RestQueryData.Query(args.CustomerId, args.CorpusId, "Test Query.", args.ServingEndpoint, jwtToken);
                        // RestResetCorpus.ResetCorpus(args.CustomerId, args.CorpusId, args.AdminEndpoint, jwtToken);
                        // RestDeleteCorpus.DeleteCorpus(args.CustomerId, args.CorpusId, args.AdminEndpoint, jwtToken);
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
    }
}