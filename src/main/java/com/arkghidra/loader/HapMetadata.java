package com.arkghidra.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata extracted from a HarmonyOS HAP file's module.json (or
 * module.json5). Contains the module name, type, version info,
 * package name, vendor, and the list of declared abilities.
 */
public final class HapMetadata {

    private final String moduleName;
    private final String moduleType;
    private final String versionName;
    private final int versionCode;
    private final String packageName;
    private final String vendorName;
    private final List<AbilityInfo> abilities;

    /**
     * Constructs a HapMetadata instance.
     *
     * @param moduleName the module name (e.g. "entry")
     * @param moduleType the module type ("entry" or "feature")
     * @param versionName the human-readable version string
     * @param versionCode the numeric version code
     * @param packageName the bundle/package name
     * @param vendorName the vendor/author
     * @param abilities the list of declared abilities
     */
    public HapMetadata(String moduleName, String moduleType,
            String versionName, int versionCode, String packageName,
            String vendorName, List<AbilityInfo> abilities) {
        this.moduleName = moduleName != null ? moduleName : "";
        this.moduleType = moduleType != null ? moduleType : "";
        this.versionName = versionName != null ? versionName : "";
        this.versionCode = versionCode;
        this.packageName = packageName != null ? packageName : "";
        this.vendorName = vendorName != null ? vendorName : "";
        this.abilities = abilities != null
                ? Collections.unmodifiableList(new ArrayList<>(abilities))
                : Collections.emptyList();
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getModuleType() {
        return moduleType;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVendorName() {
        return vendorName;
    }

    public List<AbilityInfo> getAbilities() {
        return abilities;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HAP Module: ").append(moduleName).append('\n');
        sb.append("  Type: ").append(moduleType).append('\n');
        sb.append("  Version: ").append(versionName)
                .append(" (").append(versionCode).append(")\n");
        if (!packageName.isEmpty()) {
            sb.append("  Package: ").append(packageName).append('\n');
        }
        if (!vendorName.isEmpty()) {
            sb.append("  Vendor: ").append(vendorName).append('\n');
        }
        if (!abilities.isEmpty()) {
            sb.append("  Abilities:\n");
            for (AbilityInfo ability : abilities) {
                sb.append("    - ").append(ability.getName());
                if (!ability.getLabel().isEmpty()) {
                    sb.append(" [").append(ability.getLabel()).append(']');
                }
                sb.append(" (").append(ability.getType()).append(")\n");
            }
        }
        return sb.toString();
    }

    /**
     * Information about a single ability declared in the HAP module.
     */
    public static final class AbilityInfo {

        private final String name;
        private final String label;
        private final String type;

        /**
         * Constructs an AbilityInfo instance.
         *
         * @param name the ability name
         * @param label the display label
         * @param type the ability type ("page", "service", or "data")
         */
        public AbilityInfo(String name, String label, String type) {
            this.name = name != null ? name : "";
            this.label = label != null ? label : "";
            this.type = type != null ? type : "";
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public String getType() {
            return type;
        }
    }
}
