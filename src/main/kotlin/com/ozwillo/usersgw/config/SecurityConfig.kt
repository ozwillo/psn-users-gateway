package com.ozwillo.usersgw.config

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.Health
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityWebInitializer(private val passwordEncoder: PasswordEncoder, private val userInvitationProperties: UserInvitationProperties) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http
                .requestMatcher(EndpointRequest.to(Health::class.java)).authorizeRequests().anyRequest().permitAll().and()
                .antMatcher("/api/usersgw").authorizeRequests().anyRequest().authenticated().and()
                .httpBasic().and()
                .csrf().disable()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
                .inMemoryAuthentication()
                .withUser(userInvitationProperties.username)
                .password(passwordEncoder.encode(userInvitationProperties.password))
                .roles("USER", "ADMIN")
    }
}
