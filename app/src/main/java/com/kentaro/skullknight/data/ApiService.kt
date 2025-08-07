package com.kentaro.skullknight.data

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Projects
    @GET("projects")
    suspend fun getProjects(): Response<List<Project>>
    
    @GET("projects/{projectName}")
    suspend fun getProject(@Path("projectName") projectName: String): Response<Project>
    
    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<Project>
    
    @DELETE("projects/{projectName}")
    suspend fun deleteProject(@Path("projectName") projectName: String): Response<Unit>
    
    // Items
    @POST("projects/{projectName}/items")
    suspend fun createItem(
        @Path("projectName") projectName: String,
        @Query("parent_id") parentId: String? = null,
        @Body request: CreateItemRequest
    ): Response<Item>
    
    @PUT("projects/{projectName}/items/{itemId}/status")
    suspend fun updateItemStatus(
        @Path("projectName") projectName: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemStatusRequest
    ): Response<Item>
    
    @DELETE("projects/{projectName}/items/{itemId}")
    suspend fun deleteItem(
        @Path("projectName") projectName: String,
        @Path("itemId") itemId: String
    ): Response<Unit>
} 