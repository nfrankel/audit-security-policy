package ch.frankel.blog.secureboot

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.context.support.beans
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.router

@Entity
data class Employee(
    @Id
    val userName: String,
    val firstName: String,
    val lastName: String,
    val salary: Long,
    @ManyToOne
    @JoinColumn(name = "MANAGER_ID")
    @JsonIgnore
    val manager: Employee?,
    @OneToMany(mappedBy = "manager")
    val collaborators: Collection<Employee>,
)

internal interface EmployeeRepository : JpaRepository<Employee, String>

internal fun EmployeeRepository.findByIdOrNull(id: String) = findById(id).orElse(null)

internal fun routes() = beans {
    bean {
        val repo = ref<EmployeeRepository>()
        router {
            GET("/finance/salary/{user_name}") {
                val userName = it.pathVariable("user_name")
                val employee = repo.findByIdOrNull(userName)
                if (employee == null) ServerResponse.notFound().build()
                else ServerResponse.ok().body(employee)
            }
        }
    }
}
