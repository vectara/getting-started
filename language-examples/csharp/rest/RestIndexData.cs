using Microsoft.Extensions.FileProviders;
using System.Net.Http.Headers;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

class RestIndexData
{
    private static Stream ReadFileFromResource(string dir, string fileName)
    {
        var embeddedProvider = new EmbeddedFileProvider(Assembly.GetExecutingAssembly());
        return embeddedProvider.GetFileInfo($"{dir}/{fileName}").CreateReadStream();
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

    /// <summary>
    /// Indexes data to a pre-created corpus in a customer account using FileUpload API.
    /// </summary>
    /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
    /// <param name="corpusId"> The corpus ID to which data will be indexed. </param>
    /// <param name="jwtToken"> A valid authentication token. </param>
    /// <returns> The name of the file that was uploaded. </returns>
    /// <throws> Exception if Index operation fails. </throws>
    public static string IndexViaUpload(long customerId, long corpusId, string jwtToken)
    {
        using (var client = new HttpClient())
        {
            try
            {
                var request = new HttpRequestMessage
                {
                    RequestUri = new Uri($"https://{ServerEndpoints.commonEndpoint}/v1/upload"),
                    Method = HttpMethod.Post,
                };
                // Getting a randomly generated key that will be used as boundary in 
                // multipart/form-data request.
                string boundary = GetRandomKey(8);
                var multipartContent = new MultipartFormDataContent(boundary);
                multipartContent.Add(new StringContent(customerId.ToString()), name: "c");
                multipartContent.Add(new StringContent(corpusId.ToString()), name: "o");

                // File
                string fileName = "upload.pdf";
                var fileStreamContent = new StreamContent(ReadFileFromResource("pdf", fileName));
                multipartContent.Add(fileStreamContent, name: "file", fileName: fileName);
                fileStreamContent.Headers.ContentType = new MediaTypeHeaderValue("application/pdf");

                request.Content = multipartContent;
                request.Content.Headers.Remove("Content-Type");
                request.Content.Headers.Add("Content-Type", "multipart/form-data;boundary=" + boundary);
                request.Headers.Add("Authorization", $"Bearer {jwtToken}");

                HttpResponseMessage response = client.Send(request);
                string result = response.Content.ReadAsStringAsync().Result;

                Console.WriteLine(result);
                return fileName;
            }
            catch (Exception)
            {
                throw;
            }
        }
    }

    /// <summary>
    /// Indexes some data to a pre-created corpus in a customer account using index API.
    /// </summary>
    /// <param name="customerId"> The unique customer ID in Vectara platform. </param>
    /// <param name="corpusId"> The corpus ID to which data will be indexed. </param>
    /// <param name="jwtToken"> A valid authentication token. </param>
    /// <throws> Exception if Index operation fails. </throws>
    public static void Index(long customerId, long corpusId, string jwtToken)
    {
        using (var client = new HttpClient())
        {
            try
            {
                var request = new HttpRequestMessage
                {
                    RequestUri = new Uri($"https://{ServerEndpoints.commonEndpoint}/v1/index"),
                    Method = HttpMethod.Post,
                };

                Dictionary<string, object> indexData = new();
                Dictionary<string, object> document = new();
                Dictionary<string, object> section = new();
                Dictionary<string, object> docMetadata = new();
                Dictionary<string, object> sectionMetadata = new();

                sectionMetadata.Add("SectionHeader", "Aloha!");
                section.Add("text", "Some dummy text");
                section.Add("metadataJson", JsonSerializer.Serialize(sectionMetadata));

                docMetadata.Add("Title", "Vectara");
                // Doc id should be unique for every document within this corpus.
                document.Add("documentId", "doc-id-456789");
                document.Add("title", "A Dummy title.");
                document.Add("metadataJson", JsonSerializer.Serialize(docMetadata));
                // Sections can be 0 to many. That's why following code creates a list and adds
                // one section to that list. You can add as many as you like.
                document.Add("section", new List<Object>() {section});

                indexData.Add("customerId", customerId);
                indexData.Add("corpusId", corpusId);
                indexData.Add("document", document);

                string jsonData = JsonSerializer.Serialize(indexData);

                request.Content = new StringContent(jsonData);
                request.Content.Headers.Remove("Content-Type");
                request.Content.Headers.Add("Content-Type", "application/json");

                request.Headers.Add("customer-id", customerId.ToString());
                request.Headers.Add("Authorization", $"Bearer {jwtToken}");

                HttpResponseMessage response = client.Send(request);
                string result = response.Content.ReadAsStringAsync().Result;

                Console.WriteLine(result);
            }
            catch (Exception)
            {
                throw;
            }
        }
    }
}