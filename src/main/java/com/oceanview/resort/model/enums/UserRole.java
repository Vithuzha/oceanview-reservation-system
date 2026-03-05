package com.oceanview.resort.model.enums;

/**
 * Enumeration representing user roles in the system.
 * STAFF - Can manage reservations, guests, and billing
 * MANAGER - Can view reports and billing summaries (read-only access to
 * reservations)
 */
public enum UserRole {
    STAFF,
    MANAGER
}
