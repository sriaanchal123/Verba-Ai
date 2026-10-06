package com.example.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

interface VerbaApiService {

    @Multipart
    @POST("api/v1/ingest/file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("metadata") metadata: RequestBody
    ): Response<IngestResponseDto>

    @POST("api/v1/ingest/text")
    suspend fun ingestText(
        @Body request: TextIngestRequestDto
    ): Response<IngestResponseDto>

    @POST("api/v1/ingest/url")
    suspend fun ingestUrl(
        @Body request: UrlIngestRequestDto
    ): Response<IngestResponseDto>

    @GET("api/v1/status/{taskId}")
    suspend fun getStatus(
        @Path("taskId") taskId: String
    ): Response<StatusResponseDto>

    @GET("api/v1/session/{sessionId}")
    suspend fun getSession(
        @Path("sessionId") sessionId: String
    ): Response<KnowledgeSessionDto>

    @POST("api/v1/rag/chat")
    suspend fun askRag(
        @Body request: RagChatRequestDto
    ): Response<RagChatResponseDto>

    @Streaming
    @POST("api/v1/rag/chat/stream")
    suspend fun streamRag(
        @Body request: RagChatRequestDto
    ): Response<ResponseBody>
}
