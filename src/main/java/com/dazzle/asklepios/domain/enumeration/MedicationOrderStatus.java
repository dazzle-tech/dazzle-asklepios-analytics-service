package com.dazzle.asklepios.domain.enumeration;

public enum MedicationOrderStatus {
    WAITING_DOUBLE_CHECK,
    ADMINISTERED,
    CANCELLED,
    DISCARDED,
    NEW,
    SUBMITTED,
    // Medication-level only: its doses have mixed statuses; never stored on a dose
    PARTIALLY
}
