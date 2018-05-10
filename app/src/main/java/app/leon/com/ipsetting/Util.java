package app.leon.com.ipsetting;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2018/5/10.
 */

public class Util
{
    public static final String NONE_IP = "0.0.0.0";

    public static String getIp(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo != null)
            return int2IP(connectionInfo.getIpAddress());
        else
            return NONE_IP;
    }

    public static boolean setIpWithStaticIp(Context context, String ip)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo != null)
        {
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

            WifiConfiguration wifiConfig = null;
            for (WifiConfiguration one : configuredNetworks)
            {
                if (one.networkId == connectionInfo.getNetworkId())
                {
                    wifiConfig = one;
                    break;
                }
            }


            try
            {
                setIpAssignment("STATIC", wifiConfig);
                setIpAddress(InetAddress.getByName(ip), 24, wifiConfig);
                // setGateway(InetAddress.getByName(ip), wifiConfig);
                // setDNS(InetAddress.getByName(ip), wifiConfig);
                wifiManager.updateNetwork(wifiConfig);
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private static void setIpAssignment(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException
    {
        setEnumField(wifiConf, assign, "ipAssignment");
    }


    private static void setIpAddress(InetAddress addr, int prefixLength, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, ClassNotFoundException, InstantiationException, InvocationTargetException
    {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        Class<?> laClass = Class.forName("android.net.LinkAddress");
        Constructor<?> laConstructor = laClass.getConstructor(new Class[]{InetAddress.class, int.class});
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList<Object> mLinkAddresses = (ArrayList<Object>) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    private static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException
    {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;

        Class<?> routeInfoClass = Class.forName("android.net.RouteInfo");
        Constructor<?> routeInfoConstructor = routeInfoClass.getConstructor(new Class[]{InetAddress.class});
        Object routeInfo = routeInfoConstructor.newInstance(gateway);

        ArrayList<Object> mRoutes = (ArrayList<Object>) getDeclaredField(linkProperties, "mRoutes");
        mRoutes.clear();
        mRoutes.add(routeInfo);
    }

    private static void setDNS(InetAddress dns, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException
    {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>) getDeclaredField(linkProperties, "mDnses");
        mDnses.clear();
        mDnses.add(dns);
    }

    private static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    private static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    private static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    private static String int2IP(int ip)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf((ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf(((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf(((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf(((ip >> 24) & 0xff)));
        return sb.toString();
    }
}
