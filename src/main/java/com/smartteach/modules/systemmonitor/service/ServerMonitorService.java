package com.smartteach.modules.systemmonitor.service;

import com.smartteach.modules.systemmonitor.vo.ServerInfoVO;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统监控 - 服务器软硬件信息采集，基于 oshi 跨平台库实现
 */
@Service
public class ServerMonitorService {

    private static final double GB = 1024.0 * 1024 * 1024;

    public ServerInfoVO getServerInfo() {
        ServerInfoVO vo = new ServerInfoVO();
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        vo.setCpu(getCpuInfo(hardware.getProcessor()));
        vo.setMemory(getMemoryInfo(hardware.getMemory()));
        vo.setJvm(getJvmInfo());
        vo.setDisks(getDiskInfo(os.getFileSystem()));
        vo.setSys(getSysInfo(os));
        return vo;
    }

    private ServerInfoVO.CpuInfo getCpuInfo(CentralProcessor processor) {
        ServerInfoVO.CpuInfo cpu = new ServerInfoVO.CpuInfo();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long totalCpu = user + sys + idle + iowait
                + (ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()])
                + (ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()])
                + (ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()]);
        if (totalCpu == 0) totalCpu = 1;

        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setUserUsage(round(user * 100.0 / totalCpu));
        cpu.setSysUsage(round(sys * 100.0 / totalCpu));
        cpu.setWaitUsage(round(iowait * 100.0 / totalCpu));
        cpu.setFreeUsage(round(idle * 100.0 / totalCpu));
        return cpu;
    }

    private ServerInfoVO.MemoryInfo getMemoryInfo(GlobalMemory memory) {
        ServerInfoVO.MemoryInfo vo = new ServerInfoVO.MemoryInfo();
        double total = memory.getTotal() / GB;
        double free = memory.getAvailable() / GB;
        double used = total - free;
        vo.setTotal(round(total));
        vo.setFree(round(free));
        vo.setUsed(round(used));
        vo.setUsageRate(round(used * 100.0 / total));
        return vo;
    }

    private ServerInfoVO.JvmInfo getJvmInfo() {
        ServerInfoVO.JvmInfo vo = new ServerInfoVO.JvmInfo();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        Runtime runtime = Runtime.getRuntime();
        double total = runtime.totalMemory() / GB;
        double free = runtime.freeMemory() / GB;
        double used = total - free;

        vo.setJdkVersion(System.getProperty("java.version"));
        vo.setJdkHome(System.getProperty("java.home"));
        vo.setTotal(round(total));
        vo.setFree(round(free));
        vo.setUsed(round(used));
        vo.setUsageRate(round(used * 100.0 / total));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTime = runtimeMXBean.getStartTime();
        vo.setStartTime(sdf.format(new java.util.Date(startTime)));
        long uptimeMs = runtimeMXBean.getUptime();
        vo.setRunTime(formatDuration(uptimeMs));
        return vo;
    }

    private List<ServerInfoVO.DiskInfo> getDiskInfo(FileSystem fileSystem) {
        List<ServerInfoVO.DiskInfo> list = new ArrayList<>();
        for (OSFileStore store : fileSystem.getFileStores()) {
            ServerInfoVO.DiskInfo disk = new ServerInfoVO.DiskInfo();
            double total = store.getTotalSpace() / GB;
            double free = store.getUsableSpace() / GB;
            double used = total - free;
            if (total <= 0) continue;
            disk.setDirName(store.getMount());
            disk.setSysTypeName(store.getType());
            disk.setTotal(round(total));
            disk.setFree(round(free));
            disk.setUsed(round(used));
            disk.setUsageRate(round(used * 100.0 / total));
            list.add(disk);
        }
        return list;
    }

    private ServerInfoVO.SysInfo getSysInfo(OperatingSystem os) {
        ServerInfoVO.SysInfo vo = new ServerInfoVO.SysInfo();
        vo.setOsName(os.toString());
        vo.setOsArch(System.getProperty("os.arch"));
        vo.setProjectDir(System.getProperty("user.dir"));
        try {
            vo.setServerIp(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            vo.setServerIp("127.0.0.1");
        }
        return vo;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatDuration(long millis) {
        long days = millis / (1000 * 60 * 60 * 24);
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        return days + "天" + hours + "小时" + minutes + "分钟";
    }
}
