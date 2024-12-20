package org.j_keepass.fragments.entry.dtos;

import androidx.annotation.NonNull;

public enum FieldNameType {
    TITLE {
        @NonNull
        @Override
        public String toString() {
            return "Title";
        }
    },
    USERNAME {
        @NonNull
        @Override
        public String toString() {
            return "User Name";
        }
    },
    PASSWORD {
        @NonNull
        @Override
        public String toString() {
            return "Password";
        }
    },
    URL {
        @NonNull
        @Override
        public String toString() {
            return "URL";
        }
    },
    NOTES {
        @NonNull
        @Override
        public String toString() {
            return "Notes";
        }
    },
    EXPIRY_DATE {
        @NonNull
        @Override
        public String toString() {
            return "Expiry Date";
        }
    },
    CREATED_DATE {
        @NonNull
        @Override
        public String toString() {
            return "Created Date";
        }
    },
    ADDITIONAL,
    ATTACHMENT {
        @NonNull
        @Override
        public String toString() {
            return "Attachment";
        }
    },
    LAST_ACCESSED {
        @NonNull
        @Override
        public String toString() {
            return "Last Access";
        }
    },
    LAST_MODIFIED {
        @NonNull
        @Override
        public String toString() {
            return "Last Modified";
        }
    },
    DUMMY,
    DATE,
}
