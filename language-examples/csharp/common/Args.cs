using CommandLine;

namespace VectaraExampleCommon
{
    /// <summary>
    /// A class containing Args that are needed to run the examples.
    /// </summary>
    public class Args
    {
        [Option("customer-id",
                Required = true,
                HelpText = "Unique customer ID in Vectara platform.")]
        public long CustomerId { get; set; } = 12345678;
        [Option("corpus-id",
                Required = true,
                HelpText = "Corpus ID to which data will be indexed and queried from.")]
        public long CorpusId { get; set; } = 123;
        [Option("app-client-id",
                Required = false,
                HelpText = "This appclient should have permissions for all operations in this example. "
                         + "This includes indexing, querying, and corpus creation.")]
        public String AppclientId { get; set; } = "";
        [Option("app-client-secret",
                Required = false,
                HelpText = "The authentication secret.")]
        public String AppclientSecret { get; set; } = "";
        [Option("auth-url",
                Required = false,
                HelpText = "The authentication URL for this customer.")]
        public String AuthUrl { get; set; } = "";
        [Option("api-key",
                Required = false,
                HelpText = "API key retrieved from Vectara console.")]
        public string ApiKey { get; set; } = "";
    }
}