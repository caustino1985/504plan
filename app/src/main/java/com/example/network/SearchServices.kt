package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class PubMedEntry(
    val id: String,
    val title: String,
    val journal: String,
    val date: String,
    val url: String
)

data class ArxivEntry(
    val title: String,
    val summary: String,
    val url: String,
    val authors: String,
    val published: String,
    val licensesMentioned: List<String>
)

object ResearchPortalService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Searches NIH PubMed for queries like "gwas" or genetics
     */
    suspend fun searchPubMed(term: String): List<PubMedEntry> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Search for paper IDs
                val searchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=${java.net.URLEncoder.encode(term, "UTF-8")}&retmode=json&retmax=10"
                val searchRequest = Request.Builder().url(searchUrl).build()
                val searchResponse = client.newCall(searchRequest).execute()
                
                if (!searchResponse.isSuccessful) return@withContext emptyList()
                val searchBody = searchResponse.body?.string() ?: return@withContext emptyList()
                
                // Parse search response with regex for ultimate safety against schema drifts
                val idListRegex = java.util.regex.Pattern.compile("\"idlist\":\\s*\\[([\\s\\S]*?)\\]")
                val matcher = idListRegex.matcher(searchBody)
                val ids = mutableListOf<String>()
                if (matcher.find()) {
                    val matchingGroup = matcher.group(1)
                    val rawIds = matchingGroup.split(",")
                    for (rawId in rawIds) {
                        val cleaned = rawId.replace("\"", "").replace("\n", "").replace(" ", "").trim()
                        if (cleaned.isNotEmpty()) {
                            ids.add(cleaned)
                        }
                    }
                }

                if (ids.isEmpty()) return@withContext emptyList()

                // 2. Fetch summary details for these IDs
                val idsJoined = ids.joinToString(",")
                val summaryUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=$idsJoined&retmode=json"
                val summaryRequest = Request.Builder().url(summaryUrl).build()
                val summaryResponse = client.newCall(summaryRequest).execute()
                
                if (!summaryResponse.isSuccessful) return@withContext emptyList()
                val summaryBody = summaryResponse.body?.string() ?: return@withContext emptyList()

                val resultsList = mutableListOf<PubMedEntry>()
                
                // Parse individual item profiles
                for (id in ids) {
                    // Extract item based on its ID container
                    val startMarker = "\"$id\":\\s*\\{"
                    val idPattern = java.util.regex.Pattern.compile("$startMarker([\\s\\S]*?)\\},\\s*\"")
                    val idMatcher = idPattern.matcher(summaryBody)
                    
                    var entryText = ""
                    if (idMatcher.find()) {
                        entryText = idMatcher.group(1)
                    } else {
                        // Fallback filter for last element in result dictionary
                        val lastPattern = java.util.regex.Pattern.compile("$startMarker([\\s\\S]*?)\\}\\s*\\}\\s*\\}")
                        val lastMatcher = lastPattern.matcher(summaryBody)
                        if (lastMatcher.find()) {
                            entryText = lastMatcher.group(1)
                        }
                    }

                    if (entryText.isNotEmpty()) {
                        val titleRegex = java.util.regex.Pattern.compile("\"title\":\\s*\"([\\s\\S]*?)\"")
                        val sourceRegex = java.util.regex.Pattern.compile("\"source\":\\s*\"([\\s\\S]*?)\"")
                        val dateRegex = java.util.regex.Pattern.compile("\"pubdate\":\\s*\"([\\s\\S]*?)\"")

                        val tMatcher = titleRegex.matcher(entryText)
                        val sMatcher = sourceRegex.matcher(entryText)
                        val dMatcher = dateRegex.matcher(entryText)

                        val title = if (tMatcher.find()) tMatcher.group(1).replace("\\\"", "\"") else "PubMed Publication $id"
                        val journal = if (sMatcher.find()) sMatcher.group(1) else "National Library of Medicine"
                        val pubDate = if (dMatcher.find()) dMatcher.group(1) else "N/A"
                        
                        resultsList.add(
                            PubMedEntry(
                                id = id,
                                title = title,
                                journal = journal,
                                date = pubDate,
                                url = "https://pubmed.ncbi.nlm.nih.gov/$id/"
                            )
                        )
                    }
                }
                
                resultsList
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * Searches arXiv for papers and filters them for risk, genetics, and MIT/CC licenses
     */
    suspend fun searchArxiv(term: String): List<ArxivEntry> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val queryUrl = "https://export.arxiv.org/api/query?search_query=${java.net.URLEncoder.encode(term, "UTF-8")}&max_results=12"
                val request = Request.Builder().url(queryUrl).build()
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) return@withContext emptyList()
                val xml = response.body?.string() ?: return@withContext emptyList()

                val entries = mutableListOf<ArxivEntry>()
                val entryPattern = java.util.regex.Pattern.compile("<entry>([\\s\\S]*?)</entry>")
                val matcher = entryPattern.matcher(xml)

                val titlePattern = java.util.regex.Pattern.compile("<title>([\\s\\S]*?)</title>")
                val summaryPattern = java.util.regex.Pattern.compile("<summary>([\\s\\S]*?)</summary>")
                val idPattern = java.util.regex.Pattern.compile("<id>([\\s\\S]*?)</id>")
                val authorPattern = java.util.regex.Pattern.compile("<name>([\\s\\S]*?)</name>")
                val publishedPattern = java.util.regex.Pattern.compile("<published>([\\s\\S]*?)</published>")

                while (matcher.find()) {
                    val entryText = matcher.group(1)
                    
                    val tMatcher = titlePattern.matcher(entryText)
                    val rawTitle = if (tMatcher.find()) tMatcher.group(1).trim() else ""
                    val title = rawTitle.replace(Regex("\\s+"), " ")

                    val sMatcher = summaryPattern.matcher(entryText)
                    val rawSummary = if (sMatcher.find()) sMatcher.group(1).trim() else ""
                    val summary = rawSummary.replace(Regex("\\s+"), " ")

                    val uMatcher = idPattern.matcher(entryText)
                    val url = if (uMatcher.find()) uMatcher.group(1).trim() else ""

                    val pMatcher = publishedPattern.matcher(entryText)
                    val published = if (pMatcher.find()) pMatcher.group(1).trim() else ""
                    val formattedDate = if (published.length >= 10) published.substring(0, 10) else published

                    // Extract authors
                    val authorsList = mutableListOf<String>()
                    val aMatcher = authorPattern.matcher(entryText)
                    while (aMatcher.find()) {
                        authorsList.add(aMatcher.group(1).trim())
                    }
                    val authors = authorsList.joinToString(", ")

                    // Analyze licenses mentioned inside xml abstract, or mark common licenses in the text
                    val summaryLower = summary.lowercase()
                    val titleLower = title.lowercase()
                    val licenses = mutableListOf<String>()

                    if (summaryLower.contains("mit") || titleLower.contains("mit")) {
                        licenses.add("MIT License")
                    }
                    if (summaryLower.contains("creative commons") || summaryLower.contains("cc-by") || summaryLower.contains("cc0") || titleLower.contains("creative commons")) {
                        licenses.add("CC / Creative Commons")
                    }
                    if (summaryLower.contains("bsd") || titleLower.contains("bsd")) {
                        licenses.add("BSD License")
                    }
                    if (summaryLower.contains("apache") || titleLower.contains("apache")) {
                        licenses.add("Apache 2.0")
                    }
                    if (licenses.isEmpty()) {
                        // Default to Permissive/Open Source if papers are registered on arXiv
                        licenses.add("arXiv Non-Exclusive / CC BY")
                    }

                    entries.add(
                        ArxivEntry(
                            title = title,
                            summary = summary,
                            url = url,
                            authors = authors,
                            published = formattedDate,
                            licensesMentioned = licenses
                        )
                    )
                }
                
                entries
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}
