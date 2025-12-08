package com.example.mobileappdevelopment.api

import com.example.mobileappdevelopment.data.Employee
import com.example.mobileappdevelopment.data.Report
import com.example.mobileappdevelopment.data.ReportCategory
import com.example.mobileappdevelopment.data.ReportPriority
import com.example.mobileappdevelopment.data.ReportStatus
import com.example.mobileappdevelopment.data.User
import com.example.mobileappdevelopment.data.UserRole
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    //로그인
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    //새로운 사원 등록
    @POST("employees")
    suspend fun createEmployee(
        @Body request: CreateEmployeeRequest
    ): Response<CreateEmployeeResponse>

    //사원 목록 불러오기
    @GET("employees")
    suspend fun getEmployees(): Response<List<Employee>>

    //사원 업데이트
    @PUT("employees/{id}")
    suspend fun updateEmployee(
        @Path("id") id: String,
        @Body request: UpdateEmployeeRequest
    ): Response<UpdateEmployeeResponse>

    //사원 상태 업데이트 (퇴사 처리)
    @PATCH("employees/{id}/status")
    suspend fun updateEmployeeStatus(
        @Path("id") id: String,
        @Body request: StatusUpdateRequest
    ): Response<Unit>

    //사원 삭제
    @DELETE("employees/{id}")
    suspend fun deleteEmployee(
        @Path("id") id: String
    ): Response<Unit>

    //신고내용 가져오기
    @GET("reports")
    suspend fun getReports(): Response<List<Report>>

    //신고 제출
    @POST("reports")
    suspend fun submitReport(
        @Body report: SubmitReportRequest
    ): Response<Report>

    //신고 상태 업데이트
    @PATCH("reports/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") id: String,
        @Body status: UpdateStatusRequest
    ): Response<Report>

    //신고 우선순위 업데이트
    @PATCH("reports/{id}/priority")
    suspend fun updateReportPriority(
        @Path("id") id: String,
        @Body priority: UpdatePriorityRequest
    ): Response<Report>

    //신고 노트 업데이트
    @PATCH("reports/{id}/notes")
    suspend fun updateReportNotes(
        @Path("id") id: String,
        @Body notes: UpdateNotesRequest
    ): Response<Report>

    // Merkle & ZK 관련
    @GET("zk/public-key")
    suspend fun getPublicKey(): Response<PublicKeyResponse>

    @POST("zk/register")
    suspend fun registerMerkle(
        @Body request: MerkleRegisterRequest
    ): Response<MerkleRegisterResponse>

    @GET("zk/merkle-tree")
    suspend fun getMerkleTreeInfo(): Response<MerkleTreeInfoResponse>

    @POST("zk/report")
    suspend fun submitZkReport(
        @Body request: ZkReportRequest
    ): Response<ZkReportResponse>
}

// Request/Response 모델들
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

data class SubmitReportRequest(
    val category: String,
    val title: String,
    val description: String,
    val department: String,
    val date: String
)

data class UpdateStatusRequest(val status: String)
data class UpdatePriorityRequest(val priority: String)
data class UpdateNotesRequest(val notes: String)

// Merkle & ZK 모델들
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

data class MerkleLeafData(
    val userId: Int,
    val leaf: String
)

data class MerkleTreeInfoResponse(
    val root: String,
    val leaves: List<MerkleLeafData>
)

data class ZkReportRequest(
    val encryptedContent: String,
    val zkProof: String,
    val nullifierHash: String?,
    val root: String?
)

data class ZkReportResponse(
    val message: String,
    val ipfsCid: String,
    val txHash: String
)

data class ZkReport(
    val id: Int,
    val encryptedContent: String,
    val ipfsCid: String,
    val txHash: String,
    val createdAt: String
)