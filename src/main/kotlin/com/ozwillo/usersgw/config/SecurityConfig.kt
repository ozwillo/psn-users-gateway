package com.ozwillo.usersgw.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder


@Configuration
@EnableWebSecurity
class SecurityWebInitializer(private val passwordEncoder: PasswordEncoder) : WebSecurityConfigurerAdapter() {
	override fun configure(http: HttpSecurity) {
		http
				.authorizeRequests()
				.anyRequest()
				.authenticated()
				.and()
				.httpBasic()
				.and()
				.csrf().disable()
	}

	override fun configure(auth: AuthenticationManagerBuilder) {
		auth
				.inMemoryAuthentication()
				.withUser("admin")
				.password(passwordEncoder.encode("admin"))
				.roles("USER", "ADMIN")
	}


}