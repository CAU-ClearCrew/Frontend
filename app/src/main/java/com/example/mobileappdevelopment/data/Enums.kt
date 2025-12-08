package com.example.mobileappdevelopment.data


enum class UserRole {
    ADMIN, EMPLOYEE
}

enum class EmployeeStatus {
    ACTIVE, INACTIVE
}

enum class ReportCategory(val label: String) {
    HARASSMENT("괴롭힘/따돌림"),
    DISCRIMINATION("차별"),
    CORRUPTION("부정/비리"),
    SAFETY("안전 위반"),
    ETHICS("윤리 위반"),
    OTHER("기타")
}

enum class ReportStatus(val label: String) {
    PENDING("접수"),
    INVESTIGATING("조사중"),
    RESOLVED("해결"),
    CLOSED("종료")
}

//TODO 이거 안 씀.
enum class ReportPriority(val label: String) {
    LOW("낮음"),
    MEDIUM("보통"),
    HIGH("높음")
}