package com.substring.foodies.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        String authorization = request.getHeader("Authorization");

        if(authorization!=null && authorization.startsWith("Bearer"))
        {
            String token=authorization.substring(7);
            if(jwtService.validateItem(token))
            {
                String username=jwtService.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

//              Checks if no user is currently authenticated for this request.
                if(SecurityContextHolder.getContext().getAuthentication()==null)
                {
//                    Credentials → Typically the password (or secret) used for authentication.
//👉                  After authentication is successful, we don’t need to keep the password
//                    in memory for security reasons, so we pass null.
                    UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

//                  This adds metadata like: User’s IP address, Session details,
//                  Browser info, (Spring Security likes to keep this.)
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

//                  Store the authenticated user in the SecurityContext
//                  This makes the user officially logged in for this request.
                   SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

//      It tells the filter chain to pass the request and response to the next filter
        filterChain.doFilter(request, response);
    }
}
