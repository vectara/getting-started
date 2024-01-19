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
            _ = Parser.Default.ParseArguments<Args>(args)
                .WithParsed<Args>((args) =>
                {
                    string? jwtToken = GetJwtToken(args.AuthUrl, args.AppclientId, args.AppclientSecret);
                    if (!string.IsNullOrEmpty(jwtToken))
                    {
                        try
                        {
                            uint corpusId = RestCreateCorpus.CreateCorpus(args.CustomerId, "Test Corpus from Dotnet", jwtToken);
                            string docId = RestIndexData.IndexViaUpload(args.CustomerId, corpusId, jwtToken);
                            RestIndexData.Index(args.CustomerId, corpusId, jwtToken);
                            RestQueryData.Query(args.CustomerId, corpusId, "Covid testing.", jwtToken);
                            RestDeleteDocuement.DeleteDocument(args.CustomerId, corpusId, jwtToken, docId);
                            RestResetCorpus.ResetCorpus(args.CustomerId, corpusId, jwtToken);
                            RestDeleteCorpus.DeleteCorpus(args.CustomerId, corpusId, jwtToken);
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
    }
}