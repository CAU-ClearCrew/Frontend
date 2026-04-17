package com.example.mobileappdevelopment.api

import com.example.mobileappdevelopment.data.Employee
import com.example.mobileappdevelopment.data.Report
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    //Login
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    //Register new employee
    @POST("api/employees")
    suspend fun createEmployee(
        @Body request: CreateEmployeeRequest
    ): Response<CreateEmployeeResponse>

    //Get employee list
    @GET("api/employees")
    suspend fun getEmployees(): Response<List<Employee>>

    //Update employee
    @PATCH("api/employees/{id}")
    suspend fun updateEmployee(
        @Path("id") id: String,
        @Body request: UpdateEmployeeRequest
    ): Response<UpdateEmployeeResponse>

    //Update employee status (e.g., resignation)
    @PATCH("api/employees/{id}/status")
    suspend fun updateEmployeeStatus(
        @Path("id") id: String,
        @Body request: StatusUpdateRequest
    ): Response<Unit>

    //Delete employee
    @DELETE("api/employees/{id}")
    suspend fun deleteEmployee(
        @Path("id") id: String
    ): Response<Unit>

    //Get report content
    @GET("api/report")
    suspend fun getReports(): Response<List<Report>>

    //Update report status
    @PATCH("reports/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") id: String,
        @Body status: UpdateStatusRequest
    ): Response<Report>

    //Update report priority
    @PATCH("reports/{id}/priority")
    suspend fun updateReportPriority(
        @Path("id") id: String,
        @Body priority: UpdatePriorityRequest
    ): Response<Report>

    //Update report notes
    @PATCH("reports/{id}/notes")
    suspend fun updateReportNotes(
        @Path("id") id: String,
        @Body notes: UpdateNotesRequest
    ): Response<Report>

    // Merkle & ZK related
    @GET("api/keys/public")
    suspend fun getPublicKey(): Response<PublicKeyResponse>

    @POST("api/merkle/register")
    suspend fun registerMerkle(
        @Body request: MerkleRegisterRequest
    ): Response<MerkleRegisterResponse>

    @GET("api/merkle/tree-info")
    suspend fun getMerkleTreeInfo(): Response<CircuitInputsResponse>
}

// Request/Response models
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val role: String, // "ADMIN" or "USER"
    val userId: Int,
    val name: String
)

data class CreateEmployeeRequest(
    val email: String,
    val password: String,
    val name: String,
    val position: String,
    val departmentId: Int?,
    val joinedAt: String?,
    val status: String? = "ACTIVE"
)

data class CreateEmployeeResponse(
    val message: String
)

data class UpdateEmployeeRequest(
    val name: String?,
    val position: String?,
    val departmentId: Int?,
    val joinedAt: String?
)

data class UpdateEmployeeResponse(
    val message: String
)

data class StatusUpdateRequest(
    val status: String // "ACTIVE" or "RESIGNED"
)

data class UpdateStatusRequest(val status: String)
data class UpdatePriorityRequest(val priority: String)
data class UpdateNotesRequest(val notes: String)


// Merkle & ZK models
data class PublicKeyResponse(
    val publicKey: String
)

data class MerkleRegisterRequest(
    val leaf: String
)

data class MerkleRegisterResponse(
    val message: String,
    val root: String,
    val result: Any?
)

data class CircuitInputsResponse(
    val root: String,
    val path_elements: List<String>,
    val path_indices: List<Int>,
    val active_bits: List<Int>,
    val leaf_item: LeafItemResponse
)

data class LeafItemResponse(
    val key: String,
    val value: String,
    val nextKey: String,
    val nextIdx: Int
)
