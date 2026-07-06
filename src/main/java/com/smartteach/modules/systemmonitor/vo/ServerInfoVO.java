package com.smartteach.modules.systemmonitor.vo;

import lombok.Data;

@Data
public class ServerInfoVO {

    private CpuInfo cpu;
    private MemoryInfo memory;
    private JvmInfo jvm;
    private java.util.List<DiskInfo> disks;
    private SysInfo sys;

    @Data
    public static class CpuInfo {
        private int cpuNum;
        private double sysUsage;
        private double userUsage;
        private double waitUsage;
        private double freeUsage;
    }

    @Data
    public static class MemoryInfo {
        private double total;
        private double used;
        private double free;
        private double usageRate;
    }

    @Data
    public static class JvmInfo {
        private String jdkVersion;
        private String jdkHome;
        private double total;
        private double used;
        private double free;
        private double usageRate;
        private String startTime;
        private String runTime;
    }

    @Data
    public static class DiskInfo {
        private String dirName;
        private String sysTypeName;
        private double total;
        private double used;
        private double free;
        private double usageRate;
    }

    @Data
    public static class SysInfo {
        private String osName;
        private String osArch;
        private String serverIp;
        private String projectDir;
    }
}
