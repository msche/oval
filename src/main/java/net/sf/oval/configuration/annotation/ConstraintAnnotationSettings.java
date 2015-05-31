package net.sf.oval.configuration.annotation;

import net.sf.oval.ConstraintTarget;

/**
 * Contains the basic constraint configuration
 */
public final class ConstraintAnnotationSettings {


    private final String message;
    private final ConstraintTarget[] appliesTo;
    private final String errorCode;
    private final int severity;
    private final String[] profiles;
    private final String target;
    private final String when;

    private ConstraintAnnotationSettings(Builder builder) {
        this.message = builder.message;
        this.appliesTo = builder.appliesTo;
        this.errorCode = builder.errorCode;
        this.severity = builder.severity;
        this.profiles = builder.profiles;
        this.target = builder.target;
        this.when = builder.when;
    }

    public String getMessage() {
        return message;
        }

    public ConstraintTarget[] getAppliesTo() {
        return appliesTo;
    }

    public String getErrorCode() {
        return errorCode;
        }

    public int getSeverity() {
        return severity;
        }

    public String[] getProfiles() {
        return profiles;
        }

    public String getTarget() {
        return target;
        }

    public String getWhen() {
        return when;
        }

    public static final class Builder {
        private String message;
        private ConstraintTarget[] appliesTo;
        private String errorCode;
        private int severity;
        private String[] profiles;
        private String target;
        private String when;

        public Builder() {
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder appliesTo(ConstraintTarget[] appliesTo) {
            this.appliesTo = appliesTo;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder severity(int severity) {
            this.severity = severity;
            return this;
        }

        public Builder profiles(Class<?>[] groups) {
            String[] profiles = new String[groups.length];
            for(int i=0; i<groups.length;i++) {
                profiles[i] = groups[i].getCanonicalName();
            }
            profiles(profiles);
            return this;
        }

        public Builder profiles(String[] profiles) {
            this.profiles = profiles;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public Builder when(String when) {
            this.when = when;
            return this;
        }

        public ConstraintAnnotationSettings build() {
            return new ConstraintAnnotationSettings(this);
        }
    }
}
