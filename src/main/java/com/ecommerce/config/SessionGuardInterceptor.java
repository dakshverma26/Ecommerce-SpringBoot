package com.ecommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * SessionGuardInterceptor — enforces role-based access the same way
 * SecurityConfig.requestMatchers would, but adapted for our 3-table
 * custom auth approach.
 *
 * Session attributes set by controllers:
 *   "SELLER_EMAIL" → seller is logged in
 *   "BUYER_EMAIL"  → buyer is logged in
 *   "ADMIN_USER"   → admin is logged in
 *
 * URL rules:
 *   /seller/** (except /seller/login, /seller/register) → need SELLER_EMAIL
 *   /buyer/**  (except /buyer/login, /buyer/register)   → need BUYER_EMAIL
 *   /admin/**  (except /admin/login)                    → need ADMIN_USER
 */
@Component
public class SessionGuardInterceptor implements HandlerInterceptor {

    private static final String[] SELLER_PUBLIC = {"/seller/login", "/seller/register"};
    private static final String[] BUYER_PUBLIC  = {"/buyer/login", "/buyer/register"};
    private static final String[] ADMIN_PUBLIC  = {"/admin/login"};

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // ── Seller protected area ──────────────────────────────────────────
        if (path.startsWith("/seller/") && !isPublic(path, SELLER_PUBLIC)) {
            if (session == null || session.getAttribute("SELLER_EMAIL") == null) {
                response.sendRedirect(request.getContextPath() + "/seller/login?error=session");
                return false;
            }
        }

        // ── Buyer protected area ───────────────────────────────────────────
        if (path.startsWith("/buyer/") && !isPublic(path, BUYER_PUBLIC)) {
            if (session == null || session.getAttribute("BUYER_EMAIL") == null) {
                response.sendRedirect(request.getContextPath() + "/buyer/login?error=session");
                return false;
            }
        }

        // ── Admin protected area ───────────────────────────────────────────
        if (path.startsWith("/admin/") && !isPublic(path, ADMIN_PUBLIC)) {
            if (session == null || session.getAttribute("ADMIN_USER") == null) {
                response.sendRedirect(request.getContextPath() + "/admin/login?error=session");
                return false;
            }
        }

        return true; // allow request
    }

    private boolean isPublic(String path, String[] publicPaths) {
        for (String p : publicPaths) {
            if (path.equals(p) || path.startsWith(p + "?")) return true;
        }
        return false;
    }
}
