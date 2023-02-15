package ch.frankel.blog.secureboot

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.web.client.RestClient

@Entity
internal data class Account(
    @Id
    val id: String,
    val password: String,
)

internal interface AccountRepository : JpaRepository<Account, String> {
    fun findByPassword(token: String): Account?
}

internal class OpaAuthenticationManager(
    private val accountRepo: AccountRepository,
    private val restClient: RestClient
) : AuthenticationManager {

    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication.credentials as String? ?: throw BadCredentialsException("No token passed")
        val account = accountRepo.findByPassword(token) ?: throw BadCredentialsException("Invalid token")
        val path = authentication.details as List<String>
        val decision = restClient.post()
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(OpaInput(DataInput(account.id, path)))
            .exchange { _, resp -> resp.bodyTo(DecisionOutput::class.java) ?: DecisionOutput(ResultOutput(false))  }
        if (decision.result.allow) return authentication.withPrincipal(account.id)
        else throw InsufficientAuthenticationException("OPA disallow")
    }

    private fun Authentication.withPrincipal(principal: String): Authentication =
        if (this is KeyToken) KeyTokenWithPrincipal(principal, this)
        else this
}

internal class TokenAuthenticationFilter(authManager: AuthenticationManager) :
    AbstractAuthenticationProcessingFilter("/finance/salary/**", authManager) {

    override fun attemptAuthentication(req: HttpServletRequest, resp: HttpServletResponse): Authentication {
        val header = req.getHeader("Authorization")
        val path = req.servletPath.split('/').filter { it.isNotBlank() }
        val token = KeyToken(header, path)
        return authenticationManager.authenticate(token)
    }

    override fun successfulAuthentication(
        req: HttpServletRequest,
        resp: HttpServletResponse,
        chain: FilterChain,
        auth: Authentication
    ) {
        val strategy = SecurityContextHolder.getContextHolderStrategy()
        val context = strategy.createEmptyContext()
        auth.isAuthenticated = true
        context.authentication = auth
        strategy.context = context
        chain.doFilter(req, resp)
    }
}

private class KeyToken(private val credentials: String?, private val path: List<String>) : Authentication {

    private var authenticated: Boolean = false

    override fun getName() = principal
    override fun getAuthorities() = emptyList<GrantedAuthority>()
    override fun getCredentials() = credentials
    override fun getDetails() = path
    override fun getPrincipal() = null
    override fun isAuthenticated() = authenticated

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }
}

private class KeyTokenWithPrincipal(private val principal: String, private val token: KeyToken) :
    Authentication by token {
    override fun getPrincipal() = principal
}

private data class OpaInput(
    val input: DataInput
)

private data class DataInput(
    val user: String,
    val path: List<String>,
)

private data class DecisionOutput(
    val result: ResultOutput
)

private data class ResultOutput(
    val allow: Boolean,
)
